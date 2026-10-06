import { describe, it } from 'node:test';
import assert from 'node:assert';
import { EventEmitter } from 'node:events';
import { TerminalBridge, TerminalSession } from '../src/server/terminal-bridge.ts';

describe('TerminalBridge & TerminalSession', () => {
  it('manages terminal session lifecycle', () => {
    class MockChildProcess extends EventEmitter {
      public stdin = {
        writable: true,
        write: (data: string) => {
          this.written.push(data);
          return true;
        }
      };
      public stdout = new EventEmitter();
      public stderr = new EventEmitter();
      public written: string[] = [];
      public pid = 12345;
      public killed = false;

      public kill(): boolean {
        this.killed = true;
        this.emit('close', 0, null);
        return true;
      }
    }

    let mockProcess: MockChildProcess;

    const mockSpawn = () => {
      mockProcess = new MockChildProcess();
      return mockProcess as any;
    };

    const bridge = new TerminalBridge({ spawnFn: mockSpawn as any });
    const session = bridge.createSession('test-term-1', {
      cols: 100,
      rows: 30
    });

    assert.strictEqual(session.alive, true);
    assert.deepStrictEqual(session.getDimensions(), { cols: 100, rows: 30 });

    // Test writing data
    const writeOk = bridge.write('test-term-1', 'ls -la\n');
    assert.strictEqual(writeOk, true);
    assert.deepStrictEqual(mockProcess!.written, ['ls -la\n']);

    // Test receiving stdout data
    let receivedData = '';
    bridge.on('data', (ev) => {
      if (ev.id === 'test-term-1') {
        receivedData += ev.data;
      }
    });

    mockProcess!.stdout.emit('data', Buffer.from('file1.txt\nfile2.txt'));
    assert.strictEqual(receivedData, 'file1.txt\nfile2.txt');

    // Test resize
    const resizeOk = bridge.resize('test-term-1', 120, 40);
    assert.strictEqual(resizeOk, true);
    assert.deepStrictEqual(session.getDimensions(), { cols: 120, rows: 40 });

    // Test destruction
    const destroyOk = bridge.destroySession('test-term-1');
    assert.strictEqual(destroyOk, true);
    assert.strictEqual(session.alive, false);
    assert.strictEqual(bridge.getSession('test-term-1'), undefined);
  });

  it('handles multiple sessions cleanly', () => {
    const bridge = new TerminalBridge({
      spawnFn: (() => {
        const p = new EventEmitter() as any;
        p.stdin = { writable: true, write: () => true };
        p.stdout = new EventEmitter();
        p.stderr = new EventEmitter();
        p.kill = () => { p.emit('close', 0); return true; };
        return p;
      }) as any
    });

    const s1 = bridge.createSession('s1');
    const s2 = bridge.createSession('s2');

    assert.strictEqual(s1.alive, true);
    assert.strictEqual(s2.alive, true);
    assert.ok(bridge.getSession('s1'));
    assert.ok(bridge.getSession('s2'));

    bridge.destroyAll();
    assert.strictEqual(bridge.getSession('s1'), undefined);
    assert.strictEqual(bridge.getSession('s2'), undefined);
  });
});
