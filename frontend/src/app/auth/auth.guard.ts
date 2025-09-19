import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { KeycloakService } from '../keycloak/keycloak.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private keycloak: KeycloakService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (!this.keycloak.isLoggedIn()) {
      this.keycloak.login();
      return false;
    }

    const requiredRoles = route.data['roles'] as string[] | undefined;
    if (requiredRoles && !requiredRoles.some(role => this.keycloak.getRoles().includes(role))) {
      // user logged in but not authorized
      this.router.navigate(['/home']);
      return false;
    }

    return true;
  }
}
