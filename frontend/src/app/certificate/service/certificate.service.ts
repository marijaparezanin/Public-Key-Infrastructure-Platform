import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../environments/environment';
import {
  Certificate,
  CreateCertificateDto,
  CreatedCertificateDto, CreateEECertificateDto,
  DownloadRequestDTO, RequestRevokeDTO,
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

  revokeCertificate(dto: RequestRevokeDTO): Observable<Blob> {
    return this.http.put(`${this.apiUrl}/revoke`, dto, { responseType: 'blob' });
  }

  getApplicableCA():Observable<SimpleCertificate[]>{
      return this.http.get<SimpleCertificate[]>(`${this.apiUrl}/applicable-ca`);
  }

  downloadCertificate(id: string, dto: DownloadRequestDTO): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/download`, dto, { responseType: 'blob' });
  }

  createEECertificate(dto: CreateEECertificateDto): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/ee`, dto, { responseType: 'blob' });
  }

  uploadCSR(formDataToSend: FormData): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/upload-csr`, formDataToSend, { responseType: 'blob' });

  }
}
