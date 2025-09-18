import { Component, OnInit } from '@angular/core';
import { KeycloakService } from './keycloak/keycloak.service';
import { RouterModule } from '@angular/router'; // <-- import RouterModule for router-outlet

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule], // <-- add here
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
}


