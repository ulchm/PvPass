package com.norcode.bukkit.pvpass;

import com.norcode.bukkit.metalcore.MetalCorePlugin;
import com.norcode.bukkit.pvpass.commands.PVPCommand;
import com.norcode.bukkit.pvpass.util.ConfigAccessor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.kitteh.tag.TagAPI;

import java.text.MessageFormat;

public class PVPass extends MetalCorePlugin {
    private ConfigAccessor messages;

    @Override
    public void onEnable() {
        super.onEnable();
        messages = new ConfigAccessor(this, "messages.yml");
        messages.getConfig();
        messages.saveDefaultConfig();
        messages.getConfig().options().copyDefaults(true);
        messages.saveConfig();
        getServer().getPluginManager().registerEvents(new PVPListener(this), this);
		if (getServer().getPluginManager().getPlugin("TagAPI") != null) {
			getLogger().info("TagAPI Found, colored nametags enabled.");
			getServer().getPluginManager().registerEvents(new TagListener(this), this);
		}
        getServer().getPluginCommand("pvp").setExecutor(new PVPCommand(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    /***
     * Enables pvp for player
     * @param player - Player to have PVP enabled.
     */
    public void EnablePvP(Player player) {
        ConfigurationSection cfg = this.getPlayerData(player);
        if (IsPvPEnabled(player)) {
            cfg.set("pvp-cooldown", System.currentTimeMillis() + (getConfig().getLong("pvp-cooldown", 300) * 1000));
            return;
        }

        cfg.set("pvp-cooldown", System.currentTimeMillis() + (getConfig().getLong("pvp-cooldown", 300) * 1000));
        cfg.set("pvp-enabled", true);
        player.sendMessage(getMsg("pvp-enabled"));
        this.getServer().broadcastMessage(getMsg("pvp-enabled-other", player.getName()));
        player.setMetadata("pvpass-pvp-enabled", new FixedMetadataValue(this, true));
        TagAPI.refreshPlayer(player);
    }

    /***
     * Disables PVP for a player
     * @param player - Player to have PVP disabled.
     */
    public void DisablePvP(Player player) {
        if (!IsPvPEnabled(player)) {
            return;
        }

        Long cooldown = GetPvPCooldown(player);
        if (cooldown > 0) {
            player.sendMessage(getMsg("cooldown-required", cooldown));
            return;
        }

        ConfigurationSection cfg = this.getPlayerData(player);
        cfg.set("pvp-enabled", false);
        player.sendMessage(getMsg("pvp-disabled"));
        player.setMetadata("pvpass-pvp-enabled", new FixedMetadataValue(this, false));
        TagAPI.refreshPlayer(player);
    }

    /***
     * Checks for pvp-enabled on the player and creates it if needed, defaulting to false.
     * @param player - Player to be looked up
     * @return bool - True if PVP is enabled, False if PVP is disabled
     */
    public boolean IsPvPEnabled(Player player) {
        if (!player.hasMetadata("pvpass-pvp-enabled")) {
            ConfigurationSection cfg = this.getPlayerData(player);
            boolean enabled = cfg.getBoolean("pvp-enabled", false);
            player.setMetadata("pvpass-pvp-enabled", new FixedMetadataValue(this, enabled));
        }
        return player.getMetadata("pvpass-pvp-enabled").get(0).asBoolean();
    }

    /***
     * Gets the remaining time in seconds for players PVP Cooldown
     * @param player
     * @return long that is the number of seconds before the cooldown is up.
     */
    public long GetPvPCooldown(Player player) {
        if (player.hasPermission("pvpass.cooldown.override")) { return 0; }
        ConfigurationSection cfg = this.getPlayerData(player);
        Long cooldown =  cfg.getLong("pvp-cooldown", System.currentTimeMillis()-(getConfig().getLong("pvp-cooldown", 300) * 1000));
        if (cooldown < System.currentTimeMillis()) {
            return 0;
        } else {
            return (cooldown - System.currentTimeMillis()) / 1000;
        }
    }

    /***
     * Resets the PvP cooldown for a player
     * @param player - The player to be reset
     */
    public void ResetPvPCooldown(Player player) {
        ConfigurationSection cfg = this.getPlayerData(player);
        cfg.set("pvp-cooldown", System.currentTimeMillis() - (getConfig().getLong("pvp-cooldown", 300) * 1000));
    }


    /***
     * Loads messages from yml and parses for display
     * @param key
     * @param args
     * @return
     */
    public String getMsg(String key, Object... args) {
        String tpl = messages.getConfig().getString(key);
        if (tpl == null) {
            tpl = "[" + key + "] ";
            for (int i = 0; i < args.length; i++) {
                tpl += "{" + i + "}, ";
            }
        }
        return new MessageFormat(ChatColor.translateAlternateColorCodes('&', tpl)).format(args);
    }
}
