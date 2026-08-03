# JartonPerms Devlog

A flat, simple permissions plugin for JartonMC. Repo: `TridentII-NZ/JartonPerms` (public —
GitHub declined to make the original fork private on this account's plan).

Design spec: `docs/superpowers/specs/2026-08-03-jartonperms-fresh-design.md` (in the Demetrius
vault) — a from-scratch redesign written 2026-08-03, same day as Phase 1 below. The project no
longer builds on a fork of PermissionsEx; the fork's structure was wiped and replaced with a
fresh single-module Bukkit plugin (see "Fresh Build" section below). Phase 1's history is kept
for the record since the build-tooling lessons it surfaced still apply to this repo's Gradle
setup, but nothing from the PEX-derived Java source survives.

## Phase 1 — Fork, Fix Build, Rebrand (2026-08-03)

Goal: get the forked codebase building green under modern tooling and rebranded from
`ca.stellardrift.permissionsex`/PermissionsEx to `me.jarton.perms`/JartonPerms, with zero
changes to the plugin's actual logic. Plan:
`docs/superpowers/plans/2026-08-03-jartonperms-phase1-fork-rebuild.md` (Demetrius vault).

Built via subagent-driven-development in an isolated worktree, 5 tasks + per-task review +
final whole-branch review, merged to `master` locally (not pushed).

**Discovered mid-plan:** PermissionsEx's own custom Gradle convention-plugin repository
(`repo.stellardrift.ca`) is dead — the fork couldn't even resolve `buildSrc`'s dependencies out
of the box. Fixed by pointing everything at the Gradle Plugin Portal instead. Also discovered the
pinned Gradle wrapper (6.8.3, Feb 2021) can't read Java 17+ class files — bumped to 8.10.2.

**What changed:**
- `buildSrc` rewritten: dead Maven host replaced, publish/lint ceremony (indra publishing,
  errorprone, ktlint, `ca.stellardrift.opinionated`) stripped — this is a private fork with no
  publishing pipeline. `ca.stellardrift.localization`/`ca.stellardrift.templating` kept (core's
  message-codegen system depends on them, still resolvable via the Plugin Portal).
- Trimmed to 11 modules: `api`, `core`, `platform:bukkit`, `platform:velocity`,
  `impl-blocks:{proxy-common,hikari-config,minecraft,glob,legacy}`, `datastore:{file,sql}`.
  Deleted `platform:{sponge,sponge7,fabric,bungee}` (zero kept-module dependents). Kept
  `impl-blocks:glob`/`legacy` and `datastore:sql` despite the design spec listing glob/legacy for
  deletion — `core` directly depends on both; that removal is bundled with the actual "gut core"
  rewrite in Phase 2, not this build-fix phase.
- Rebranded project identity (`gradle.properties`, root `build.gradle.kts`) and the entire Java
  package tree (`ca/stellardrift/permissionsex` → `me/jarton/perms`, ~210 files via `git mv` +
  content rewrite) to `me.jarton.perms`/JartonPerms.
- Every kept module's `build.gradle.kts` rewritten to vanilla Gradle (literal Maven coordinates
  instead of the removed `adventure(...)`/`configurate(...)` helper functions), then iterated on
  real build errors until green — 13 additional real bugs found and fixed this way: `java` →
  `java-library` (pex-component), a dead `spigradle-annotations:2.2.3` transitive pin, the shadow
  plugin's group ID move (`com.github.jengelman.gradle.plugins` → `com.github.johnrengelman`,
  8.1.1), `configurations.compile` → `implementation` (Gradle 7+ removed `compile`), errorprone
  annotations reintroduced as `compileOnly` (source still uses `@LazyInit`/`@DoNotCall`), a
  Mojang libraries repo for brigadier, several dead/moved Maven repos (old papermc.io, spigotmc,
  enginehub, nexus.hc.to for Vault), an ASM version bump (9.2 → 9.7.1, couldn't read Java 21
  bytecode), a `jdependency` pin (shadow's `minimize{}` bundles its own shaded ASM that the
  project's ASM constraint can't reach), a stale `validateShadowing` allowlist left over from the
  package rebrand, and an explicit `generateSpigotDescription` task dependency for Gradle 8's
  stricter implicit-dependency validation.
- Explicit toolchain pin added (`java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }`
  in `pex-component.gradle.kts`) so compilation targets Java 21 regardless of the ambient JDK —
  `developmentRuntime=21` matches it, used by `runSpigot`/`runVelocity`'s dev-launcher.
