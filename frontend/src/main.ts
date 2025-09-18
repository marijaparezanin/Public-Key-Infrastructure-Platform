import { APP_INITIALIZER } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { routes } from './app/app-routing.module';
import { KeycloakService } from './app/keycloak/keycloak.service';
import { provideHttpClient } from '@angular/common/http';

export function initializeKeycloak(keycloak: KeycloakService) {
  return () => keycloak.init();
}

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },
  ],
}).catch(err => console.error('Bootstrap failed:', err));
