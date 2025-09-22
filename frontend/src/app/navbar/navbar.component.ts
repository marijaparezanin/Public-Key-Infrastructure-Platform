import { Component } from '@angular/core';
import {KeycloakService} from '../keycloak/keycloak.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  imports:[CommonModule,FormsModule],
  standalone: true,
})
export class NavbarComponent {
  constructor(private keycloak: KeycloakService) {}

  logout() {
    this.keycloak.logout();
  }
}
