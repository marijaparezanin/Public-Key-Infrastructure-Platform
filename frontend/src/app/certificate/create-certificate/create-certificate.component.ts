import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormsModule, NgForm } from '@angular/forms';
import {
  CertificateType,
  CreateCertificateDto,
  KEYSTOREDOWNLOADFORMAT,
  SimpleCertificate
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
  dialogType: 'info' | 'error' | 'confirm' | 'download' = 'error';

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
      this.showDialogDownload();
    } else {
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

  showDialogDownload() {
    this.dialogMessage = "Please download the certificate and private key now. You won't be able to access the private key again.";
    this.dialogType = 'download';
    this.showDialog = true;
  }

  onDialogClose() {
    this.showDialog = false;
  }

  protected readonly Object = Object;

  onDialogConfirm(data?: {extension?: string, alias?: string, password?: string, reason?: string}) {
    if (this.role === 'ee' && data) {
      this.certificateForm.type = CertificateType['END_ENTITY'];
      const dto = {
        ...this.certificateForm,
        extensions: Object.fromEntries(this.extensionEntries),
        alias: data.alias || '',
        password: data.password || '',
        keyStoreFormat: data.extension === '.jks' ? KEYSTOREDOWNLOADFORMAT.JKS : KEYSTOREDOWNLOADFORMAT.PKCS12
      };
      this.certificateService.createEECertificate(dto).subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          const extension = data.extension || '.p12';
          a.download = `certificate${extension}`;
          a.click();
          window.URL.revokeObjectURL(url);
          this.showDialogInfo('Certificate created and downloaded successfully.');
        },
        error: err => {
          console.error('Failed to create certificate:', err);
          this.showDialogError('Failed to create certificate. Please try again.');
        }
      });
    }
  }
}
