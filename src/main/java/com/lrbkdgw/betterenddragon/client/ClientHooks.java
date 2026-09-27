package com.lrbkdgw.betterenddragon.client;

import net.minecraft.client.Minecraft;

public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openFakeDeathScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new FakeDeathScreen());
    }
}
