package org.ginafro.notenoughfakepixel.features.skyblock.slayers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;

import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import java.awt.Color;

public class BlazeAttunements {

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        if (!Configuration.dungeonsStarredMobs) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        if (mc.theWorld == null) return;

        WorldClient world = mc.theWorld;

        world.loadedEntityList.forEach(entity -> {
            if (entity == null) return;
            if (entity.getName() == null) return;
            if (!(entity instanceof EntityArmorStand)) return;

            // Remove formatting codes (including bold) and convert to lowercase
            String unformattedName = entity.getName().replaceAll("(?i)§[0-9A-FK-OR]", "").toLowerCase();

            Color color;
            if (unformattedName.contains("auric")) {
                color = new Color(0x80FFFF00, true); // Yellow with transparency
            } else if (unformattedName.contains("crystal")) {
                color = new Color(0x8018FFFF, true); // Light Blue with transparency
            } else if (unformattedName.contains("ashen")) {
                color = new Color(0x80808080, true); // Gray with transparency
            } else if (unformattedName.contains("spirit")) {
                color = new Color(0x80FFFFFF, true); // White with transparency
            } else {
                return;
            }

            // Create a bounding box of player size (0.6 width, 1.8 height) centered on the entity's position
            double width = 0.6;
            double height = 1.8;
            double halfWidth = width / 2.0;
            AxisAlignedBB bb = new AxisAlignedBB(
                    entity.posX - halfWidth,
                    entity.posY,
                    entity.posZ - halfWidth,
                    entity.posX + halfWidth,
                    entity.posY + height,
                    entity.posZ + halfWidth
            );

            // Render the filled bounding box using RenderUtils
            drawFilledBB(bb, color.getRGB());
        });
    }
    private void drawFilledBB(AxisAlignedBB bb, int color) {
        // Extract alpha, red, green, and blue components
        float a = (float) ((color >> 24) & 0xFF) / 255F;
        float r = (float) ((color >> 16) & 0xFF) / 255F;
        float g = (float) ((color >> 8) & 0xFF) / 255F;
        float b = (float) (color & 0xFF) / 255F;

        // Set up OpenGL state for transparency and disable textures for a flat color
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(r, g, b, a);

        // Use the Tessellator to draw the box as quads
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        // Bottom face
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();

        // Top face
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();

        // You can also add the side faces here if you wish to fill the entire box.
        tessellator.draw();

        // Restore OpenGL state
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
}
