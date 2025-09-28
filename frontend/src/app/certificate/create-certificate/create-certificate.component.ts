import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormsModule, NgForm } from '@angular/forms';
import {
  CertificateType,
  CreateCertificateDto,
  SimpleCertificate,
  SimpleCertificateTemplateDTO
} from '../model/certificate.model';
import { DialogComponent } from "../../shared/dialog/dialog.component";
import { CertificateService } from '../service/certificate.service';
import { Organization } from '../model/organization.model';
import { OrganizationService } from '../service/organziation.service';

@Component({
  selector: 'app-create-certification',
  templateUrl: './create-certificate.component.html',
  styleUrls: ['../../shared/form.css'],
  imports: [FormsModule, CommonModule, DialogComponent],
  standalone: true,
})
export class CreateCertificationComponent implements OnInit {
  @Input() role: 'admin' | 'ca' | 'ee' | null = null;

  showDialog: boolean = false;
  dialogMessage: string = '';
  dialogType: 'info' | 'error' | 'confirm' = 'error';
  availableTemplates: SimpleCertificateTemplateDTO[] = [];
  selectedTemplate: SimpleCertificateTemplateDTO | null = null;
  selectedTemplateName: string | null = null;

  certificateForm: CreateCertificateDto = {
    type: null,
    commonName: '',
    surname: '',
    givenName: '',
    organization: '',
    organizationalUnit: '',
    country: '',
    email: '',
    startDate: null,
    endDate: null,
    extensions: {},
    issuerCertificateId: '',
    assignToOrganizationName: null
  };

  supportedExtensions = [
    'keyusage',
    'extendedkeyusage',
    'subjectaltname',
    'keycertsign',
    'digitalsignature',
    'crldistributionpoints',
    'authorityinfoaccess'
  ];

  // source-of-truth store + object mirror for stable binding
  extensionEntries: Map<string, string> = new Map<string, string>();
  extensionEntriesObject: { [key: string]: string } = {}; // used in [(ngModel)] for inputs

  // explicit order array so we preserve insertion order and avoid keyvalue sorting issues
  extensionOrder: string[] = [];

  // which extension keys were added by the currently applied template
  templateAddedKeys: Set<string> = new Set<string>();

  availableCertificates: SimpleCertificate[] = [];
  allOrganizations: Organization[] = [];

  constructor(private certificateService: CertificateService, private organizationService:OrganizationService) {}

  ngOnInit() {
    this.certificateService.getApplicableCA().subscribe(cers => {
      this.availableCertificates = cers;
    });
    this.organizationService.getAll().subscribe(orgs => {
      this.allOrganizations = orgs;
    })
  }

  /* -------------------- Utility sync functions -------------------- */
  private syncMapToObjectAndOrder() {
    // rebuild the object mirror and order from the map WITHOUT reordering existing keys:
    this.extensionEntriesObject = {};
    this.extensionOrder = [];
    this.extensionEntries.forEach((v, k) => {
      this.extensionEntriesObject[k] = v;
      this.extensionOrder.push(k);
    });
  }

  private setMapEntryAndMirror(key: string, value: string, appendIfMissing = true) {
    const exists = this.extensionEntries.has(key);
    this.extensionEntries.set(key, value);
    this.extensionEntriesObject[key] = value;
    if (!exists && appendIfMissing) this.extensionOrder.push(key);
  }

  private deleteMapEntryAndMirror(key: string) {
    this.extensionEntries.delete(key);
    delete this.extensionEntriesObject[key];
    const idx = this.extensionOrder.indexOf(key);
    if (idx >= 0) this.extensionOrder.splice(idx, 1);
  }

  /* -------------------- UI actions -------------------- */

  addExtension() {
    const availableKey = this.supportedExtensions.find(k => !this.extensionEntries.has(k));
    if (!availableKey) return;
    this.setMapEntryAndMirror(availableKey, '');
  }

  removeExtension(key: string) {
    // if it was added by template, remove from templateAddedKeys too
    if (this.templateAddedKeys.has(key)) this.templateAddedKeys.delete(key);
    this.deleteMapEntryAndMirror(key);
  }

  // this is used to prevent selecting the same key twice in the select
  isKeyDisabled(key: string, currentKey: string): boolean {
    return this.extensionEntries.has(key) && key !== currentKey;
  }

  // called when user changes the extension key via the select in a row
  onExtensionKeyChange(index: number, newKey: string) {
    const oldKey = this.extensionOrder[index];
    if (oldKey === newKey) return;
    // guard against duplicates (shouldn't happen because we disable, but safe)
    if (this.extensionEntries.has(newKey)) return;

    const val = this.extensionEntries.get(oldKey) ?? '';
    const oldWasTemplateKey = this.templateAddedKeys.has(oldKey);

    // remove old
    this.extensionEntries.delete(oldKey);
    delete this.extensionEntriesObject[oldKey];

    // set new at same position
    this.extensionOrder[index] = newKey;
    this.extensionEntries.set(newKey, val);
    this.extensionEntriesObject[newKey] = val;

    // preserve templateAddedKeys status if old one was template-provided
    if (oldWasTemplateKey) {
      this.templateAddedKeys.delete(oldKey);
      this.templateAddedKeys.add(newKey);
    }
  }

