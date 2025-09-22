import { Organization } from "./organization.model";

export enum CertificateType{
    'Root',
    'Intermediate',
    'End-Entity'
}

export interface CreateCertificateDto {
  type: CertificateType;
  commonName: string;
  surname: string;
  givenName: string;
  organization: string;
  organizationalUnit: string;
  country: string;
  email: string;
  startDate: Date; 
  endDate: Date;
  extensions: string[];
  issuerCertificateId: string;
}

export interface Certificate {
    id: number;
    organization: Organization;
    type: CertificateType;
    serialNumber: string;
    startDate: Date;
    endDate: Date;
    certificateEncoded?: string;
    privateKeyEncrypted?: string; 
    iv: string;
    extensionsJson?: string;
    revoked: boolean;
}

export interface CACertificate {
  id: number;
  name: string;
}
