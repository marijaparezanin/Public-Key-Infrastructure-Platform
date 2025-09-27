import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormsModule, NgForm } from '@angular/forms';
import { DialogComponent } from "../../shared/dialog/dialog.component";
import { CertificateService } from '../service/certificate.service';
import {CreateCertificateTemplateDto, SimpleCertificate} from '../model/certificate.model';


@Component({
  selector: 'app-create-certificate-template',
  templateUrl: './create-certificate-template.component.html',
  styleUrls: ['../../shared/form.css'],
  imports: [FormsModule, CommonModule, DialogComponent],
  standalone: true,
})
export class CreateCertificateTemplateComponent implements OnInit {

  templateFormModel: CreateCertificateTemplateDto = {
    name: '',
    issuerCertificateId: '',
    commonNameRegex: '',
    subjectAlternativeNameRegex: '',
    ttlDays: 0,
    keyUsage: '',
    extendedKeyUsage: ''
  };

  availableIssuers: SimpleCertificate[] = [];

  showDialog: boolean = false;
  dialogMessage: string = '';
  dialogType: 'info' | 'error' = 'error';

  constructor(
    private certificateService: CertificateService,
  ) {}

  ngOnInit() {
    this.certificateService.getApplicableCA().subscribe(cers => {
      this.availableIssuers = cers;
    });
  }

  customRequired(control: AbstractControl, message: string) {
    return control.value ? null : { required: message };
  }

  submitTemplate(form: NgForm) {
    form.form.markAllAsTouched();
    if (!form.valid) {
      this.showDialogError('Please fill all required fields correctly.');
      return;
    }

    console.log('Creating template:', this.templateFormModel);
    this.certificateService.createTemplate(this.templateFormModel).subscribe({
      next: () => {
        this.showDialogInfo('Template created successfully.');
      },
      error: err => {
        console.error('Failed to create template:', err);
        this.showDialogError('Failed to create template. Please try again.');
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
}
