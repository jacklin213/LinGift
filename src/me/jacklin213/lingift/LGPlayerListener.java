package me.jacklin213.lingift;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LGPlayerListener implements Listener {
	
	public static LinGift plugin;

	public LGPlayerListener(LinGift instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		plugin.OFH.writeTempFile(player);
	}
}