package com.warmthdawn.justenoughdrags.compact.rs;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.container.ContainerGrid;
import com.raoulvdberge.refinedstorage.container.slot.filter.SlotFilter;
import com.raoulvdberge.refinedstorage.container.slot.filter.SlotFilterFluid;
import com.raoulvdberge.refinedstorage.container.slot.legacy.SlotLegacyFilter;
import com.raoulvdberge.refinedstorage.gui.GuiBase;
import com.raoulvdberge.refinedstorage.gui.grid.GuiGrid;
import com.raoulvdberge.refinedstorage.network.MessageSlotFilterSet;
import com.raoulvdberge.refinedstorage.network.MessageSlotFilterSetFluid;
import com.raoulvdberge.refinedstorage.util.StackUtils;
import com.warmthdawn.justenoughdrags.compact.HeiState;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RSGhostHandler implements IGhostIngredientHandler<GuiBase> {

    @Override
    public <I> List<Target<I>> getTargets(GuiBase gui, I ingredient, boolean doStart) {


        List<Target<I>> targets = new ArrayList<>();

        //Only shift for pattern grid
        boolean isShiftDown = ((Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)));
        if (gui instanceof GuiGrid && !isShiftDown) {
            return targets;
        }

        // HEI compat - unwrap once up front so both the ItemStack and the
        // FluidStack branches below trigger correctly on bookmark ingredients.
        // Under vanilla JEI this is a no-op.
        final Object effective = HeiState.unwrap(ingredient);

        for (Slot slot : gui.inventorySlots.inventorySlots) {
            if (!slot.isEnabled()) {
                continue;
            }


            Rectangle bounds = new Rectangle(gui.getGuiLeft() + slot.xPos, gui.getGuiTop() + slot.yPos, 17, 17);
            if (effective instanceof ItemStack && (slot instanceof SlotLegacyFilter || slot instanceof SlotFilter)) {
                targets.add(new Target<I>() {
                    @Override
                    public Rectangle getArea() {
                        return bounds;
                    }

                    @Override
                    public void accept(I ing) {
                        Object eff = HeiState.unwrap(ing);
                        if (eff instanceof ItemStack) {
                            ItemStack stack = (ItemStack) eff;
                            slot.putStack(stack);
                            RS.INSTANCE.network.sendToServer(new MessageSlotFilterSet(slot.slotNumber, stack));
                        }
                    }
                });
            } else {
                FluidStack fluid = null;
                if (effective instanceof ItemStack) {
                    ItemStack stack = ((ItemStack) effective).copy();
                    fluid = FluidUtil.getFluidContained(stack);
                }
                if (effective instanceof FluidStack) {
                    fluid = (FluidStack) effective;
                }
                if (fluid != null) {
                    if (slot instanceof SlotFilterFluid) {
                        targets.add(new Target<I>() {
                            @Override
                            public Rectangle getArea() {
                                return bounds;
                            }

                            @Override
                            public void accept(I ing) {
                                Object eff = HeiState.unwrap(ing);
                                FluidStack fluid = null;
                                if (eff instanceof ItemStack) {
                                    ItemStack stack = ((ItemStack) eff).copy();
                                    fluid = FluidUtil.getFluidContained(stack);
                                }
                                if (eff instanceof FluidStack) {
                                    fluid = (FluidStack) eff;
                                }
                                if (fluid != null) {
                                    RS.INSTANCE.network.sendToServer(new MessageSlotFilterSetFluid(slot.slotNumber, StackUtils.copy(fluid, Fluid.BUCKET_VOLUME)));

                                }

                            }
                        });
                    }
                }
            }

        }

        return targets;
    }

    @Override
    public void onComplete() {
        // NO OP
    }
}