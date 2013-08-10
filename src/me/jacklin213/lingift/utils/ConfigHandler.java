package me.jacklin213.lingift.utils;

import java.io.File;

import me.jacklin213.lingift.LinGift;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;


public class ConfigHandler {
	
	public static LinGift plugin;
	
	private FileConfiguration config;
	private File configFile;
	int maxRadius = 0;
	boolean allowOfflineSend = false;
	double fee = 0;
	boolean crossWorldSend = true;
	boolean useEco = false;
	
	/**
	 * Constuctor for ConfigHandler - Runs the createConfig() method.
	 */
	public ConfigHandler(LinGift instance){
		plugin = instance;
		createConfig();
	}
	
	/**
	 * Copys configuration from defaults and makes it into a file
	 */
	public void createConfig(){
		File configFile = new File(plugin.getDataFolder() + File.separator
				+ "config.yml");
		if (!configFile.exists()) {
			// Tells console its creating a config.yml
			plugin.getLogger().info("Cannot find config.yml, Generating now....");
			plugin.getLogger().info("Config generated !");
			plugin.getConfig().options().copyDefaults(true);
			plugin.saveDefaultConfig();
		}
	}
	
	/**
	 * Reloads the configuration and sends the sender a message.
	 * @param sender - CommandSender player/console
	 * @param message - String to send on completion
	 */
	public void reloadConfig(CommandSender sender, String message){
		plugin.reloadConfig();
		sender.sendMessage(message);
	}
	
	/**
	 * Gets the config from the plugin
	 * @return - the Configuration
	 */
	public FileConfiguration getConfig(){
		config = plugin.getConfig();
		return config;
	}
	
	/**
	 * Gets the actual file from the system
	 * @return - Configuration File
	 */
	public File getConfigFile(){
		return configFile;
	}
	
	/**
	 * Gets all configuration values
	 */
	public void getConfigValues(){
		maxRadius = config.getInt("Restriction.max-radius", 0);
		allowOfflineSend = config.getBoolean("Allow-offline", false);
		useEco = config.getBoolean("Eco.use", false);
		fee = config.getDouble("Eco.transaction-fee", 0);
		crossWorldSend = config.getBoolean("Cross-world-sending", true);
	}
	
	public void setConfigValues(){
		plugin.maxRadius = maxRadius;
		plugin.allowOfflineSend = allowOfflineSend;
		plugin.useEco = useEco;
		plugin.fee = fee;
		plugin.crossWorldSend = crossWorldSend;
	}
}
