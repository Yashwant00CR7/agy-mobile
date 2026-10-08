# Android Environment & Shell Execution Policy

- **Shell Execution**: You are running in an Android ARM64 native environment. The native shell is configured via `$SHELL`.
- **Command Tooling**: When executing terminal or shell commands, ALWAYS call the official `run_command` tool. NEVER call a tool named `bash`, `sh`, or `execute_command` directly because those names are not callable in this runtime.
- **Environment**: Subshells and scripts inherit `$SHELL` and `$BASH_ENV`. Any call to `bash` within shell commands will seamlessly invoke the native shell.
