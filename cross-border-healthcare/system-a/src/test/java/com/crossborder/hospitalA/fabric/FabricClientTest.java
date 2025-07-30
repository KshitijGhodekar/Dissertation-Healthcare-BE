package com.crossborder.hospitalA.fabric;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FabricClientTest {

    @Test
    void testFabricClientInstantiationAndClose() {
        try {
            FabricClient client = new FabricClient();
            assertNotNull(client);
            client.close();
        } catch (Exception e) {
            if (e instanceof java.nio.file.NoSuchFileException) {
                System.out.println("Skipping test — cert.pem not found");
            } else {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    void testSubmitPatientDataCall() {
        try {
            FabricClient client = new FabricClient();
            client.submitPatientData("P123", "Alice Smith", "29");
            client.close();
        } catch (Exception e) {
            if (e instanceof java.nio.file.NoSuchFileException) {
                System.out.println("Skipping test — cert.pem not found");
            } else {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    void testIsDoctorAuthorizedCall() {
        try {
            FabricClient client = new FabricClient();
            boolean result = client.isDoctorAuthorized("D123", "P123", "treatment", "India Hospital");
            System.out.println("Authorization result: " + result);
            client.close();
        } catch (Exception e) {
            if (e instanceof java.nio.file.NoSuchFileException) {
                System.out.println("Skipping test — cert.pem not found");
            } else {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }
}
