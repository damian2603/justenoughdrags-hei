package com.warmthdawn.justenoughdrags.mixin;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

public class JEDMixinLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        configs.add("mixins.justenoughdrags.json");
        if (Loader.isModLoaded("appliedenergistics2")) {
            configs.add("mixins.justenoughdrags-ae2.json");
        }
        if (Loader.isModLoaded("enderio")) {
            configs.add("mixins.justenoughdrags-enderio.json");
        }
        return configs;
    }
}
