package com.warmthdawn.justenoughdrags.mixin;

import appeng.client.gui.AEBaseGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * AE2's {@code bookmarkedJEIghostItem(int, int)} is package-private.
 * {@link MixinAEBaseGui} needs to call it from outside the package after
 * the @Redirect short-circuits the direct call - this accessor exposes it.
 */
@Mixin(value = AEBaseGui.class, remap = false)
public interface AEBaseGuiAccessor {

    @Invoker("bookmarkedJEIghostItem")
    void jed$callBookmarkedJEIghostItem(int mouseX, int mouseY);
}
