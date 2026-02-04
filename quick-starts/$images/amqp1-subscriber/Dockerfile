FROM python:3.12-slim

# Install build dependencies for python-qpid-proton
RUN apt-get update && \
    apt-get install -y --no-install-recommends gcc libc-dev libqpid-proton11-dev pkg-config && \
    pip install --no-cache-dir python-qpid-proton && \
    apt-get purge -y gcc libc-dev pkg-config && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

COPY subscribe.py /subscribe.py

CMD ["python", "/subscribe.py"]
