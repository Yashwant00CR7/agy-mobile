// agy-mobile Client Controller

interface AgyEvent {
  type: string;
  delta?: string;
  id?: string;
  name?: string;
  args?: Record<string, unknown>;
  description?: string;
  diff?: string;
  file?: string;
  status?: string;
  message?: string;
  error?: string;
  conversationId?: string;
}

class AgyMobileApp {
  private chatWs: WebSocket | null = null;
  private ptyWs: WebSocket | null = null;
  private isRunning: boolean = false;
  private isAutonomous: boolean = true;
  private currentCwd: string = '';
  private currentConversationId: string | null = null;
  private currentAssistantCard: HTMLElement | null = null;
  private currentThoughtCard: HTMLElement | null = null;
  private isRecordingVoice: boolean = false;
  private recognition: any = null;

  // DOM Elements
  private messagesContainer!: HTMLElement;
  private promptInput!: HTMLTextAreaElement;
  private sendBtn!: HTMLElement;
  private micBtn!: HTMLElement;
  private modeToggleBtn!: HTMLElement;
  private modeLabel!: HTMLElement;
  private headerWsName!: HTMLElement;
  private headerGitBranch!: HTMLElement;
  private tabChat!: HTMLElement;
  private tabTerm!: HTMLElement;
  private chatPane!: HTMLElement;
  private termPane!: HTMLElement;
  private termText!: HTMLElement;
  private drawerBackdrop!: HTMLElement;
  private drawer!: HTMLElement;
  private drawerWsList!: HTMLElement;
  private drawerCurrentPath!: HTMLElement;
  private drawerConvsList!: HTMLElement;

  public init(): void {
    this.cacheElements();
    this.bindEvents();
    this.initPWA();
    this.initVoiceRecognition();
    this.connectChatWs();
    this.fetchInitialStatus();
  }

  private cacheElements(): void {
    this.messagesContainer = document.getElementById('chat-messages')!;
    this.promptInput = document.getElementById('prompt-input') as HTMLTextAreaElement;
    this.sendBtn = document.getElementById('btn-send')!;
    this.micBtn = document.getElementById('btn-mic')!;
    this.modeToggleBtn = document.getElementById('mode-toggle-btn')!;
    this.modeLabel = document.getElementById('mode-label')!;
    this.headerWsName = document.getElementById('header-ws-name')!;
    this.headerGitBranch = document.getElementById('header-git-branch')!;
    this.tabChat = document.getElementById('tab-chat')!;
    this.tabTerm = document.getElementById('tab-term')!;
    this.chatPane = document.getElementById('chat-pane')!;
    this.termPane = document.getElementById('term-pane')!;
    this.termText = document.getElementById('term-text')!;
    this.drawerBackdrop = document.getElementById('drawer-backdrop')!;
    this.drawer = document.getElementById('drawer')!;
    this.drawerWsList = document.getElementById('drawer-ws-list')!;
    this.drawerCurrentPath = document.getElementById('drawer-current-path')!;
    this.drawerConvsList = document.getElementById('drawer-convs-list')!;
  }

