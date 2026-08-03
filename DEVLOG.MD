# JartonPerms Devlog

Fork of [PermissionsEx](https://github.com/PEXPlugins/PermissionsEx) (`PEXPlugins/PermissionsEx`),
rebranded to JartonPerms. Repo: `TridentII-NZ/JartonPerms` (public — GitHub declined to make
the fork private on this account's plan).

Design spec: `docs/superpowers/specs/2026-08-03-jartonperms-design.md` (in the Demetrius vault).

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

**Not done:** not pushed to GitHub, not deployed anywhere. Phase 2 (gutting `core`'s ANTLR-based
contextual calculator, deleting `impl-blocks:glob`/`legacy`, rebuilding around the flat User/Group
data model from the design spec, deciding `datastore:sql`'s fate) is separate future work.
