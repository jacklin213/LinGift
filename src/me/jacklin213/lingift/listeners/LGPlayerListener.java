package me.jacklin213.lingift.listeners;

import me.jacklin213.lingift.LinGift;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LGPlayerListener implements Listener {
	
	public static LinGift plugin;
	public static PlayerJoinEvent event;

	public LGPlayerListener(LinGift instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
	}
	
	//end of class
}