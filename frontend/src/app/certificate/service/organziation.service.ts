import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../environments/environment';
import { Organization } from '../model/organization.model';

@Injectable({ providedIn: 'root' })
export class OrganizationService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.serverUrl}/organizations`; 
  
  getAll(): Observable<Organization[]> {
    return this.http.get<Organization[]>(`${this.apiUrl}`);
  }
  
}
