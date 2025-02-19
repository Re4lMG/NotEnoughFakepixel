package org.ginafro.notenoughfakepixel.features.skyblock.slayers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;
import org.ginafro.notenoughfakepixel.variables.Gamemode;

import java.util.*;

public class MinibossAlert {
    private static final Set<String> MINIBOSS = new HashSet<>(Arrays.asList(
            "Revenant Sycophant", "Revenant Champion", "Deformed Revenant", "Atoned Champion", "Atoned Revenant", // Zombie
            "Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula", // Spider
            "Pack Enforcer", "Sven Follower", "Sven Alpha", // Wolf
            "Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac", // Enderman
            "Flare Demon", "Kindleheart Demon", "Burningsoul Demon" // Blaze
    ));

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Set<UUID> detectedMinibosses = new HashSet<>();

    private static String displayText = "";
    private static long endTime = 0;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!(ScoreboardUtils.currentGamemode == Gamemode.SKYBLOCK) || mc.thePlayer == null) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity.getUniqueID() != null && detectedMinibosses.contains(entity.getUniqueID())) continue;

            String entityName = entity.getDisplayName().getUnformattedText();
            for (String keyword : MINIBOSS) {
                if (entityName.contains(keyword)) {
                    detectedMinibosses.add(entity.getUniqueID());
                    triggerAlerts();
                    break;
                }
            }
        }
    }

    private void triggerAlerts() {
        if (Configuration.slayerMinibossTitle) {
            showCustomOverlay(EnumChatFormatting.RED + "MINIBOSS SPAWNED!", 1000);
        }
        if (Configuration.slayerMinibossSound) {
            playSoundWithDelay(5, 70);
        }
    }

    private void playSoundWithDelay(int times, int delay) {
        new Thread(() -> {
            try {
                for (int i = 0; i < times; i++) {
                    playSound();
                    Thread.sleep(delay);
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void playSound() {
        if (mc.theWorld != null) {
            mc.theWorld.playSound(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    "random.orb",
                    1.0F,
                    1.0F,
                    false
            );
        }
    }

    private void showCustomOverlay(String text, int durationMillis) {
        displayText = text;
        endTime = System.currentTimeMillis() + durationMillis;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (System.currentTimeMillis() > endTime) return;

        FontRenderer fr = mc.fontRendererObj;

        int screenWidth = event.resolution.getScaledWidth();
        int screenHeight = event.resolution.getScaledHeight();

        GlStateManager.pushMatrix();
        GlStateManager.scale(4.0F, 4.0F, 4.0F);
        int textWidth = fr.getStringWidth(displayText);
        int x = (screenWidth / 8) - (textWidth / 2);
        int y = (screenHeight / 8) - 10;
        fr.drawStringWithShadow(displayText, x, y, 0xFF5555);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld == null) {
            displayText = "";
        }
    }
}