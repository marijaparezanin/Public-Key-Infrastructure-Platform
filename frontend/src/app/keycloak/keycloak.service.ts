import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private keycloak: Keycloak | null = null;
  private initialized = false;

  init(): Promise<void> {
    this.keycloak = new Keycloak({
      url: environment.keycloakUrl,
      realm: environment.keycloakRealm,
      clientId: environment.keycloakClient,
    });

    // Detect if this is a redirect after logout
    const url = new URL(window.location.href);
    const isLogoutRedirect = url.searchParams.get('logout') === 'true';

    // Clean URL params immediately
    if (isLogoutRedirect || url.searchParams.has('code') || url.searchParams.has('state')) {
      url.searchParams.delete('logout');
      url.searchParams.delete('code');
      url.searchParams.delete('state');
      window.history.replaceState({}, document.title, url.pathname);
    }

    return this.keycloak
      .init({
        onLoad: 'check-sso', // never force login
        checkLoginIframe: false,
      })
      .then(authenticated => {
        console.log('Keycloak authenticated:', authenticated);
        this.initialized = true;

        if (!authenticated && !isLogoutRedirect) {
          // Only redirect to login if user is not authenticated AND not coming from logout
          this.keycloak?.login({ redirectUri: window.location.origin + '/home' });
        }
      })
      .catch(err => {
        console.error('Keycloak init error:', err);
        this.initialized = true;
      });
  }

  login(): void {
    this.keycloak?.login({ redirectUri: window.location.origin + '/home' });
  }

  logout(): void {
    // Redirect after logout with a flag so we don't auto-login
    const logoutRedirect = new URL(window.location.origin);
    logoutRedirect.searchParams.set('logout', 'true');

    this.keycloak?.logout({ redirectUri: logoutRedirect.toString() });
  }

  getToken(): string | undefined {
    return this.keycloak?.token;
  }

  isLoggedIn(): boolean {
    return !!this.keycloak?.token;
  }

  getRoles(): string[] {
    return this.keycloak?.realmAccess?.roles || [];
  }

  isReady(): boolean {
    return this.initialized;
  }
}
