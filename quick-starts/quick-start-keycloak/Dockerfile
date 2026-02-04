# -----------------------------------------------------------------------------
# Stage 1: Build KETE JAR from source
# -----------------------------------------------------------------------------

FROM maven:3.9.9-eclipse-temurin-21-noble AS maven-build

WORKDIR /src
COPY pom.xml .
RUN mvn clean
RUN mvn -q -DskipTests dependency:go-offline
COPY . .
RUN mvn -B -DskipTests package

# -----------------------------------------------------------------------------
# Stage 2: Build Keycloak with KETE provider
# -----------------------------------------------------------------------------

FROM quay.io/keycloak/keycloak:26.0.7 AS keycloak-build

COPY --from=maven-build --chown=keycloak:keycloak /src/target/kete.jar /opt/keycloak/providers/kete.jar
RUN /opt/keycloak/bin/kc.sh build

# -----------------------------------------------------------------------------
# Stage 3: Final image
# -----------------------------------------------------------------------------

FROM quay.io/keycloak/keycloak:26.0.7 AS final

COPY --from=keycloak-build /opt/keycloak/ /opt/keycloak/

ENV KC_BOOTSTRAP_ADMIN_USERNAME=admin
ENV KC_BOOTSTRAP_ADMIN_PASSWORD=admin

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev"]
