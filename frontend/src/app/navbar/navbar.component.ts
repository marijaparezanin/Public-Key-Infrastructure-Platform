import { Component } from '@angular/core';
import {KeycloakService} from '../keycloak/keycloak.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  constructor(private keycloak: KeycloakService) {}

  logout() {
    this.keycloak.logout();
  }
}
