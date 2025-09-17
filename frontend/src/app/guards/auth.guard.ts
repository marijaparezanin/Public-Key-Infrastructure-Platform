import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { KeycloakService } from '../keycloak/keycloak.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private keycloak: KeycloakService) {}

  canActivate(): boolean {
    if (!this.keycloak.isLoggedIn()) {
      this.keycloak.login();
      return false;
    }
    return true;
  }
}
