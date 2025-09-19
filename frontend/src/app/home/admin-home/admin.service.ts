// src/app/admin/admin.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../environments/environment';

export interface CreateCertificateDto {
  name: string;
  cn: string;
  o: string;
  ou: string;
  c: string;
  email: string;
  type: 'Root' | 'Intermediate' | 'End-Entity';
  validity: number; // in days
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.serverUrl}/admin`; // adjust according to backend

  createCertificate(dto: CreateCertificateDto): Observable<any> {
    return this.http.post(`${this.apiUrl}/certificates`, dto);
  }
}
