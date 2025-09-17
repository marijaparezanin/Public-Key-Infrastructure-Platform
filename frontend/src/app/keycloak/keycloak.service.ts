import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private keycloak: Keycloak | null = null;

  init(): Promise<boolean> {
    console.log('Keycloak');
    this.keycloak = new Keycloak({
      url: "http://localhost:8080/",
      realm: "pki",
      clientId: "pki-frontend",
    });

    return this.keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
    }).then(authenticated => {
      console.log('Keycloak:', authenticated);
      return authenticated;
    }).catch(err => {
      console.error('Keycloak init error:', err);
      return false;
    });
  }

  login() { this.keycloak?.login(); }
  logout() { this.keycloak?.logout(); }
  getToken(): string | undefined { return this.keycloak?.token; }
  isLoggedIn(): boolean { return !!this.keycloak?.token; }
}
