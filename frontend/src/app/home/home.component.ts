import { Component } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak.service';

@Component({
  selector: 'app-home',
  template: `
    <h1>Home</h1>
    <p>Welcome! You are logged in.</p>
    <button (click)="logout()">Logout</button>
  `,
})
export class HomeComponent {
  constructor(private keycloak: KeycloakService) {}

  logout() {
    this.keycloak.logout();
  }
}
