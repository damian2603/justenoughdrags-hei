package com.warmthdawn.justenoughdrags.mixin;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.AEGuiHandler;
import appeng.container.interfaces.IJEIGhostIngredients;
import com.warmthdawn.justenoughdrags.compact.HeiState;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * The third piece of the AE2-UEL fix.
 *
 * Even with the polling killed ({@link MixinAEBaseGui}) and the
 * MouseClickEvent guard relaxed ({@link MixinClientHelper}), HEI still
 * passes raw {@code BookmarkItem} instances into
 * {@code AEGuiHandler.getTargets(...)}. AE2-UEL's per-GUI
 * {@code getPhantomTargets} implementations check
 * {@code instanceof ItemStack}/{@code FluidStack} and bail out on a
 * {@code BookmarkItem}, returning an empty list - which HEI takes as
 * "no valid drop targets" and silently cancels the drag.
 *
 * The fix is to unwrap the {@code BookmarkItem} before passing it on to
 * AE2's own slot logic, AND to re-wrap the returned {@code Target}s so
 * that whatever HEI eventually hands to {@code Target.accept(...)} (also
 * a {@code BookmarkItem}) gets unwrapped on the way back through.
 *
 * The method signature is pinned to {@code AEBaseGui}'s overload to avoid
 * accidentally hitting the synthetic bridge method that the compiler
 * generates for the {@code IGhostIngredientHandler<AEBaseGui>} interface
 * declaration. Both have {@code "getTargets"} as a name.
 */
@Mixin(value = AEGuiHandler.class, remap = false)
public abstract class MixinAEGuiHandler {

    @SuppressWarnings({"unchecked"})
    @Inject(
            method = "getTargets(Lappeng/client/gui/AEBaseGui;Ljava/lang/Object;Z)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <I> void jed$heiUnwrap(AEBaseGui gui,
                                   I ingredient,
                                   boolean doStart,
                                   CallbackInfoReturnable<List<Target<I>>> cir) {
        if (!HeiState.isHei) {
            return;
        }
        if (!(gui instanceof IJEIGhostIngredients)) {
            return;
        }

        Object unwrapped = HeiState.unwrap(ingredient);
        if (unwrapped == null || unwrapped == ingredient) {
            // Either unwrap failed (null) or the value wasn't a BookmarkItem
            // to begin with (drag from main JEI panel, not bookmark panel).
            // Let AE2's normal code path run.
            return;
        }

        IJEIGhostIngredients ghostGui = (IJEIGhostIngredients) gui;
        List<Target<?>> phantoms = ghostGui.getPhantomTargets(unwrapped);

        List<Target<I>> wrapped = new ArrayList<>(phantoms.size());
        for (Target<?> raw : phantoms) {
            final Target<Object> rawObj = (Target<Object>) raw;
            wrapped.add(new Target<I>() {
                @Override
                public Rectangle getArea() {
                    return rawObj.getArea();
                }

                @Override
                public void accept(I ing) {
                    Object u = HeiState.unwrap(ing);
                    if (u != null) {
                        rawObj.accept(u);
                    }
                }
            });
        }

        cir.setReturnValue(wrapped);
    }
}
