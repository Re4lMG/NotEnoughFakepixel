package org.ginafro.notenoughfakepixel.features.skyblock.crimson;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.events.PacketReadEvent;
import org.ginafro.notenoughfakepixel.features.skyblock.diana.ParticleProcessor;
import org.ginafro.notenoughfakepixel.utils.RenderUtils;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;
import org.ginafro.notenoughfakepixel.utils.SoundUtils;
import org.ginafro.notenoughfakepixel.variables.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ginafro.notenoughfakepixel.Configuration.*;

public class AshfangHelper {

    private final ParticleProcessor.Waypoint[] waypoints2 = new ParticleProcessor.Waypoint[2];
    private static int blazingSoulsCounter = 0;
    private static int ashfangFollowersCounter = 0;
    private final Queue<S2APacketParticles> particlesQueue = new ConcurrentLinkedQueue<>();
    private String underling = "§c§cAshfang Underling";
    private String follower = "§c§8Ashfang Follower";
    private String acolyte = "§c§9Ashfang Acolyte";
    private String blazingSoul = "Blazing Soul";
    private static Entity currentAshfang;
    private static Entity currentGravityOrb;
    private ParticleProcessor.Waypoint waypoint1;
    private ParticleProcessor.Waypoint waypoint2;
    private static Pattern ashfangHPPattern = Pattern.compile("([0-9]*[.,]?[0-9]*)([Mk])");

