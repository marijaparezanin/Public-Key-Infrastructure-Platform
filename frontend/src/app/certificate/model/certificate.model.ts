import { Organization } from "./organization.model";
import {RevocationReason} from './revocation-reason.model';

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
  type: CertificateType;
  commonName: string;
  surname: string;
  givenName: string;
  organization: string;
  organizationalUnit: string;
  country: string;
  email: string;
  startDate: Date|null;
  endDate: Date|null;
  revoked: boolean;
  valid: boolean;
  serialNumber: string;
}

export enum KEYSTOREDOWNLOADFORMAT {
  JKS,
  PKCS12
}

export interface DownloadRequestDTO {
  certificateId: string;
  password: string;
  alias: string;
  format: KEYSTOREDOWNLOADFORMAT;
}

export interface RequestRevokeDTO {
  certificateId: string;
  reason: RevocationReason;

}
