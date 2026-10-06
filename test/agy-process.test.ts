import { describe, it } from 'node:test';
import assert from 'node:assert';
import { EventEmitter } from 'node:events';
import { AgyProcessManager } from '../src/server/agy-process.ts';
import type { AgyEvent } from '../src/types/agy.ts';

describe('AgyProcessManager', () => {
  it('manages configuration and state properties', () => {
    const mgr = new AgyProcessManager();
    assert.strictEqual(mgr.getAutonomous(), true);

    mgr.setAutonomous(false);
    assert.strictEqual(mgr.getAutonomous(), false);

    mgr.setConversationId('test-conv-123');
    assert.strictEqual(mgr.getConversationId(), 'test-conv-123');

    assert.strictEqual(mgr.isRunning(), false);
  });

  it('correctly parses stream-json NDJSON events', () => {
    const mgr = new AgyProcessManager();
    const captured: AgyEvent[] = [];

    mgr.on('event', (ev: AgyEvent) => captured.push(ev));

    // 1. Thought event
    mgr.handleOutputLine(JSON.stringify({ type: 'thought', delta: 'Thinking about mobile...' }), true);
    assert.strictEqual(captured.length, 1);
    assert.strictEqual(captured[0].type, 'thought');
    if (captured[0].type === 'thought') {
      assert.strictEqual(captured[0].delta, 'Thinking about mobile...');
    }

    // 2. Text event
    mgr.handleOutputLine(JSON.stringify({ type: 'text', delta: 'Hello from Termux!' }), true);
    assert.strictEqual(captured.length, 2);
    assert.strictEqual(captured[1].type, 'text');
    if (captured[1].type === 'text') {
      assert.strictEqual(captured[1].delta, 'Hello from Termux!');
    }

    // 3. Tool call in autonomous mode (no approval required)
    mgr.handleOutputLine(JSON.stringify({
      type: 'tool_call',
      id: 'call_1',
      name: 'run_command',
      args: { CommandLine: 'ls' }
    }), true);
    assert.strictEqual(captured.length, 3);
    assert.strictEqual(captured[2].type, 'tool_call');

    // 4. Tool call in supervised mode (requires approval)
    mgr.handleOutputLine(JSON.stringify({
      type: 'tool_call',
      id: 'call_2',
      name: 'run_command',
      args: { CommandLine: 'rm -rf test' },
      requiresApproval: true
    }), false);
    // Should emit tool_call + approval_required + status
    const approvalEvents = captured.filter(e => e.type === 'approval_required');
    assert.strictEqual(approvalEvents.length, 1);
    if (approvalEvents[0].type === 'approval_required') {
      assert.strictEqual(approvalEvents[0].id, 'call_2');
      assert.strictEqual(approvalEvents[0].name, 'run_command');
    }

    // 5. Diff event
    mgr.handleOutputLine(JSON.stringify({
      type: 'diff',
      file: 'src/main.ts',
      diff: '+ console.log(1);'
    }), true);
    const diffEvents = captured.filter(e => e.type === 'diff');
    assert.strictEqual(diffEvents.length, 1);
    if (diffEvents[0].type === 'diff') {
      assert.strictEqual(diffEvents[0].file, 'src/main.ts');
    }

    // 6. Real agy step_update event
    mgr.handleOutputLine(JSON.stringify({
      event: 'step_update',
      step_update: {
        conversation_id: 'conv-real-123',
        step_index: 1,
        state: 'ACTIVE',
        step_type: 'agent_response',
        text_delta: 'Real streamed text'
      }
    }), true);
    assert.strictEqual(mgr.getConversationId(), 'conv-real-123');
    const stepTextEvents = captured.filter(e => e.type === 'text' && (e as any).delta === 'Real streamed text');
    assert.strictEqual(stepTextEvents.length, 1);

    // 7. Real agy result event
    mgr.handleOutputLine(JSON.stringify({
      event: 'result',
      result: {
        conversation_id: 'conv-real-123',
        status: 'SUCCESS',
        response: 'Final complete response'
      }
    }), true);
    const resultEvents = captured.filter(e => e.type === 'turn_complete');
    assert.strictEqual(resultEvents.length, 1);
    assert.strictEqual((resultEvents[0] as any).summary, 'Final complete response');

    // 8. Plain text fallback
    mgr.handleOutputLine('Non-json raw log line', true);
    const textEvents = captured.filter(e => e.type === 'text' && e.delta.includes('Non-json'));
    assert.strictEqual(textEvents.length, 1);
  });

  it('runs prompt with mock spawn runner and emits lifecycle events', async () => {
    class MockChildProcess extends EventEmitter {
      public stdout = new EventEmitter();
      public stderr = new EventEmitter();
      public killed = false;

      public kill(signal?: string): boolean {
        this.killed = true;
        this.emit('close', 130);
        return true;
      }
    }

    let spawnedArgs: string[] = [];

    const mockSpawn = ((_bin: string, args: string[]) => {
      spawnedArgs = args;
      const child = new MockChildProcess();

      // Simulate async stream emission
      setImmediate(() => {
        child.stdout.emit('data', Buffer.from(JSON.stringify({
          type: 'text',
          delta: 'Result from agy'
        }) + '\n'));

        child.emit('close', 0);
      });

      return child as any;
    }) as any;

    const mgr = new AgyProcessManager({ spawnFn: mockSpawn });
    const events: AgyEvent[] = [];
    mgr.on('event', (e) => events.push(e));

    await mgr.runPrompt({
      prompt: 'Test prompt',
      autonomous: true
    });

    assert.ok(spawnedArgs.includes('-p'));
    assert.ok(spawnedArgs.includes('Test prompt'));
    assert.ok(spawnedArgs.includes('--dangerously-skip-permissions'));
    assert.ok(spawnedArgs.includes('--output-format'));
    assert.ok(!spawnedArgs.includes('--input-format'), 'Must not pass --input-format when -p is specified');

    const completed = events.find(e => e.type === 'turn_complete');
    assert.ok(completed, 'Should emit turn_complete event');
  });
});
