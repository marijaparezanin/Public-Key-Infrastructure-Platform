import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../navbar/navbar.component';
import { CreateCertificationComponent } from '../../certificate/create-certificate/create-certificate.component';
import { AllCertificationsComponent } from '../../certificate/all-certificates/all-certificates.component';
import { DialogType, DialogComponent } from '../../shared/dialog/dialog.component';
import {
  CreateCertificateTemplateComponent
} from '../../certificate/create-certificate-template/create-certificate-template.component';

@Component({
  selector: 'app-ca-home',
  templateUrl: './ca-home.component.html',
  styleUrls: ['../../shared/page.css','../../shared/tabs.css','../../shared/table.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, CreateCertificationComponent, AllCertificationsComponent, DialogComponent, CreateCertificateTemplateComponent],
})
export class CaHomeComponent {
  activeTab: string = 'issueCerts';

  dialogVisible = false;
  dialogMessage = '';
  dialogType: DialogType = 'info';
  deleteTarget: any = null;

  templates = [
    {name:'Default EE', caIssuer:'Org1 Root', cnRegex:'.*\.example\.com', sanRegex:'.*\.example\.com', ttl:180, keyUsage:'digitalSignature', extendedKeyUsage:'clientAuth'}
  ];

  openDeleteDialog(template: any) {
    this.deleteTarget = template;
    this.dialogMessage = `Are you sure you want to delete template "${template.name}"?`;
    this.dialogType = 'confirm';
    this.dialogVisible = true;
  }

  onDialogClose() {
    this.dialogVisible = false;
    this.deleteTarget = null;
  }

  onDialogConfirm() {
    if (this.deleteTarget) {
      this.templates = this.templates.filter(t => t !== this.deleteTarget);
    }
    this.onDialogClose();
  }

}
