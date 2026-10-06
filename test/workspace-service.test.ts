import { describe, it } from 'node:test';
import assert from 'node:assert';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { WorkspaceService } from '../src/server/workspace-service.ts';

describe('WorkspaceService', () => {
  it('navigates and validates active workspace path', () => {
    const cwd = process.cwd();
    const service = new WorkspaceService(cwd);

    assert.strictEqual(service.getActivePath(), path.resolve(cwd));

    // Try setting invalid path
    assert.throws(() => {
      service.setActivePath('/path/that/does/not/exist/99999');
    }, /Invalid directory path/);
  });

  it('scans workspace files, directories, and git information', () => {
    const cwd = process.cwd();
    const service = new WorkspaceService(cwd);
    const info = service.getWorkspaceInfo();

    assert.strictEqual(info.currentPath, path.resolve(cwd));
    assert.strictEqual(info.isGitRepo, true);
    assert.strictEqual(info.gitBranch, 'main');
    assert.ok(Array.isArray(info.items));
    assert.ok(info.items.length > 0);

    // Check directory sorting (directories first)
    let seenFile = false;
    for (const item of info.items) {
      if (!item.isDirectory) {
        seenFile = true;
      }
      if (seenFile && item.isDirectory) {
        assert.fail('Found directory after file in sorted list');
      }
    }
  });

  it('discovers and parses recent agy conversations from brain directory', () => {
    const tempBrainDir = fs.mkdtempSync(path.join(os.tmpdir(), 'agy-brain-test-'));

    try {
      const convId1 = 'conv-1111';
      const convId2 = 'conv-2222';

      const conv1Dir = path.join(tempBrainDir, convId1, '.system_generated/logs');
      const conv2Dir = path.join(tempBrainDir, convId2, '.system_generated/logs');
      fs.mkdirSync(conv1Dir, { recursive: true });
      fs.mkdirSync(conv2Dir, { recursive: true });

      fs.writeFileSync(
        path.join(conv1Dir, 'transcript.jsonl'),
        JSON.stringify({ type: 'USER_INPUT', content: 'Create a mobile app in Termux' }) + '\n'
      );

      fs.writeFileSync(
        path.join(conv2Dir, 'transcript.jsonl'),
        JSON.stringify({ type: 'USER_INPUT', content: 'Debug memory leak' }) + '\n'
      );

      const service = new WorkspaceService(process.cwd(), tempBrainDir);
      const conversations = service.getRecentConversations();

      assert.strictEqual(conversations.length, 2);
      const c1 = conversations.find(c => c.id === convId1);
      const c2 = conversations.find(c => c.id === convId2);

      assert.ok(c1);
      assert.ok(c2);
      assert.strictEqual(c1?.snippet, 'Create a mobile app in Termux');
      assert.strictEqual(c2?.snippet, 'Debug memory leak');
    } finally {
      fs.rmSync(tempBrainDir, { recursive: true, force: true });
    }
  });
});
