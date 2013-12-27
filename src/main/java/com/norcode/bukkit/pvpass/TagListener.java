package com.norcode.bukkit.pvpass;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class TagListener implements Listener {
	private PVPass plugin;

	public TagListener(PVPass plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onReceiveTag(PlayerReceiveNameTagEvent event) {
		if (plugin.IsPvPEnabled(event.getNamedPlayer())) {
			event.setTag(ChatColor.RED + event.getNamedPlayer().getDisplayName());
		}
	}
}
