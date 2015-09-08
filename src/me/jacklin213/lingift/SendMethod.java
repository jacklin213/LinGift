package me.jacklin213.lingift;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SendMethod {
	
	private LinGift plugin;
	
	public SendMethod(LinGift instance) {
		plugin = instance;
	}
	
	public void sendToPlayer(Player sender, Player recipient, Material itemtype, short durability, int giveamount, String playername) {
		// player is not online, store in offline.txt
		if (recipient == null || !recipient.isOnline()) {
			plugin.OFH.writeOfflineFile(sender, playername, itemtype, durability, giveamount, false);
		} else { // both online, do in real time
			sendOnline(sender, recipient, itemtype, durability, giveamount);
		}
	}

	private void sendOnline(Player sender, Player recipient, Material itemtype, short durability, int giveamount) {
		// make sure that the receiving player's inventory isn't full
		if (recipient.getInventory().firstEmpty() >= 0) {
			// remove the item
			int amount_left = giveamount;
			//int stack_size = Material.getMaterial(givetypeid).getMaxStackSize();
			int stack_size = itemtype.getMaxStackSize();
			//if (givetypeid == 357)
			if (itemtype == Material.COOKIE)
				stack_size = 8;
			while (amount_left > 0) {
				if (recipient.getInventory().firstEmpty() == -1)
					break;
				recipient.getInventory().addItem(new ItemStack(itemtype, Math.min(amount_left, stack_size), durability));
				amount_left -= Math.min(amount_left, stack_size);
			}
			//String materialname = Material.getMaterial(givetypeid).toString().toLowerCase().replace("_", " ");
			String materialname = itemtype.toString().toLowerCase().replace("_", " ");
			if (giveamount > 1) {
				if (materialname.endsWith("s") || materialname.endsWith("z"))
					materialname = materialname + "es";
				else
					materialname = materialname + "s";
			}

			sender.sendMessage(ChatColor.GRAY + "You gave " 
					+ ChatColor.GREEN + recipient.getName() + " " 
					+ ChatColor.GRAY + giveamount + " " 
					+ ChatColor.RED + materialname);
			recipient.sendMessage(ChatColor.GREEN + sender.getName()
					+ ChatColor.GRAY + " gave you " + giveamount + " "
					+ ChatColor.RED + materialname);
			if (amount_left > 0) {
				plugin.OFH.writeOfflineFile(sender, recipient.getName(), itemtype, durability, amount_left, false);
				sender.sendMessage(ChatColor.GREEN + recipient.getName() + "'s "
						+ ChatColor.GRAY + " inventory is full. Only part of the items were sent.");
				recipient.sendMessage(ChatColor.GREEN + sender.getName()
								+ ChatColor.GRAY + " tried to send you something, but you have no space left. Try to reconnect with some space.");
			}
		} else {
			sender.sendMessage(ChatColor.GREEN + recipient.getName() + "'s "
					+ ChatColor.GRAY + " inventory is full. Try again later.");
			recipient.sendMessage(ChatColor.GREEN + sender.getName()
							+ ChatColor.GRAY + " tried to send you something, but you have no space left. Try to reconnect with some space.");
		}
	}
}
