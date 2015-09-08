package me.jacklin213.lingift.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import me.jacklin213.lingift.LinGift;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OfflineFileHandler {
	
	private LinGift plugin;
	private File offlineFile;
	
	public OfflineFileHandler(File file, LinGift instance) {
		plugin = instance;
		this.offlineFile = file;
		
		if (!this.offlineFile.exists()) {
			try {
				this.offlineFile.mkdirs();
				this.offlineFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Gets the offline File from the OfflineFileHandler class.
	 * 
	 * @return - The offlineFile
	 */
	public File getOfflineFile() {
		return offlineFile;
	}
	
	public void writeOfflineFile(Player sender, String recipient, Material itemtype, short durability, int giveamount, boolean listener) {
		// Write the send to file
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(offlineFile, true));

			String textToWrite = recipient + ":" + itemtype.toString() + ":" + giveamount + ":" + sender.getName() + ":" + durability;

			out.write(textToWrite);
			out.newLine();

			// Close the output stream
			out.close();

			//String materialname = Material.getMaterial(givetypeid).toString().toLowerCase().replace("_", " ");
			String materialname = itemtype.toString().toLowerCase().replace("_", " ");
			if (giveamount > 1) {
				if (materialname.endsWith("s") || materialname.endsWith("z"))
					materialname = materialname + "es";
				else
					materialname = materialname + "s";
			}

			if (!listener) {
				sender.sendMessage(ChatColor.GRAY + "You gave " + ChatColor.GREEN + recipient + " " + ChatColor.GRAY + giveamount + " " + ChatColor.RED + materialname);
				sender.sendMessage(ChatColor.GRAY + "They will receive it when they log in.");
			}

		} catch (Exception e) {
			LinGift.log.info("Offline transfer to " + recipient + " failed: " + e);
		}
	}
	
	
	public void writeTempFile(Player player) {
		File tempFile = new File(plugin.getDataFolder() + File.separator + "offline.tmp");

		if (!tempFile.exists()) {
			try {
				tempFile.createNewFile();
			} catch (IOException e) {
				LinGift.log.severe("Cannot create temp file: " + tempFile.getPath() + "/" + tempFile.getName());
			}
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(offlineFile));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line;
			String[] splittext;

			while ((line = br.readLine()) != null) {
				splittext = line.split(":");
				if (splittext[0].equals(player.getName())) {
					if (player.getInventory().firstEmpty() >= 0) {
						//int givetypeid = Integer.parseInt(splittext[1]);
						Material itemtype = Material.matchMaterial(splittext[2]);
						int giveamount = Integer.parseInt(splittext[2]);
						short givedurability = Short.valueOf(splittext[4]);

						int tmpamount = giveamount;
						int stack_size = itemtype.getMaxStackSize();
						//if (givetypeid == 357)
						if (itemtype == Material.COOKIE)
							stack_size = 8;
						while (tmpamount > 0) {
							if (player.getInventory().firstEmpty() == -1)
								break;
							player.getInventory().addItem(new ItemStack(itemtype, Math.min(tmpamount, stack_size), givedurability));
							tmpamount -= Math.min(tmpamount, stack_size);
						}
						if (tmpamount > 0) {
							writeOfflineFile(player, splittext[3], itemtype, givedurability, tmpamount, true);
						}
						//String materialname = Material.getMaterial(givetypeid).toString().toLowerCase().replace("_", " ");
						String materialname = itemtype.toString().toLowerCase().replace("_", " ");
						if (giveamount > 1)
							materialname = materialname + "s";

						if (tmpamount == 0)
							player.sendMessage(ChatColor.GREEN + splittext[3]
											+ ChatColor.GRAY + " gave you "
											+ giveamount + " " + ChatColor.RED
											+ materialname);
						else
							player.sendMessage(ChatColor.GREEN + splittext[3]
									+ ChatColor.GRAY + " gave you " + (giveamount - tmpamount) + " "
									+ ChatColor.RED	+ materialname
									+ ChatColor.GRAY + " but "
									+ ChatColor.RED	+ tmpamount + " more did not fit. Try to reconnect after emptying your inventory.");
					} else {
						player.sendMessage(ChatColor.GRAY + "You have items sent to you, but your inventory is full.");
						player.sendMessage(ChatColor.GRAY + "Please make space and relog to get your items.");

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
			LinGift.log.severe("Offline file read error: " + e);
		}

	}
}
