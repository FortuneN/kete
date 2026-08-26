# -----------------------------------------------------------------------------
# Stage 1: Build Keycloak with the packaged KETE provider
#
# Expects target/kete.jar in the build context (the repository root): run
# `mvn package -DskipTests` first. The image therefore ships exactly the jar
# that was tested and released instead of a second build from source.
# -----------------------------------------------------------------------------

FROM quay.io/keycloak/keycloak:26.0.7 AS keycloak-build

# Set build-time options for Keycloak optimization
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

COPY --chown=keycloak:keycloak target/kete.jar /opt/keycloak/providers/kete.jar
RUN /opt/keycloak/bin/kc.sh build --health-enabled=true --metrics-enabled=true

# -----------------------------------------------------------------------------
# Stage 2: Final image
# -----------------------------------------------------------------------------

FROM quay.io/keycloak/keycloak:26.0.7 AS final

COPY --from=keycloak-build /opt/keycloak/ /opt/keycloak/

ENV KC_HTTP_ENABLED=true
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV kete.metrics.enabled=true
ENV KC_BOOTSTRAP_ADMIN_USERNAME=admin
ENV KC_BOOTSTRAP_ADMIN_PASSWORD=admin

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev"]
