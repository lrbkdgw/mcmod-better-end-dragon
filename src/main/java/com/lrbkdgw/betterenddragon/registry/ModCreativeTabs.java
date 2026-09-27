package com.lrbkdgw.betterenddragon.registry;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BetterEndDragon.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.betterenddragon.main"))
            .icon(() -> ModItems.TRUTH_CRYSTAL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.WITHER_CRYSTAL.get());
                output.accept(ModItems.NETHER_CRYSTAL.get());
                output.accept(ModItems.DEEP_CRYSTAL.get());
                output.accept(ModItems.TRUTH_CRYSTAL.get());
                output.accept(ModBlocks.KEEP_INVENTORY_DEVICE.get());
                output.accept(ModBlocks.CREATIVE_FLIGHT_DEVICE.get());
                output.accept(ModBlocks.RETURN_HOME_DEVICE.get());
            })
            .build());

    private ModCreativeTabs() {
    }
}