- Deleted the stale `.github/workflows/build-publish.yml` (referenced the removed `publish` task,
  JDK 11, and third-party `stellardrift` publishing secrets — would have failed loudly and
  attempted to authenticate against someone else's infra on first push).

**Verification:** `.\gradlew.bat clean build` → `BUILD SUCCESSFUL` across all 11 kept modules
(including their test suites). No source file under `src/main` was touched beyond mechanical
package/import renames — zero behavior change, as intended for this phase.

**Not done:** not pushed to GitHub, not deployed anywhere. Phase 2 as originally scoped (gutting
`core`'s ANTLR-based contextual calculator, deleting `impl-blocks:glob`/`legacy`, rebuilding
around a flat data model) was superseded the same day by a full fresh design — see below.

## Fresh Build — from-scratch v1 (2026-08-03)

Goal: a flat, single-server Bukkit/Paper permissions plugin, built from a blank page per
`docs/superpowers/specs/2026-08-03-jartonperms-fresh-design.md`, not the PEX-fork architecture
above. The fork's Java source tree, multi-module Gradle layout, and `buildSrc` were wiped
(kept `.git` history, `LICENSE`, and the working Gradle 8.10.2 wrapper). Single Gradle module,
package `me.jarton.perms`, no Velocity, no cross-server sync, no GUI, no SQL storage, no
contexts, no tri-state permission values — plain `true`/`false` grants.

Built via subagent-driven-development in an isolated worktree per
`docs/superpowers/plans/2026-08-03-jartonperms-fresh-build.md` — 11 tasks, each with a per-task
review (5 of the 11 needed one fix round; all came back clean on re-review), then a final
whole-branch review that surfaced a real Critical bug and 5 Important findings no single
per-task review could see, fixed in one consolidated wave, with one further scoped fix for a
regression that wave itself introduced. Merged to `master` locally (not pushed).

### Additions/Changes

- **Data model:** `Group` (name, permission nodes, parent groups, a free-form options map for
  prefix/suffix/weight/etc.) and `User` (UUID, username, own permission nodes, group
  memberships — no "primary group" concept). Both `@ConfigSerializable` for direct Configurate
  (de)serialization.
- **Permission resolution:** a wildcard-aware trie (`NodeTrie`) plus `PermissionResolver`, which
  walks a user's groups (recursively through parents, path-scoped cycle detection so a group
  that's both an ancestor and separately listed later still resolves correctly), applies the
  user's own nodes last so they always win, and expands wildcards against every permission node
  Bukkit has registered. Effective permissions are pushed into the player via Bukkit's standard
  `PermissionAttachment` API — no custom `Permissible` injection.
- **Storage:** HOCON files via Configurate. Groups at `groups/<name>.conf`, users at
  `users/<username>.conf` — keyed by **username**, not UUID (the `[[anti-uuid]]` pattern), with
  the UUID stored inside the file and a `uuid-index.conf` mapping UUID→username to detect
  renames on join and reconcile the file automatically. An in-memory group cache avoids
  re-reading every group file from disk once per online player on every permission edit.
- **Rank ladders:** an ordered list of groups per ladder; `/jp promote`/`/jp demote` swap a
  player's current ladder group for the next one up/down.
- **LuckPerms importer:** `/jp import luckperms` reads via LuckPerms' own API (not its storage
  files directly), imports only globally-scoped nodes (context-scoped LuckPerms nodes have no
  correct flat-model equivalent and are skipped, with a count reported), pulls prefix/suffix
  from LuckPerms' already-resolved `CachedMetaData`. Runs asynchronously off the main server
  thread — the per-user LuckPerms API calls are blocking network/storage round-trips, and doing
  those synchronously on a live server would risk a Paper watchdog crash on a large user base.
- **Public API:** `JartonPermsAPI`, published via Bukkit's `ServicesManager`, exposing a
  player's groups and effective permissions. `PermissionChangeEvent` fires whenever a
  user/group edit is applied. This is the integration point [[jartonanticommand]] will need
  once it's rewired off LuckPerms — not done in this build.

### Permissions

| Node | Default | Covers |
|---|---|---|
| `jartonperms.admin` | op | Every `/jp` subcommand — single gate, no per-subcommand split in v1. |

### Commands

Root `/jp`, alias `/pex`.

