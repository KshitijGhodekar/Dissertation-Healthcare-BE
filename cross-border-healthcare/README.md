# Backend Setup – Cross-Border Healthcare Data Exchange

This README provides setup instructions for the **backend services** of the Cross-Border Healthcare Data Exchange system.  
It covers how to start the supporting services (Kafka, PostgreSQL, Hyperledger Fabric) and run the hospital microservices (System A & System B) for testing cross-border patient data requests.

If you are looking for the overall project description, features, and repository structure, please see the [main README](../README.md).


### 1. Start the Supporting Services (Kafka, PostgreSQL, Fabric, System A & B)

Run the following command to start all services:

```bash
docker-compose up -d
```
This will start the following components:

- **Apache Kafka** → Message broker for cross-border communication
- **PostgreSQL** → Encrypted patient data storage
- **Hyperledger Fabric network** → Audit logging & access control
- **System A** → Hospital (Data Provider)
- **System B** → Hospital (Data Requester)  

### 2. Start the Fabric Network, Create Channel and Deploy

Initialize the Fabric network and create a channel (e.g., `mychannel`):

```bash
cd fabric-samples/test-network
./network.sh down && docker system prune -af
./network.sh up createChannel -c mychannel

./network.sh deployCC -c mychannel -ccn healthcarecc \
-ccp ../chaincode/healthcare-contract -ccl node
```

### 3. Trigger a Cross-Border Request (System B → System A)

You can test the full flow by sending a patient request from **System B**.  
There are three ways to do this:

**Option A — Frontend Dashboard (recommended for ease of use):**  
Log in to the **Cross-Border Healthcare Portal** as a doctor, navigate to the **Patient Request Form**, and submit the request via the UI.

**Option B — REST API:**
```bash
# System B API (example)
curl -X POST http://localhost:8081/api/requests \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": "doc123",
    "doctorName": "Kshitij Ghodekar",
    "patientIds": ["P003"],
    "purpose": "emergency",
    "hospitalName": "Ireland Hospital"
  }'


