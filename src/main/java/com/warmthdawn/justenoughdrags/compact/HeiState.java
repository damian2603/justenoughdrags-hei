package com.warmthdawn.justenoughdrags.compact;

import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Field;

/**
 * Detects HEI (HadEnoughItems, the CleanroomMC fork of JEI) at preInit time
 * and provides a single unwrap() helper used by every ghost-drag handler in
 * this mod.
 *
 * Why: HEI wraps every entry shown in the JEI bookmark panel in an instance
 * of mezz.jei.bookmarks.BookmarkItem - a class that does not exist in
 * vanilla JEI. The ghost-drag handlers in the original JustEnoughDrags do
 *
 *     if (ingredient instanceof ItemStack) ...
 *     else if (ingredient instanceof FluidStack) ...
 *
 * which silently fails on the wrapper, so dragging from the HEI bookmark
 * panel into any of the supported GUIs does nothing. unwrap() peels the
 * wrapper off via reflection on its private "ingredient" field before the
 * type check sees it.
 *
 * Under vanilla JEI (no HEI installed) isHei stays false and unwrap()
 * returns the input unchanged, so the original behaviour is preserved.
 */
public final class HeiState {

    public static boolean isHei = false;

    private static Class<?> bookmarkItemClass;
    private static Field ingredientField;

    private HeiState() {
    }

    /** Called from JustEnoughDrags.preInit once. */
    public static void detect() {
        if (!Loader.isModLoaded("jei")) {
            return;
        }
        try {
            bookmarkItemClass = Class.forName("mezz.jei.bookmarks.BookmarkItem");
            ingredientField = bookmarkItemClass.getDeclaredField("ingredient");
            ingredientField.setAccessible(true);
            isHei = true;
        } catch (ClassNotFoundException e) {
            // Vanilla JEI - leave isHei = false.
        } catch (NoSuchFieldException e) {
            // HEI exists but its internals changed in a future version we
            // didn't anticipate. Better to fall back to the original (broken
            // under HEI) behaviour than to crash.
            bookmarkItemClass = null;
            ingredientField = null;
        }
    }

    /**
     * If the value is a HEI BookmarkItem wrapper, return the wrapped
     * ingredient (typically ItemStack or FluidStack). Otherwise return the
     * input unchanged. Returns null only if reflection failed unexpectedly.
     */
    public static Object unwrap(Object ingredient) {
        if (!isHei || ingredient == null || bookmarkItemClass == null) {
            return ingredient;
        }
        if (!bookmarkItemClass.isInstance(ingredient)) {
            return ingredient;
        }
        try {
            return ingredientField.get(ingredient);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