    public AshfangHelper() {
        this.waypoints2[0] = new ParticleProcessor.Waypoint("ASHFANG",new int[]{-484, 141, -1015});
        this.waypoints2[1] = new ParticleProcessor.Waypoint("GRAVITYORB",new int[]{-490, -200, -1015});
    }

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        if (Crimson.checkEssentials()) return;
        int[] position = new int[] {Minecraft.getMinecraft().thePlayer.getPosition().getX(), Minecraft.getMinecraft().thePlayer.getPosition().getY(), Minecraft.getMinecraft().thePlayer.getPosition().getZ()};
        if (!Crimson.checkAshfangArea(position)) return;
        if (Configuration.ashfangWaypoint || Configuration.gravityOrbWaypoint) drawWaypoints(event.partialTicks);
        renderEntities(event.partialTicks);
    }

    @SubscribeEvent
    public void onChat(@NotNull ClientChatReceivedEvent e){
        if (Crimson.checkEssentials()) return;
        if (Configuration.ashfangMuteChat) {
            Matcher matcher = Pattern.compile("can only be damaged by").matcher(e.message.getUnformattedText());
            Matcher matcher2 = Pattern.compile("hit you for").matcher(e.message.getUnformattedText());
            if (matcher.find() || matcher2.find()) {
                e.setCanceled(true);
                return;
            }
        }
        if (Configuration.ashfangHurtSound || Configuration.ashfangMuteChat) {
            Matcher matcher3 = Pattern.compile("The Blazing Soul dealt").matcher(e.message.getUnformattedText());
            if (matcher3.find()) {
                int[] position = new int[]{Minecraft.getMinecraft().thePlayer.getPosition().getX(), Minecraft.getMinecraft().thePlayer.getPosition().getY(), Minecraft.getMinecraft().thePlayer.getPosition().getZ()};
                if (Configuration.ashfangHurtSound) SoundUtils.playSound(position, "mob.wither.hurt", 0.7f, 0.7f);
                if (Configuration.ashfangMuteChat) e.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onSoundPacketReceive(PacketReadEvent event) {
        if (Crimson.checkEssentials()) return;
        if (!Configuration.ashfangMuteSound) return;
        Packet packet = event.packet;
        if (packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect soundEffect = (S29PacketSoundEffect) packet;
            String soundName = soundEffect.getSoundName();
            // Remove blaze unhittable sound feature
            if (soundName.equals("mob.bat.hurt")) {
                if (event.isCancelable()) event.setCanceled(true);
            }
        }
    }

    private void renderEntities(float partialTicks) {
        final Color[] newColor = {new Color(255, 0, 0, 100)};
        WorldClient world = Minecraft.getMinecraft().theWorld;
        AtomicInteger blazingSoulCounter = new AtomicInteger();
        AtomicInteger ashfangFollowerCounter = new AtomicInteger();
        boolean gravityFound = false;
        for (int i = 0; i < world.loadedEntityList.size(); i++) {
            Entity entity = world.loadedEntityList.get(i);
            if (entity == null) continue;
            if (entity.getName() == null) continue;
            int[] position = new int[]{entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ()};

            String entityName = entity.getName();
            // GRAVITY ORB
            if (entity instanceof EntityArmorStand) {
                if (!Crimson.checkAshfangArea(position)) continue;
                ItemStack it = ((EntityArmorStand) entity).getEquipmentInSlot(4); // Head slot
                if (it != null && it.getUnlocalizedName().contains("item.skull.char")) {
                    if (!Configuration.gravityOrbWaypoint) continue;
                    if (it.getDisplayName().contains("Burning Eye")) {
                        // ALREADY DONE IN BLAZINGSOUL
                    } else {
                        // Gravity Orb
                        System.out.println("GRAVITY ORB DETECTED:" + it.getDisplayName() + ", " + entity.getUniqueID() + ", " + entity.getPosition().getX() + ", " + entity.getPosition().getY() + ", " + entity.getPosition().getZ());
                        waypoint2 = new ParticleProcessor.Waypoint("GRAVITYORB", position);
                        gravityFound = true;
                        if (currentGravityOrb == null) {
                            SoundUtils.playSound(position, "random.pop", 5.0f, 1.5f);
                        } else if (entity.getUniqueID() != currentGravityOrb.getUniqueID()) {
                            SoundUtils.playSound(position, "random.pop", 5.0f, 1.5f);
                        }
                        currentGravityOrb = entity;
                    }
                } else if (entityName.contains("Ashfang") && !isNameAshfangMinion(entityName) &&
                        (currentAshfang == null || currentAshfang.getUniqueID() != entity.getUniqueID())) {
                    if (!Configuration.ashfangWaypoint) continue;
                    if (!Crimson.checkAshfangArea(position)) continue;
                    waypoint1 = new ParticleProcessor.Waypoint("ASHFANG", position);
                    //System.out.println("ASHFANG DETECTED: " + entity.getUniqueID() + ", " + entity.getPosition().getX() + ", " + entity.getPosition().getY() + ", " + entity.getPosition().getZ());
                    currentAshfang = entity;
                } else if (isNameAshfangMinion(entityName)) {
                    if (!Configuration.ashfangHitboxes) continue;
                    if (entityName.contains(underling)) {
                        newColor[0] = new Color(255, 0, 0, 150);
                    } else if (entityName.contains(acolyte)) {
                        newColor[0] = new Color(0, 0, 255, 150);
                    } else if (entityName.contains(follower)) {
                        ashfangFollowerCounter.getAndIncrement();
                        newColor[0] = new Color(255, 255, 255, 255); // new Color(96, 96, 96, 150);
                    }
                    RenderUtils.renderEntityHitbox(
                            entity,
                            partialTicks,
                            newColor[0],
                            MobDisplayTypes.BLAZE
                    );
                } else if (entityName.contains(blazingSoul)) {
                    if (!Configuration.ashfangHitboxes) continue;
                    blazingSoulCounter.getAndIncrement();
                    newColor[0] = new Color(255, 255, 0, 255);
                    RenderUtils.renderEntityHitbox(
                            entity,
                            partialTicks,
                            newColor[0],
                            MobDisplayTypes.BLAZINGSOUL
                    );
                }
            }
        }
        blazingSoulsCounter = blazingSoulCounter.get();
        if (!gravityFound) waypoint2 = null;
    }

    private ParticleProcessor.Waypoint[] getWaypoints() {
        return waypoints2;
    }

    private boolean isNameAshfangMinion(String name) {
        return name.contains(underling) || name.contains(acolyte) || name.contains(follower);
    }

    private void drawWaypoint (ParticleProcessor.Waypoint waypoint, float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
        if (waypoint == null) return;
        Color colorDrawWaypoint = new Color(255,255,255);
        int offsetBossY = 0;

        if (waypoint.getType().equals("GRAVITYORB")) colorDrawWaypoint = blazingSoulWaypointColor.toJavaColor();
        if (waypoint.getType().equals("ASHFANG")) {
            colorDrawWaypoint = ashfangWaypointColor.toJavaColor();
            offsetBossY = -6;
        }
        //System.out.println("Drawing "+waypoint.getType() + colorDrawWaypoint);
        colorDrawWaypoint = new Color(colorDrawWaypoint.getRed(), colorDrawWaypoint.getGreen(), colorDrawWaypoint.getBlue(), 75);
        AxisAlignedBB bb = new AxisAlignedBB(
                waypoint.getCoordinates()[0] - viewerX,
                waypoint.getCoordinates()[1] - viewerY + offsetBossY,
                waypoint.getCoordinates()[2] - viewerZ,
                waypoint.getCoordinates()[0] + 1 - viewerX,
                waypoint.getCoordinates()[1] + 1 - viewerY + 150,
                waypoint.getCoordinates()[2] + 1 - viewerZ
        ).expand(0.01f, 0.01f, 0.01f);
        //if (waypoint.getType().equals("GRAVITYORB")) GlStateManager.disableCull();
        GlStateManager.disableCull();
        if (waypoint.getType().equals("ASHFANG")) GlStateManager.disableDepth();
        RenderUtils.drawFilledBoundingBox(bb, 1f, colorDrawWaypoint);
        if (waypoint.getType().equals("ASHFANG")) GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
    }

    private void drawWaypoints(float partialTicks){
        if (!BossNotifier.getAshfangScheduled()[0]) {
            drawWaypoint(waypoint1, partialTicks);
        }
        drawWaypoint(waypoint2, partialTicks);
    }

    public static int getBlazingSoulCounter(){
        return blazingSoulsCounter;
    }

    public static int getAshfangFollowersCounter() {
        return ashfangFollowersCounter;
    }

    public static float getAshfangHP() {
        if (currentAshfang == null) return -1.0f;
        Matcher matcher = ashfangHPPattern.matcher(currentAshfang.getName());
        if (matcher.find()) {
            String numberString = matcher.group(1).replace(",", ""); // Eliminar comas
            String ordinalString = matcher.group(2);
            try {
                float number = Float.parseFloat(numberString); // Convertir el número limpio
                if (ordinalString == null) return number;
                if (ordinalString.equals("M")) return number * (float)Math.pow(10, 6);
                if (ordinalString.equals("k")) return number * (float)Math.pow(10, 3);
                return number;
            } catch (NumberFormatException e) {
                System.err.println("Error when converting " + numberString);
                e.printStackTrace();
                return -1.0f;
            }
        }
        return -1.0f;
    }


    public static int getHitsNeeded() {
        if (currentAshfang == null) return -1;
        return (int)Math.ceil(getAshfangHP()/(2*Math.pow(10,6)));
    }

}
