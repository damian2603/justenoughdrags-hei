package com.warmthdawn.justenoughdrags.mixin;

import appeng.client.gui.AEBaseGui;
import com.warmthdawn.justenoughdrags.compact.HeiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Redirects the call to {@code bookmarkedJEIghostItem(mouseX, mouseY)} that
 * AE2-UEL's {@code drawScreen} does every frame. Under HEI, that
 * polling-based approach is broken (instanceof ItemStack rejects HEI's
 * BookmarkItem wrapper) and worse, conflicts with HEI's native ghost-drag
 * handling - it sets {@code mc.player.inventory.setItemStack(...)} to an
 * empty stack and marks itself as the active drag source, preventing
 * HEI's IGhostIngredientHandler from ever being called.
 *
 * Under HEI we skip the polling entirely. Under vanilla JEI we call the
 * original method via {@link AEBaseGuiAccessor} (the method is
 * package-private so we can't call it directly from outside the package).
 *
 * Mirrors the {@code if (Platform.isModLoaded("jei") && !ClientHelper.isHei)}
 * guard added in upstream AE2-UEL commit fd39333.
 *
 * The method selector "drawScreen" is the MCP/deobf name; the mixin
 * annotation processor maps it to the SRG name {@code func_73863_a} via
 * the generated refmap at build time. The {@code @At target} is set to
 * remap=false because {@code bookmarkedJEIghostItem} is an AE2 method,
 * not a vanilla MC one.
 */
@Mixin(value = AEBaseGui.class, remap = true)
public abstract class MixinAEBaseGui {

    @Redirect(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/gui/AEBaseGui;bookmarkedJEIghostItem(II)V",
                    remap = false
            ),
            remap = true
    )
    private void jed$skipPollingUnderHei(AEBaseGui self, int mouseX, int mouseY) {
        if (!HeiState.isHei) {
            ((AEBaseGuiAccessor) self).jed$callBookmarkedJEIghostItem(mouseX, mouseY);
        }
        // Under HEI: deliberately do nothing. HEI will fire
        // IGhostIngredientHandler through MixinAEGuiHandler when the user
        // actually drops the bookmark.
    }
}
