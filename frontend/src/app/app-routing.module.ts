import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AdminHomeComponent } from './home/admin-home/admin-home.component';
import { CaHomeComponent } from './home/ca-home/ca-home.component';
import { EeHomeComponent } from './home/ee-home/ee-home.component';
import { AuthGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  {
    path: 'home/admin',
    component: AdminHomeComponent,
    canActivate: [AuthGuard],
    data: { roles: ['admin'] }
  },
  {
    path: 'home/ca-user',
    component: CaHomeComponent,
    canActivate: [AuthGuard],
    data: { roles: ['ca_user'] }
  },
  {
    path: 'home/ee-user',
    component: EeHomeComponent,
    canActivate: [AuthGuard],
    data: { roles: ['ee_user'] }
  },
  { path: '**', redirectTo: '/home' },
];
