package org.ginafro.notenoughfakepixel.features.skyblock.slayers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.utils.RenderUtils;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlazeAttunements {

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (!Configuration.slayerBlazeAttunements) return;
        if (!ScoreboardUtils.currentGamemode.isSkyblock() || !ScoreboardUtils.currentLocation.isCrimson()) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                String displayName = armorStand.getDisplayName().getUnformattedText();

                Matcher matcher = COLOR_PATTERN.matcher(displayName);
                if (matcher.find()) {
                    String attunement = matcher.group().toUpperCase();

                    Entity entityBelow = getEntityBelow(armorStand, 2.5f);
                    if (entityBelow instanceof EntityLivingBase) {
                        EntityLivingBase livingEntity = (EntityLivingBase) entityBelow;

                        boolean isValidEntity = livingEntity instanceof EntityBlaze ||
                                livingEntity instanceof EntityPigZombie ||
                                (livingEntity instanceof EntitySkeleton && ((EntitySkeleton) livingEntity).getSkeletonType() == 1);

                        if (isValidEntity) {
                            boolean allowed = false;
                            if (livingEntity instanceof EntitySkeleton && ((EntitySkeleton) livingEntity).getSkeletonType() == 1) { // Wither Skeleton
                                allowed = attunement.equals("SPIRIT") || attunement.equals("CRYSTAL");
                            } else if (livingEntity instanceof EntityPigZombie) { // Pigman
                                allowed = attunement.equals("ASHEN") || attunement.equals("AURIC");
                            } else if (livingEntity instanceof EntityBlaze) {
                                allowed = true;
                            }

                            if (allowed) {
                                int color = getColorForAttunement(attunement);

                                double x = entityBelow.lastTickPosX + (entityBelow.posX - entityBelow.lastTickPosX) * event.partialTicks;
                                double y = entityBelow.lastTickPosY + (entityBelow.posY - entityBelow.lastTickPosY) * event.partialTicks;
                                double z = entityBelow.lastTickPosZ + (entityBelow.posZ - entityBelow.lastTickPosZ) * event.partialTicks;

                                AxisAlignedBB aabb = entityBelow.getEntityBoundingBox()
                                        .offset(x - entityBelow.posX, y - entityBelow.posY, z - entityBelow.posZ)
                                        .expand(0.1, 0.1, 0.1);
                                RenderUtils.drawFilledBoundingBoxEntity(aabb, 0.8f, new Color(color, true), event.partialTicks);
                            }
                        }
                    }
                }
            }
        }
    }

    private static final Pattern COLOR_PATTERN = Pattern.compile("ASHEN|SPIRIT|CRYSTAL|AURIC");

    private static int getColorForAttunement(String attunement) {
        switch (attunement) {
            case "ASHEN":
                return Color.DARK_GRAY.getRGB();
            case "SPIRIT":
                return Color.WHITE.getRGB();
            case "CRYSTAL":
                return Color.CYAN.getRGB();
            case "AURIC":
                return Color.YELLOW.getRGB();
            default:
                return -1;
        }
    }

    private static Entity getEntityBelow(Entity armorStand, float height) {
        double x = armorStand.posX;
        double y = armorStand.posY - height;
        double z = armorStand.posZ;
        AxisAlignedBB aabb = new AxisAlignedBB(
                x - 0.5, y, z - 0.5, // Lower bounds
                x + 0.5, armorStand.posY, z + 0.5 // Upper bounds
        );

        // Get the first valid entity below the armor stand
        List<Entity> entitiesBelow = armorStand.worldObj.getEntitiesWithinAABBExcludingEntity(armorStand, aabb);
        for (Entity entity : entitiesBelow) {
            if (entity instanceof EntityBlaze || entity instanceof EntityPigZombie ||
                    (entity instanceof EntitySkeleton && ((EntitySkeleton) entity).getSkeletonType() == 1)) {
                return entity;
            }
        }
        return null;
    }
}