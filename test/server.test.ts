import { describe, it } from 'node:test';
import assert from 'node:assert';
import http from 'node:http';
import { WebSocket } from 'ws';
import { createAgyMobileServer } from '../src/server/index.ts';
import { AgyProcessManager } from '../src/server/agy-process.ts';

describe('AgyMobileServer HTTP & WebSocket APIs', () => {
  it('serves REST API endpoints and manages mode transitions', async () => {
    const app = createAgyMobileServer({ port: 0, host: '127.0.0.1' });
    await app.listen(0, '127.0.0.1');

    const address = app.server.address() as any;
    const baseUrl = `http://127.0.0.1:${address.port}`;

    try {
      // 1. GET /api/status
      const statusRes = await fetch(`${baseUrl}/api/status`);
      assert.strictEqual(statusRes.status, 200);
      const statusData = await statusRes.json();
      assert.strictEqual(statusData.version, '1.0.0');
      assert.strictEqual(statusData.isAutonomous, true);
      assert.strictEqual(typeof statusData.activeCwd, 'string');

      // 2. POST /api/status/mode
      const modeRes = await fetch(`${baseUrl}/api/status/mode`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ autonomous: false })
      });
      assert.strictEqual(modeRes.status, 200);
      const modeData = await modeRes.json();
      assert.strictEqual(modeData.ok, true);
      assert.strictEqual(modeData.isAutonomous, false);

      // 3. GET /api/workspaces
      const wsRes = await fetch(`${baseUrl}/api/workspaces`);
      assert.strictEqual(wsRes.status, 200);
      const wsData = await wsRes.json();
      assert.ok(Array.isArray(wsData.items));

      // 4. GET /api/conversations
      const convRes = await fetch(`${baseUrl}/api/conversations`);
      assert.strictEqual(convRes.status, 200);
      const convData = await convRes.json();
      assert.ok(Array.isArray(convData.conversations));
    } finally {
      await app.close();
    }
  });

  it('streams agy events over /ws/chat and handles client messaging', async () => {
    const agyManager = new AgyProcessManager();
    const app = createAgyMobileServer({ port: 0, host: '127.0.0.1', agyManager });
    await app.listen(0, '127.0.0.1');

    const address = app.server.address() as any;
    const wsUrl = `ws://127.0.0.1:${address.port}/ws/chat`;

    try {
      const ws = new WebSocket(wsUrl);
      const received: any[] = [];

      await new Promise<void>((resolve, reject) => {
        ws.on('open', () => resolve());
        ws.on('error', reject);
        ws.on('message', (data) => {
          received.push(JSON.parse(data.toString('utf-8')));
        });
      });

      // First message received on connection is status
      assert.ok(received.length >= 1);
      assert.strictEqual(received[0].type, 'status');

      // Emit event through manager and assert delivery
      agyManager.handleOutputLine(JSON.stringify({ type: 'text', delta: 'Streaming chunk' }), true);

      // Wait a tick for ws delivery
      await new Promise((r) => setTimeout(r, 50));

      const textMsg = received.find(m => m.type === 'text');
      assert.ok(textMsg);
      assert.strictEqual(textMsg.delta, 'Streaming chunk');

      ws.close();
    } finally {
      await app.close();
    }
  });

  it('connects to /ws/pty and initiates terminal session', async () => {
    const { EventEmitter } = await import('node:events');
    const mockSpawn = () => {
      const p = new EventEmitter() as any;
      p.stdin = { writable: true, write: () => true, end: () => {} };
      p.stdout = new EventEmitter();
      p.stderr = new EventEmitter();
      p.kill = () => { p.emit('close', 0); return true; };
      return p;
    };
    const { TerminalBridge } = await import('../src/server/terminal-bridge.ts');
    const terminalBridge = new TerminalBridge({ spawnFn: mockSpawn as any });
    const app = createAgyMobileServer({ port: 0, host: '127.0.0.1', terminalBridge });
    await app.listen(0, '127.0.0.1');

    const address = app.server.address() as any;
    const wsUrl = `ws://127.0.0.1:${address.port}/ws/pty`;

    try {
      const ws = new WebSocket(wsUrl);

      await new Promise<void>((resolve, reject) => {
        ws.on('open', () => resolve());
        ws.on('error', reject);
      });

      assert.strictEqual(ws.readyState, WebSocket.OPEN);

      // Send resize and command
      ws.send(JSON.stringify({ type: 'resize', cols: 80, rows: 24 }));
      ws.send(JSON.stringify({ type: 'data', data: 'echo test\n' }));

      // Wait brief moment for relay
      await new Promise((r) => setTimeout(r, 50));

      ws.close();
    } finally {
      await app.close();
    }
  });
});
