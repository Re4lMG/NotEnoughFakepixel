package org.ginafro.notenoughfakepixel.features.skyblock.qol;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.NotEnoughFakepixel;
import org.ginafro.notenoughfakepixel.utils.FileUtils;
import org.ginafro.notenoughfakepixel.utils.RenderUtils;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;
import org.ginafro.notenoughfakepixel.variables.Gamemode;
import org.ginafro.notenoughfakepixel.variables.Location;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FairySouls {

    private String island;
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e){
        if(ScoreboardUtils.currentGamemode != Gamemode.SKYBLOCK) return;
        if(!Configuration.fairysouls) return;
        Location currentIsland = ScoreboardUtils.currentLocation;
        List<String> souls = new ArrayList<>();
            if(currentIsland == Location.HUB){
                souls = FileUtils.getAllSouls().locations.get("hub");
                island = "hub";
            }
            if(currentIsland == Location.SPIDERS_DEN){
                souls = FileUtils.getAllSouls().locations.get("spider");
                island = "spider";
            }
            if(currentIsland == Location.CRIMSON_ISLE){
                souls = FileUtils.getAllSouls().locations.get("crimson");
                island = "crimson";
            }
            if(currentIsland == Location.THE_END){
                souls = FileUtils.getAllSouls().locations.get("end");
                island = "end";
            }
            if(currentIsland == Location.PARK){
                souls = FileUtils.getAllSouls().locations.get("park");
                island = "park";
            }
            if(currentIsland == Location.BARN){
                souls = FileUtils.getAllSouls().locations.get("farming");
                island = "farming";
            }
            if(currentIsland == Location.GOLD_MINE){
                souls = FileUtils.getAllSouls().locations.get("gold");
                island = "gold";
            }
            if(currentIsland == Location.DUNGEON_HUB){
                souls = FileUtils.getAllSouls().locations.get("dungeon_hub");
                island = "dungeon_hub";
            }
            if(currentIsland == Location.JERRY){
                souls = FileUtils.getAllSouls().locations.get("winter");
                island = "winter";
            }
        List<String> renderedSouls = checkSouls(souls);
        for(String s : renderedSouls) {
            String[] coords = s.split(",");
            Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
            double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * e.partialTicks ;
            double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * e.partialTicks;
            double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * e.partialTicks;
            int x = Integer.parseInt(coords[0].trim());
            int y = Integer.parseInt(coords[1].trim());
            int z = Integer.parseInt(coords[2].trim());
            GlStateManager.color(1f,1f,1f,1f);
            AxisAlignedBB aab = new AxisAlignedBB(
                    x - viewerX + 0.2,
                    y - viewerY - 1,
                    z - viewerZ + 0.2 ,
                    x + 0.8 - viewerX ,
                    y - viewerY + 256,
                    z  + 0.8 - viewerZ
            ).expand(0.01f, 0.01f, 0.01f);
            OneColor c = Configuration.fairySoulColor;
            c.setAlpha(102);
            RenderUtils.highlightBlock(new BlockPos(x, y, z), c.toJavaColor(), true, e.partialTicks);
            GlStateManager.disableCull();
            Color fairySoulC = Configuration.fairySoulColor.toJavaColor();
            Color fairySoulColor = new Color(fairySoulC.getRed(), fairySoulC.getGreen(), fairySoulC.getBlue(), 102);
            GlStateManager.disableDepth();
            RenderUtils.renderBeaconBeam(new BlockPos(x, y, z),fairySoulColor.getRGB(),1.0f,e.partialTicks);
//            RenderUtils.drawFilledBoundingBox(aab, 1f, fairySoulColor);
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e){
        if(StringUtils.stripControlCodes(e.message.getUnformattedText()).equalsIgnoreCase("SOUL! You found a Fairy Soul!")
        || StringUtils.stripControlCodes(e.message.getFormattedText()).equalsIgnoreCase("You already found that Fairy Soul!")
        ){
            System.out.println("Chat Recieved");
            String soul = null;
            double closestDistSq = 5 * 5;
            FairySoulData soulData = FileUtils.getAllSouls();
            FairySoulData soulData1 = FileUtils.getSoulData();
            if (island == null || soulData1.locations == null) {
                System.out.println("Island or soulData.locations is null");
                return;
            }
            List<String> souls = soulData.locations.get(island);
            List<String> gainedSouls = soulData1.locations.get(island);
            for (String s : souls) {
                String[] s1 = s.split(",");
                BlockPos pos = new BlockPos(Integer.parseInt(s1[0]), Integer.parseInt(s1[1]), Integer.parseInt(s1[2]));
                double distSq = pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition());

                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    soul = s;
                }
            }
            if(soul != null && !gainedSouls.contains(soul)){
                gainedSouls.add(soul);
                soulData1.locations.put(island,gainedSouls);
                soulData1.soulCount++;
                FileUtils.saveSoulData(soulData1);
            }
        }
    }
    private List<String> checkSouls(List<String> shownSouls) {
        List<String> souls = new ArrayList<>();
        FairySoulData data = FileUtils.getSoulData();
            if(data != null) {
                if (data.locations != null) {
                    if (data.locations.get(island) != null) {
                        for (String s : shownSouls) {
                            if (!data.locations.get(island).contains(s)) {
                                souls.add(s);
                            }
                        }
                    }
                }
            }
            if(souls.isEmpty()){
                return shownSouls;
            }
        return souls;
    }

}
