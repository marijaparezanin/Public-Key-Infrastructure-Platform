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
    'basicConstraints',
    'crldistributionpoints',
    'authorityinfoaccess'
  ];
  extensionEntries: Map<string,string> = new Map<string,string>();

  availableCertificates: SimpleCertificate[] = [];
  allOrganizations: Organization[] = [];
  // inside your CertificateCreateComponent (where the certificate form is)
  templatesForIssuer: SimpleCertificateTemplateDTO[] = [];
  selectedTemplateId: string = '';


  onIssuerChange(issuerId: string) {
    this.templatesForIssuer = [];
    this.selectedTemplateId = '';

    if (!issuerId) return;

    this.certificateService.getTemplatesForCA(issuerId).subscribe({
      next: (templates: SimpleCertificateTemplateDTO[]) => {
        this.templatesForIssuer = templates;
      },
      error: err => {
        console.error('Failed to load templates for issuer:', err);
        this.showDialogError('Failed to load templates for selected issuer.');
      }
    });
  }


  constructor(private certificateService: CertificateService, private organizationService:OrganizationService) {}

  ngOnInit() {
    this.certificateService.getApplicableCA().subscribe(cers => {
      this.availableCertificates = cers;
    });
    this.organizationService.getAll().subscribe(orgs => {
      this.allOrganizations = orgs;
    })
  }

  addExtension() {
    const availableKey = this.supportedExtensions.find(
      key => !this.extensionEntries.has(key)
    );
    if (availableKey) {
      this.extensionEntries.set(availableKey, '');
    }
  }

  removeExtension(key: string) {
    this.extensionEntries.delete(key);
  }

  isKeyDisabled(key: string, currentKey: string): boolean {
    return this.extensionEntries.has(key) && key !== currentKey;
  }


  customRequired(control: AbstractControl, message: string) {
    return control.value ? null : { required: message };
  }

  submitCertificate(form: NgForm) {
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

    this.certificateForm.extensions
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


}
