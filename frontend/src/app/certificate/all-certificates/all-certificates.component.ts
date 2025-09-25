import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {Certificate, KEYSTOREDOWNLOADFORMAT, SimpleCertificate} from '../model/certificate.model';
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
  dialogType: 'info' | 'error' | 'confirm' | 'download' = 'info';
  selectedCert: SimpleCertificate | null = null;
  downloadOptions: string[] = [".jks", ".p12"];

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
    this.dialogMessage = `Download certificate ${cert.serialNumber}:`;
    this.dialogType = 'download';
    this.showDialog = true;
  }

  onConfirm(data?: {extension?: string, alias?: string, password?: string}) {
    if (this.selectedCert) {
      if (this.dialogType === 'download' && data) {
        let format: KEYSTOREDOWNLOADFORMAT = KEYSTOREDOWNLOADFORMAT.JKS;
        switch (data.extension) {
          case '.jks':
            format = KEYSTOREDOWNLOADFORMAT.JKS;
            break;
          case '.p12':
            format = KEYSTOREDOWNLOADFORMAT.PKCS12;
            break;
        }
        this.certificateService.downloadCertificate(this.selectedCert.id, {
          certificateId: this.selectedCert.id,
          format: format,
          password: data.password || '',
          alias: data.alias || ''
        })
          .subscribe(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            const extension = data.extension || '.jks';
            a.download = `${this.selectedCert?.serialNumber}${extension}`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
          });
      } else if (this.dialogType === 'confirm') {
        console.log('Revoking:', this.selectedCert);
        alert(`Revoked certificate ${this.selectedCert.serialNumber}!`);
      }
    }
    this.showDialog = false;
  }

  onClose() {
    this.showDialog = false;
  }
}
