# Public Key Infrastructure Platform

A comprehensive PKI (Public Key Infrastructure) platform built with Spring Boot backend, Angular frontend, and Keycloak for authentication and authorization.

## 🏗️ Architecture

This project consists of several components:

- **Backend**: Spring Boot application providing PKI services
- **Frontend**: Angular application for user interface
- **Keycloak**: Identity and access management
- **Custom Keycloak Extensions**: Password policy providers and custom themes
- **PostgreSQL**: Database for storing certificates and organizational data

## 🚀 Features

### Certificate Management

- **Certificate Templates**: Create and manage certificate templates with configurable validation rules
- **Certificate Issuance**: Support for CA, Intermediate CA, and End Entity certificates
- **Certificate Revocation**: Revoke certificates with proper CRL management
- **Certificate Download**: Export certificates in various formats (.jks, .p12)
- **CSR Processing**: Upload and process Certificate Signing Requests

### User Roles & Access Control

- **Admin**: Full system access, organization management, CA user creation
- **CA User**: Certificate issuance, template management, certificate operations
- **End Entity User**: Certificate requests, CSR uploads, certificate downloads

### Security Features

- **Keycloak Integration**: Robust authentication and authorization
- **Custom Password Policy**: Integration with HaveIBeenPwned API for password security
- **AES Encryption**: Secure storage of sensitive organizational data
- **RSA Key Management**: Comprehensive RSA key generation and management
- **X.509 Certificate Validation**: Full certificate chain validation

## 📋 Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL (or use Docker)

## 🛠️ Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Bruda04/public-key-infrastructure-platform.git
cd public-key-infrastructure-platform
```

### 2. Environment Configuration

#### Backend Configuration

Create `.env` file in the backend directory:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=pki_db
DB_USERNAME=pki_user
DB_PASSWORD=your_password
MASTER_KEY=your_base64_master_key
KEYCLOAK_SERVER_URL=http://localhost:8080
KEYCLOAK_REALM=pki
KEYCLOAK_CLIENT_ID=pki-backend
KEYCLOAK_CLIENT_SECRET=your_client_secret
```

#### Frontend Configuration

Update `environment.ts`:

```typescript
export const environment = {
  keycloakUrl: "http://localhost:8080",
  keycloakRealm: "pki",
  keycloakClient: "pki-frontend",
  serverUrl: "https://localhost:8081/api",
};
```

### 3. Using Docker Compose (Recommended)

```bash
docker-compose up --build
```

This will start:

- PostgreSQL database
- Keycloak server with custom theme and extensions
- Backend application
- Frontend application

## 🔐 Keycloak Configuration

### Custom Extensions

#### Password Policy Provider

- **Location**: `keycloak-pwned-policy`
- **Feature**: Integrates with HaveIBeenPwned API to prevent compromised passwords
- **Build**: `mvn package` - produces `keycloak-pwned-policy.jar`
- **Deployment**: Copy to `providers` directory

#### Custom Theme

- **Location**: `themes/custom-theme`
- **Features**:
  - Modern UI design
  - Dark mode support
  - WebAuthn support
  - Custom login flows

### Realm Setup

1. Create realm named `pki`
2. Configure clients:
   - `pki-frontend` (public client for Angular app)
   - `pki-backend` (confidential client for Spring Boot)
3. Create roles: `admin`, `ca_user`, `ee_user`
4. Enable custom password policy with pwned password checking

## 🔒 Security Considerations

1. **Encryption**: All sensitive organizational data is encrypted using AES-256-GCM
2. **Key Management**: RSA keys are securely generated and stored
3. **Authentication**: Multi-factor authentication supported via Keycloak
4. **Password Security**: Integration with HaveIBeenPwned prevents compromised passwords
5. **HTTPS**: TLS encryption for all communications

## 👥 Authors

- Luka Bradić ([GitHub](https://github.com/Bruda04))
- Marija Parežanin ([GitHub](https://github.com/marijaparezanin))
- Anđela Ristić ([GitHub](https://github.com/RisticAndjela))

**Note**: This is an educational project demonstrating PKI concepts and implementation. Ensure proper security review before any production use.
