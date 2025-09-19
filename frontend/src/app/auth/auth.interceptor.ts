// src/app/auth/auth.interceptor.ts
import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { KeycloakService } from '../keycloak/keycloak.service';

export const authInterceptorFn: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);

  // Ako Keycloak još nije inicijalizovan, samo proslijedi request (ili možeš čekati init)
  if (!keycloak || !keycloak.isReady()) {
    return next(req);
  }

  // updateTokenIfNeeded vraća Promise<string|null> -> konvertujemo u Observable
  return from(keycloak.updateTokenIfNeeded()).pipe(
    switchMap((token) => {
      if (token) {
        const authReq = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        });
        return next(authReq);
      }
      // nema tokena -> pošalji request bez Authorization header-a
      return next(req);
    })
  );
};
