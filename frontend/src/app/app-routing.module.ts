import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AdminHomeComponent } from './home/admin-home/admin-home.component';
import { CaHomeComponent } from './home/ca-home/ca-home.component';
import { EeHomeComponent } from './home/ee-home/ee-home.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'home/admin', component: AdminHomeComponent, canActivate: [AuthGuard] },
  { path: 'home/ca-user', component: CaHomeComponent, canActivate: [AuthGuard] },
  { path: 'home/ee-user', component: EeHomeComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/home' },
];
