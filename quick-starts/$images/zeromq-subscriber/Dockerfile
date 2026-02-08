FROM python:3.12-alpine

RUN pip install --no-cache-dir pyzmq

COPY subscribe.py /subscribe.py

CMD ["python", "/subscribe.py"]
