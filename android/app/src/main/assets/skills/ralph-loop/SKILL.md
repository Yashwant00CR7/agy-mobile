---
name: ralph-loop
description: Autonomous iterative problem solver powered by GSD Core (Git. Ship. Done.). Grills the user first until full alignment on their mental model, produces GSD spec artifacts (.planning/ with ROADMAP, REQUIREMENTS, CONTEXT, PLAN), and executes autonomously via tracer-first iterative loops with brainstorm, TDD, code-review, verification, and shipping disciplines. Use when the user asks for a Ralph Loop, Ralph Wiggum, autonomous iterative problem solving, or wants to be grilled first before looping.
---

# Ralph Loop: Grilling-Driven Spec-First Autonomous System
*(Powered by GSD Core — Git. Ship. Done.)*

A comprehensive autonomous development workflow combining Ralph's **relentless alignment grilling** with GSD Core's **context engineering, spec-driven development, and phased shipping lifecycle**.

```text
Phase 1: Discuss & Grill ──► Phase 2: Plan & Spec ──► Phase 3: Autonomous Loop ──► Phase 4: Verify & Ship
 (Mental Model Alignment)     (GSD Plan Decomposition)   (Tracer-First Execution)      (Honest Verification)
```

You never guess what the user wants or how they think. You interrogate the problem space until both of you share the exact same mental model, externalize the plan into durable disk artifacts in `.planning/`, prevent context rot through clean subagent dispatch, and autonomously iterate through discrete tasks until everything is built, tested, verified, and shipped.

---

## Core Principles & GSD Alignment

1. **Context Engineering:** Main orchestrator sessions stay lean. Heavy research, implementation, and review run in bounded subagents to prevent **context rot** (the silent degradation of attention as token counts rise).
2. **Externalized Durable State (`.planning/`):** All knowledge, decisions, and progress live in human-readable disk files (`STATE.md`, `ROADMAP.md`, `REQUIREMENTS.md`, `CONTEXT.md`, `PLAN.md`, `VERIFICATION.md`, `SUMMARY.md`), surviving context resets and fully interoperable with `gsd-tools` and all `/gsd-*` commands.
3. **Tracer-First Decomposition:** Plans always lead with a production-quality end-to-end vertical slice (tracer) connecting all architectural seams before any horizontal expansion tasks.
4. **Gates Taxonomy:** Enforce deterministic gates at every boundary:
   - **Pre-flight Gate:** Validates preconditions before starting.
   - **Revision Gate:** Evaluates output quality with bounded retries (cap at 3 iterations) and stall detection.
   - **Escalation Gate:** Surfaces genuine trade-offs or unresolvable blockers to the user.
   - **Abort Gate:** Stops immediately if dangerous drift, test bypass, or catastrophic state occurs.

---

## Phase 1: GSD Discuss & Relentless Grilling (Mental Model Alignment)

**Goal:** Interrogate the user's intent, mental model, assumptions, constraints, and architecture until both user and agent are in complete, unambiguous alignment, capturing decisions into `.planning/CONTEXT.md`.

### Rules of Grilling:
1. **One question at a time.** Never ask multiple questions at once. Asking bundles of questions is overwhelming and degrades decision quality.
2. **Always include a recommended answer.** Provide your recommended answer with a brief rationale for every question, so the user can easily confirm, pick, or redirect. Use the `/brainstorm` skill to probe orthogonal perspectives, inversion, and simpler alternatives before framing recommendations.
3. **Look up facts first.** Before asking any question, actively explore the repository (file tree, existing code, configurations, dependencies, tests, git log). Never ask the user for a fact you can inspect yourself. Only ask for *decisions, intent, mental models, and trade-offs*.
4. **Extract "How the User Thinks":**
   - **Abstractions & Mental Model:** What domain metaphors, entities, and data flows does the user envision?
   - **Architecture & Style:** What conventions, design patterns, or existing patterns in the codebase must be mirrored? Which antipatterns are forbidden?
   - **Invariants & Failure Modes:** What must never break? How should edge cases and errors be handled?
   - **Backpressure Seams:** What exact command proves a task is complete? (e.g. `npm test`, `pytest`, `cargo test`, `go test ./...`, lint/typecheck, build).
5. **Always Use the Interactive Window (`ask_question`):** You MUST deliver every grilling question via the interactive question tool (`ask_question`). Do NOT output grilling questions as static chat text. Present the question, recommended choice, and concrete options directly in the interactive window so the user can interactively select, write-in, or submit.
6. **Collaborative Options (2-4 choices):** When presenting options in the interactive window, format them from the user's perspective. Prefix the recommended choice with `(Recommended)`. If the user writes in a freeform response signaling they want to explain freely, follow up appropriately before returning to interactive mode.
7. **Walk the decision tree.** Follow each decision down its branches, resolving dependencies one by one.

### Phase 1 Completion Criterion:
Continue grilling until all ambiguities and decisions are resolved **AND** the user explicitly confirms alignment (e.g., *"We are on the same page"*, *"Looks good, proceed"*, or equivalent confirmation). Do **NOT** proceed to planning until you receive this confirmation.

