package com.warmthdawn.justenoughdrags.mixin;

import appeng.client.ClientHelper;
import com.warmthdawn.justenoughdrags.compact.HeiState;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Half of the AE2-UEL fix (the other half is in {@link MixinAEBaseGui}).
 *
 * AE2-UEL's {@code ClientHelper.MouseClickEvent} cancels mouse-press
 * events whenever the AE2 GUI has a bookmarked ingredient hovered, so that
 * AE2-UEL's own polling-based ghost drag can run. With HEI installed this
 * <em>prevents</em> HEI's native ghost-drag from ever seeing the click,
 * which is the root cause of the bookmark-drag breakage. Skipping the
 * cancellation under HEI lets HEI's IGhostIngredientHandler integration
 * (which {@link MixinAEGuiHandler} hooks into) take the click instead.
 *
 * Mirrors exactly the {@code if (isHei) return;} guard added in upstream
 * AE2-UEL commit fd39333 - we just store the flag in our own HeiState
 * instead of AE2-UEL's ClientHelper.
 */
@Mixin(value = ClientHelper.class, remap = false)
public abstract class MixinClientHelper {

    @Inject(
            method = "MouseClickEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    private void jed$skipUnderHei(GuiScreenEvent.MouseInputEvent.Pre event, CallbackInfo ci) {
        if (HeiState.isHei) {
            ci.cancel();
        }
    }
}
