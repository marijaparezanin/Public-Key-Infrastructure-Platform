import { Organization } from "./organization.model";

export enum CertificateType{
    'Root',
    'Intermediate',
    'End-Entity'
}

export interface CreateCertificateDto {
  type: CertificateType|null; //it will be invalid when null
  commonName: string;
  surname: string;
  givenName: string;
  organization: string;
  organizationalUnit: string;
  country: string;
  email: string;
  startDate: Date|null; 
  endDate: Date|null;
  extensions: string[];
  issuerCertificateId: string;
}

export interface Certificate {
    id: string;
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

export interface SimpleCertificate {
  id: string;             
  serialNumber: string;
  subjectCommonName: string;
  validTo: Date;
}
