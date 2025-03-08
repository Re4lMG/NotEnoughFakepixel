package org.ginafro.notenoughfakepixel.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CopyCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "copytoclipboard";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/copytoclipboard <text>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) return;

        String text = String.join(" ", args);
        copyToClipboard(text);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEF] Copied to clipboard!"));
    }
}

