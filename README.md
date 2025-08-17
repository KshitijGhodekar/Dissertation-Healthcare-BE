<p align="center">
  <img src="ULlogo.svg" alt="Dissertation Project Logo" width="200"/>
</p>

## Dissertation

<p align="center">
  <b>Design and Development of a Secure, Blockchain-Backed Data Exchange Broker for Privacy-Preserving and Interoperable Cross-Border Healthcare Information Systems</b>
</p>

<h1 align="center">Cross-Border Healthcare Data Exchange – Backend</h1>

---

## Author

**Kshitij Ghodekar**

24149802

MSc Software Engineering

University of Limerick

---

## Overview
The backend implements a **Blockchain-Backed Data Exchange Broker** for secure, auditable, and real-time cross-border healthcare data sharing.  
It is based on two Spring Boot microservices:

- **System A** → India Hospital (**Data Provider**)  
- **System B** → Ireland Hospital (**Data Requester**)  

Data exchange is secured using:
- **AES-256 encryption** – Confidentiality  
- **ECDSA digital signatures** – Authenticity & Integrity  
- **Hyperledger Fabric** – Immutable Audit Logs  
- **HL7 FHIR** – Healthcare Data Interoperability  

---

## Technology Stack

| Layer                | Technology Used    |
|----------------------|--------------------|
| Backend Framework    | Java (Spring Boot) |
| Messaging Broker     | Apache Kafka       |
| Blockchain           | Hyperledger Fabric |
| Database             | PostgreSQL         |
| Encryption           | AES-256            |
| Digital Signature    | ECDSA              |
| Data Standard        | HL7 FHIR           |

---

## Repository Structure

<pre lang="text">
    <code>
    cross-border-healthcare/
   │
   ├── encryption-lib/        # Shared encryption utilities
   ├── fabric-network/        # Hyperledger Fabric setup and chaincode
   ├── fhir-conditions/       # FHIR validation and transformation
   ├── kafka/                 # Kafka cluster setup
   │
   ├── system-a/              # System A (e.g. India Hospital) - AES encryption, ECDSA signing, Blockchain client, Kafka consumer
   ├── system-b/              # System B (e.g. Ireland Hospital) - AES decryption, ECDSA verification, Kafka producer & response handler
   │
   ├── .env                   # Environment variables
   ├── docker-compose.yml     # Orchestration for Kafka, PostgreSQL, and services
   ├── README.md              # Project documentation
   └── .gitignore             # Git ignore rules

    </code>
</pre>

---
# Getting Started

To run the Cross-Border Healthcare Data Exchange system, supporting services and hospital microservices need to be started.  

Please refer to the [backend setup README](cross-border-healthcare/README.md) for detailed instructions on:  
- Starting Kafka, PostgreSQL, and Hyperledger Fabric  
- Initializing the Fabric channel and deploying chaincode  
- Running System A (Data Provider) and System B (Data Requester)  
- Triggering cross-border data requests  

---

