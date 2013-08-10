package me.jacklin213.lingift;

//java stuff
import info.somethingodd.OddItem.OddItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import me.jacklin213.lingift.utils.ConfigHandler;
import me.jacklin213.lingift.utils.OfflineFileHandler;
import me.jacklin213.lingift.utils.UpdateChecker;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class LinGift extends JavaPlugin {
	
	public static LinGift plugin;
	
	public Logger log = Logger.getLogger("Minecraft");
	
	public LGPlayerListener LGPL = new LGPlayerListener(this);
	public LinGiftCommand LGC = new LinGiftCommand(this);
	public UpdateChecker updateChecker;
	/**
	 * Contains all the methods used to send items
	 */
	public SendMethod SM = new SendMethod(this);
	/**
	 * OfflineFileHandler - Handles any method used to create/write to the offlineFile
	 */
	public OfflineFileHandler OFH;
	/**
	 * Configuration Handler
	 */
	public ConfigHandler configHandler;
	public static OddItem OI = null;
	public static Economy economy = null;
	
	public int maxRadius = 0;
	public boolean allowOfflineSend = false;
	public boolean crossWorldSend = true;
	public boolean useEco = false;
	public double fee = 0;
	 // tools that can be damaged
	public ArrayList<Integer> tools = new ArrayList<Integer>(Arrays.asList(
			256, 257, 258, 259, 267, 268, 269, 270, 271, 272, 273, 274, 275,
			276, 277, 278, 279, 283, 284, 285, 286, 290, 291, 292, 293, 294,
			298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310,
			311, 312, 313, 314, 315, 316, 317, 346, 359));
	List<String> commandAliases = new ArrayList<String>(Arrays.asList("gift", "send", "linsend"));
	
	@Override
	public void onDisable() {
		log.info(String.format("[%s] Disabled Version %s", getDescription()
				.getName(), getDescription().getVersion()));
	}

	@Override
	public void onEnable() {
		
		// Sets the pluginFolder to the data folder
		String pluginFolder = this.getDataFolder().getAbsolutePath();
		(new File(pluginFolder)).mkdirs();
		//Make offline.txt
		this.OFH = new OfflineFileHandler(new File(pluginFolder + File.separator + "offline.txt"), this);
		//Create config and get values
		configHandler = new ConfigHandler(this);
		configHandler.setConfigValues();
		
		//Setup OddItem
		setupOddItem();
		//If using economy, check for vault
		if (useEco) {
			if (!setupEconomy()) {
				log.info(String.format("[%s] - Disabled due to no Vault dependency found!",
						getDescription().getName()));
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}
		//Update Checking
		Boolean updateCheck = Boolean.valueOf(getConfig().getBoolean("UpdateCheck"));
		
		this.updateChecker = new UpdateChecker(this, "http://dev.bukkit.org/server-mods/lingift/files.rss");
				
		if ((updateCheck) && (this.updateChecker.updateNeeded())) {
			log.info(String.format("[%s] A new update is avalible, Version: %s", getDescription().getName(), this.updateChecker.getVersion()));
			log.info(String.format("[%s] Get it now from: %s", getDescription().getName(), this.updateChecker.getLink()));
		}
		//Register events/commands
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(LGPL, this);
		getCommand("lingift").setExecutor(LGC);
		getCommand("lingift").setAliases(commandAliases);

		// Print that the plugin has been enabled!
		log.info(String.format("[%s] Version: %s by jacklin213 & (Former Author) nitnelave is enabled!", 
				getDescription().getName(), getDescription().getVersion()));
	}

	private Boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	private void setupOddItem(){
		// Check to see if plugins folder has OddItem
		OI = (OddItem) getServer().getPluginManager().getPlugin("OddItem");
		if (OI != null) {
			log.info(String.format("[%s] Successfully connected with OddItem", getDescription().getName()));
		}
	}
}