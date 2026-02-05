FROM python:3.12-alpine

RUN pip install --no-cache-dir kafka-python-ng

COPY subscribe.py /subscribe.py

CMD ["python", "/subscribe.py"]
