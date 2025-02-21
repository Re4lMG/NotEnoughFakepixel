package org.ginafro.notenoughfakepixel.features.skyblock.dungeons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;
import org.ginafro.notenoughfakepixel.utils.RenderUtils;

public class MiscDungFeatures {

    private final Minecraft mc = Minecraft.getMinecraft();

    private static String displayText = "";
    private static long endTime = 0;

    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());

        if (!ScoreboardUtils.currentLocation.isDungeon()) return;

        if (message.contains("[BOSS] The Watcher: That will be enough for now.")) {
            if (Configuration.dungeonsBloodReady) {
                showCustomOverlay(EnumChatFormatting.RED + "BLOOD READY!", 2000);
                if (mc.theWorld != null) {
                    mc.theWorld.playSound(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            "note.pling",
                            2.0F,
                            1.0F,
                            false
                    );
                }
            }
        }
    }

    private void showCustomOverlay(String text, int durationMillis) {
        displayText = text;
        endTime = System.currentTimeMillis() + durationMillis;
    }

    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!ScoreboardUtils.currentGamemode.isSkyblock()) return;
        if (!Configuration.dungeonsCroesus) return;
        if (!(event.gui instanceof GuiChest)) return;

        GuiChest chestGui = (GuiChest) event.gui;
        Container container = chestGui.inventorySlots;
        if (!(container instanceof ContainerChest)) return;

        ContainerChest containerChest = (ContainerChest) container;
        String chestName = containerChest.getLowerChestInventory().getDisplayName().getUnformattedText();
        if (!"Croesus".equals(chestName)) return;

        for (Slot slot : chestGui.inventorySlots.inventorySlots) {
            if (slot.inventory == chestGui.mc.thePlayer.inventory) continue;

            ItemStack stack = slot.getStack();
            if (stack != null && stack.getItem() == Items.skull && stack.hasDisplayName()) {
                String lore = stack.getDisplayName();
                int xSlotPos = slot.xDisplayPosition;
                int ySlotPos = slot.yDisplayPosition;

                if (lore.contains(EnumChatFormatting.RED + "No more chests to open!")) {
                    RenderUtils.drawOnSlot(16, xSlotPos, ySlotPos, 0xFFFF0000); // Red
                } else if (lore.contains(EnumChatFormatting.GREEN + "No Chests Opened!")) {
                    RenderUtils.drawOnSlot(16, xSlotPos, ySlotPos, 0xFF00FF00); // Green
                }
            }
        }
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

