FROM ghcr.io/microsoft/garnet:latest
USER root
RUN apt-get update && apt-get install -y --no-install-recommends redis-tools && rm -rf /var/lib/apt/lists/*
USER app
