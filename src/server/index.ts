import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { WebSocketServer, WebSocket } from 'ws';
import { AgyProcessManager } from './agy-process.ts';
import { TerminalBridge } from './terminal-bridge.ts';
import { WorkspaceService } from './workspace-service.ts';
import type { AgyEvent, PromptRequest, ApprovalResponse } from '../types/agy.ts';

export interface ServerOptions {
  port?: number;
  host?: string;
  staticDir?: string;
  agyManager?: AgyProcessManager;
  terminalBridge?: TerminalBridge;
  workspaceService?: WorkspaceService;
}

export function createAgyMobileServer(options: ServerOptions = {}) {
  const agyManager = options.agyManager ?? new AgyProcessManager();
  const terminalBridge = options.terminalBridge ?? new TerminalBridge();
  const workspaceService = options.workspaceService ?? new WorkspaceService(agyManager.getCwd());

  // Determine static files directory
  const currentDir = path.dirname(fileURLToPath(import.meta.url));
  const candidateDirs = [
    path.join(currentDir, 'public'),
    path.join(currentDir, '../dist/public'),
    path.join(process.cwd(), 'dist/public')
  ];
  let resolvedStaticDir = candidateDirs[0];
  for (const dir of candidateDirs) {
    if (fs.existsSync(dir)) {
      resolvedStaticDir = dir;
      break;
    }
  }
  const staticDir = options.staticDir ?? resolvedStaticDir;

  const mimeTypes: Record<string, string> = {
    '.html': 'text/html; charset=utf-8',
    '.js': 'application/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.webmanifest': 'application/manifest+json; charset=utf-8',
    '.svg': 'image/svg+xml',
    '.png': 'image/png',
    '.ico': 'image/x-icon'
  };

  const server = http.createServer(async (req, res) => {
    // CORS headers for local mobile dev
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
      res.writeHead(204);
      res.end();
      return;
    }

    const parsedUrl = new URL(req.url ?? '/', `http://${req.headers.host ?? 'localhost'}`);
    const pathname = parsedUrl.pathname;

    // REST API endpoints
    if (pathname.startsWith('/api/')) {
      res.setHeader('Content-Type', 'application/json; charset=utf-8');

      // GET /api/status
      if (req.method === 'GET' && pathname === '/api/status') {
        const responseData = {
          running: agyManager.isRunning(),
          isAutonomous: agyManager.getAutonomous(),
          activeCwd: agyManager.getCwd(),
          conversationId: agyManager.getConversationId(),
          version: '1.0.0',
          agyBinary: agyManager.findAgyBinary()
        };
        res.writeHead(200);
        res.end(JSON.stringify(responseData));
        return;
      }

      // POST /api/status/mode
      if (req.method === 'POST' && pathname === '/api/status/mode') {
        const body = await parseJsonBody(req);
        if (typeof body.autonomous === 'boolean') {
          agyManager.setAutonomous(body.autonomous);
          res.writeHead(200);
          res.end(JSON.stringify({ ok: true, isAutonomous: agyManager.getAutonomous() }));
          return;
        }
        res.writeHead(400);
        res.end(JSON.stringify({ error: 'Missing boolean field: autonomous' }));
        return;
      }

      // GET /api/workspaces
      if (req.method === 'GET' && pathname === '/api/workspaces') {
        const targetPath = parsedUrl.searchParams.get('path') ?? agyManager.getCwd();
        try {
          const info = workspaceService.getWorkspaceInfo(targetPath);
          res.writeHead(200);
          res.end(JSON.stringify(info));
        } catch (err: any) {
          res.writeHead(400);
          res.end(JSON.stringify({ error: err.message }));
        }
        return;
      }

      // POST /api/workspaces/select
      if (req.method === 'POST' && pathname === '/api/workspaces/select') {
        const body = await parseJsonBody(req);
        if (body.path && typeof body.path === 'string') {
          try {
            const newPath = workspaceService.setActivePath(body.path);
            agyManager.setCwd(newPath);
            res.writeHead(200);
            res.end(JSON.stringify({ ok: true, activeCwd: newPath }));
            return;
          } catch (err: any) {
            res.writeHead(400);
            res.end(JSON.stringify({ error: err.message }));
            return;
          }
        }
        res.writeHead(400);
        res.end(JSON.stringify({ error: 'Missing path' }));
        return;
      }

      // GET /api/conversations
      if (req.method === 'GET' && pathname === '/api/conversations') {
        const limit = Number(parsedUrl.searchParams.get('limit')) || 20;
        const convs = workspaceService.getRecentConversations(limit);
        res.writeHead(200);
        res.end(JSON.stringify({ conversations: convs }));
        return;
      }

      // POST /api/prompt
      if (req.method === 'POST' && pathname === '/api/prompt') {
        const body = (await parseJsonBody(req)) as PromptRequest;
        if (!body.prompt) {
          res.writeHead(400);
          res.end(JSON.stringify({ error: 'Prompt is required' }));
          return;
        }

        // Run prompt in background; streaming updates will flow over WebSocket
        agyManager.runPrompt(body).catch(() => {});

        res.writeHead(200);
        res.end(JSON.stringify({ ok: true, message: 'Turn initiated' }));
        return;
      }

      // POST /api/approve
      if (req.method === 'POST' && pathname === '/api/approve') {
        const body = (await parseJsonBody(req)) as ApprovalResponse;
        if (!body.toolCallId || typeof body.approved !== 'boolean') {
          res.writeHead(400);
          res.end(JSON.stringify({ error: 'toolCallId and approved are required' }));
          return;
        }
        const resolved = agyManager.resolveApproval(body);
        res.writeHead(200);
        res.end(JSON.stringify({ ok: true, resolved }));
        return;
      }

      // POST /api/cancel
      if (req.method === 'POST' && pathname === '/api/cancel') {
        const cancelled = agyManager.cancel();
        res.writeHead(200);
        res.end(JSON.stringify({ ok: true, cancelled }));
        return;
      }

      res.writeHead(404);
      res.end(JSON.stringify({ error: 'Endpoint not found' }));
      return;
    }

    // Static file serving for PWA
    let filePath = path.join(staticDir, pathname === '/' ? 'index.html' : pathname);

    // Prevent path traversal
    if (!filePath.startsWith(staticDir)) {
      res.writeHead(403);
      res.end('Forbidden');
      return;
    }

    if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
      filePath = path.join(staticDir, 'index.html');
    }

    if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
      const ext = path.extname(filePath).toLowerCase();
      const contentType = mimeTypes[ext] ?? 'application/octet-stream';
      res.setHeader('Content-Type', contentType);
      res.writeHead(200);
      fs.createReadStream(filePath).pipe(res);
      return;
    }

    // Fallback if index.html doesn't exist yet
    res.setHeader('Content-Type', 'text/html; charset=utf-8');
    res.writeHead(200);
    res.end('<!DOCTYPE html><html><head><title>agy-mobile</title></head><body><h1>agy-mobile server running</h1></body></html>');
  });

  // Setup WebSockets
  const wssChat = new WebSocketServer({ noServer: true });
  const wssPty = new WebSocketServer({ noServer: true });

  // Broadcast agy events to all connected chat clients
  agyManager.on('event', (event: AgyEvent) => {
    const payload = JSON.stringify(event);
    for (const client of wssChat.clients) {
      if (client.readyState === WebSocket.OPEN) {
        client.send(payload);
      }
    }
  });

  // Handle incoming messages on /ws/chat
  wssChat.on('connection', (ws) => {
    // Send initial status immediately on connection
    ws.send(JSON.stringify({
      type: 'status',
      status: agyManager.isRunning() ? 'running' : 'idle',
      timestamp: Date.now()
    }));

    ws.on('message', async (raw) => {
      try {
        const msg = JSON.parse(raw.toString('utf-8'));
        if (msg.type === 'prompt' && msg.prompt) {
          agyManager.runPrompt(msg).catch(() => {});
        } else if (msg.type === 'approve') {
          agyManager.resolveApproval(msg);
        } else if (msg.type === 'cancel') {
          agyManager.cancel();
        } else if (msg.type === 'mode' && typeof msg.autonomous === 'boolean') {
          agyManager.setAutonomous(msg.autonomous);
        }
      } catch {
        // Ignore invalid WS messages
      }
    });
  });

  // Handle /ws/pty connections
  wssPty.on('connection', (ws, req) => {
    const termId = `term_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const session = terminalBridge.createSession(termId, {
      cwd: agyManager.getCwd()
    });

    const onData = ({ id, data }: { id: string; data: string }) => {
      if (id === termId && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'data', data }));
      }
    };

    const onExit = ({ id, code }: { id: string; code: number | null }) => {
      if (id === termId && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'exit', code }));
        ws.close();
      }
    };

    terminalBridge.on('data', onData);
    terminalBridge.on('exit', onExit);

    ws.on('message', (raw) => {
      try {
        const msg = JSON.parse(raw.toString('utf-8'));
        if (msg.type === 'data' && typeof msg.data === 'string') {
          terminalBridge.write(termId, msg.data);
        } else if (msg.type === 'resize' && msg.cols && msg.rows) {
          terminalBridge.resize(termId, Number(msg.cols), Number(msg.rows));
        }
      } catch {
        // If raw string sent, write directly
        terminalBridge.write(termId, raw.toString('utf-8'));
      }
    });

    ws.on('close', () => {
      terminalBridge.off('data', onData);
      terminalBridge.off('exit', onExit);
      terminalBridge.destroySession(termId);
    });
  });

  // Route HTTP Upgrade headers to respective WebSocketServer
  server.on('upgrade', (req, socket, head) => {
    const { pathname } = new URL(req.url ?? '/', `http://${req.headers.host ?? 'localhost'}`);

    if (pathname === '/ws/chat') {
      wssChat.handleUpgrade(req, socket, head, (ws) => {
        wssChat.emit('connection', ws, req);
      });
    } else if (pathname === '/ws/pty') {
      wssPty.handleUpgrade(req, socket, head, (ws) => {
        wssPty.emit('connection', ws, req);
      });
    } else {
      socket.destroy();
    }
  });

  return {
    server,
    wssChat,
    wssPty,
    agyManager,
    terminalBridge,
    workspaceService,
    listen: (port = options.port ?? 8080, host = options.host ?? '0.0.0.0') => {
      return new Promise<void>((resolve) => {
        server.listen(port, host, () => {
          console.log(`agy-mobile server listening on http://${host}:${port}`);
          resolve();
        });
      });
    },
    close: () => {
      return new Promise<void>((resolve) => {
        for (const client of wssChat.clients) {
          try { client.terminate(); } catch {}
        }
        for (const client of wssPty.clients) {
          try { client.terminate(); } catch {}
        }
        terminalBridge.destroyAll();
        wssChat.close();
        wssPty.close();
        server.close(() => resolve());
      });
    }
  };
}

// Support direct CLI invocation
if (process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])) {
  let port = 8080;
  let host = '0.0.0.0';

  for (let i = 2; i < process.argv.length; i++) {
    if (process.argv[i] === '--port' && process.argv[i + 1]) {
      port = Number(process.argv[i + 1]);
      i++;
    } else if (process.argv[i] === '--host' && process.argv[i + 1]) {
      host = process.argv[i + 1];
      i++;
    }
  }

  const app = createAgyMobileServer({ port, host });
  app.listen(port, host);
}

async function parseJsonBody(req: http.IncomingMessage): Promise<Record<string, any>> {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', (chunk) => {
      body += chunk.toString('utf-8');
    });
    req.on('end', () => {
      try {
        resolve(JSON.parse(body || '{}'));
      } catch {
        resolve({});
      }
    });
  });
}
