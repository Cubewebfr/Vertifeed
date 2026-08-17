# Contributing to Vertifeed

Thanks for helping. This mod targets **Minecraft 26.2** (Fabric, Java 25).

## Before you start

1. Open an [issue](https://github.com/Cubewebfr/Vertifeed/issues) for bugs or feature ideas when possible.
2. Keep PRs focused. Large refactors that change the vertical render path need discussion first — that pass is easy to break.
3. Do not commit `run/`, `.gradle/`, `build/`, or `.mc-src-extra/`.

## Setup

```bash
./gradlew runClient
```

You need the [NDI Runtime](https://ndi.link/NDIRedistV6) installed to test the feed.

## Pull requests

- Branch from `main`.
- Describe what you changed and how you tested (title screen, world select, inventory, NDI in OBS if relevant).
- Match existing style: package `fr.cubeweb.vertifeed`, mixins with `remap = false`.
- Update README / lang strings if you change user-facing behavior.

## Reporting bugs

Include:

- Minecraft / Fabric Loader / Fabric API / Vertifeed versions
- Whether NDI Runtime is installed
- What you see in the 16:9 window vs the NDI® feed
- Relevant lines from `latest.log`

## Code of conduct

Be respectful. Harassment and bad-faith contributions will be rejected.
