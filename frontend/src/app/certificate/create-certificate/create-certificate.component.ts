import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormsModule, NgForm } from '@angular/forms';
import { CertificateType, CreateCertificateDto, SimpleCertificate } from '../model/certificate.model';
import { DialogComponent } from "../../shared/dialog/dialog.component";
import { CertificateService } from '../service/certificate.service';

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
    extensions: {},   // ✅ Record<string,string>
    issuerCertificateId: ''
  };

  availableCertificates: SimpleCertificate[] = [];

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

  constructor(private certificateService: CertificateService) {}

  ngOnInit() {
    this.certificateService.getSimpleCertificates().subscribe(cers => {
      this.availableCertificates = cers;
    });
  }

  addExtension() {
    const availableKey = this.supportedExtensions.find(
      key => !(key in this.certificateForm.extensions)
    );
    if (availableKey) {
      this.certificateForm.extensions[availableKey] = '';
    }
  }

  removeExtension(key: string) {
    delete this.certificateForm.extensions[key];
  }

  isKeyDisabled(key: string): boolean {
    return key in this.certificateForm.extensions;
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

    console.log('Issuing certificate:', this.certificateForm);
    this.certificateService.createCertificate(this.certificateForm).subscribe();
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
