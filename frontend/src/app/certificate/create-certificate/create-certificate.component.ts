import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormsModule, NgForm } from '@angular/forms';
import { CertificateType, CreateCertificateDto, SimpleCertificate } from '../model/certificate.model';
import { DialogComponent } from "../../shared/dialog/dialog.component";
import { CertificateService } from '../service/certificate.service';

interface ExtensionEntry {
  key: string;
  value: string;
}

@Component({
  selector: 'app-create-certification',
  templateUrl: './create-certificate.component.html',
  styleUrls: ['../../shared/form.css'],
  imports: [FormsModule, CommonModule, DialogComponent],
  standalone:true,
})
export class CreateCertificationComponent implements OnInit{
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
      extensions: [],
      issuerCertificateId: ''
  };
  availableCertificates:SimpleCertificate[]=[];
  supportedExtensions = [
      'keyCertSign',
      'digitalSignature',
      'basicConstraints',
      'subjectAltName',
      'authorityKeyIdentifier'
  ];
  extensionEntries: ExtensionEntry[] = [];
  constructor(private certificateService: CertificateService){}

  ngOnInit(){
    this.certificateService.getSimpleCertificates().subscribe(cers=>{
      this.availableCertificates=cers;
    });
  }

  addExtension() {
    const availableKey = this.supportedExtensions.find(
      key => !this.extensionEntries.some(e => e.key === key)
    );
    if (availableKey) {
      this.extensionEntries.push({ key: availableKey, value: '' });
    }
  }

  removeExtension(index: number) {
    this.extensionEntries.splice(index, 1);
  }
  
  isKeyDisabled(key: string, currentEntry: ExtensionEntry): boolean {
    return this.extensionEntries.some(e => e.key === key && e !== currentEntry);
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
      this.certificateForm.type = CertificateType['End-Entity'];
    }

    this.certificateForm.extensions = this.extensionEntries.map(e => `${e.key}=${e.value}`);
    console.log('Issuing certificate:', this.certificateForm);
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

}
