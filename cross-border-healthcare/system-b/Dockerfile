FROM openjdk:17-jdk-slim

WORKDIR /app

# ✅ Copy the built JAR
COPY target/system-b-1.0.0.jar app.jar

# ✅ Copy application config
COPY application.yml application.yml

# ✅ Copy optional file-based wallet (can be removed if unused)
COPY wallet/ wallet/
COPY src/main/resources/connection-org1.json connection-org1.json

COPY src/main/resources/peer0-org1-tls.crt src/main/resources/peer0-org1-tls.crt


# ✅ Copy crypto material into image (for in-memory wallet)
COPY src/main/resources/fabric-identities/cert.pem cert.pem
COPY src/main/resources/fabric-identities/key_sk key_sk



EXPOSE 8081

# ✅ Run Spring Boot with external config path
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.yml"]