---

## Phase 2: Specification & Task Externalization (GSD Planning)

Once alignment is confirmed, synthesize decisions into durable GSD artifacts under `.planning/`.

### 1. Initialize GSD State (`.planning/STATE.md`)
Create or update `.planning/STATE.md` as the persistent navigation spine:

```markdown
# GSD Project State

**Active Milestone:** v1.0
**Current Phase:** Phase 1 - <Phase Name>
**Status:** Planning Complete
**Last Updated:** <YYYY-MM-DD HH:MM:SS>

## Active Decisions & Constraints
- <Locked decision 1 from grilling>
- <Locked decision 2 from grilling>

## Current Position
- Phase: 01-<phase-name>
- Total Plans: <N>
- Completed Plans: 0
- Next Action: Execute Plan 01
```

*(Note: Can also be initialized or updated using `gsd-tools state`)*

### 2. Create `.planning/CONTEXT.md` (Discuss Decisions)
Capture all locked implementation decisions from Phase 1:

```markdown
# Phase Context: <Phase Name>

## Problem & User Intent
<Concise statement of the problem and the user's vision.>

## Mental Model & Core Abstractions
<How the user thinks about this system: core entities, lifecycles, and data flow.>

## Locked Architectural Decisions
- <Decision 1: rationale and chosen pattern>
- <Decision 2: forbidden patterns or explicit boundaries>
- <Error handling and edge case strategy>

## Verification & Backpressure Seams
- Test: `<test command>`
- Lint/Typecheck: `<lint command>`
- Build: `<build command>`
```

### 3. Create `.planning/REQUIREMENTS.md`
Define numbered, traceable requirements:

```markdown
# Requirements: <Phase Name>

- [ ] **REQ-01**: <Observable requirement statement>
- [ ] **REQ-02**: <Observable requirement statement>
- [ ] **REQ-03**: <Observable requirement statement>
```

### 4. Create Phase Plan (`.planning/phases/<phase>/01-PLAN.md` or `PLAN.md`)
Every plan decomposes work into dependency waves using **Tracer-First** ordering:

```markdown
---
phase: 01-<phase-name>
plan: 01
type: execute
tracer_first: true
---

# Plan: <Plan Title>

## Objective
<What this plan builds and the user outcome it unlocks.>

## Requirements Addressed
- REQ-01, REQ-02

## Execution Waves

### Wave 1: Tracer Slice (End-to-End Vertical Seam)
<!-- The tracer slice is the thinnest production-quality end-to-end path connecting input to output. -->
#### Task 1.1: <Tracer Task Title>
- **Type:** tdd | auto
- **Objective:** <Build minimal end-to-end flow connecting caller to result>
- **Target Files:** <Files to create/edit>
- **Acceptance Criteria:** <Verifiable condition>
- **Verification Command:** `<test command>`
- **Reversibility:** two-way | one-way (costly)

### Wave 2: Expansion & Robustness
#### Task 2.1: <Expansion Task Title>
- **Type:** tdd | auto
- **Objective:** <Flesh out feature logic, domain edge cases, validators>
- **Target Files:** <Files to create/edit>
- **Acceptance Criteria:** <Verifiable condition>
- **Verification Command:** `<test command>`

### Wave 3: Integration, Polish & Hardening
#### Task 3.1: <Hardening Task Title>
- **Type:** auto
- **Objective:** <Connect full UI/API, handle error boundaries, logging>
- **Target Files:** <Files to create/edit>
- **Acceptance Criteria:** <Verifiable condition>
- **Verification Command:** `<full test & lint command>`
```

### 5. Pre-flight Gate:
Before executing, verify:
- [x] All requirements in `REQUIREMENTS.md` are covered by at least one task.
- [x] Verification commands exist and run cleanly (or fail as expected before code is written).
- [x] Target files are explicitly listed with no ambiguous wildcards.
Present the plan summary to the user. Upon confirmation, launch Phase 3.

---

## Phase 3: The Autonomous GSD Ralph Loop (Iterate & Solve)

**Goal:** Autonomously solve the problem task-by-task and wave-by-wave, verifying each step against backpressure, maintaining context hygiene, and committing atomic outcomes.

### The Iteration Cycle:
For each task in `PLAN.md`:

1. **Read External State & Pin Baseline Commit:**
   - Read `.planning/STATE.md` to confirm active task.
   - Read the task objective and acceptance criteria from `PLAN.md`.
   - Record the baseline git commit SHA (`git rev-parse HEAD`) as the fixed point for code review.

2. **Context Engineering & Fresh Context Subagents:**
   - Keep the main orchestrator session lean.
   - For complex, heavy, or multi-step tasks, dispatch to a clean subagent (`invoke_subagent` or isolated execution context) with the bounded task prompt, relevant files, and `CONTEXT.md`.
   - Never let massive tool logs pollute the long-term orchestrator context.

3. **Think in the User's Mindset (with `/brainstorm`):**
   - Review *Mental Model & Locked Architectural Decisions* in `CONTEXT.md`.
   - For non-trivial tasks or tricky seams, run a fast pass through `/brainstorm` orthogonal lenses (inversion, radical simplicity, caller ergonomics) to avoid fragile architectures.

