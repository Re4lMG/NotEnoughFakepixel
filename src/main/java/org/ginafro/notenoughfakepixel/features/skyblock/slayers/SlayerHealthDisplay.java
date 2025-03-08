package org.ginafro.notenoughfakepixel.features.skyblock.slayers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SlayerHealthDisplay {

    private final Minecraft mc = Minecraft.getMinecraft();
    private String displayText = "";

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        // Reset display text
        displayText = "";

        // Get the client player's username (strip formatting and ranks)
        String clientUsername = StringUtils.stripControlCodes(mc.thePlayer.getDisplayName().getUnformattedText());

        // Scan all entities in the world
        for (Entity entity : mc.theWorld.loadedEntityList) {
            String nametag = entity.getDisplayName().getUnformattedText();

            // Check if the entity's nametag contains "Spawned by: (client player name)"
            if (nametag.contains("Spawned by: " + clientUsername)) {
                // Get the entity below this one
                AxisAlignedBB boundingBox = entity.getEntityBoundingBox().expand(0, -1, 0); // Look directly below
                for (Entity belowEntity : mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, boundingBox)) {
                    String bossNametag = belowEntity.getDisplayName().getFormattedText();

                    // Use the original nametag of the entity below as the boss name
                    displayText = bossNametag;
                    break; // Only use the first entity below
                }
            }
        }

        // If we have a valid display text, render it
        if (!displayText.isEmpty()) {
            FontRenderer fr = mc.fontRendererObj;
            int screenWidth = event.resolution.getScaledWidth();
            int screenHeight = event.resolution.getScaledHeight();

            // Save current GL state
            GlStateManager.pushMatrix();

            // Scale and position
            GlStateManager.scale(4.0F, 4.0F, 4.0F);
            int textWidth = fr.getStringWidth(displayText);
            int x = (screenWidth / 8) - (textWidth / 2);
            int y = (screenHeight / 8) - 30;

            // Draw with shadow
            fr.drawStringWithShadow(displayText, x, y, 0xFF5555);

            // Restore GL state
            GlStateManager.popMatrix();
        }
    }
}
