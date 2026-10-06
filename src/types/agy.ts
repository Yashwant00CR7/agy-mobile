export type ExecutionMode = 'autonomous' | 'supervised';

export type ReasoningEffort = 'low' | 'medium' | 'high' | 'xhigh' | 'max';

export interface PromptRequest {
  prompt: string;
  conversationId?: string;
  autonomous?: boolean;
  cwd?: string;
  model?: string;
  effort?: ReasoningEffort;
}

export interface ApprovalRequest {
  toolCallId: string;
  toolName: string;
  args: Record<string, unknown>;
  description?: string;
}

export interface ApprovalResponse {
  toolCallId: string;
  approved: boolean;
  alwaysAllow?: boolean;
}

export type AgyEventType =
  | 'turn_start'
  | 'thought'
  | 'text'
  | 'tool_call'
  | 'tool_result'
  | 'diff'
  | 'turn_complete'
  | 'approval_required'
  | 'status'
  | 'stderr'
  | 'error';

export interface BaseAgyEvent {
  type: AgyEventType;
  timestamp: number;
}

export interface TurnStartEvent extends BaseAgyEvent {
  type: 'turn_start';
  conversationId?: string;
}

export interface ThoughtEvent extends BaseAgyEvent {
  type: 'thought';
  delta: string;
}

export interface TextEvent extends BaseAgyEvent {
  type: 'text';
  delta: string;
}

export interface ToolCallEvent extends BaseAgyEvent {
  type: 'tool_call';
  id: string;
  name: string;
  args: Record<string, unknown>;
}

export interface ApprovalRequiredEvent extends BaseAgyEvent {
  type: 'approval_required';
  id: string;
  name: string;
  args: Record<string, unknown>;
  description?: string;
}

export interface ToolResultEvent extends BaseAgyEvent {
  type: 'tool_result';
  id: string;
  name: string;
  output?: string;
  error?: string;
}

export interface DiffEvent extends BaseAgyEvent {
  type: 'diff';
  file: string;
  diff: string;
}

export interface TurnCompleteEvent extends BaseAgyEvent {
  type: 'turn_complete';
  conversationId?: string;
  summary?: string;
  error?: string;
}

export interface StatusEvent extends BaseAgyEvent {
  type: 'status';
  status: 'idle' | 'running' | 'waiting_approval' | 'error';
  message?: string;
}

export interface StderrEvent extends BaseAgyEvent {
  type: 'stderr';
  text: string;
}

export interface ErrorEvent extends BaseAgyEvent {
  type: 'error';
  error: string;
}

export type AgyEvent =
  | TurnStartEvent
  | ThoughtEvent
  | TextEvent
  | ToolCallEvent
  | ApprovalRequiredEvent
  | ToolResultEvent
  | DiffEvent
  | TurnCompleteEvent
  | StatusEvent
  | StderrEvent
  | ErrorEvent;

export interface ConversationSummary {
  id: string;
  lastActive: number;
  snippet?: string;
  cwd?: string;
}
