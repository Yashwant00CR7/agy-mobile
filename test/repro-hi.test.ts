import { describe, it } from 'node:test';
import assert from 'node:assert';
import { AgyProcessManager } from '../src/server/agy-process.ts';
import type { AgyEvent } from '../src/types/agy.ts';

describe('Reproduction: Send "hi" and receive response', () => {
  it('sends "hi" to agy and receives text streaming events', async () => {
    const mgr = new AgyProcessManager();
    const events: AgyEvent[] = [];

    mgr.on('event', (ev) => {
      events.push(ev);
    });

    await mgr.runPrompt({
      prompt: 'hi',
      autonomous: true
    });

    const textEvents = events.filter(e => e.type === 'text');
    assert.ok(textEvents.length > 0, 'Must receive at least one text delta event');
    
    const combinedText = textEvents.map(e => (e as any).delta).join('');
    assert.ok(combinedText.length > 0, 'Text response should not be empty');
  });
});
