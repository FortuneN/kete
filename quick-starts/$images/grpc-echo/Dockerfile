FROM python:3.13-alpine3.21

WORKDIR /app

COPY server.py .

RUN pip install --no-cache-dir grpcio==1.78.0

EXPOSE 50051 8080

CMD ["python", "server.py"]
