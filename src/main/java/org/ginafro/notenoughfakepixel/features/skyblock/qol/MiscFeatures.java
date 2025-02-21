package org.ginafro.notenoughfakepixel.features.skyblock.qol;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.utils.ScoreboardUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class MiscFeatures {
    ArrayList<Block> flowerPlaceable = new ArrayList<>(Arrays.asList(
            Blocks.grass,
            Blocks.dirt,
            Blocks.flower_pot,
            Blocks.tallgrass,
            Blocks.double_plant
    ));

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!ScoreboardUtils.currentGamemode.isSkyblock() || Minecraft.getMinecraft().thePlayer != event.entityPlayer) return;
        ItemStack item = event.entityPlayer.getHeldItem();
        if (item == null) return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(event.pos).getBlock();

            if (flowerPlaceable.contains(block)) {
                if (Configuration.qolBlockPlacingItems && item.getDisplayName().contains("Flower of Truth")) {
                    event.setCanceled(true);
                }
                if (Configuration.qolBlockPlacingItems && item.getDisplayName().contains("Spirit Sceptre")) {
                    event.setCanceled(true);
                }
            }

            if (Configuration.qolBlockPlacingItems && item.getItem() == Item.getItemFromBlock(Blocks.hopper) && item.getDisplayName().contains("Weird Tuba")) {
                event.setCanceled(true);
            }
        }
    }
}
