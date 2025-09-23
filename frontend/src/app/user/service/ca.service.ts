import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {environment} from '../../../environments/environment';
import { Certificate, CertificateType, SimpleCertificate } from '../../certificate/model/certificate.model';

@Injectable({ providedIn: 'root' })
export class CAService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.serverUrl}/ca`;

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

  getSimpleCertificates():SimpleCertificate[]{
      return [
          {
              id: '550e8400-e29b-41d4-a716-446655440000',
              serialNumber: '1234567890',
              subjectCommonName: 'John Doe',
              validTo: new Date('2026-12-31'),
          },
          {
              id: '550e8400-e29b-41d4-a716-446655440001',
              serialNumber: '0987654321',
              subjectCommonName: 'Acme Corp Server',
              validTo: new Date('2025-11-15'),
          },
          {
              id: '550e8400-e29b-41d4-a716-446655440002',
              serialNumber: '1122334455',
              subjectCommonName: 'Jane Smith',
              validTo: new Date('2027-01-20'),
          },
          {
              id: '550e8400-e29b-41d4-a716-446655440003',
              serialNumber: '6677889900',
              subjectCommonName: 'Test CA',
              validTo: new Date('2028-05-10'),
          },
      ];
  }
}