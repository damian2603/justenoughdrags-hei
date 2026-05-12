# Changelog

## 1.6.0-hei

- Initial HEI fork of Just Enough Drags.
- Added HEI bookmark-panel ghost-drag support across all upstream-supported
  mods (source-level edits in their respective handlers).
- AE2-UEL: Mixin patches (ports of Exaxxion's unmerged
  [fd39333](https://github.com/AE2-UEL/Applied-Energistics-2/commit/fd39333))
  fix AE2-UEL's own `AEGuiHandler` / `AEBaseGui` / `ClientHelper` in place,
  preserving AE2-UEL features (shift-click bulk-fill, IJEIGhostIngredients,
  proper packet routing).
- EnderIO: Single `@ModifyVariable` mixin on `IHaveGhostTargets$1.accept`
  fixes every EnderIO filter GUI (basic/advanced/big/existing/limited item
  filter, fluid filter, enchantment filter, mod/power/species/soul filter)
  in one shot.
- Build infrastructure: migrated to CleanroomMC's `TemplateDevEnv`
  (RetroFuturaGradle 2.0 + Gradle 9.2 + Java 25 toolchain), replacing the
  unmaintained ForgeGradle 2.3 + MixinGradle 0.6 stack.
