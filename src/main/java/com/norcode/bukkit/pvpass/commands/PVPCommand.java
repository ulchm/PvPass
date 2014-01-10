package com.norcode.bukkit.pvpass.commands;

import com.norcode.bukkit.metalcore.command.CommandError;
import com.norcode.bukkit.pvpass.PVPass;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class PVPCommand extends BaseCommand {
    public PVPCommand(PVPass plugin) {
        super(plugin, "pvp", new String[] {}, "pvpass.command", null);
        registerSubcommand(new OnCommand(plugin));
        registerSubcommand(new OffCommand(plugin));
    }

    @Override
    protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
        Player player = (Player) commandSender;
        String message = "Your PvP status is: ";
        if (plugin.IsPvPEnabled(player)) {
            Long seconds = plugin.GetPvPCooldown(player);
            message = plugin.getMsg("status-enabled", seconds);
        } else {
            message = plugin.getMsg("status-disabled");
        }
        player.sendMessage(message);
    }
}
