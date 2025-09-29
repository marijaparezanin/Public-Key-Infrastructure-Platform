export const KEY_USAGES = [
  'digitalSignature',
  'nonRepudiation',
  'keyEncipherment',
  'dataEncipherment',
  'keyAgreement',
  'keyCertSign',
  'cRLSign',
  'encipherOnly',
  'decipherOnly'
];

export const EXTENDED_KEY_USAGES = [
  'serverAuth',
  'clientAuth',
  'codeSigning',
  'emailProtection',
  'timeStamping',
  'OCSPSigning'
];


export const SUPPORTED_EXTENSIONS = [
  'keyusage',
  'extendedkeyusage',
  'subjectaltname',
];

export const SUPPORATED_EXTENSIONS_OIDS: {[key: string]: string} = {
  'keyusage': "2.5.29.15",
  'extendedkeyusage': "2.5.29.37",
  'subjectaltname': "2.5.29.17",
}
