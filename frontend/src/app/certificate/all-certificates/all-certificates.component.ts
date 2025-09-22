import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Certificate } from '../model/certificate.model';
import { EEService } from '../../user/service/ee.service';
import { AdminService } from '../../user/service/admin.service';
import { CAService } from '../../user/service/ca.service';
import { DialogComponent } from '../../shared/dialog/dialog.component';

@Component({
  selector: 'app-all-certifications',
  templateUrl: './all-certificates.component.html',
  styleUrls: ['../../shared/table.css'],
  imports: [FormsModule, CommonModule, DialogComponent],
  standalone: true,
})
export class AllCertificationsComponent implements OnInit {
  @Input() role: 'admin' | 'ca' | 'ee' | null = null;
  certificates: Certificate[] = [];

  showDialog = false;
  dialogMessage = '';
  dialogType: 'info' | 'error' | 'confirm' = 'info';
  selectedCert: Certificate | null = null;

  constructor(
    private adminService: AdminService,
    private eeService: EEService,
    private caService: CAService
  ) {}

  ngOnInit() {
    if (this.role === 'admin') {
      this.certificates = this.adminService.getAllCertificates();
    } else if (this.role === 'ca') {
      this.certificates = this.eeService.getAllCertificates();
    } else if (this.role === 'ee') {
      this.certificates = this.caService.getAllCertificates();
    }
  }

  revokeCertificate(cert: Certificate) {
    this.selectedCert = cert;
    this.dialogMessage = `Are you sure you want to revoke certificate ${cert.serialNumber}?`;
    this.dialogType = 'confirm';
    this.showDialog = true;
  }

  downloadCertificate(cert: Certificate) {
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
