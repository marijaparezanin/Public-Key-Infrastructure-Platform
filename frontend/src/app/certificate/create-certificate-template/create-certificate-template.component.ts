import { Component, OnInit } from '@angular/core';
import { FormsModule, NgForm, AbstractControl } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { DialogComponent } from "../../shared/dialog/dialog.component";
import { CertificateService } from '../service/certificate.service';
import { CreateCertificateTemplateDto, SimpleCertificate } from '../model/certificate.model';
import {KEY_USAGES, EXTENDED_KEY_USAGES} from '../model/certificate-extensions.constants';

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

  keyUsageOptions = KEY_USAGES;
  extendedKeyUsageOptions = EXTENDED_KEY_USAGES;

  showDialog: boolean = false;
  dialogMessage: string = '';
  dialogType: 'info' | 'error' = 'error';

  constructor(private certificateService: CertificateService) {}

  ngOnInit() {
    this.certificateService.getApplicableCA().subscribe(cers => {
      this.availableIssuers = cers;
    });
  }

  // Toggle value in CSV string
  toggleCheckbox(key: 'keyUsage' | 'extendedKeyUsage', option: string) {
    const currentValue = this.templateFormModel[key] || '';
    let values = currentValue.split(',').map(v => v.trim()).filter(v => v.length);
    if (values.includes(option)) {
      values = values.filter(v => v !== option);
    } else {
      values.push(option);
    }
    this.templateFormModel[key] = values.join(',');
  }

  isChecked(key: 'keyUsage' | 'extendedKeyUsage', option: string): boolean {
    const currentValue = this.templateFormModel[key] || '';
    return currentValue.split(',').map(v => v.trim()).includes(option);
  }

  customRequired(control: AbstractControl, message: string) {
    return control.value ? null : { required: message };
  }

  submitTemplate(form: NgForm) {
    form.form.markAllAsTouched();

    const nameControl = form.controls['name'];
    if (!nameControl?.valid) {
      this.showDialogError('Please fill all required fields correctly.');
      return;
    }

    // Call backend to check if name is taken
    this.certificateService.isTemplateNameTaken(this.templateFormModel.name).subscribe({
      next: (taken: boolean) => {
        if (taken) {
          if (nameControl) {
            nameControl.setErrors({ ...nameControl.errors, nameTaken: true });
          }
          return; // stop submission
        }

        // Name is free → submit the template
        this.certificateService.createTemplate(this.templateFormModel).subscribe({
          next: () => this.showDialogInfo('Template created successfully.'),
          error: (err) => {
            console.error('Failed to create template:', err);
            this.showDialogError('Failed to create template. Please try again.');
          }
        });
      },
      error: (err) => {
        console.error('Error checking template name:', err);
        this.showDialogError('Could not check template name. Please try again.');
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

  validateTTL(control: AbstractControl) {
    if (!control.value) {
      control.setErrors({ required: 'Enter TTL in days' });
    } else if (control.value < 1) {
      control.setErrors({ min: 'TTL must be at least 1 day' });
    } else {
      control.setErrors(null);
    }
  }

}
