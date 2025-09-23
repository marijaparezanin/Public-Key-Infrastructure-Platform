import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../environments/environment';
import { Certificate, CreateCertificateDto, SimpleCertificate } from '../../certificate/model/certificate.model';

@Injectable({ providedIn: 'root' })
export class CertificateService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.serverUrl}/certificates`; 

  createCertificate(dto: CreateCertificateDto): Observable<Certificate> {
    return this.http.post<Certificate>(`${this.apiUrl}`, dto);
  }

  getAllCertificates(): Observable<Certificate[]> {
    return this.http.get<Certificate[]>(`${this.apiUrl}/all`);
  }

  getSimpleCertificates():Observable<SimpleCertificate[]>{
      return this.http.get<SimpleCertificate[]>(`${this.apiUrl}/ca`);
  }
  
}
