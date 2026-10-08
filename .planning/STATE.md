# GSD Project State

**Active Milestone:** v1.5.0
**Current Phase:** Phase 05 - Model Conflict Resolution & Persistent Stream-JSON Daemon
**Status:** Completed
**Last Updated:** 2026-10-08 17:49:00

## User Directives & Decisions
- **Model Collision Resolution**: Strict Base-Model Mapping. For models with fixed tiers (e.g. Gemini 3.1 Pro High, Gemini 3.1 Pro Low, Claude, GPT-OSS 120B), lock effort to that tier and omit `--effort` CLI argument entirely to prevent CLI collision crashes.
- **Persistent Stream-JSON Daemon**: Maintain a single persistent `libagy.so` process with `--input-format stream-json --output-format stream-json` across turns, feeding prompts over stdin (`{"event":"user","message":{"content":prompt}}`), eliminating 10-14s cold starts on all subsequent turns.
- **Effort UI Synchronization**: UI reflects locked effort for fixed-tier models and allows toggling for dynamic models.

## Current Position
- Phase: 05-persistent-engine-model-effort-fix
- Total Plans: 1
- Completed Plans: 1
- Next Action: Milestone Complete - Ready for testing.
