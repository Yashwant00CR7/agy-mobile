import { describe, it } from 'node:test';
import assert from 'node:assert';

describe('Project Setup & Environment', () => {
  it('runs tests with Node 22 native test runner and TypeScript', () => {
    assert.strictEqual(typeof describe, 'function');
    assert.strictEqual(1 + 1, 2);
  });
});
