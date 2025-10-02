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
  activeTab: string = 'csr';
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

  csrFormModel = {
    issuerCertificateId: '',
    validFrom: '',
    validTo: ''
  };

  selectedFiles: { csrFile?: File} = {};

  onFileSelect(event: Event, type: 'csrFile') {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFiles[type] = input.files[0];
    }
  }

  uploadCSR() {
    if (!this.selectedFiles.csrFile) {
      this.showDialog('Please upload CSR.', 'error');
      return;
    }

    const dto = {
      issuerCertificateId: this.csrFormModel.issuerCertificateId,
      validFrom: this.csrFormModel.validFrom,
      validTo: this.csrFormModel.validTo
    };

    const formDataToSend = new FormData();
    formDataToSend.append('file', this.selectedFiles.csrFile);
    formDataToSend.append('data', new Blob([JSON.stringify(dto)], { type: 'application/json' }));

    this.certificateService.uploadCSR(formDataToSend).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const extension = ".pem";
        a.download = `certificate${extension}`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.showDialog('Certificate created and downloaded successfully.');
      },
      error: err => this.showDialog('CSR upload failed.', 'error')
    });
  }
}
