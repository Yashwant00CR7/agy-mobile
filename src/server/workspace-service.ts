import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { execSync } from 'node:child_process';
import type { ConversationSummary } from '../types/agy.ts';

export interface FileItem {
  name: string;
  path: string;
  isDirectory: boolean;
  isGitRepo: boolean;
  size?: number;
  modified: number;
}

export interface WorkspaceInfo {
  currentPath: string;
  gitBranch: string | null;
  isGitRepo: boolean;
  hasUncommittedChanges: boolean;
  items: FileItem[];
}

export class WorkspaceService {
  private activePath: string;
  private readonly brainPath: string;

  constructor(initialPath: string = process.cwd(), customBrainPath?: string) {
    this.activePath = path.resolve(initialPath);
    this.brainPath = customBrainPath ?? path.join(os.homedir(), '.gemini/antigravity-cli/brain');
  }

  public getActivePath(): string {
    return this.activePath;
  }

  public setActivePath(targetPath: string): string {
    const resolved = path.resolve(targetPath);
    if (!fs.existsSync(resolved) || !fs.statSync(resolved).isDirectory()) {
      throw new Error(`Invalid directory path: ${targetPath}`);
    }
    this.activePath = resolved;
    return this.activePath;
  }

  public getWorkspaceInfo(dirPath: string = this.activePath): WorkspaceInfo {
    const targetDir = path.resolve(dirPath);
    if (!fs.existsSync(targetDir) || !fs.statSync(targetDir).isDirectory()) {
      throw new Error(`Directory not found: ${dirPath}`);
    }

    const isGit = fs.existsSync(path.join(targetDir, '.git'));
    let gitBranch: string | null = null;
    let hasUncommittedChanges = false;

    if (isGit) {
      gitBranch = this.getGitBranch(targetDir);
      hasUncommittedChanges = this.checkGitDirty(targetDir);
    }

    const items: FileItem[] = [];
    try {
      const entries = fs.readdirSync(targetDir, { withFileTypes: true });
      for (const entry of entries) {
        // Skip .DS_Store
        if (entry.name === '.DS_Store') continue;

        const fullPath = path.join(targetDir, entry.name);
        try {
          const stat = fs.statSync(fullPath);
          const isDir = entry.isDirectory();
          items.push({
            name: entry.name,
            path: fullPath,
            isDirectory: isDir,
            isGitRepo: isDir && fs.existsSync(path.join(fullPath, '.git')),
            size: isDir ? undefined : stat.size,
            modified: stat.mtimeMs
          });
        } catch {
          // Ignore files with permission issues
        }
      }
    } catch {
      // Return empty if directory cannot be read
    }

    // Sort directories first, then alphabetically
    items.sort((a, b) => {
      if (a.isDirectory && !b.isDirectory) return -1;
      if (!a.isDirectory && b.isDirectory) return 1;
      return a.name.localeCompare(b.name);
    });

    return {
      currentPath: targetDir,
      gitBranch,
      isGitRepo: isGit,
      hasUncommittedChanges,
      items
    };
  }

  public getGitBranch(repoDir: string): string | null {
    try {
      const headFile = path.join(repoDir, '.git/HEAD');
      if (fs.existsSync(headFile)) {
        const headContent = fs.readFileSync(headFile, 'utf-8').trim();
        if (headContent.startsWith('ref: refs/heads/')) {
          return headContent.replace('ref: refs/heads/', '');
        }
        return headContent.substring(0, 7); // Detached HEAD SHA
      }
    } catch {
      // Fallback
    }
    return null;
  }

  public checkGitDirty(repoDir: string): boolean {
    try {
      const output = execSync('git status --porcelain', {
        cwd: repoDir,
        encoding: 'utf-8',
        stdio: ['ignore', 'pipe', 'ignore'],
        timeout: 1000
      });
      return output.trim().length > 0;
    } catch {
      return false;
    }
  }

  public getRecentConversations(limit: number = 20): ConversationSummary[] {
    if (!fs.existsSync(this.brainPath)) {
      return [];
    }

    const conversations: ConversationSummary[] = [];

    try {
      const entries = fs.readdirSync(this.brainPath, { withFileTypes: true });

      for (const entry of entries) {
        if (!entry.isDirectory()) continue;

        const convId = entry.name;
        const convDir = path.join(this.brainPath, convId);
        const transcriptPath = path.join(convDir, '.system_generated/logs/transcript.jsonl');

        let lastActive = 0;
        let snippet = 'Conversation ' + convId.substring(0, 8);
        let cwd: string | undefined;

        try {
          const stat = fs.statSync(convDir);
          lastActive = stat.mtimeMs;

          if (fs.existsSync(transcriptPath)) {
            const transcriptStat = fs.statSync(transcriptPath);
            lastActive = transcriptStat.mtimeMs;

            // Read the first few lines to find user intent or prompt
            const content = fs.readFileSync(transcriptPath, 'utf-8');
            const lines = content.split('\n');

            for (const line of lines) {
              if (!line.trim()) continue;
              try {
                const parsed = JSON.parse(line);
                if (parsed.type === 'USER_INPUT' && parsed.content) {
                  snippet = String(parsed.content).substring(0, 100).replace(/\n/g, ' ');
                  break;
                }
              } catch {
                // Ignore line parse errors
              }
            }
          }
        } catch {
          // Skip if unreadable
        }

        conversations.push({
          id: convId,
          lastActive,
          snippet,
          cwd
        });
      }

      // Sort newest first
      conversations.sort((a, b) => b.lastActive - a.lastActive);
    } catch {
      return [];
    }

    return conversations.slice(0, limit);
  }
}
