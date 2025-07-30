package com.crossborder.hospitalA.fabric;

import com.crossborder.hospitalA.model.FabricResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

@Service
public class FabricClient {

    private static final Logger log = LoggerFactory.getLogger(FabricClient.class);

    private final Gateway gateway;
    private final Network network;
    private final Contract contract;

    public FabricClient() throws Exception {
        log.info("------Creating in-memory wallet------");

        Path certPath = Paths.get("cert.pem");
        Path keyPath  = Paths.get("key_sk");

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
        this.contract = network.getContract("healthcare");

        log.info("FabricClient initialised — channel: {}", network.getChannel().getName());
    }

    public FabricResponse isDoctorAuthorizedDetailed(
            String doctorId,
            String patientId,
            String purpose,
            String hospitalName) throws Exception {

        log.debug("Calling verifyAccess({}, {}, {}, {})", doctorId, patientId, purpose, hospitalName);

        // Submit transaction
        Transaction transaction = contract.createTransaction("verifyAccess");
        byte[] result = transaction.submit(doctorId, patientId, purpose, hospitalName);

        // Convert payload to string
        String responsePayload = new String(result, StandardCharsets.UTF_8);

        // Parse JSON to check authorization
        JsonNode json = new ObjectMapper().readTree(responsePayload);
        boolean authorized = json.has("authorized") && json.get("authorized").asBoolean();

        // Create response
        FabricResponse response = new FabricResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setBlockNumber(0L);
        response.setValidationCode("N/A");
        response.setResponsePayload(responsePayload);
        response.setInputArgsJson(new ObjectMapper().writeValueAsString(
                List.of(doctorId, patientId, purpose, hospitalName)
        ));
        response.setEndorsersJson("[]");
        response.setAccessGranted(authorized);

        log.info("Access check doctor {} → authorized={}, TxID={}", doctorId, authorized, transaction.getTransactionId());
        return response;
    }

    public boolean isDoctorAuthorized(String doctorId, String patientId, String purpose, String hospitalName) throws Exception {
        return isDoctorAuthorizedDetailed(doctorId, patientId, purpose, hospitalName).isAccessGranted();
    }

    public void submitPatientData(String patientId, String doctorId, String data) throws Exception {
        log.debug("Submitting patient data: patientId={}, doctorId={}", patientId, doctorId);
        contract.submitTransaction("submitPatientData", patientId, doctorId, data);
    }

    public void close() {
        log.info("Closing Fabric gateway");
        gateway.close();
    }

    private static X509Certificate readX509Certificate(String pem) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (InputStream certStream =
                     new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))) {
            return (X509Certificate) factory.generateCertificate(certStream);
        }
    }
}