  private bindEvents(): void {
    // Send / Cancel
    this.sendBtn.addEventListener('click', () => this.handleActionClick());

    // Textarea auto-resize and Enter key
    this.promptInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        this.submitPrompt();
      }
    });

    this.promptInput.addEventListener('input', () => {
      this.promptInput.style.height = 'auto';
      this.promptInput.style.height = `${Math.min(this.promptInput.scrollHeight, 120)}px`;
    });

    // Quick chips
    document.querySelectorAll('.chip').forEach((btn) => {
      btn.addEventListener('click', () => {
        const prompt = btn.getAttribute('data-prompt');
        if (prompt) {
          this.promptInput.value = prompt;
          this.submitPrompt();
        }
      });
    });

    // Voice button
    this.micBtn.addEventListener('click', () => this.toggleVoice());

    // Mode Toggle
    this.modeToggleBtn.addEventListener('click', () => this.toggleMode());

    // Tabs
    this.tabChat.addEventListener('click', () => this.switchView('chat'));
    this.tabTerm.addEventListener('click', () => this.switchView('term'));

    // Drawer toggles
    document.getElementById('btn-menu')?.addEventListener('click', () => this.openDrawer());
    document.getElementById('btn-close-drawer')?.addEventListener('click', () => this.closeDrawer());
    this.drawerBackdrop.addEventListener('click', () => this.closeDrawer());

    // Drawer subtabs
    document.getElementById('subtab-ws')?.addEventListener('click', () => this.switchDrawerTab('ws'));
    document.getElementById('subtab-convs')?.addEventListener('click', () => this.switchDrawerTab('convs'));

    // Terminal accessory bar buttons
    document.querySelectorAll('.acc-key').forEach((btn) => {
      btn.addEventListener('click', () => {
        const key = btn.getAttribute('data-key');
        if (key && this.ptyWs && this.ptyWs.readyState === WebSocket.OPEN) {
          this.ptyWs.send(JSON.stringify({ type: 'data', data: key }));
        }
      });
    });
  }

  private initPWA(): void {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('/sw.js').catch(() => {});
    }
  }

  private initVoiceRecognition(): void {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      this.recognition = new SpeechRecognition();
      this.recognition.continuous = false;
      this.recognition.interimResults = true;

      this.recognition.onresult = (event: any) => {
        let transcript = '';
        for (let i = event.resultIndex; i < event.results.length; ++i) {
          transcript += event.results[i][0].transcript;
        }
        this.promptInput.value = transcript;
        this.promptInput.dispatchEvent(new Event('input'));
      };

      this.recognition.onend = () => {
        this.isRecordingVoice = false;
        this.micBtn.classList.remove('recording');
      };
    }
  }

  private toggleVoice(): void {
    if (!this.recognition) {
      alert('Speech recognition is not supported in this browser.');
      return;
    }

    if (this.isRecordingVoice) {
      this.recognition.stop();
      this.isRecordingVoice = false;
      this.micBtn.classList.remove('recording');
    } else {
      try {
        this.recognition.start();
        this.isRecordingVoice = true;
        this.micBtn.classList.add('recording');
      } catch {
        // Recognition already started
      }
    }
  }

  private async fetchInitialStatus(): Promise<void> {
    try {
      const res = await fetch('/api/status');
      if (res.ok) {
        const data = await res.json();
        this.updateStatus(data);
      }
      this.loadWorkspaceInfo();
    } catch {
      // Retry in 2 seconds
      setTimeout(() => this.fetchInitialStatus(), 2000);
    }
  }

  private updateStatus(data: any): void {
    this.isAutonomous = Boolean(data.isAutonomous);
    this.currentCwd = data.activeCwd || '';
    this.currentConversationId = data.conversationId || null;
    this.setRunningState(Boolean(data.running));

    this.modeLabel.textContent = this.isAutonomous ? 'Autonomous' : 'Supervised';
    if (this.isAutonomous) {
      this.modeToggleBtn.classList.remove('supervised');
    } else {
      this.modeToggleBtn.classList.add('supervised');
    }

    const wsParts = this.currentCwd.split('/').filter(Boolean);
    this.headerWsName.textContent = wsParts[wsParts.length - 1] || 'root';
    this.drawerCurrentPath.textContent = this.currentCwd;
  }

  private setRunningState(running: boolean): void {
    this.isRunning = running;
    if (running) {
      this.sendBtn.classList.add('running');
      this.sendBtn.textContent = '■';
      this.sendBtn.setAttribute('title', 'Cancel current turn');
    } else {
      this.sendBtn.classList.remove('running');
      this.sendBtn.textContent = '➤';
      this.sendBtn.setAttribute('title', 'Send prompt');
      this.currentAssistantCard = null;
      this.currentThoughtCard = null;
    }
  }

  private async toggleMode(): Promise<void> {
    const nextMode = !this.isAutonomous;
    try {
      const res = await fetch('/api/status/mode', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ autonomous: nextMode })
      });
      if (res.ok) {
        const data = await res.json();
        this.isAutonomous = data.isAutonomous;
        this.modeLabel.textContent = this.isAutonomous ? 'Autonomous' : 'Supervised';
        if (this.isAutonomous) {
          this.modeToggleBtn.classList.remove('supervised');
        } else {
          this.modeToggleBtn.classList.add('supervised');
        }
      }
    } catch {
      // Failed to toggle mode
    }
  }

  private connectChatWs(): void {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/chat`;

    this.chatWs = new WebSocket(wsUrl);

    this.chatWs.onmessage = (event) => {
      try {
        const data: AgyEvent = JSON.parse(event.data);
        this.handleAgyEvent(data);
      } catch {
        // Parse error
      }
    };

    this.chatWs.onclose = () => {
      setTimeout(() => this.connectChatWs(), 2000);
    };
  }

  private handleAgyEvent(event: AgyEvent): void {
    switch (event.type) {
      case 'status':
        if (event.status === 'running') this.setRunningState(true);
        if (event.status === 'idle') this.setRunningState(false);
        break;

      case 'thought':
        this.appendThought(event.delta || '');
        break;

      case 'text':
        this.appendText(event.delta || '');
        break;

      case 'tool_call':
        this.renderToolCall(event);
        break;

      case 'approval_required':
        this.renderApprovalRequest(event);
        break;

      case 'diff':
        this.renderDiff(event);
        break;

      case 'turn_complete':
        this.setRunningState(false);
        if (event.conversationId) {
          this.currentConversationId = event.conversationId;
        }
        break;

      case 'error':
        this.renderError(event.error || 'Unknown error');
        this.setRunningState(false);
        break;
    }
    this.scrollToBottom();
  }

  private appendThought(delta: string): void {
    if (!this.currentThoughtCard) {
      this.currentThoughtCard = document.createElement('div');
      this.currentThoughtCard.className = 'thought-card';
      this.messagesContainer.appendChild(this.currentThoughtCard);
    }
    this.currentThoughtCard.textContent += delta;
  }

  private appendText(delta: string): void {
    if (!this.currentAssistantCard) {
      this.currentAssistantCard = document.createElement('div');
      this.currentAssistantCard.className = 'message-card assistant';
      this.messagesContainer.appendChild(this.currentAssistantCard);
    }
    this.currentAssistantCard.textContent += delta;
  }

  private renderToolCall(event: AgyEvent): void {
    const card = document.createElement('div');
    card.className = 'tool-card';

    const header = document.createElement('div');
    header.className = 'tool-header';
    header.textContent = `⚡ Tool: ${event.name || 'action'}`;

    const body = document.createElement('div');
    body.className = 'tool-body';
    body.textContent = JSON.stringify(event.args || {}, null, 2);

    header.addEventListener('click', () => {
      body.style.display = body.style.display === 'none' ? 'block' : 'none';
    });

    card.appendChild(header);
    card.appendChild(body);
    this.messagesContainer.appendChild(card);
  }

  private renderApprovalRequest(event: AgyEvent): void {
    // Vibrate phone for attention
    if ('vibrate' in navigator) {
      navigator.vibrate([150, 50, 150]);
    }

    const card = document.createElement('div');
    card.className = 'approval-card';

    const title = document.createElement('div');
    title.className = 'approval-title';
    title.textContent = `⚠️ Approval Required: ${event.name}`;

    const desc = document.createElement('div');
    desc.style.fontSize = '0.85rem';
    desc.textContent = event.description || `Tool ${event.name} requires your approval.`;

    const code = document.createElement('pre');
    code.style.fontFamily = 'monospace';
    code.style.fontSize = '0.78rem';
    code.style.background = '#000';
    code.style.padding = '8px';
    code.style.borderRadius = '6px';
    code.textContent = JSON.stringify(event.args || {}, null, 2);

    const actions = document.createElement('div');
    actions.className = 'approval-actions';

    const btnApprove = document.createElement('button');
    btnApprove.className = 'btn-approve';
    btnApprove.textContent = 'Approve';
    btnApprove.onclick = () => {
      this.sendApproval(event.id!, true);
      card.remove();
    };

    const btnDeny = document.createElement('button');
    btnDeny.className = 'btn-deny';
    btnDeny.textContent = 'Deny';
    btnDeny.onclick = () => {
      this.sendApproval(event.id!, false);
      card.remove();
    };

    actions.appendChild(btnApprove);
    actions.appendChild(btnDeny);

    card.appendChild(title);
    card.appendChild(desc);
    card.appendChild(code);
    card.appendChild(actions);

    this.messagesContainer.appendChild(card);
  }

  private sendApproval(toolCallId: string, approved: boolean): void {
    if (this.chatWs && this.chatWs.readyState === WebSocket.OPEN) {
      this.chatWs.send(JSON.stringify({
        type: 'approve',
        toolCallId,
        approved
      }));
    }
  }

  private renderDiff(event: AgyEvent): void {
    const card = document.createElement('div');
    card.className = 'diff-card';

    const header = document.createElement('div');
    header.className = 'diff-header';
    header.textContent = `File Diff: ${event.file || 'modified'}`;

    const content = document.createElement('div');
    content.className = 'diff-content';

    const lines = (event.diff || '').split('\n');
    for (const line of lines) {
      const span = document.createElement('span');
      if (line.startsWith('+')) span.className = 'diff-add';
      else if (line.startsWith('-')) span.className = 'diff-del';
      span.textContent = line + '\n';
      content.appendChild(span);
    }

    card.appendChild(header);
    card.appendChild(content);
    this.messagesContainer.appendChild(card);
  }

  private renderError(errMsg: string): void {
    const card = document.createElement('div');
    card.className = 'message-card assistant';
    card.style.borderColor = 'var(--danger)';
    card.style.color = 'var(--danger)';
    card.textContent = `❌ ${errMsg}`;
    this.messagesContainer.appendChild(card);
  }

  private scrollToBottom(): void {
    this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
  }

  private handleActionClick(): void {
    if (this.isRunning) {
      // Cancel
      if (this.chatWs && this.chatWs.readyState === WebSocket.OPEN) {
        this.chatWs.send(JSON.stringify({ type: 'cancel' }));
      }
    } else {
      this.submitPrompt();
    }
  }

  private submitPrompt(): void {
    const text = this.promptInput.value.trim();
    if (!text || this.isRunning) return;

    // Append user card
    const userCard = document.createElement('div');
    userCard.className = 'message-card user';
    userCard.textContent = text;
    this.messagesContainer.appendChild(userCard);

    this.promptInput.value = '';
    this.promptInput.style.height = 'auto';

    // Reset card pointers
    this.currentAssistantCard = null;
    this.currentThoughtCard = null;

    if (this.chatWs && this.chatWs.readyState === WebSocket.OPEN) {
      this.chatWs.send(JSON.stringify({
        type: 'prompt',
        prompt: text,
        conversationId: this.currentConversationId ?? undefined,
        autonomous: this.isAutonomous
      }));
    }

    this.scrollToBottom();
  }

  private switchView(view: 'chat' | 'term'): void {
    if (view === 'chat') {
      this.tabChat.classList.add('active');
      this.tabTerm.classList.remove('active');
      this.chatPane.classList.remove('hidden');
      this.termPane.classList.add('hidden');
    } else {
      this.tabTerm.classList.add('active');
      this.tabChat.classList.remove('active');
      this.termPane.classList.remove('hidden');
      this.chatPane.classList.add('hidden');
      this.connectPtyWs();
    }
  }

  private connectPtyWs(): void {
    if (this.ptyWs && this.ptyWs.readyState === WebSocket.OPEN) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/pty`;

    this.ptyWs = new WebSocket(wsUrl);

    this.ptyWs.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        if (msg.type === 'data') {
          this.termText.textContent += msg.data;
          this.termText.scrollTop = this.termText.scrollHeight;
        }
      } catch {
        this.termText.textContent += event.data;
      }
    };
  }

  private openDrawer(): void {
    this.drawerBackdrop.classList.add('open');
    this.drawer.classList.add('open');
    this.loadWorkspaceInfo();
    this.loadConversations();
  }

  private closeDrawer(): void {
    this.drawerBackdrop.classList.remove('open');
    this.drawer.classList.remove('open');
  }

  private switchDrawerTab(tab: 'ws' | 'convs'): void {
    const wsView = document.getElementById('drawer-ws-view')!;
    const convsView = document.getElementById('drawer-convs-view')!;
    const btnWs = document.getElementById('subtab-ws')!;
    const btnConvs = document.getElementById('subtab-convs')!;

    if (tab === 'ws') {
      btnWs.classList.add('active');
      btnConvs.classList.remove('active');
      wsView.style.display = 'block';
      convsView.style.display = 'none';
    } else {
      btnConvs.classList.add('active');
      btnWs.classList.remove('active');
      convsView.style.display = 'block';
      wsView.style.display = 'none';
    }
  }

  private async loadWorkspaceInfo(): Promise<void> {
    try {
      const res = await fetch(`/api/workspaces?path=${encodeURIComponent(this.currentCwd)}`);
      if (res.ok) {
        const data = await res.json();
        this.headerGitBranch.textContent = data.gitBranch ? `git: ${data.gitBranch}` : 'git: none';
        this.drawerWsList.innerHTML = '';

        // Add parent directory option
        const parentItem = document.createElement('div');
        parentItem.className = 'drawer-item';
        parentItem.textContent = '📁 .. (Parent Directory)';
        parentItem.onclick = () => this.selectWorkspace(this.currentCwd + '/..');
        this.drawerWsList.appendChild(parentItem);

        for (const item of data.items || []) {
          if (item.isDirectory) {
            const el = document.createElement('div');
            el.className = 'drawer-item';
            el.textContent = `📁 ${item.name}`;
            el.onclick = () => this.selectWorkspace(item.path);
            this.drawerWsList.appendChild(el);
          }
        }
      }
    } catch {
      // Fetch error
    }
  }

  private async selectWorkspace(newPath: string): Promise<void> {
    try {
      const res = await fetch('/api/workspaces/select', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ path: newPath })
      });
      if (res.ok) {
        const data = await res.json();
        this.currentCwd = data.activeCwd;
        this.updateStatus({ activeCwd: this.currentCwd, isAutonomous: this.isAutonomous });
        this.loadWorkspaceInfo();
      }
    } catch {
      // Select error
    }
  }

  private async loadConversations(): Promise<void> {
    try {
      const res = await fetch('/api/conversations');
      if (res.ok) {
        const data = await res.json();
        this.drawerConvsList.innerHTML = '';

        for (const conv of data.conversations || []) {
          const el = document.createElement('div');
          el.className = 'drawer-item';
          el.style.flexDirection = 'column';
          el.style.alignItems = 'flex-start';

          const title = document.createElement('div');
          title.style.fontWeight = '600';
          title.textContent = `ID: ${conv.id.substring(0, 8)}...`;

          const snippet = document.createElement('div');
          snippet.className = 'item-snippet';
          snippet.textContent = conv.snippet || 'No preview available';

          el.appendChild(title);
          el.appendChild(snippet);

          el.onclick = () => {
            this.currentConversationId = conv.id;
            this.closeDrawer();
            alert(`Resumed conversation ${conv.id.substring(0, 8)}`);
          };

          this.drawerConvsList.appendChild(el);
        }
      }
    } catch {
      // Fetch error
    }
  }
}

// Start app on DOM load
window.addEventListener('DOMContentLoaded', () => {
  const app = new AgyMobileApp();
  app.init();
});
