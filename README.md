# Just Enough Drags &mdash; HEI fork

Fork of [WarmthDawn's Just Enough Drags](https://github.com/WarmthDawn/justenoughdrags)
that fixes JEI-bookmark-panel ghost-drag for every supported mod under
[HEI (HadEnoughItems)](https://github.com/CleanroomMC/HadEnoughItems).

## The bug, in one paragraph

HEI wraps every entry in the JEI bookmark panel inside a
`mezz.jei.bookmarks.BookmarkItem` &mdash; a class that doesn't exist in
vanilla JEI. The ghost-drag handlers in upstream Just Enough Drags (and
in AE2-UEL, and in EnderIO, and in basically every other mod that does
this) do `if (ingredient instanceof ItemStack)` or
`if (ingredient instanceof FluidStack)` before placing the item. The
wrapper fails both checks, so dragging from the bookmark panel silently
does nothing. From the user's perspective, drag highlights show up but
items don't actually go into the filter slot.

## How this fork fixes it

A single `HeiState` helper detects HEI at preInit (probing for
`mezz.jei.bookmarks.BookmarkItem` via `Class.forName`) and exposes
`HeiState.unwrap(Object)` &mdash; reflection on the wrapper's private
`ingredient` field to return whatever's inside. Under vanilla JEI it's a
no-op.

That helper is then plugged in via **two different techniques** depending
on the mod:

| Mod | Technique | Why |
|---|---|---|
| XNet, RFTools, Refined Storage, RandomThings, Blood Magic, Actually Additions, Modular Routers, Thermal Dynamics, CraftTweaker, Translocators | Direct source edit &mdash; `unwrap()` call added before every `instanceof` site in JED's existing handlers | Upstream JED owns these, edit is mechanical |
| **Applied Energistics 2 (UEL)** | **Mixin** &mdash; patches AE2-UEL's own `AEGuiHandler`, `AEBaseGui` and `ClientHelper` in place | AE2-UEL has its own ghost-drag handler that JED-style overrides would silently displace (losing shift-click bulk-fill, the `IJEIGhostIngredients` integration, the correct `PLACE_JEI_GHOST_ITEM` packet). Mixin patches preserve all of AE2-UEL's behaviour while fixing the HEI bug. 1:1 with [Exaxxion's unmerged fix](https://github.com/AE2-UEL/Applied-Energistics-2/commit/fd39333) for the AE2-UEL repo (which has been effectively unmaintained for over a year). |
| **EnderIO** | **Mixin** &mdash; `@ModifyVariable` on `IHaveGhostTargets$1.accept` (anonymous inner Target class) | All EnderIO filter GUIs (basic / advanced / big / existing / limited item filter, fluid filter, enchantment filter, mod filter, ...) funnel through that one class. One mixin fixes them all. |

Mixins ship in `src/main/resources/mixins.justenoughdrags.json` and are
loaded by **MixinBooter** at runtime.

## Supported mods

Bookmark drag from HEI works in:

- Applied Energistics 2 (UEL) - all GUIs covered by AE2-UEL's own AEGuiHandler
- EnderIO filters (every variant)
- XNet Controller
- RFTools Item Filter, Storage Filter
- Refined Storage filters (item and fluid)
- Random Things item filter
- Blood Magic routing nodes
- Actually Additions filters
- Modular Routers item filters
- Thermal Dynamics duct connection
- CraftTweaker GUI
- Translocators

## Build infrastructure

This project uses CleanroomMC's
[TemplateDevEnv](https://github.com/CleanroomMC/TemplateDevEnv):
**RetroFuturaGradle 2.0** + **Gradle 9.2** + **Java 25 toolchain**
(compiling Java 8 bytecode for Minecraft 1.12.2). The old ForgeGradle 2.3
+ MixinGradle 0.6 stack was replaced because both are essentially
unmaintained and have growing friction with modern JDKs / Gradle.

Mod info is parameterised through `gradle.properties` &mdash; you change
the version there, not in source. `tags.properties` feeds the auto-
generated `com.warmthdawn.justenoughdrags.Tags` class that
`JustEnoughDrags.java` reads `MODID`/`NAME`/`VERSION` from.

## Building locally

Requires JDK 17 or later (the gradle build runs under it; the produced
mod is Java 8 bytecode). No separate Gradle install needed - the wrapper
takes care of pulling Gradle 9.2.

```
./gradlew build
```

The jar lands in `build/libs/`. Drop it into `mods/`.

Override version at build time:

```
./gradlew build -Pmod_version=1.6.0-hei.4
```

## Building on GitHub Actions

`.github/workflows/build.yml` builds on every push and PR, uploads the
jar as a workflow artifact (download from the Actions tab on github.com),
and publishes a GitHub Release on `v*` tag pushes.

Version-string mapping:

| Trigger | Version string |
|---|---|
| Tag push `v1.6.0` | `1.6.0` |
| Branch push / PR | `1.6.0-hei-dev.abc1234` |
| Manual dispatch | `1.6.0-hei-dev.abc1234` |

## Runtime dependencies

| Mod | Required? | Why |
|---|---|---|
| JEI (or HEI) | Required | The mod is a JEI plugin. |
| MixinBooter | Required | Loads our mixin config at runtime. |
| AE2 (UEL) | Soft | The AE2 mixins only apply if AE2 is loaded; otherwise they're harmlessly skipped. |
| EnderIO | Soft | Same as AE2. |
| All other supported mods | Soft | Their handlers register through `Enables` flags. |

## Multiplayer note

Modid is kept as `justenoughdrags` so this drop-in replaces the original
on server-side network compat checks. Both client and server need the
same modid loaded - vanilla JED and this fork are mutually exclusive
(Forge will reject the second to load).

The Mixin patches only apply client-side. The server can keep running
vanilla JED, AE2-UEL, EnderIO without modification - only your client
jar needs this fork.

## License

Original mod by WarmthDawn, MIT. This fork is also MIT - see `LICENSE`.
The AE2 Mixin logic mirrors Exaxxion's
[fd39333](https://github.com/AE2-UEL/Applied-Energistics-2/commit/fd39333),
which is LGPL-licensed as part of AE2-UEL.
