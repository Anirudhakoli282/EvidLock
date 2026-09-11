# 🛡️ EvidLock — Digital Evidence Chain-of-Custody Management System

![Java](https://img.shields.io/badge/Java%2017-Backend-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.2-Framework-brightgreen)
![Angular](https://img.shields.io/badge/Angular%2017-Frontend-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL%2016-Database-blue)
![Cryptography](https://img.shields.io/badge/SHA--256-Tamper--Evident-purple)
![Security](https://img.shields.io/badge/RBAC-Proxy%20Pattern-navy)

**EvidLock** is a digital evidence chain-of-custody platform engineered for law enforcement agencies, forensic labs, and judicial bodies. Unlike generic document management systems, every evidence record in EvidLock is cryptographically sealed, immutable, and independently verifiable.

---

## ⚡ Quick & Simple Guide (Read First)

### 🔗 Website Links
* **Frontend Web App (Open this in your browser)**: [http://localhost:4200](http://localhost:4200)
* **Backend API Server**: [http://localhost:8081](http://localhost:8081)

---

### 🔑 Login Credentials

The password for **ALL** users is the same: **`Web2324.`**

| Role | Email / Username | Password | What You Can Do In This Role |
| :--- | :--- | :--- | :--- |
| 👑 **Supervisor** (Admin) | `supervisor@evidlock.gov` | `Web2324.` | Full control. Manage cases, delete records, view all agencies and files. |
| 🔍 **Investigator** | `investigator@evidlock.gov` | `Web2324.` | Upload and add new evidence, create cases, receive and accept custody transfers. |
| 📦 **Custodian** (Vault Manager) | `custodian@evidlock.gov` | `Web2324.` | Manage vault storage, initiate custody transfers, turn Legal Hold on or off. |
| ⚖️ **Auditor** | `auditor@evidlock.gov` | `Web2324.` | Read-only. View audit logs, tamper alerts, and hash chains (cannot edit or delete). |

> 💡 **Tip**: When you open the login page at [http://localhost:4200](http://localhost:4200), you can also simply click any of the **Quick Demo Role buttons** (`Supervisor`, `Investigator`, `Custodian`, `Auditor`) to auto-fill the credentials with 1 click!

---

### 🚀 How to Run the Project (3 Simple Steps)

#### 1. Make Sure PostgreSQL is Running
* PostgreSQL needs to be running on port `5432`.
* Database name: `docufy` (username: `postgres`, password: `postgres`).

#### 2. Start the Backend Server (Terminal 1)
Open a terminal in the project folder and run:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
Wait until you see:
`Started EvidLockApplication in ... seconds` (running on port `8081`).

#### 3. Start the Frontend Server (Terminal 2)
Open a second terminal in the project folder and run:
```powershell
cd frontend
npm.cmd start
```
Wait until you see:
`Local: http://localhost:4200/`

#### 4. Open in Your Browser
Open your web browser and go to:
👉 **[http://localhost:4200](http://localhost:4200)**

---

## 📋 Table of Contents

- [⚡ Quick & Simple Guide](#-quick--simple-guide-read-first)
- [Core Differentiators & Cryptographic Guarantees](#-core-differentiators--cryptographic-guarantees)
- [Architecture & Design Patterns](#-architecture--design-patterns)
  - [3-Tier Full-Stack Overview](#3-tier-full-stack-overview)
  - [Security Proxy RBAC Pattern](#security-proxy-rbac-pattern)
  - [Append-Only Cryptographic Hash Chain](#append-only-cryptographic-hash-chain)
  - [On-Download File Integrity Verification](#on-download-file-integrity-verification)
  - [Dual-Custody Handover Workflow](#dual-custody-handover-workflow)
  - [DAO-Layer Legal Hold Enforcement](#dao-layer-legal-hold-enforcement)
- [Demo Credentials](#-demo-credentials)
- [Step-by-Step: How to Run](#-step-by-step-how-to-run)
  - [Step 1 — PostgreSQL Setup](#step-1--postgresql-setup)
  - [Step 2 — Backend Setup & Test Suite](#step-2--backend-setup--test-suite)
  - [Step 3 — Frontend Setup & Build](#step-3--frontend-setup--build)
  - [Step 4 — Launching the Application](#step-4--launching-the-application)
- [Court-Ready PDF Reports](#-court-ready-pdf-reports)
- [Project Structure](#-project-structure)
- [Automated Test Coverage](#-automated-test-coverage)

---

## 🔒 Core Differentiators & Cryptographic Guarantees

1. **Tamper-Evident SHA-256 Custody Event Ledger**: Every custody event (Ingestion, Transfer, Analysis, Viewing, Legal Hold change) computes an immutable block hash linked to the prior block's hash. The PostgreSQL database further enforces this via a trigger prohibiting any `UPDATE` or `DELETE` on `custody_events`.
2. **File Integrity Verification on Retrieval**: Upon upload, the server calculates and seals the genuine SHA-256 checksum. Whenever an evidence file is downloaded or accessed, its hash is recalculated on the fly. If any bytes were altered, the download is blocked with HTTP 409 Conflict, an `INTEGRITY_ALERT` event is recorded, and the item is permanently flagged as `COMPROMISED`.
3. **Dual-Custody Handover Protocol**: Transfers cannot be unilateral. A releasing custodian authorizes a transfer request with purpose and a step-up password signature. The recipient must authenticate, review the handover request, and provide their own step-up password signature to legally assume custody.
4. **Proxy-Pattern Role-Based Access Control**: Four specialized actors (`UserSupervisor`, `UserInvestigator`, `UserCustodian`, `UserAuditor`). The `UserAuditor` proxy strictly intercepts and blocks any state-mutating operation, throwing HTTP 403 Forbidden, guaranteeing non-repudiation and pure audit compliance.
5. **Hard DAO-Layer Legal Hold Enforcement**: Evidence items flagged with `legalHold = true` are locked against deletion, premature disposal, or file alterations directly at the DAO layer.
6. **Court-Ready PDF Reports**: Automated generation of legal-grade Chain of Custody documents with timestamps, custodian history, and hash verifications using OpenPDF.

---

## 🏗️ Architecture & Design Patterns

### 3-Tier Full-Stack Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Angular 17 SPA (Port 4200)                                 │
│  - Evidence Vault, Audit Timeline, Dual-Custody Modals      │
│  - Chart.js Health & Retention Dashboards                   │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP REST API (Basic Auth)
┌──────────────────────────────▼──────────────────────────────┐
│  Spring Boot 3.2 Backend (Port 8081)                        │
│  - MVC Controllers & REST Services                          │
│  - Security Proxy RBAC Layer                                │
│  - HashChainService & FileIntegrityService                  │
│  - RetentionAlertScheduler (JavaMail notifications)         │
│  - OpenPDF Court Report Generator                           │
└──────────────────────────────┬──────────────────────────────┘
                               │ JDBC / PostgreSQL Driver
┌──────────────────────────────▼──────────────────────────────┐
│  PostgreSQL 16 Database (Port 5432)                         │
│  - Table triggers: trg_custody_events_append_only           │
│  - Trigger: trg_evidence_items_legal_hold                   │
└─────────────────────────────────────────────────────────────┘
```

### Security Proxy RBAC Pattern

Every incoming request resolves the authenticated principal into a concrete proxy class implementing the domain interface:

- **`UserSupervisor`**: Administrative authority to configure agencies, purge records (if not under Legal Hold), and oversee all cases.
- **`UserInvestigator`**: Case lead authority to ingest evidence, initiate case inquiries, and accept custody transfers for examination.
- **`UserCustodian`**: Evidence room authority managing physical/digital vault transfers and placing/lifting legal holds.
- **`UserAuditor`**: Read-only oversight role. Every mutation method (`uploadEvidence`, `deleteEvidence`, `initiateTransfer`, `toggleLegalHold`) immediately throws `ResponseStatusException(HttpStatus.FORBIDDEN)`.

### Append-Only Cryptographic Hash Chain

Every custody event computes:
$$\text{eventHash} = \text{SHA-256}(\text{previousEventHash} + \text{actorId} + \text{action} + \text{timestamp} + \text{evidenceItemId})$$

- **Genesis Block**: Created on evidence upload with `previousEventHash = "0000000000000000000000000000000000000000000000000000000000000000"`.
- **Audit Verification**: The ledger modal iterates through all blocks and verifies each link. If any block or hash was manipulated, the verification fails and pinpoints the broken block.

---

## 🔑 Demo Credentials

EvidLock includes pre-seeded accounts representing all four security roles across multiple agencies. The login page provides 1-click credential loading:

| Role | Email | Password | Agency | Privileges |
|------|-------|----------|--------|------------|
| 👑 **Supervisor** | `supervisor@evidlock.gov` | `Web2324.` | MPD-01 (Metropolitan Police) | Full administration, case oversight, deletion |
| 🔍 **Investigator** | `investigator@evidlock.gov` | `Web2324.` | MPD-01 (Metropolitan Police) | Evidence upload, case management, transfer acceptance |
| 📦 **Custodian** | `custodian@evidlock.gov` | `Web2324.` | SFL-02 (State Forensic Lab) | Vault custody, transfer initiation, legal holds |
| ⚖️ **Auditor** | `auditor@evidlock.gov` | `Web2324.` | OIG-04 (Inspector General) | **Strictly Read-Only** (403 on mutations) |

---

## 🚀 Step-by-Step: How to Run

### Step 1 — PostgreSQL Setup

1. Ensure PostgreSQL 14+ is running on port `5432`:
   ```powershell
   Get-Service -Name "postgresql*"
   ```
2. Create and seed the `docufy` database using `database/schema.sql` and `database/data.sql`:
   ```powershell
   psql -U postgres -c "DROP DATABASE IF EXISTS docufy;"
   psql -U postgres -c "CREATE DATABASE docufy;"
   psql -U postgres -d docufy -f "database/schema.sql"
   psql -U postgres -d docufy -f "database/data.sql"
   ```

### Step 2 — Backend Setup & Test Suite

1. Verify `backend/src/main/resources/application.properties`:
   ```properties
   server.port=8081
   db.url=jdbc:postgresql://localhost:5432/docufy
   db.user=postgres
   db.password=postgres
   ```
2. Run unit and integration tests (tests hash chains, file integrity checking, legal hold locks, and proxy RBAC):
   ```powershell
   cd backend
   .\mvnw.cmd test
   ```
   *Expected result: `BUILD SUCCESS` (13 tests passed, 0 failures).*

3. Start the Spring Boot backend:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *The backend API will be available at http://localhost:8081.*

### Step 3 — Frontend Setup & Build

1. Open a new terminal in `frontend/`:
   ```powershell
   cd frontend
   npm.cmd install
   ```
2. Validate frontend compilation:
   ```powershell
   npm.cmd run build
   ```
   *Expected result: `Application bundle generation complete. [0 errors]`.*

### Step 4 — Launching the Application

1. Start the Angular development server:
   ```powershell
   npm.cmd start
   ```
2. Navigate to:
   ```
   http://localhost:4200
   ```
3. Use the 1-click demo buttons on the login screen to sign in as **Supervisor**, **Investigator**, **Custodian**, or **Auditor**.

---

## 📄 Court-Ready PDF Reports

EvidLock generates official, tamper-evident Chain of Custody summary reports via OpenPDF:
- Header containing the Evidence Tracking Tag, Case Number, Ingestion Date, and Description.
- Monospace SHA-256 Genesis Hash and current vault integrity status.
- Chronological timeline table with block sequence numbers, timestamps, actions, actors, transfer parties, and previous/current event SHA-256 hashes.
- Legal declaration and signature block for court exhibit submission.

To generate a report, click the **PDF** button on any evidence row or click **Court-Ready PDF** inside the Cryptographic Ledger modal.

---

## 📁 Project Structure

```
Docufy-main/
├── backend/
│   ├── src/main/java/com/evidlock/
│   │   ├── controller/
│   │   │   ├── service/           # REST endpoints (Evidence, Custody, Transfers, Reports, Files)
│   │   │   ├── HashChainService.java      # SHA-256 chain calculation & audit verification
│   │   │   └── FileIntegrityService.java  # File hash calculation & integrity defense
│   │   └── persistence/
│   │       ├── model/             # EvidenceItem, Case, CustodyEvent, CustodyTransferRequest
│   │       │   ├── User.java              # Base user & proxy contracts
│   │       │   ├── UserSupervisor.java    # Administrative proxy
│   │       │   ├── UserInvestigator.java  # Investigator proxy
│   │       │   ├── UserCustodian.java     # Vault custodian proxy
│   │       │   └── UserAuditor.java       # Read-only auditor proxy (enforces 403)
│   │       └── dao/postgres/      # DAOs with Legal Hold locks and append-only constraints
│   ├── src/test/java/com/evidlock/ # Automated unit & security tests
│   └── pom.xml                    # Maven dependencies (OpenPDF, Spring Boot 3.2, PostgreSQL)
│
├── frontend/
│   ├── src/app/
│   │   ├── model/                 # TypeScript interfaces (EvidenceItem, CustodyEvent, Case)
│   │   ├── service/               # HTTP services (evidence, custody, transfers, report)
│   │   └── view/
│   │       ├── _DASHBOARD/        # Case status breakdown & integrity charts
│   │       ├── _DOCUMENTI/        # Evidence Vault table, ledger modal, dual-custody modals
│   │       └── _LOGIN/            # 1-click role authentication portal
│   └── angular.json
│
├── database/
│   ├── schema.sql                 # Complete PostgreSQL schema with append-only triggers
│   └── data.sql                   # Seed data with genuine SHA-256 hashes and custody chains
└── README.md
```

---

## 🧪 Automated Test Coverage

The backend test suite (`mvn test`) runs 13 automated tests across four core domains:

1. **`HashChainTest`**:
   - `testGenesisHashCalculation`: Verifies correct genesis block SHA-256 computation.
   - `testTamperBreaksChain`: Simulates tampering with an intermediate block and verifies the chain verification fails and detects the exact compromised block.
   - `testSeededDatabaseChainIntegrity`: Validates the seeded production chain against actual database records.
2. **`FileIntegrityTest`**:
   - `testKnownHashVector`: Validates SHA-256 engine against NIST test vectors.
   - `testUnalteredFilePassesIntegrityCheck`: Verifies valid files pass checksum checks.
   - `testCorruptedFileFailsIntegrityCheck`: Modifies file contents and verifies download blocking with `INTEGRITY_COMPROMISED`.
3. **`LegalHoldEnforcementTest`**:
   - `testLegalHoldPreventsDeletion`: Verifies DAO layer blocks deletion when `legalHold = true`.
   - `testLegalHoldPreventsDisposal`: Verifies premature disposal is prevented.
   - `testLegalHoldTogglingAllowsOperation`: Verifies lifting the hold re-enables authorized actions.
4. **`SecurityProxyRbacTest`**:
   - `testAuditorIsStrictlyReadOnly`: Verifies `UserAuditor` throws 403 on all mutation methods.
   - `testInvestigatorRestrictedFromDeleting`: Verifies investigators cannot delete evidence.
   - `testCustodianCanInitiateTransfers`: Verifies custodians can place legal holds and release transfers.
   - `testSupervisorHasAdminPrivileges`: Verifies supervisor administrative capabilities.

---
*EvidLock — Digital Evidence Chain-of-Custody Management System.*
