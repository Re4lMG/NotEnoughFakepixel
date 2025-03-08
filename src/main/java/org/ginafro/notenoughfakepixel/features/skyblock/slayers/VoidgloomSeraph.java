package org.ginafro.notenoughfakepixel.features.skyblock.slayers;

import net.minecraft.block.BlockBeacon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.events.Handlers.ScoreboardHandler;
import org.ginafro.notenoughfakepixel.events.PacketReadEvent;
import org.ginafro.notenoughfakepixel.utils.RenderUtils;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;
import org.ginafro.notenoughfakepixel.utils.SoundUtils;
import org.ginafro.notenoughfakepixel.utils.Waypoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.ginafro.notenoughfakepixel.Configuration.slayerBeaconColor;

public class VoidgloomSeraph {
    ArrayList<EntityFallingBlock> fallingBlocks = new ArrayList<>();
    ArrayList<Waypoint> waypoints = new ArrayList<>();
    public static boolean isBoss = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.side == net.minecraftforge.fml.relauncher.Side.CLIENT) {
            List<String> sidebarLines = ScoreboardHandler.getSidebarLines();

            // Loop through the sidebar lines and check for "Slay the boss!"
            for (String line : sidebarLines) {
                if (line.contains("Slay the boss!")) {
                    isBoss = true;
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (checkEssentials()) return;
        if (isBoss) {
        AxisAlignedBB bb = Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().expand(20, 20, 20);
        Collection<EntityFallingBlock> entities = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityFallingBlock.class, bb);
        ArrayList<Boolean> fallingBlocksFound = new ArrayList<>(fallingBlocks.size());
        for (int i = 0; i < fallingBlocks.size(); i++) {
            fallingBlocksFound.add(Boolean.FALSE);
        }
        for (EntityFallingBlock entity : entities) {
            if (fallingBlocks.contains(entity) ) {
                fallingBlocksFound.set(fallingBlocks.indexOf(entity), true);
                continue;
            }
            //System.out.println(entity.getBlock().getBlock().getClass().getName());
            //System.out.println(entity.getBlock());
            if (!(entity.getBlock().getBlock() instanceof BlockBeacon)) continue;
            fallingBlocks.add(entity);
            fallingBlocksFound.add(true);
        }
        ArrayList<Integer> indexToRemove = new ArrayList<>();
        for (int i = 0; i < fallingBlocksFound.size(); i++) {
            if (fallingBlocksFound.get(i)) continue;
            //System.out.println(fallingBlocks.get(i).getDisplayName());
            int[] coords = new int[]{(int)Math.floor(fallingBlocks.get(i).posX), (int)Math.floor(fallingBlocks.get(i).posY), (int)Math.floor(fallingBlocks.get(i).posZ)};
            waypoints.add(new Waypoint("BEACON",coords));
            SoundUtils.playSound(coords,"random.pop", 3.0f, 0.5f);
            indexToRemove.add(i);
        }
        for (Integer i : indexToRemove) {
            fallingBlocks.remove((int)i);
            fallingBlocksFound.remove((int)i);
        }
    }
    }

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        if (checkEssentials()) return;
        if (!Configuration.slayerShowBeaconPath) return;
        if (isBoss) {
        drawWaypoints(event.partialTicks);
        }
    }

    @SubscribeEvent
    public void onSoundPacketReceive(PacketReadEvent event) {
        if (checkEssentials()) return;
        if (!Configuration.slayerShowBeaconPath) return;
        Packet packet = event.packet;
        if (packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect soundEffect = (S29PacketSoundEffect) packet;
            String soundName = soundEffect.getSoundName();
            int[] coordsSound = new int[] {(int)Math.floor(((S29PacketSoundEffect) packet).getX()), (int)Math.floor(((S29PacketSoundEffect) packet).getY()),(int)Math.floor(((S29PacketSoundEffect) packet).getZ())};
            //System.out.println(soundName);
            // Remove blaze unhittable sound feature
            if (soundName.equals("random.break")) {
                Waypoint waypointToDelete = Waypoint.getClosestWaypoint(waypoints, coordsSound);
                if (waypointToDelete != null) waypoints.remove(waypointToDelete);
                waypoints.clear();
            } else if (soundName.equals("note.bass")) {
                fallingBlocks.clear();
                waypoints.clear();
            }
        }
    }

    /*@SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        System.out.println("BLOCKCHANGE EVENT");
    }*/

    @SubscribeEvent()
    public void onWorldUnload(WorldEvent.Unload event) {
        if (Configuration.slayerShowBeaconPath) {
            fallingBlocks.clear();
            waypoints.clear();
        }
    }

    private void drawWaypoints(float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
        for (Waypoint waypoint : waypoints) {
            if (waypoint == null || waypoint.isHidden()) continue;
            Color colorDrawWaypoint = slayerBeaconColor.toJavaColor();
            colorDrawWaypoint = new Color(colorDrawWaypoint.getRed(), colorDrawWaypoint.getGreen(), colorDrawWaypoint.getBlue(), 150);
            AxisAlignedBB bb = new AxisAlignedBB(
                    waypoint.getCoordinates()[0] - viewerX,
                    waypoint.getCoordinates()[1] - viewerY - 1,
                    waypoint.getCoordinates()[2] - viewerZ,
                    waypoint.getCoordinates()[0] + 1 - viewerX,
                    waypoint.getCoordinates()[1] + 1 - viewerY + 150,
                    waypoint.getCoordinates()[2] + 1 - viewerZ
            ).expand(0.01f, 0.01f, 0.01f);
            GlStateManager.disableCull();
            RenderUtils.drawFilledBoundingBox(bb, 1f, colorDrawWaypoint);
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
        }
    }

    private static boolean checkEssentials(){
        return (Minecraft.getMinecraft().thePlayer == null) ||
                (!ScoreboardUtils.currentGamemode.isSkyblock()) ||
                (!ScoreboardUtils.currentLocation.isEnd());
    }


    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (Configuration.slayerShowBeaconPath && ScoreboardUtils.currentGamemode.isSkyblock() && ScoreboardUtils.currentLocation.isEnd()) {
            String message = StringUtils.stripControlCodes(event.message.getUnformattedText());

            if (message.contains("SLAYER QUEST COMPLETE!")) {
                fallingBlocks.clear();
                waypoints.clear();
            }
        }
    }
}
