package org.ginafro.notenoughfakepixel.features.skyblock.dungeons.score;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.features.skyblock.dungeons.DungeonManager;

public class SPlusNotifier {

    private static boolean remindedSPlus = true;
    private static boolean remindedUnreachable = true;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (!DungeonManager.checkEssentials() ||
                e.phase == TickEvent.Phase.END ||
                Minecraft.getMinecraft().thePlayer == null ||
                Minecraft.getMinecraft().theWorld == null) return;
        if (ScoreManager.currentSeconds > 0 && ScoreManager.currentSeconds <= 8) {
            remindedSPlus = false;
            remindedUnreachable = false;
        }
        reminderSPlus();
        reminderUnreachable();
    }

    public static void reminderSPlus() {
        if (remindedSPlus) return;
        if (!Configuration.dungeonsSPlusNotifier && !Configuration.dungeonsSPlusMessage) return;

        if (ScoreManager.getSecretPercentage() >= ScoreManager.getRequiredSecretNeeded() && ScoreManager.getRequiredSecretNeeded() != -1) {
            if (Configuration.dungeonsSPlusNotifier) {
                Minecraft.getMinecraft().ingameGUI.displayTitle(EnumChatFormatting.GOLD + "S+", "", 2, 100, 2);
            }
            if (Configuration.dungeonsSPlusMessage) {
                String customMessage = Configuration.dungeonsSPlusCustom.trim();
                if (!customMessage.isEmpty()) {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc " + customMessage);
                } else {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc [NEF] S+ virtually reached, get 100% completion and enter portal!");
                }
            }
            remindedSPlus = true;
        }
    }

    public void reminderUnreachable() {
        if (remindedUnreachable) return;
        if (!Configuration.dungeonsSPlusNotifier && !Configuration.dungeonsSPlusMessage) return;

        if (ScoreManager.getRequiredSecretNeeded() == -1) {
            if (Configuration.dungeonsSPlusMessage) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc [NEF] S+ may not be reached by secrets only, do crypts or restart");
            }
            remindedUnreachable = true;
            return;
        }
    }
}
