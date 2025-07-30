package com.crossborder.hospitalB.fabric;

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
    void testLogAccessCall() {
        try {
            FabricClient client = new FabricClient();
            client.logAccess("D123", "P001", "research", "Ireland Hospital");
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
            client.submitPatientData("P001", "John Doe", "35");
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
