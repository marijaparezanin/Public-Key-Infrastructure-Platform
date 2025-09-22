import { Component } from '@angular/core';
import { NavbarComponent } from "../../navbar/navbar.component";
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import {AdminService, CreateCertificateDto} from './admin.service';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [
    NavbarComponent,
    CommonModule,
    FormsModule,
    HttpClientModule
  ],
  templateUrl: './admin-home.component.html',
  styleUrls: ['./admin-home.component.css']
})
export class AdminHomeComponent {
  activeTab: string = 'addUser';

  certificates = [
    {id:1, cn:'example.com', o:'Org1', ou:'IT', c:'RS', email:'admin@example.com', type:'Root', validity:365, status:'Active'},
    {id:2, cn:'server.example.com', o:'Org1', ou:'IT', c:'RS', email:'admin@example.com', type:'End-Entity', validity:180, status:'Active'}
  ];

  // Bind these to your form inputs using [(ngModel)]
  certificateForm: CreateCertificateDto = {
    name: '',
    cn: '',
    o: '',
    ou: '',
    c: '',
    email: '',
    type: 'Root',
    validity: 365
  };

  constructor(private adminService: AdminService) {}

  submitCertificate() {
    console.log('Submitting certificate', this.certificateForm);

    this.adminService.createCertificate(this.certificateForm).subscribe({
      next: (res: any) => {
        console.log('Certificate created', res);
        alert('Certificate successfully created!');
        // Optional: refresh the certificates table
      },
      error: (err: any) => {
        console.error('Error creating certificate', err);
        alert('Failed to create certificate.');
      }
    });
  }
}
