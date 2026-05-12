package com.warmthdawn.justenoughdrags.jei;

import WayofTime.bloodmagic.client.gui.GuiItemRoutingNode;
import codechicken.lib.inventory.container.SlotDummy;
import cofh.thermaldynamics.gui.client.GuiDuctConnection;
import com.blamejared.ctgui.api.SlotRecipe;
import com.blamejared.ctgui.client.gui.craftingtable.GuiCraftingTable;
import com.warmthdawn.justenoughdrags.JustEnoughDrags;
import com.warmthdawn.justenoughdrags.compact.Enables;
import com.warmthdawn.justenoughdrags.compact.actuallyadditions.AAFilterGhostHandler;
import com.warmthdawn.justenoughdrags.compact.bm2.RoutingNodeGhostHandler;
import com.warmthdawn.justenoughdrags.compact.mcjty.RFToolsGhostHandler;
import com.warmthdawn.justenoughdrags.compact.mrouters.MRFilterGhostHandler;
import com.warmthdawn.justenoughdrags.compact.rs.RSGhostHandler;
import com.warmthdawn.justenoughdrags.compact.rthings.RandomThingsGhostHandler;
import com.warmthdawn.justenoughdrags.compact.xnet.XNetControllerGhostHandler;
import de.ellpeck.actuallyadditions.mod.inventory.gui.GuiFilter;
import de.ellpeck.actuallyadditions.mod.inventory.gui.GuiLaserRelayItemWhitelist;
import de.ellpeck.actuallyadditions.mod.inventory.gui.GuiRangedCollector;
import mcjty.rftools.blocks.itemfilter.GuiItemFilter;
import mcjty.rftools.items.storage.GuiStorageFilter;
import mcjty.xnet.blocks.controller.gui.GuiController;
import me.desht.modularrouters.client.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.client.gui.filter.GuiModFilter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiFurnace;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        if (Enables.CRAFT_TWEAKER) {
            registry.addGhostIngredientHandler(GuiCraftingTable.class, new GenericGhostHandler<>(SlotRecipe.class));
            registry.addGhostIngredientHandler(GuiFurnace.class, new GenericGhostHandler<>(SlotRecipe.class));
        }
        if (Enables.THERMAL_DYNAMICS)
            registry.addGhostIngredientHandler(GuiDuctConnection.class, new GenericGhostHandler<>(cofh.thermaldynamics.gui.slot.SlotFilter.class));
        // AE2 handlers intentionally not registered here.
        //
        // Earlier this fork DID register per-subclass AE2 handlers, but they
        // override AE2-UEL's own (better) AEGuiHandler via JEI's most-specific-
        // class-wins lookup, losing AE2-UEL features like shift-click bulk fill
        // and proper IJEIGhostIngredients integration.
        //
        // Instead we now patch AE2-UEL's own classes via Mixin (see
        // com.warmthdawn.justenoughdrags.mixin.MixinClientHelper,
        // MixinAEBaseGui, MixinAEGuiHandler). The mixins are 1:1 with the
        // upstream Exaxxion fix at AE2-UEL/Applied-Energistics-2 commit
        // fd39333 (https://github.com/AE2-UEL/Applied-Energistics-2/commit/fd39333)
        // which has never been merged to AE2-UEL official because the repo is
        // effectively unmaintained.
        //
        // Net result: AE2-UEL keeps its own handler, but that handler now
        // correctly unwraps HEI's BookmarkItem.

        if (Enables.ACTUALLY_ADDITIONS) {
            registry.addGhostIngredientHandler(GuiLaserRelayItemWhitelist.class, new AAFilterGhostHandler<>());
            registry.addGhostIngredientHandler(GuiFilter.class, new AAFilterGhostHandler<>());
            registry.addGhostIngredientHandler(GuiRangedCollector.class, new AAFilterGhostHandler<>());
        }
        if (Enables.RFTOOLS) {
            registry.addGhostIngredientHandler(GuiItemFilter.class, new RFToolsGhostHandler<>());
            registry.addGhostIngredientHandler(GuiStorageFilter.class, new RFToolsGhostHandler<>());
        }
        if (Enables.TRANSLOCATORS) {
//            registry.addGhostIngredientHandler(GuiTranslocator.class, new GenericGhostHandler<>(SlotDummy.class));
            registerByName(registry, "codechicken.translocators.client.gui.GuiTranslocator", new GenericGhostHandler<>(SlotDummy.class));
        }
        if (Enables.BLOOD_MAGIC) {
            registry.addGhostIngredientHandler(GuiItemRoutingNode.class, new RoutingNodeGhostHandler());
        }
        if (Enables.RANDOM_THINGS) {
            registry.addGhostIngredientHandler(lumien.randomthings.client.gui.GuiItemFilter.class, new RandomThingsGhostHandler());
        }

        if (Enables.MODULAR_ROUTERS) {
            registry.addGhostIngredientHandler(GuiBulkItemFilter.class, new MRFilterGhostHandler<>());
            registry.addGhostIngredientHandler(GuiModFilter.class, new MRFilterGhostHandler<>());
        }

        if (Enables.XNET) {
            registry.addGhostIngredientHandler(GuiController.class, new XNetControllerGhostHandler());
        }

        if (Enables.REFINED_STORAGE) {
            registry.addGhostIngredientHandler(com.raoulvdberge.refinedstorage.gui.GuiBase.class, new RSGhostHandler());
        }

    }


    @SuppressWarnings("unchecked")
    private <T extends GuiScreen> void registerByName(IModRegistry registry, String className, IGhostIngredientHandler<T> handler) {
        try {
            registry.addGhostIngredientHandler(
                (Class<T>) Class.forName(className),
                handler);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
