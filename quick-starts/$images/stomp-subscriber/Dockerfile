FROM python:3.12-alpine

RUN pip install --no-cache-dir stomp.py

COPY subscribe.py /subscribe.py

CMD ["python", "/subscribe.py"]
