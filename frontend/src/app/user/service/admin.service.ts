import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../environments/environment';
import { Certificate, CertificateType, CreateCertificateDto } from '../../certificate/model/certificate.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.serverUrl}/admin`; // adjust according to backend

  createCertificate(dto: CreateCertificateDto): Observable<any> {
    //dto.extensions = dto.extensions
    return this.http.post(`${this.apiUrl}/certificates`, dto);
  }

  getAllCertificates(): Certificate[] {
    return [
      {
        id: 1,
        organization: { id: 1, name: 'Org1' },
        type: CertificateType.Root,
        serialNumber: '123456',
        startDate: new Date(),
        endDate: new Date(),
        iv: 'iv-test',
        revoked: false
      },
      {
        id: 2,
        organization: { id: 2, name: 'Org2' },
        type: CertificateType['End-Entity'],
        serialNumber: '654321',
        startDate: new Date(),
        endDate: new Date(),
        iv: 'iv-test-2',
        revoked: false
      }
    ];
  }

}
