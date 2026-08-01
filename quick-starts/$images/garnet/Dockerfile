FROM ghcr.io/microsoft/garnet:1.0.6
USER root
RUN apt-get update && apt-get install -y --no-install-recommends redis-tools && rm -rf /var/lib/apt/lists/*
USER app
