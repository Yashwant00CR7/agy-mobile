import { spawn, type ChildProcess } from 'node:child_process';
import { EventEmitter } from 'node:events';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import type {
  AgyEvent,
  PromptRequest,
  ApprovalResponse,
  ApprovalRequest
} from '../types/agy.ts';

export interface AgyProcessManagerOptions {
  customBinaryPath?: string;
  spawnFn?: typeof spawn;
}

export class AgyProcessManager extends EventEmitter {
  private currentProcess: ChildProcess | null = null;
  private currentConversationId: string | null = null;
  private activeCwd: string = process.cwd();
  private isAutonomous: boolean = true;
  private pendingApprovals = new Map<string, (approved: boolean) => void>();
  private readonly customBinaryPath?: string;
  private readonly spawnFn: typeof spawn;

  constructor(options: AgyProcessManagerOptions = {}) {
    super();
    this.customBinaryPath = options.customBinaryPath;
    this.spawnFn = options.spawnFn ?? spawn;
  }

  public getCwd(): string {
    return this.activeCwd;
  }

  public setCwd(newCwd: string): void {
    if (fs.existsSync(newCwd) && fs.statSync(newCwd).isDirectory()) {
      this.activeCwd = path.resolve(newCwd);
    } else {
      throw new Error(`Directory does not exist: ${newCwd}`);
    }
  }

  public getAutonomous(): boolean {
    return this.isAutonomous;
  }

  public setAutonomous(value: boolean): void {
    this.isAutonomous = Boolean(value);
  }

  public getConversationId(): string | null {
    return this.currentConversationId;
  }

  public setConversationId(id: string | null): void {
    this.currentConversationId = id;
  }

  public isRunning(): boolean {
    return this.currentProcess !== null && !this.currentProcess.killed;
  }

  public findAgyBinary(): string {
    if (this.customBinaryPath) {
      return this.customBinaryPath;
    }

    const home = os.homedir();
    const candidates = [
      path.join(home, '.local/bin/agy'),
      '/data/data/com.termux/files/home/.local/bin/agy',
      '/usr/local/bin/agy',
      '/usr/bin/agy',
      'agy'
    ];

    for (const candidate of candidates) {
      if (candidate === 'agy' || (fs.existsSync(candidate) && fs.statSync(candidate).isFile())) {
        return candidate;
      }
    }

    return 'agy';
  }

  public async runPrompt(req: PromptRequest): Promise<void> {
    if (this.isRunning()) {
      throw new Error('An agy agent turn is already in progress');
    }

    const cwd = req.cwd ? path.resolve(req.cwd) : this.activeCwd;
    const autonomous = req.autonomous ?? this.isAutonomous;
    const convId = req.conversationId ?? this.currentConversationId;

    const args: string[] = [
      '-p',
      req.prompt,
      '--input-format',
      'stream-json',
      '--output-format',
      'stream-json'
    ];

    if (autonomous) {
      args.push('--dangerously-skip-permissions');
    }

    if (convId) {
      args.push('--conversation', convId);
    }

    if (req.model) {
      args.push('--model', req.model);
    }

    if (req.effort) {
      args.push('--effort', req.effort);
    }

    const binary = this.findAgyBinary();

    this.emitEvent({
      type: 'status',
      status: 'running',
      message: `Starting agy turn in ${cwd}`,
      timestamp: Date.now()
    });

    this.emitEvent({
      type: 'turn_start',
      conversationId: convId ?? undefined,
      timestamp: Date.now()
    });

    return new Promise((resolve, reject) => {
      try {
        const child = this.spawnFn(binary, args, {
          cwd,
          env: {
            ...process.env,
            PAGER: 'cat'
          },
          stdio: ['pipe', 'pipe', 'pipe']
        });

        this.currentProcess = child;

        let stdoutBuffer = '';
        let stderrBuffer = '';

        child.stdout?.on('data', (chunk: Buffer) => {
          stdoutBuffer += chunk.toString('utf-8');
          const lines = stdoutBuffer.split('\n');
          stdoutBuffer = lines.pop() ?? '';

          for (const line of lines) {
            this.handleOutputLine(line.trim(), autonomous);
          }
        });

        child.stderr?.on('data', (chunk: Buffer) => {
          const text = chunk.toString('utf-8');
          stderrBuffer += text;
          this.emitEvent({
            type: 'stderr',
            text,
            timestamp: Date.now()
          });
        });

        child.on('error', (err) => {
          this.currentProcess = null;
          this.emitEvent({
            type: 'error',
            error: err.message,
            timestamp: Date.now()
          });
          this.emitEvent({
            type: 'status',
            status: 'error',
            message: err.message,
            timestamp: Date.now()
          });
          reject(err);
        });

        child.on('close', (code) => {
          // Process any remaining buffered output
          if (stdoutBuffer.trim()) {
            this.handleOutputLine(stdoutBuffer.trim(), autonomous);
          }

          this.currentProcess = null;

          if (code === 0) {
            this.emitEvent({
              type: 'turn_complete',
              conversationId: this.currentConversationId ?? undefined,
              timestamp: Date.now()
            });
            this.emitEvent({
              type: 'status',
              status: 'idle',
              timestamp: Date.now()
            });
            resolve();
          } else {
            const errMsg = stderrBuffer || `agy exited with code ${code}`;
            this.emitEvent({
              type: 'error',
              error: errMsg,
              timestamp: Date.now()
            });
            this.emitEvent({
              type: 'status',
              status: 'error',
              message: errMsg,
              timestamp: Date.now()
            });
            resolve(); // Do not crash server promise, turn completed with error
          }
        });
      } catch (err) {
        this.currentProcess = null;
        reject(err);
      }
    });
  }

