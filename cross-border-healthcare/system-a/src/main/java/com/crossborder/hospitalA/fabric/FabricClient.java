package com.crossborder.hospitalA.fabric;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class FabricClient {

    private static final Logger log = LoggerFactory.getLogger(FabricClient.class);

    private final Gateway gateway;
    private final Network network;
    private final Contract contract;

    public FabricClient() throws Exception {
        log.info("------Creating in-memory wallet------");

        Path certPath = Paths.get("cert.pem");
        Path keyPath  = Paths.get("key_sk");

        //If adding certifica
        String certPem         = Files.readString(certPath);
        PrivateKey privateKey  = Identities.readPrivateKey(Files.newBufferedReader(keyPath));
        X509Certificate cert   = readX509Certificate(certPem);

        Wallet wallet = Wallets.newInMemoryWallet();
        wallet.put("admin", Identities.newX509Identity("Org1MSP", cert, privateKey));
        log.info("Loaded 'Doctor' identity into in-memory wallet");

        Path networkConfigPath = Paths.get("connection-org1.json");

        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, "admin")
                .networkConfig(networkConfigPath)
                .discovery(true);

        this.gateway  = builder.connect();
        this.network  = gateway.getNetwork("mychannel");
        this.contract = network.getContract("healthcare");      // chaincode

        log.info("FabricClient initialised — channel: {}", network.getChannel().getName());
    }

    // Verify doctor access to patient
    public boolean isDoctorAuthorized(String doctorId, String patientId, String purpose, String hospitalName) throws Exception {
        log.debug("Calling verifyAccess({}, {}, {}, {})", doctorId, patientId, purpose, hospitalName);
        byte[] result = contract.evaluateTransaction("verifyAccess", doctorId, patientId, purpose, hospitalName);

        JsonNode json = new ObjectMapper().readTree(result);
        boolean authorized = json.has("authorized") && json.get("authorized").asBoolean();

        log.info("Access check doctor {} → authorized={}", doctorId, authorized);
        return authorized;
    }

    // Submit patient data (createPatient on-chain)
    public void submitPatientData(String patientId, String name, String age) throws Exception {
        log.debug("Submitting createPatient({}, {}, {})", patientId, name, age);
        byte[] result = contract.submitTransaction("createPatient", patientId, name, age);
        log.info("Fabric result: {}", new String(result, StandardCharsets.UTF_8));
    }

    public void close() {
        log.info("Closing Fabric gateway");
        gateway.close();
    }

    // Utility: Convert PEM to X509 cert
    private static X509Certificate readX509Certificate(String pem) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (InputStream certStream =
                     new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))) {
            return (X509Certificate) factory.generateCertificate(certStream);
        }
    }
}
