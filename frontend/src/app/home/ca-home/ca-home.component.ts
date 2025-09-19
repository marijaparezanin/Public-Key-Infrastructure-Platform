import { Component } from '@angular/core';
import {NavbarComponent} from "../../navbar/navbar.component";
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ca-home',
    imports: [
        NavbarComponent,
      CommonModule, FormsModule
    ],
  templateUrl: './ca-home.component.html',
  styleUrl: './ca-home.component.css'
})
export class CaHomeComponent {
  activeTab: string = 'issueIntermediate';

  certificates = [
    {id:1, cn:'server1.example.com', o:'Org1', ou:'IT', c:'RS', email:'user@example.com', type:'Intermediate', validity:365, status:'Active'},
    {id:2, cn:'device1.example.com', o:'Org1', ou:'IT', c:'RS', email:'user@example.com', type:'End-Entity', validity:180, status:'Active'}
  ];

  templates = [
    {name:'Default EE', caIssuer:'Org1 Root', cnRegex:'.*\.example\.com', sanRegex:'.*\.example\.com', ttl:180, keyUsage:'digitalSignature', extendedKeyUsage:'clientAuth'}
  ];
}