  public handleOutputLine(line: string, autonomous: boolean): void {
    if (!line) return;

    try {
      const data = JSON.parse(line) as Record<string, any>;
      this.processNdjsonEvent(data, autonomous);
    } catch {
      // Fallback for non-JSON lines printed to stdout
      this.emitEvent({
        type: 'text',
        delta: line + '\n',
        timestamp: Date.now()
      });
    }
  }

  public processNdjsonEvent(raw: Record<string, any>, autonomous: boolean): void {
    const timestamp = Date.now();

    // Check for conversation ID updates
    if (raw.conversationId && typeof raw.conversationId === 'string') {
      this.currentConversationId = raw.conversationId;
    }

    // Map common agy stream-json schemas
    const eventType = raw.type || raw.event;

    if (eventType === 'thought' || raw.thought) {
      const delta = String(raw.delta || raw.thought || raw.content || '');
      this.emitEvent({ type: 'thought', delta, timestamp });
      return;
    }

    if (eventType === 'text' || eventType === 'model_chunk' || raw.text) {
      const delta = String(raw.delta || raw.text || raw.content || '');
      this.emitEvent({ type: 'text', delta, timestamp });
      return;
    }

    if (eventType === 'tool_call' || raw.tool_call) {
      const call = raw.tool_call || raw;
      const callId = String(call.id || `call_${Date.now()}`);
      const toolName = String(call.name || call.tool || 'unknown_tool');
      const args = (call.args || call.parameters || {}) as Record<string, unknown>;

      this.emitEvent({
        type: 'tool_call',
        id: callId,
        name: toolName,
        args,
        timestamp
      });

      // If in supervised mode and requires approval
      if (!autonomous && (raw.requiresApproval || !autonomous)) {
        this.emitEvent({
          type: 'approval_required',
          id: callId,
          name: toolName,
          args,
          description: raw.description || `Approval required to execute ${toolName}`,
          timestamp
        });
        this.emitEvent({
          type: 'status',
          status: 'waiting_approval',
          message: `Waiting for touch approval: ${toolName}`,
          timestamp
        });
      }
      return;
    }

    if (eventType === 'tool_result' || raw.tool_result) {
      const res = raw.tool_result || raw;
      this.emitEvent({
        type: 'tool_result',
        id: String(res.id || ''),
        name: String(res.name || ''),
        output: res.output ? String(res.output) : undefined,
        error: res.error ? String(res.error) : undefined,
        timestamp
      });
      return;
    }

    if (eventType === 'diff' || raw.diff) {
      this.emitEvent({
        type: 'diff',
        file: String(raw.file || raw.path || 'unknown'),
        diff: String(raw.diff || ''),
        timestamp
      });
      return;
    }

    if (eventType === 'turn_complete' || raw.done) {
      this.emitEvent({
        type: 'turn_complete',
        conversationId: this.currentConversationId ?? undefined,
        summary: raw.summary,
        timestamp
      });
      return;
    }

    // Default generic text fallback
    if (raw.content) {
      this.emitEvent({
        type: 'text',
        delta: String(raw.content),
        timestamp
      });
    }
  }

  public resolveApproval(response: ApprovalResponse): boolean {
    const resolver = this.pendingApprovals.get(response.toolCallId);
    if (resolver) {
      resolver(response.approved);
      this.pendingApprovals.delete(response.toolCallId);
      this.emitEvent({
        type: 'status',
        status: 'running',
        message: response.approved ? 'Approved by user' : 'Denied by user',
        timestamp: Date.now()
      });
      return true;
    }
    return false;
  }

  public cancel(): boolean {
    if (this.currentProcess && !this.currentProcess.killed) {
      this.currentProcess.kill('SIGINT');
      this.emitEvent({
        type: 'status',
        status: 'idle',
        message: 'Cancelled by user',
        timestamp: Date.now()
      });
      return true;
    }
    return false;
  }

  private emitEvent(event: AgyEvent): void {
    this.emit('event', event);
  }
}