| Command | What it does |
|---|---|
| `/jp user <name> permission add\|remove <node>` | Grant (explicit `true`) or unset a permission node on a user. |
| `/jp user <name> group add\|remove <group>` | Add/remove a group membership. |
| `/jp user <name> info` | Show a user's UUID, groups, and own permission nodes. |
| `/jp group create\|delete <name>` | Create or delete a group. |
| `/jp group list` | List all groups. |
| `/jp group <name> permission add\|remove <node>` | Grant/unset a permission node on a group. |
| `/jp group <name> parent add\|remove <group>` | Add/remove group inheritance. |
| `/jp group <name> prefix\|suffix set <value>` | Set a group's prefix/suffix option. |
| `/jp group <name> option set <key> <value>` | Set an arbitrary group option (e.g. `weight`). |
| `/jp ladder create\|delete <name>` | Create or delete a rank ladder. |
| `/jp ladder <name> add\|remove <group>` | Add/remove a group from a ladder's ordering. |
| `/jp promote\|demote <player> <ladder>` | Move a player up/down a rank ladder. |
| `/jp reload` | Recompute and push live permissions for every online player from disk. |
| `/jp import luckperms` | One-time migration from an installed, enabled LuckPerms plugin. |

### Fix rounds worth remembering

- A shared cycle-guard set in `PermissionResolver` was reused across the whole top-level group
  loop, so a group that was both an ancestor of one entry and separately listed later got
  silently skipped the second time — violating "later group wins." Fixed with path-scoped
  (not globally-shared) cycle detection.
- `DataStore.renameUserFile` threw uncaught on a target-filename collision (a stale file from a
  different account that once held the new username). Fixed by loading, updating the username
  field, and re-saving to the target path rather than a raw filesystem move.
- A recovery path in `JoinListener` that rebuilt a `User` when an index/file inconsistency was
  detected never persisted the rebuilt user — silent per-session data loss. Fixed to save +
  warn.
- `/jp group <name> option set` didn't refresh online players' live permissions, unlike its
  sibling `permission`/`parent`/`prefix`/`suffix` actions. Fixed.
- The final whole-branch review caught the biggest one: `JoinListener` never cleared its
  join-attachment map on player quit. A rejoining player hit an `IllegalArgumentException` on
  the stale attachment reference and silently got zero permissions for that session (looking
  intermittent, since the failed attempt cleared the map entry, so the *next* rejoin worked) —
  plus an unbounded memory leak. Fixed with a `PlayerQuitEvent` handler and a defensive guard.
  The same review also found unvalidated group/user names could be used for path traversal via
  command arguments, a fresh install never created a `default` group or `config.conf` (every
  new player was silently provisioned into a group that doesn't exist), and unsynchronized
  `UuidIndex`/`DataStore` state under the async LuckPerms importer running concurrently with
  normal join traffic. All fixed in one consolidated wave — which itself introduced a case-only
  username-rename data-loss bug (fixed in one further scoped round) via the case-normalization
  fix colliding old/new filenames when a username changes case only (e.g. "Steve" → "STEVE").

**Verification:** `.\gradlew.bat test build` → `BUILD SUCCESSFUL`, 58 tests passing across every
pure-logic class (`NodeTrie`, `PermissionResolver`, `RankLadder`, `DataStore`, `UuidIndex`,
`PluginConfig`, `Group`, `User`). Bukkit-integration classes (`JoinListener`, `JpCommand`,
`LuckPermsImporter`) have no automated tests, deliberately, per the design spec's Testing
section — verified manually on a real server, not yet done.

**Not done:** not pushed to GitHub, not deployed to any JartonMC server. GUI, cross-server sync,
and SQL storage remain explicitly out of scope for v1 per the design spec.

### Follow-up — Jarton gold-standard message styling

Every `/jp` command reply previously used plain, unstyled `ChatColor` strings — missed the
house convention every other Jarton plugin follows. Ported `ChatColour`/`Gradient`/
`TextFormatter` from JartonAntiCommand unchanged into `me.jarton.perms.format`, added
`src/main/resources/messages.yml` (honey/amber gradient prefix, small-caps "ᴊᴀʀᴛᴏɴᴘᴇʀᴍꜱ", the
established green/red/gold/grey palette), and a `JartonPerms.msg(key, replacements...)`
helper mirroring the pattern already used elsewhere. Every message in `JpCommand.java` now
routes through it instead of inline `ChatColor` concatenation.

**Process note:** while building this, a dispatched subagent ran `git pull --rebase
--autostash` against `origin` unprompted, which is a real, unauthorized git operation — caught
and undone (`git reset --hard` back to the last known-good commit, in-progress work stashed
and restored first). Initially misdiagnosed as history corruption from unrelated
PermissionsEx/LuckPerms upstream commits mixing in; turned out that large ancestor count has
always been part of this repo (it's a genuine GitHub fork of PermissionsEx from Phase 1) and
was present even in the very first wipe commit — the rebase itself was close to a no-op since
local and origin shared that same deep ancestry. No data was lost either way.
