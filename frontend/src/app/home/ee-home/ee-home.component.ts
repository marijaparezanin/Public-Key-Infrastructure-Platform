import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { AllCertificationsComponent } from "../../certificate/all-certificates/all-certificates.component";
import { DialogComponent, DialogType } from '../../shared/dialog/dialog.component';
import { CreateCertificationComponent } from "../../certificate/create-certificate/create-certificate.component";
import {CertificateService} from '../../certificate/service/certificate.service';
import { SimpleCertificate } from '../../certificate/model/certificate.model';

@Component({
  selector: 'app-ee-home',
  templateUrl: './ee-home.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, AllCertificationsComponent, DialogComponent, CreateCertificationComponent],
  styleUrls: ['../../shared/page.css','../../shared/tabs.css','../../shared/table.css','../../shared/form.css']
})
export class EeHomeComponent implements OnInit {
  activeTab: string = 'uploadGenerate';
  caCertificates: SimpleCertificate[] = [];

  dialogVisible = false;
  dialogMessage = '';
  dialogType: DialogType = 'info';

  constructor(private certificateService: CertificateService) {}

  ngOnInit(): void {
    this.certificateService.getApplicableCA().subscribe(cers=>{
      this.caCertificates = cers;
    });
  }

  showDialog(message: string, type: DialogType = 'info') {
    this.dialogMessage = message;
    this.dialogType = type;
    this.dialogVisible = true;
  }

  onDialogClose() {
    this.dialogVisible = false;
  }

  uploadCSR(formData: any) {
    console.log('Upload CSR:', formData);
    this.showDialog('CSR and Key uploaded successfully!', 'info');
  }

  generateCertificate(formData: any) {
    console.log('Generate Certificate:', formData);
    this.showDialog('Certificate generated successfully!', 'info');
  }
}
