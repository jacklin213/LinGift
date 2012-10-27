package me.jacklin213.lingift.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import me.jacklin213.lingift.LinGift;

public class ConfigHandler {

	public static LinGift plugin;
	public static File configFile;
	public static FileConfiguration config;

	public static void saveConfig() {
		try {
			LinGift.log.info("Saving configuration file.");
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadConfig() {
		try {
			config.load(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {

		}
	}
	
	//end of class
}
