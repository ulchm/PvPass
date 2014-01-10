package com.norcode.bukkit.pvpass.commands;

import com.norcode.bukkit.metalcore.command.CommandError;
import com.norcode.bukkit.pvpass.PVPass;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class OnCommand extends BaseCommand {
    public OnCommand(PVPass plugin) {
        super(plugin, "on", new String[] {"yes","true","1","on","y","enable"}, "pvpass.command.enable", null);
    }

    @Override
    protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
        plugin.EnablePvP((Player) commandSender);
    }
}