4. **Implement (Tracer-First & TDD Discipline):**
   - **Wave 1 First:** Always establish and verify the tracer slice before touching expansion tasks.
   - **TDD Candidate Heuristic:** If the task involves business logic, parsing, validation, algorithms, or API contracts (can describe `expect(fn(input)).toBe(output)` beforehand), use strict TDD:
     - **RED:** Write the test asserting desired behavior. Run test to verify it fails for the expected reason.
     - **GREEN:** Write minimal production code to pass the test.
     - **REFACTOR:** Clean up code, remove duplication, preserve passing tests.
   - For `type="auto"` tasks (UI layout, glue code, configuration), implement directly and verify immediately.
   - Confine changes to target files of the current task.

5. **Enforce Backpressure & The `/brainstorm` Fallback Rule:**
   - Run the agreed verification commands (unit tests, integration tests, linter, typechecker, build).
   - **The `/brainstorm` Fallback Rule:** If verification fails 2+ consecutive times or implementation hits an unexpected dead end, **do not thrash down the same path**. Immediately fallback to `/brainstorm`: zoom out, unearth hidden premises that failed, probe orthogonal angles (is the test seam wrong? can we delete code instead? is the API boundary misplaced?), select a competing approach, and resume.
   - **Strict Abort Rule:** Never bypass, disable, skip, or delete existing tests to force a pass.

6. **Run `/code-review` Gate (Standards & Spec):**
   - Execute `/code-review` on the diff since baseline (`git diff <start-sha>...HEAD`).
   - Verify along two distinct axes:
     - **Standards:** Code smells (Mysterious Name, Duplicated Code, Shotgun Surgery, Primitive Obsession), style, and codebase conventions.
     - **Spec:** Exact fidelity to the task objective, acceptance criteria in `PLAN.md`, and requirements in `REQUIREMENTS.md`.
   - If issues are detected, resolve them immediately before marking the task complete.

7. **Atomic Outcome Commit (Git Integration):**
   - Commit the task locally using Conventional Commits:
     ```bash
     git add <changed-files>
     git commit -m "<type>(<scope>): <concise description>"
     ```
   - **Identity Policy:** Always adhere to repository identity rules:
     - For personal repositories (`~/Downloads/Personal Projects/`): enforce `10a19yashwant@gmail.com` / `Yashwant00CR7` and SSH host `github-personal`. Never touch global git config.

8. **Update Durable State (`STATE.md` & `SUMMARY.md`):**
   - Append completed task details, verification output, and review evidence to `.planning/phases/<phase>/SUMMARY.md`.
   - Update `.planning/STATE.md` progress metrics (or run `gsd-tools state`).
   - Check off completed items in `REQUIREMENTS.md` (`- [x] REQ-XX`).

---

## Phase 4: GSD Verify & Ship (Done Phase)

Once all execution waves and tasks in `PLAN.md` are finished:

### 1. Honest Verifier Gate (`.planning/phases/<phase>/VERIFICATION.md`)
Run the full test suite, linter, typecheck, and build one final time across the entire repository.
Generate `.planning/phases/<phase>/VERIFICATION.md`:

```markdown
# Phase Verification Report: <Phase Name>

## Requirement Traceability
| Requirement ID | Description | Status | Evidence |
| :--- | :--- | :--- | :--- |
| REQ-01 | <Requirement> | VERIFIED | <Test name / command output> |
| REQ-02 | <Requirement> | VERIFIED | <Test name / command output> |

## Decision Compliance
- [x] All locked decisions in CONTEXT.md followed without drift.
- [x] No forbidden patterns introduced.

## Final Verification Commands Run
- Tests: `PASSED` (<count> tests)
- Typecheck: `PASSED`
- Lint: `PASSED`
- Build: `PASSED`
```

### 2. Update GSD State & Archive
- Update `.planning/STATE.md`: Mark phase status as `Complete`.
- Update `.planning/ROADMAP.md`: Check off completed phase.
- Commit phase planning metadata:
  ```bash
  git add .planning/
  git commit -m "docs(planning): complete phase <phase-name> verification and state"
  ```

### 3. Ship Phase (PR / Branch Finalization)
- Present a clean final report to the user:
  - Executive summary of shipped features.
  - Verification proof table.
  - Git commit log of all atomic commits.
- If requested or configured for shipping, run `/gsd-ship` or `/gsd-pr-branch` to prepare the pull request and ship the release increment.

---

## Quick Reference / Commands

| Skill / Command | Purpose |
| :--- | :--- |
| `/ralph` or `/ralph-loop` | Launch the full Grilling-Driven Spec-First Autonomous System |
| `gsd-tools state` | Inspect or update the current GSD project state |
| `/gsd-progress` | Check overall milestone and phase completion progress |
| `/gsd-verify-work` | Run standalone GSD verification audit |
| `/gsd-ship` | Create pull request and archive phase |
| `/brainstorm` | Fallback lens for unblocking, inversion, and simpler design alternatives |
| `/code-review` | Dual-axis review gate (Standards & Spec) |