  // called when input changes (keeps map in sync with object mirror)
  setExtensionValue(key: string, value: string) {
    this.extensionEntries.set(key, value);
    // object mirror is bound two-way by [(ngModel)] so it's already set,
    // but keep the Set call to be explicit
    this.extensionEntriesObject[key] = value;
  }

  /* -------------------- Template & issuer logic -------------------- */

  onIssuerChange(issuerId: string) {
    this.certificateService.getTemplatesForCA(issuerId).subscribe(templates => {
      this.availableTemplates = templates || [];
      this.selectedTemplate = null;
      this.selectedTemplateName = null;
      // DO NOT clear user's custom extension entries here (only clear template-added ones)
      // remove previously template-added keys:
      this.templateAddedKeys.forEach(k => this.deleteMapEntryAndMirror(k));
      this.templateAddedKeys.clear();
    });
  }

  onTemplateChange(templateName: string | null) {
    // remove previously template-added keys always
    this.templateAddedKeys.forEach(k => this.deleteMapEntryAndMirror(k));
    this.templateAddedKeys.clear();

    if (!templateName) {
      this.selectedTemplate = null;
      this.selectedTemplateName = null;
      // removed template -> do not re-order or touch user-booked keys (already removed template keys above)
      return;
    }

    const template = this.availableTemplates.find(t => t.name === templateName);
    if (!template) return;

    this.selectedTemplate = template;
    this.selectedTemplateName = template.name;

    // store regexes in certificate form for later validation (front-end will use these for CN/SAN validation)
    this.certificateForm.extensions['commonNameRegex'] = template.commonNameRegex;
    this.certificateForm.extensions['subjectAlternativeNameRegex'] = template.subjectAlternativeNameRegex;

    // Add/overwrite keys provided by the template. We will mark them in templateAddedKeys so switching templates removes them.
    const keysToApply: { key: string, value: string }[] = [];
    if (template.keyUsage) keysToApply.push({ key: 'keyusage', value: template.keyUsage });
    if (template.extendedKeyUsage) keysToApply.push({ key: 'extendedkeyusage', value: template.extendedKeyUsage });
    // For SAN template, we add the subjectaltname extension as an empty value for user to fill,
    // while the SAN regex is kept in certificateForm.extensions for validation.
    if (template.subjectAlternativeNameRegex) keysToApply.push({ key: 'subjectaltname', value: '' });

    // Apply: remove existing templateAddedKeys already done above; now add new ones
    keysToApply.forEach(pair => {
      // overwrite existing values even if user had added same key (template override)
      if (this.extensionEntries.has(pair.key)) {
        // replace value and ensure order unchanged (we won't reinsert if present)
        this.extensionEntries.set(pair.key, pair.value);
        this.extensionEntriesObject[pair.key] = pair.value;
      } else {
        // new key -> append at end
        this.setMapEntryAndMirror(pair.key, pair.value, true);
      }
      this.templateAddedKeys.add(pair.key);
    });

    // ensure we reflect mirror and order correctly
    this.syncMapToObjectAndOrder();

    // TTL -> update endDate if startDate present
    if (template.ttlDays && this.certificateForm.startDate) {
      const start = new Date(this.certificateForm.startDate);
      const end = new Date(start);
      end.setDate(start.getDate() + template.ttlDays);
      this.certificateForm.endDate = end.toISOString().split('T')[0] as any;
    }
  }

  /* -------------------- small helpers / form submit -------------------- */

  customRequired(control: AbstractControl, message: string) {
    return control.value ? null : { required: message };
  }

  submitCertificate(form: NgForm) {
    // ensure object mirror -> map (in case user typed and for safety)
    Object.keys(this.extensionEntriesObject).forEach(k => this.extensionEntries.set(k, this.extensionEntriesObject[k]));

    // populate certificateForm.extensions if your process needs it (keeps your current flow intact)
    // (I intentionally do not change your certificate creation logic other than making sure ext Map is accurate)
    this.certificateForm.extensions = {};
    this.extensionEntries.forEach((value, key) => {
      this.certificateForm.extensions[key] = value;
    });

    form.form.markAllAsTouched();
    if (!form.valid) {
      this.dialogMessage = 'Please fill all required fields correctly.';
      this.dialogType = 'error';
      this.showDialog = true;
      return;
    }

    if (this.role === 'ee') {
      this.certificateForm.type = CertificateType['END_ENTITY'];
    }

    console.log('Issuing certificate:', this.certificateForm);
    this.certificateService.createCertificate(this.certificateForm).subscribe({
      next: () => {
        this.showDialogInfo('Certificate created successfully.');
      },
      error: err => {
        console.error('Failed to create certificate:', err);
        this.showDialogError('Failed to create certificate. Please try again.');
      }
    });
  }

  showDialogError(message: string) {
    this.dialogMessage = message;
    this.dialogType = 'error';
    this.showDialog = true;
  }

  showDialogInfo(message: string) {
    this.dialogMessage = message;
    this.dialogType = 'info';
    this.showDialog = true;
  }

  onDialogClose() {
    this.showDialog = false;
  }

  protected readonly Object = Object;

  trackByExtensionKey(index: number, key: string) {
    return key;
  }
}
