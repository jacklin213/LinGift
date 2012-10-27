package me.jacklin213.lingift.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import me.jacklin213.lingift.LinGift;
import me.jacklin213.lingift.listeners.LGPlayerListener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class LGConfigWriter {
	
	public static LinGift plugin;
	public static LGPlayerListener lgpl;

	public LGConfigWriter(LinGift instance) {
		plugin = instance;
	}
	
	static Logger log = Logger.getLogger("Minecraft");
	static FileConfiguration config = new YamlConfiguration();
	static File configFile = new File(LinGift.dataFolder , "config.yml");
	static File offlinesends = new File(LinGift.dataFolder + "/offline.txt");
	public static PlayerJoinEvent event;
	
	public static void createConfig() {
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			config.set("max-range", 100);
			config.set("allow-offine", true);
			config.set("use-permissions", "permissions");
			config.set("transaction-fee", 0.0);
			config.set("allow-cross-world-sending", true);
			config.set("use-economy", false);
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!offlinesends.exists()) {
			try {
				offlinesends.createNewFile();
			} catch (IOException e) {
				System.out
						.println("cannot create file " + offlinesends.getPath()
								+ "/" + offlinesends.getName());
			}
		}
	}
	
	public static void createOfflineFile(){
		File offlineFile = new File(LinGift.dataFolder, "offline.txt");
		File tempFile = new File(LinGift.dataFolder, "offline.tmp");

		if (!tempFile.exists()) {
			try {
				tempFile.createNewFile();
			} catch (IOException e) {
				System.out.println("cannot create temp file "
						+ tempFile.getPath() + "/" + tempFile.getName());
			}
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(offlineFile));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line;
			String[] splittext;

			while ((line = br.readLine()) != null) {
				splittext = line.split(":");
				if (splittext[0].equals(event.getPlayer().getName())) {
					if (event.getPlayer().getInventory().firstEmpty() >= 0) {
						int givetypeid = Integer.parseInt(splittext[1]);
						int giveamount = Integer.parseInt(splittext[2]);
						short givedurability = Short.valueOf(splittext[4]);

						int tmpamount = giveamount;
						int stack_size = Material.getMaterial(givetypeid)
								.getMaxStackSize();
						if (givetypeid == 357)
							stack_size = 8;
						while (tmpamount > 0) {
							if (event.getPlayer().getInventory().firstEmpty() == -1)
								break;
							event.getPlayer()
									.getInventory()
									.addItem(
											new ItemStack(givetypeid, Math.min(
													tmpamount, stack_size),
													givedurability));
							tmpamount -= Math.min(tmpamount, stack_size);
						}
						if (tmpamount > 0) {
							LGConfigWriter.writeOffline(event.getPlayer(),
									splittext[3], givetypeid, givedurability,
									tmpamount, true);

						}
						String materialname = Material.getMaterial(givetypeid)
								.toString().toLowerCase().replace("_", " ");
						if (giveamount > 1)
							materialname = materialname + "s";

						if (tmpamount == 0)
							event.getPlayer().sendMessage(
									ChatColor.GREEN + splittext[3]
											+ ChatColor.GRAY + " gave you "
											+ giveamount + " " + ChatColor.RED
											+ materialname);
						else
							event.getPlayer()
									.sendMessage(
											ChatColor.GREEN
													+ splittext[3]
													+ ChatColor.GRAY
													+ " gave you "
													+ (giveamount - tmpamount)
													+ " "
													+ ChatColor.RED
													+ materialname
													+ ChatColor.GRAY
													+ " but "
													+ ChatColor.RED
													+ tmpamount
													+ " more did not fit. Try to reconnect after emptying your inventory.");
					} else {
						event.getPlayer()
								.sendMessage(
										ChatColor.GRAY
												+ "You have items sent to you, but your inventory is full.");
						event.getPlayer()
								.sendMessage(
										ChatColor.GRAY
												+ "Please make space and relog to get your items.");

						pw.println(line);
						pw.flush();
					}
				} else {
					pw.println(line);
					pw.flush();
				}
			}

			pw.close();
			br.close();

			offlineFile.delete();
			tempFile.renameTo(offlineFile);

		} catch (IOException e) {
			System.out.println("[GiftSend] Offline file read error: " + e);
		}
	}
	
	public static void writeOffline(Player sender, String recipient, int givetypeid,
			short durability, int giveamount, boolean listener) {
		File offlineFile = new File(LinGift.dataFolder , "offline.txt");
		// Write the send to file
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(offlineFile,
					true));

			String textToWrite = recipient + ":" + givetypeid + ":"
					+ giveamount + ":" + sender.getName() + ":" + durability;

			out.write(textToWrite);
			out.newLine();

			// Close the output stream
			out.close();

			String materialname = Material.getMaterial(givetypeid).toString()
					.toLowerCase().replace("_", " ");
			if (giveamount > 1) {
				if (materialname.endsWith("s") || materialname.endsWith("z"))
					materialname = materialname + "es";
				else
					materialname = materialname + "s";
			}

			if (!listener) {
				sender.sendMessage(ChatColor.GRAY + "You gave "
						+ ChatColor.GREEN + recipient + " " + ChatColor.GRAY
						+ giveamount + " " + ChatColor.RED + materialname);
				sender.sendMessage(ChatColor.GRAY
						+ "They will receive it when they log in.");
			}

		} catch (Exception e) {
			log.info("[LinGift] Offline transfer to " + recipient + " failed: "
					+ e);
		}
	}
	
	//end of class
}



