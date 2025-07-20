package com.fhir.validator;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;   // ✅
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;

public class FHIRValidator {

    public static void validatePatient(JSONObject patientJson) {
        validate(patientJson, "/schemas/patient.schema.json");
    }

    public static void validateCondition(JSONObject conditionJson) {
        validate(conditionJson, "/schemas/condition.schema.json");
    }

    private static void validate(JSONObject data, String schemaPath) {
        try (InputStream schemaInputStream = FHIRValidator.class.getResourceAsStream(schemaPath)) {
            if (schemaInputStream == null) {
                throw new RuntimeException("Schema file not found at: " + schemaPath);
            }

            JSONObject rawSchema = new JSONObject(new JSONTokener(schemaInputStream));
            Schema schema = SchemaLoader.load(rawSchema);

            // 🔍 Validate (may throw ValidationException)
            schema.validate(data);

        } catch (ValidationException ve) {
            System.out.println("Schema violations:");
            ve.getAllMessages().forEach(msg -> System.out.println(" • " + msg));
            throw new RuntimeException("FHIR JSON validation failed: " + ve.getMessage(), ve);

        } catch (Exception e) {
            throw new RuntimeException("FHIR JSON validation failed: " + e.getMessage(), e);
        }
    }
}
