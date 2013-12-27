package com.norcode.bukkit.pvpass.commands;

import com.norcode.bukkit.playerid.command.CommandError;
import com.norcode.bukkit.pvpass.PVPass;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class OffCommand extends BaseCommand {
    public OffCommand(PVPass plugin) {
        super(plugin, "off", new String[] {"no","false","0","off","n","disable"}, "pvpass.command.disable", null);
    }

    @Override
    protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
        plugin.DisablePvP((Player) commandSender);
    }
}
