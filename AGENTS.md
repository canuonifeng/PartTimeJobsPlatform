# AGENTS.md

## Project Overview

Spec-driven development project using the OpenSpec workflow. No application source code yet — this is a scaffolded workspace ready for change proposals and implementation.

## Key Commands

| Command | Purpose |
|---------|---------|
| `/opsx-propose <name>` | Create a new change (proposal + design + tasks) |
| `/opsx-apply [name]` | Implement tasks from an existing change |
| `/opsx-archive [name]` | Archive a completed change |
| `/opsx-explore [...]` | Explore ideas, investigate problems, clarify requirements |
| `openspec new change "<name>"` | Scaffold a new change directory |
| `openspec status --change "<name>" --json` | Check artifact/task status |
| `openspec instructions <artifact-id> --change "<name>" --json` | Get artifact template & instructions |

## Project Structure

```
./
  openspec/
    config.yaml          # Spec-driven schema config (editable context/rules)
    specs/               # 8 capability specs (job, schedule, payroll, worker, enterprise, user, platform, notification)
    changes/             # Active changes in progress
    changes/archive/     # Archived completed changes
  .opencode/             # OpenCode config (commands, skills)
  .codex/                # Codex config (mirrored skills)
  docs/superpowers/specs/ # Design documents
  AGENTS.md
```

## Workflow

1. **Explore** (`/opsx-explore`) — think through the problem
2. **Propose** (`/opsx-propose`) — create change with proposal/design/tasks
3. **Apply** (`/opsx-apply`) — implement tasks iteratively
4. **Archive** (`/opsx-archive`) — finalize completed change

## Important Notes

- **JDK 17 required.** The system default is JDK 11. Use: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home`
- Maven multi-module projects: `enterprise-service/`, `c-service/`, `platform-service/`
- All 3 services share the same MySQL instance but have separate schemas
- Enterprise and C-side use RocketMQ + XXL-Job; platform-service does not
- The `openspec` CLI is required for change lifecycle. Run `which openspec` to verify availability.
- Skills under `.opencode/skills/` and `.codex/skills/` define the OpenSpec workflow instructions.
- `openspec/config.yaml` has empty `context` and no custom `rules` — these should be filled in as the project grows.
