import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CertificateType, CreateCertificateDto } from '../model/certificate.model';

@Component({
  selector: 'app-create-certification',
  templateUrl: './create-certificate.component.html',
  styleUrls: ['../../shared/form.css'],
  imports:[FormsModule,CommonModule],
  standalone:true,
})
export class CreateCertificationComponent {
  @Input() role: 'admin' | 'ca' | null = null;

  certificateForm: CreateCertificateDto = {
      type: CertificateType.Root,
      commonName: '',
      surname: '',
      givenName: '',
      organization: '',
      organizationalUnit: '',
      country: '',
      email: '',
      startDate: new Date(),
      endDate: new Date(),
      extensions: [],
      issuerCertificateId: ''
  };;

  submitCertificate() {
    console.log('Issuing certificate:', this.certificateForm);
  }
}
