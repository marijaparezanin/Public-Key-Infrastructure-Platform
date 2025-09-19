import { Component } from '@angular/core';
import {NavbarComponent} from "../../navbar/navbar.component";

import { CommonModule } from '@angular/common'; // <- Import CommonModule for ngIf
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-home',
    imports: [
        NavbarComponent,
      CommonModule, FormsModule
    ],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.css'
})
export class AdminHomeComponent {
  activeTab: string = 'addUser';

  certificates = [
    // Sample data
    {id:1, cn:'example.com', o:'Org1', ou:'IT', c:'RS', email:'admin@example.com', type:'Root', validity:365, status:'Active'},
    {id:2, cn:'server.example.com', o:'Org1', ou:'IT', c:'RS', email:'admin@example.com', type:'End-Entity', validity:180, status:'Active'}
  ];
}
