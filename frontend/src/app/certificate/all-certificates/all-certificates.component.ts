import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {Certificate, SimpleCertificate} from '../model/certificate.model';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { CertificateService } from '../service/certificate.service';

@Component({
  selector: 'app-all-certifications',
  templateUrl: './all-certificates.component.html',
  styleUrls: ['../../shared/table.css'],
  imports: [FormsModule, CommonModule, DialogComponent],
  standalone: true,
})
export class AllCertificationsComponent implements OnInit {
  @Input() role: 'admin' | 'ca' | 'ee' | null = null;
  certificates: SimpleCertificate[] = [];

  showDialog = false;
  dialogMessage = '';
  dialogType: 'info' | 'error' | 'confirm' = 'info';
  selectedCert: SimpleCertificate | null = null;

  constructor(
    private certificateService: CertificateService
  ) {}

  ngOnInit() {
    this.certificateService.getAllCertificates().subscribe(certs=>{
      this.certificates = certs;
    })
  }

  revokeCertificate(cert: SimpleCertificate) {
    this.selectedCert = cert;
    this.dialogMessage = `Are you sure you want to revoke certificate ${cert.serialNumber}?`;
    this.dialogType = 'confirm';
    this.showDialog = true;
  }

  downloadCertificate(cert: SimpleCertificate) {
    this.selectedCert = cert;
    this.dialogMessage = `Downloading certificate ${cert.serialNumber}...`;
    this.dialogType = 'info';
    this.showDialog = true;
  }

  onConfirm() {
    if (this.selectedCert) {
      console.log('Revoking:', this.selectedCert);
      alert(`Revoked certificate ${this.selectedCert.serialNumber}!`);
    }
    this.showDialog = false;
  }

  onClose() {
    this.showDialog = false;
  }
}
