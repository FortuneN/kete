FROM node:20-alpine

WORKDIR /app

COPY server.js .

RUN npm init -y && npm install socket.io@4 express@4

EXPOSE 3000

CMD ["node", "server.js"]
