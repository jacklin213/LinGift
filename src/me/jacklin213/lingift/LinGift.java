package me.jacklin213.lingift;

//java stuff
import info.somethingodd.OddItem.OddItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import me.jacklin213.lingift.utils.ConfigHandler;
import me.jacklin213.lingift.utils.OfflineFileHandler;
import me.jacklin213.lingift.utils.Updater;
import me.jacklin213.lingift.utils.Updater.UpdateResult;
import me.jacklin213.lingift.utils.Updater.UpdateType;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class LinGift extends JavaPlugin {
	
	public static LinGift plugin;
	public static Logger log;
	
	public LGListener listener = new LGListener(this);
	public LinGiftCommand LGC = new LinGiftCommand(this);
	public Updater updater;
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
	String[] commandAliases = {"gift", "send", "linsend"};
	
	@Override
	public void onDisable() {
		log.info(String.format("Disabled Version %s", getDescription().getVersion()));
	}

	@Override
	public void onEnable() {
		log = getLogger();
		//Make offline.txt
		this.OFH = new OfflineFileHandler(new File(getDataFolder() + File.separator + "offline.txt"), this);
		//Create config and get values
		configHandler = new ConfigHandler(this);
		
		//Setup OddItem
		setupOddItem();
		//If using economy, check for vault
		if (useEco) {
			if (!setupEconomy()) {
				log.severe("Disabling due to no Vault dependency found!");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}
		//Update Checking
		Boolean updateCheck = Boolean.valueOf(getConfig().getBoolean("UpdateCheck"));
		Boolean autoUpdate = Boolean.valueOf(getConfig().getBoolean("AutoUpdate"));
		
		this.updateCheck(updateCheck, autoUpdate, 43468);
		//Register events/commands
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(listener, this);
		getCommand("lingift").setExecutor(LGC);
		getCommand("lingift").setAliases(Arrays.asList(commandAliases));

		// Print that the plugin has been enabled!
		log.info(String.format("Version: %s by jacklin213 & (Former Author) nitnelave is now enabled!", getDescription().getVersion()));
	}

	private Boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	private void setupOddItem() {
		// Check to see if plugins folder has OddItem
		OI = (OddItem) getServer().getPluginManager().getPlugin("OddItem");
		if (OI != null) {
			log.info("Successfully connected with OddItem");
		}
	}
	
	private void updateCheck(boolean updateCheck, boolean autoUpdate, int ID) {
		if (updateCheck && !autoUpdate) {
			updater = new Updater(this, ID, this.getFile(), UpdateType.NO_DOWNLOAD, true);
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			    log.info("New version available! " + updater.getLatestName());
			} else if (updater.getResult() == UpdateResult.NO_UPDATE) {
				log.info(String.format("You are running the latest version of %s", getDescription().getName()));
			}
		} else if (autoUpdate && !updateCheck) {
			updater = new Updater(this, ID, this.getFile(), UpdateType.NO_VERSION_CHECK, true);
		} else if (autoUpdate && updateCheck) {
			updater = new Updater(this, ID, this.getFile(), UpdateType.DEFAULT, true);
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			    log.info("New version available! " + updater.getLatestName());
			}
			if (updater.getResult() == UpdateResult.NO_UPDATE) {
				log.info(String.format("You are running the latest version of %s", getDescription().getName()));
			}
		}
	}
}