package com.norcode.bukkit.pvpass;

import com.norcode.bukkit.playerid.PlayerID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PVPListener implements Listener {
    private final PVPass plugin;

    public PVPListener(PVPass plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled=true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {

            Player attacker = null;

            if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    attacker = (Player) ((Projectile) event.getDamager()).getShooter();
                }
            }
            if (attacker != null) {
                Player victim = (Player) event.getEntity();
                if (!plugin.IsPvPEnabled(victim)) {
                    event.setCancelled(true);
                } else if (plugin.IsPvPEnabled(victim)) {
                    plugin.EnablePvP(attacker);
                    plugin.EnablePvP(victim);
                    return;
                }

            }
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onEntityCombustEntityEvent(EntityCombustByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player attacker = null;
            if (event.getCombuster() instanceof Player) {
                attacker = (Player) event.getCombuster();
            } else if (event.getCombuster() instanceof Projectile) {
                if (((Projectile) event.getCombuster()).getShooter() instanceof Player) {
                    attacker = (Player) ((Projectile) event.getCombuster()).getShooter();
                }
            }
            if (attacker != null) {
                Player victim = (Player) event.getEntity();
                if (!plugin.IsPvPEnabled(victim)) {
                    event.setCancelled(true);
                } else if (plugin.IsPvPEnabled(victim)) {
                    plugin.EnablePvP(attacker);
                    plugin.EnablePvP(victim);
                    return;
                }
            }
        }
    }

	private static Set<PotionEffectType> harmfulPotionEffectTypes = new HashSet<PotionEffectType>();
	static {
		harmfulPotionEffectTypes.add(PotionEffectType.BLINDNESS);
		harmfulPotionEffectTypes.add(PotionEffectType.CONFUSION);
		harmfulPotionEffectTypes.add(PotionEffectType.HARM);
		harmfulPotionEffectTypes.add(PotionEffectType.POISON);
		harmfulPotionEffectTypes.add(PotionEffectType.SLOW);
		harmfulPotionEffectTypes.add(PotionEffectType.SLOW_DIGGING);
		harmfulPotionEffectTypes.add(PotionEffectType.HUNGER);
		harmfulPotionEffectTypes.add(PotionEffectType.WEAKNESS);
		harmfulPotionEffectTypes.add(PotionEffectType.WITHER);
	}

	@EventHandler(ignoreCancelled=true)
    public void onPotionSplashEvent(PotionSplashEvent event) {

        Player attacker = null;
        Boolean affectedPlayer = false;
        Boolean isHarmful = false;
        ThrownPotion potion = event.getPotion();

        //get the shooter and verify it was a player
        if (!(potion.getShooter() instanceof Player)) {
            return;
        }

		attacker = (Player) potion.getShooter();

		// Lets determine if it's harmful first. if its not we don't care.
		for (PotionEffect e: potion.getEffects()) {
			if (harmfulPotionEffectTypes.contains(e.getType())) {
				isHarmful = true;
				break;
			}
		}
		plugin.getServer().getLogger().info("Potion is" + (isHarmful ? "" : " not") + " harmful.");

		if (!isHarmful) return;

		//loop through affected entities,  figure out if any have pvp enabled (other then attacker)


		for (LivingEntity e: event.getAffectedEntities()) {
			if (e instanceof Player && !e.equals(attacker)) {
				if (plugin.IsPvPEnabled((Player) e)) {
					plugin.EnablePvP((Player) e);
				} else {
					event.setIntensity(e, 0);
				}
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if  ((plugin.IsPvPEnabled(player)) && ((plugin.GetPvPCooldown(player) - System.currentTimeMillis() * (plugin.getConfig().getLong("pvp-cooldown", 300) * 1000)) > (plugin.getConfig().getLong("disconnect-slay-time", 15)))) {
            plugin.getServer().broadcastMessage(plugin.getMsg("logged-out-combat", player.getName()));
            final Location loc = event.getPlayer().getLocation();
            for (int i=0;i<5;i++) {
                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        loc.getWorld().strikeLightningEffect(loc);
                    }
                }, i*5);
            }
            player.setHealth(0);
            ConfigurationSection cfg = PlayerID.getPlayerData(plugin.getName(), player);
            cfg.set("pvp-join-message", plugin.getMsg("player-logged-out-combat"));
            PlayerID.savePlayerData(plugin.getName(), player, cfg);
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ConfigurationSection cfg = PlayerID.getPlayerData(plugin.getName(), player);
        String message = cfg.getString("pvp-join-message");

        if (message != null) {
            player.sendMessage(message);
            cfg.set("pvp-join-message", null);
            PlayerID.savePlayerData(plugin.getName(), player, cfg);
        }

        if (plugin.IsPvPEnabled(player)){
            player.sendMessage(plugin.getMsg("join-enabled"));
            return;
        }
        player.sendMessage(plugin.getMsg("join-disabled"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getKiller() != null) {
			plugin.ResetPvPCooldown(player);
            plugin.DisablePvP(player);
        }
    }

}
