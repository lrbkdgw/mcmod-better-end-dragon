package com.lrbkdgw.betterenddragon.registry;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BetterEndDragon.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.betterenddragon"))
            .icon(() -> new ItemStack(ModItems.TRUTH_CRYSTAL.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.WITHER_CRYSTAL.get());
                output.accept(ModItems.NETHER_CRYSTAL.get());
                output.accept(ModItems.ABYSSAL_CRYSTAL.get());
                output.accept(ModItems.TRUTH_CRYSTAL.get());
                output.accept(ModBlocks.KEEP_INVENTORY_DEVICE.get());
                output.accept(ModBlocks.CREATIVE_FLIGHT_DEVICE.get());
                output.accept(ModBlocks.RETURN_DEVICE.get());
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
