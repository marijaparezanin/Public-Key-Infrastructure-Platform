import { Organization } from "./organization.model";

export enum CertificateType{
    'ROOT',
    'INTERMEDIATE',
    'END_ENTITY'
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
  extensions: Record<string, string>;
  issuerCertificateId: string;
  assignToOrganizationName: string|null;
}

export interface CreatedCertificateDto {
  commonName: string;
  surname: string;
  givenName: string;
  organization: string;
  organizationalUnit: string;
  country: string;
  email: string;
  startDate: Date|null;
  endDate: Date|null;
  extensions: Record<string, string>;
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
    revoked: boolean;
}

export interface SimpleCertificate {
  id: string;
  serialNumber: string;
  subjectCommonName: string;
  validTo: Date;
}
