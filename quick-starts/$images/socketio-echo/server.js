const { createServer } = require('http');
const { Server } = require('socket.io');
const express = require('express');

const app = express();
const httpServer = createServer(app);
const io = new Server(httpServer, {
  cors: { origin: '*' },
  allowEIO3: true,
  transports: ['websocket', 'polling']
});

const events = [];

io.on('connection', (socket) => {
  console.log('Client connected: ' + socket.id);
  socket.onAny((eventName, data) => {
    const msg = typeof data === 'object' ? JSON.stringify(data) : String(data);
    console.log('EVENT-RECEIVED: ' + msg);
    events.push(msg);
  });
});

app.get('/events', (req, res) => res.json(events));
app.get('/health', (req, res) => res.send('OK'));

httpServer.listen(3000, '0.0.0.0', () => {
  console.log('Socket.IO server started on port 3000');
});

process.on('uncaughtException', (err) => {
  console.error('Uncaught exception:', err);
});
