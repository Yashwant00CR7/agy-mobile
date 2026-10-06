import { spawn, type ChildProcess } from 'node:child_process';
import { EventEmitter } from 'node:events';
import os from 'node:os';
import fs from 'node:fs';
import path from 'node:path';

export interface TerminalSessionOptions {
  cwd?: string;
  command?: string;
  args?: string[];
  cols?: number;
  rows?: number;
  env?: NodeJS.ProcessEnv;
  spawnFn?: typeof spawn;
}

export class TerminalSession extends EventEmitter {
  public readonly id: string;
  private child: ChildProcess | null = null;
  private isAlive: boolean = false;
  private cols: number = 80;
  private rows: number = 24;

  constructor(id: string) {
    super();
    this.id = id;
  }

  public get alive(): boolean {
    return this.isAlive;
  }

  public getDimensions(): { cols: number; rows: number } {
    return { cols: this.cols, rows: this.rows };
  }

  public start(options: TerminalSessionOptions = {}): void {
    if (this.isAlive) {
      throw new Error(`Terminal session ${this.id} is already running`);
    }

    this.cols = options.cols ?? 80;
    this.rows = options.rows ?? 24;

    const defaultShell = this.detectShell();
    const command = options.command ?? defaultShell;
    const args = options.args ?? ['-i'];
    const cwd = options.cwd ?? process.cwd();

    const spawnFn = options.spawnFn ?? spawn;

    const mergedEnv: NodeJS.ProcessEnv = {
      ...process.env,
      TERM: 'xterm-256color',
      COLORTERM: 'truecolor',
      COLUMNS: String(this.cols),
      LINES: String(this.rows),
      PAGER: 'cat',
      ...options.env
    };

    try {
      this.child = spawnFn(command, args, {
        cwd,
        env: mergedEnv,
        stdio: ['pipe', 'pipe', 'pipe']
      });

      this.isAlive = true;

      this.child.stdout?.on('data', (chunk: Buffer) => {
        this.emit('data', chunk.toString('utf-8'));
      });

      this.child.stderr?.on('data', (chunk: Buffer) => {
        this.emit('data', chunk.toString('utf-8'));
      });

      this.child.on('error', (err) => {
        this.emit('error', err);
        this.cleanup();
      });

      this.child.on('close', (code, signal) => {
        this.cleanup();
        this.emit('exit', { code, signal });
      });
    } catch (err) {
      this.cleanup();
      throw err;
    }
  }

  public write(data: string): void {
    if (!this.isAlive || !this.child?.stdin?.writable) {
      return;
    }
    this.child.stdin.write(data);
  }

  public resize(cols: number, rows: number): void {
    this.cols = cols;
    this.rows = rows;

    if (this.isAlive && this.child?.stdin?.writable) {
      // In piped environments, stty can be notified via command sequence or SIGWINCH
      try {
        if (process.platform !== 'win32' && this.child.pid) {
          process.kill(this.child.pid, 'SIGWINCH');
        }
      } catch {
        // Ignore if child doesn't handle SIGWINCH
      }
    }
  }

  public kill(signal: NodeJS.Signals = 'SIGTERM'): void {
    if (this.child && this.isAlive) {
      try {
        this.child.stdin?.end();
        this.child.kill(signal);
        this.child.kill('SIGKILL');
      } catch {
        // Ignore errors during process kill
      }
    }
    this.cleanup();
  }

  private cleanup(): void {
    this.isAlive = false;
    this.child = null;
  }

  private detectShell(): string {
    const termuxBash = '/data/data/com.termux/files/usr/bin/bash';
    if (fs.existsSync(termuxBash)) {
      return termuxBash;
    }

    if (process.env.SHELL && fs.existsSync(process.env.SHELL)) {
      return process.env.SHELL;
    }

    return process.platform === 'win32' ? 'powershell.exe' : 'bash';
  }
}

export class TerminalBridge extends EventEmitter {
  private sessions = new Map<string, TerminalSession>();
  private readonly spawnFn?: typeof spawn;

  constructor(options: { spawnFn?: typeof spawn } = {}) {
    super();
    this.spawnFn = options.spawnFn;
  }

  public createSession(id: string, options: TerminalSessionOptions = {}): TerminalSession {
    if (this.sessions.has(id)) {
      this.destroySession(id);
    }

    const session = new TerminalSession(id);
    this.sessions.set(id, session);

    session.on('data', (data) => this.emit('data', { id, data }));
    session.on('exit', (info) => {
      this.emit('exit', { id, ...info });
      this.sessions.delete(id);
    });
    session.on('error', (err) => this.emit('error', { id, err }));

    session.start({
      ...options,
      spawnFn: this.spawnFn ?? options.spawnFn
    });

    return session;
  }

  public getSession(id: string): TerminalSession | undefined {
    return this.sessions.get(id);
  }

  public write(id: string, data: string): boolean {
    const session = this.sessions.get(id);
    if (session && session.alive) {
      session.write(data);
      return true;
    }
    return false;
  }

  public resize(id: string, cols: number, rows: number): boolean {
    const session = this.sessions.get(id);
    if (session && session.alive) {
      session.resize(cols, rows);
      return true;
    }
    return false;
  }

  public destroySession(id: string): boolean {
    const session = this.sessions.get(id);
    if (session) {
      session.kill();
      this.sessions.delete(id);
      return true;
    }
    return false;
  }

  public destroyAll(): void {
    for (const [id] of this.sessions) {
      this.destroySession(id);
    }
  }
}
