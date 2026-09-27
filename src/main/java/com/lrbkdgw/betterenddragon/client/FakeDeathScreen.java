package com.lrbkdgw.betterenddragon.client;

import com.lrbkdgw.betterenddragon.network.ModNetwork;
import com.lrbkdgw.betterenddragon.network.UnderworldRespawnPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Looks like the vanilla death screen, but nothing actually died: pressing the
 * button moves the player into the Underworld with the whole inventory intact.
 */
public class FakeDeathScreen extends Screen {
    private Button respawnButton;
    private int delayTicker;

    public FakeDeathScreen() {
        super(Component.translatable("screen.betterenddragon.death.title"));
    }

    @Override
    protected void init() {
        this.delayTicker = 0;
        this.respawnButton = this.addRenderableWidget(Button
                .builder(Component.translatable("screen.betterenddragon.death.respawn"), button -> {
                    ModNetwork.sendToServer(new UnderworldRespawnPacket());
                    button.active = false;
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(null);
                    }
                })
                .bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20)
                .build());
        this.respawnButton.active = false;
    }

    @Override
    public void tick() {
        super.tick();
        this.delayTicker++;
        if (this.delayTicker >= 20 && this.respawnButton != null) {
            this.respawnButton.active = true;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
        graphics.pose().pushPose();
        graphics.pose().scale(2.0F, 2.0F, 2.0F);
        graphics.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, 0xFFFFFF);
        graphics.pose().popPose();
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.betterenddragon.death.subtitle"),
                this.width / 2, this.height / 4 + 48, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
