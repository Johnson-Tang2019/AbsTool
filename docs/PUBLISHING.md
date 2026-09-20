# Publishing releases

GitHub Actions publishes release JARs to Modrinth, CurseForge, and GitHub when
a version tag is pushed. The workflow builds both loader variants with Java 25
and publishes each JAR with its correct loader metadata.

## One-time repository setup

Create these GitHub Actions secrets under **Settings > Secrets and variables >
Actions > Secrets**:

- `MODRINTH_TOKEN`: a Modrinth personal access token with version creation
  permission for the project.
- `CURSEFORGE_TOKEN`: a CurseForge API token with upload permission for the
  project.

Create this GitHub Actions variable under **Settings > Secrets and variables >
Actions > Variables**:

- `MODRINTH_PROJECT_ID`: the Modrinth project slug or project ID.

The CurseForge project ID defaults to `1700814`. It can be overridden with an
Actions variable named `CURSEFORGE_PROJECT_ID` if the project is ever moved.
Tokens must stay in GitHub Actions secrets and must never be committed.

## Publish a release

1. Update `mod_version` in `gradle.properties` and update `CHANGELOG.md`.
2. Commit and push the release changes to `main`.
3. Create and push a matching tag, such as `v0.3.1` for
   `mod_version=0.3.1`.
4. Watch the **Release** workflow. It must complete all Modrinth, CurseForge,
   and GitHub Release steps before the release is considered published.

The workflow deliberately rejects a tag that does not exactly match
`mod_version`. A failed platform upload also prevents the GitHub Release step,
so a partial release is visible as a failed workflow rather than a false
success.

The workflow may also be started manually for an existing tag. Enter the full
tag including the `v` prefix. This is intended for retrying a failed workflow;
platforms can reject an upload that was already completed during an earlier
partial run.
