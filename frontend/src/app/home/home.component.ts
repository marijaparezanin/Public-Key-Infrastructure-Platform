import { Component, OnInit } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  template: `<p>Redirecting to your dashboard...</p>`,
  standalone: false
})
export class HomeComponent implements OnInit {
  constructor(private keycloak: KeycloakService, private router: Router) {}

  ngOnInit() {
    if (!this.keycloak.isLoggedIn()) {
      this.keycloak.login();
      return;
    }

    const roles = this.keycloak.getRoles();
    if (roles.includes('admin')) {
      this.router.navigate(['/home/admin']);
    } else if (roles.includes('ca_user')) {
      this.router.navigate(['/home/ca-user']);
    } else {
      this.router.navigate(['/home/ee-user']);
    }
  }
}
