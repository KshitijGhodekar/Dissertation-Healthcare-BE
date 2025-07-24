package com.crossborder.hospitalB.fabric;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class FabricClient {
    private static final Logger log = LoggerFactory.getLogger(FabricClient.class);

    private final Gateway gateway;
    private final Network network;
    private final Contract contract;

    public FabricClient() throws Exception {
        log.info("--------Creating in-memory wallet------");

        /* Read cert & key that were copied into the Docker image */
        Path certPath = Paths.get("cert.pem");
        Path keyPath  = Paths.get("key_sk");

        X509Certificate certificate = Identities.readX509Certificate(Files.newBufferedReader(certPath));
        PrivateKey privateKey       = Identities.readPrivateKey      (Files.newBufferedReader(keyPath));

        /* Build in-memory wallet and add admin identity */
        Wallet wallet = Wallets.newInMemoryWallet();
        wallet.put("admin", Identities.newX509Identity("Org1MSP", certificate, privateKey));
        log.info("Loaded 'admin' identity into in-memory wallet");

        /* Connection profile (bundled in the JAR) */
        Path networkConfigPath = Paths.get("connection-org1.json");
        log.info("Using connection profile: {}", networkConfigPath.toAbsolutePath());

        /* Connect gateway */
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, "admin")
                .networkConfig(networkConfigPath)
                .discovery(true);

        this.gateway  = builder.connect();
        this.network  = gateway.getNetwork("mychannel");
        this.contract = network.getContract("healthcare");

        log.info("FabricClient initialised — channel: {}", network.getChannel().getName());
    }

    /* ---------- helper methods ---------- */

//    public boolean isDoctorAuthorized(String doctorId, String patientId,
//                                      String purpose, String hospitalName) throws Exception {
//
//        log.debug("Calling verifyAccess({}, {}, {}, {})", doctorId, patientId, purpose, hospitalName);
//        byte[] result = contract.evaluateTransaction(
//                "verifyAccess", doctorId, patientId, purpose, hospitalName);
//
//        JsonNode json = new ObjectMapper().readTree(result);
//        boolean authorized = json.has("authorized") && json.get("authorized").asBoolean();
//
//        log.info("Access check doctor {} → authorized={}", doctorId, authorized);
//        return authorized;
//    }

    public void logAccess(String doctorId, String patientId,
                          String purpose, String hospitalName) throws Exception {

        log.debug("Submitting logAccess({}, {}, {}, {})",
                doctorId, patientId, purpose, hospitalName);

        contract.submitTransaction("logAccess",
                doctorId, patientId, purpose, hospitalName);

        log.info("Access logged on-chain for doctor {}", doctorId);
    }

    public void submitPatientData(String patientId, String name, String age) throws Exception {
        log.debug("Submitting createPatient({}, {}, {})", patientId, name, age);
        contract.submitTransaction("createPatient", patientId, name, age);
        log.info("Patient {} created/updated on-chain", patientId);
    }

    public void close() {
        log.info("Closing Fabric gateway");
        gateway.close();
    }
}
