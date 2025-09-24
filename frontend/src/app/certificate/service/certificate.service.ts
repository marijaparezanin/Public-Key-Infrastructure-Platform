import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../environments/environment';
import {
  Certificate,
  CreateCertificateDto,
  CreatedCertificateDto,
  DownloadRequestDTO,
  SimpleCertificate
} from '../../certificate/model/certificate.model';

@Injectable({ providedIn: 'root' })
export class CertificateService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.serverUrl}/certificates`;

  createCertificate(dto: CreateCertificateDto): Observable<CreatedCertificateDto> {
    return this.http.post<CreatedCertificateDto>(`${this.apiUrl}`, dto);
  }

  getAllCertificates(): Observable<SimpleCertificate[]> {
    return this.http.get<SimpleCertificate[]>(`${this.apiUrl}/all`);
  }

  getApplicableCA():Observable<SimpleCertificate[]>{
      return this.http.get<SimpleCertificate[]>(`${this.apiUrl}/applicable-ca`);
  }

  downloadCertificate(id: string, dto: DownloadRequestDTO): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/download`, dto, { responseType: 'blob' });
  }

}
