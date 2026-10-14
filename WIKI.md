# Ab's Tool documentation wiki

The bilingual wiki source is maintained in the [wiki](wiki/) directory.

- [English Home](wiki/Home.md)
- [简体中文首页](wiki/Home.zh-CN.md)

The pages are generated from the current repository behavior and version branches, including Minecraft 26.2 stable and Minecraft 26.3 beta.

## Automatic GitHub Wiki sync

`.github/workflows/wiki-sync.yml` mirrors the entire `wiki/` directory to the repository's GitHub Wiki whenever `wiki/**` changes on `main`. It can also be run manually with **Actions -> Wiki Sync -> Run workflow**.

GitHub stores the Wiki in a separate `.wiki.git` repository. GitHub requires that repository to be initialized once before automation can push to it:

1. Make sure **Settings -> General -> Features -> Wikis** is enabled.
2. Open the repository's **Wiki** tab.
3. Create any initial Home page and save it.
4. Run the **Wiki Sync** workflow once.

After that bootstrap step, edit only the tracked `wiki/` directory in the main repository. The workflow mirrors additions, edits, renames, and deletions to the live GitHub Wiki.

The workflow uses the repository-scoped `GITHUB_TOKEN` with `contents: write`; no personal access token is stored in the repository.
