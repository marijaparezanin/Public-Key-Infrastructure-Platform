import { Component } from '@angular/core';
import {NavbarComponent} from '../../navbar/navbar.component';
import { CommonModule } from '@angular/common';

interface Certificate {
  id: number;
  cn: string;
  o: string;
  ou: string;
  c: string;
  email: string;
  type: string;
  validity: number;
  status: string;
}

interface CACertificate {
  id: number;
  name: string;
}


@Component({
  selector: 'app-ee-home',
  imports: [
    NavbarComponent,
    CommonModule
  ],
  templateUrl: './ee-home.component.html',
  styleUrl: './ee-home.component.css'
})
export class EeHomeComponent {
  // Active tab
  activeTab: string = 'uploadGenerate';

  // Mock list of CA Certificates for selection
  caCertificates: CACertificate[] = [
    { id: 1, name: 'Org1 Root CA' },
    { id: 2, name: 'Org1 Intermediate CA' },
    { id: 3, name: 'Org2 Root CA' }
  ];

  // Mock list of certificates for table views
  certificates: Certificate[] = [
    { id: 1, cn: 'user1.ftn.com', o: 'FTN', ou: 'IT', c: 'RS', email: 'user1@ftn.com', type: 'End-Entity', validity: 365, status: 'Valid' },
    { id: 2, cn: 'device1.ftn.com', o: 'FTN', ou: 'Lab', c: 'RS', email: 'device1@ftn.com', type: 'End-Entity', validity: 180, status: 'Valid' },
    { id: 3, cn: 'user2.ftn.com', o: 'FTN', ou: 'Admin', c: 'RS', email: 'user2@ftn.com', type: 'End-Entity', validity: 365, status: 'Revoked' }
  ];

  constructor() {}

  // Methods to handle form submissions
  uploadCSR(formData: any) {
    console.log('Upload CSR:', formData);
    alert('CSR and Key uploaded successfully!');
  }

  generateCertificate(formData: any) {
    console.log('Generate Certificate:', formData);
    alert('Certificate generated successfully!');
  }

  selectCACertificate(selectedCA: CACertificate) {
    console.log('Selected CA Certificate:', selectedCA);
    alert(`CA Certificate "${selectedCA.name}" selected.`);
  }

  downloadCertificate(cert: Certificate) {
    console.log('Download Certificate:', cert);
    alert(`Downloading certificate ${cert.cn}...`);
  }

  revokeCertificate(cert: Certificate, reason: string) {
    console.log('Revoke Certificate:', cert, 'Reason:', reason);
    alert(`Certificate ${cert.cn} revoked for reason: ${reason}`);
  }
}
