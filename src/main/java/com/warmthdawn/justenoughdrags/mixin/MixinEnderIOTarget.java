package com.warmthdawn.justenoughdrags.mixin;

import com.warmthdawn.justenoughdrags.compact.HeiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Single-mixin fix for every EnderIO filter GUI that supports JEI ghost
 * drag.
 *
 * EnderIO's JEI integration funnels every filter slot through an anonymous
 * inner Target class compiled as
 * {@code crazypants.enderio.base.integration.jei.IHaveGhostTargets$1}. Its
 * {@code accept(Object ingredient)} method does:
 *
 * <pre>
 *   if (ingredient instanceof ItemStack) ... putStack
 *   else if (ingredient instanceof FluidStack) ... putFluidStack
 *   else if (ingredient instanceof Fluid) ... putFluid
 *   else if (ingredient instanceof EnchantmentData) ... putEnchantmentData
 * </pre>
 *
 * None of these match HEI's {@code BookmarkItem} wrapper, so dragging from
 * the bookmark panel visually completes (the area highlight works because
 * {@code getArea()} doesn't care about ingredient type) but the drop is a
 * silent no-op.
 *
 * Rather than @Inject-replacing the whole accept body, we use
 * {@code @ModifyVariable} with {@code argsOnly = true} to rewrite the
 * {@code ingredient} parameter at HEAD before any instanceof check runs.
 * Under vanilla JEI the helper is a no-op so behaviour is unchanged.
 *
 * Covers all of:
 *   - Basic / Big / Advanced Item Filter
 *   - Existing Item Filter
 *   - Limited Item Filter
 *   - Mod Filter / Power Filter / Species Filter
 *   - Soul Filter
 *   - Fluid Filter
 *   - Enchantment Filter
 *
 * Targeted by string name because {@code IHaveGhostTargets$1} is an
 * anonymous inner class - the compiler doesn't expose it as a Class<?>
 * we could pass to {@code @Mixin(value = ...)}.
 */
@Mixin(targets = "crazypants.enderio.base.integration.jei.IHaveGhostTargets$1", remap = false)
public abstract class MixinEnderIOTarget {

    @ModifyVariable(
            method = "accept",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private Object jed$heiUnwrap(Object ingredient) {
        return HeiState.unwrap(ingredient);
    }
}
