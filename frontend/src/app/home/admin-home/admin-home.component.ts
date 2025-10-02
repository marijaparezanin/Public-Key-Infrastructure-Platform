import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AllCertificationsComponent } from '../../certificate/all-certificates/all-certificates.component';
import { CreateCertificationComponent } from '../../certificate/create-certificate/create-certificate.component';
import { NavbarComponent } from '../../navbar/navbar.component';
import { RegisterCAComponent } from "../../user/register-ca/register-ca.component";

@Component({
  selector: 'app-admin-home',
  templateUrl: './admin-home.component.html',
  styleUrls: ['../../shared/page.css','../../shared/tabs.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, AllCertificationsComponent, CreateCertificationComponent, NavbarComponent, RegisterCAComponent],
})
export class AdminHomeComponent {
  activeTab: string = 'addUser';
  constructor() {}
}
