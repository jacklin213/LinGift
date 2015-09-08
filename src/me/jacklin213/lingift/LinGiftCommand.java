package me.jacklin213.lingift;

import info.somethingodd.OddItem.OddItem;

import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LinGiftCommand implements CommandExecutor {
	
	private LinGift plugin;
	
	private FileConfiguration config;
	
	private Economy economy;
	
	public LinGiftCommand(LinGift instance) {
		plugin = instance;
		config = plugin.getConfig();
		economy = LinGift.economy;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		boolean canUseCommand = true;
		boolean paying = false;
		int maxRadius = plugin.maxRadius;
		boolean allowOfflineSend = plugin.allowOfflineSend;
		double fee = plugin.fee;
		boolean crossWorldSend = plugin.crossWorldSend;
		boolean useEco = plugin.useEco;

		HashMap<Integer, ? extends ItemStack> itemsarray = new HashMap<Integer, ItemStack>();

		if (sender instanceof Player) {
			Player player = (Player) sender;

			String permissions_config = null;
			permissions_config = config.getString("use-permissions", "OP").trim();

			double money = 0;
			if (useEco)
				money = economy.getBalance(player);

			if (!permissions_config.equalsIgnoreCase("false")) {
				if (permissions_config.equalsIgnoreCase("permissions")) {
					canUseCommand = player.hasPermission("LinGift.send");
				} else if (permissions_config.equalsIgnoreCase("OP")) {
					canUseCommand = player.isOp();
				}
			}
			if (!canUseCommand)
				sender.sendMessage("You don't have Permissions to do that");

			if (canUseCommand && fee > 0 && useEco) {
				paying = true;
				if (money < fee) {
					canUseCommand = false;
					player.sendMessage("You don't have enough money to send some items.");
				}

				if (!canUseCommand) {
					canUseCommand = player.hasPermission("LinGift.nofee");
					paying = false;
				}
			}

			if (player.isOp())
				canUseCommand = true;

			if (canUseCommand) {
				if (args.length > 0) {
					String playername = args[0];
					String itemamount = "1"; // default value
					String tmpdurability = null;
					String itemstring = null;
					if (args.length > 1)
						itemamount = args[1];
					if (args.length > 2)
						itemstring = args[2];
					if (args.length > 3)
						tmpdurability = args[3];

					String errormsg = "";
					int giveamount = 0;
					try {
						giveamount = Integer.parseInt(itemamount);
					} catch (NumberFormatException e) {
						return false;
					}
					int givetypeid = 0;
					short durability = 0;

					if (tmpdurability != null) {
						try {
							durability = Short.parseShort(tmpdurability);
						} catch (NumberFormatException e) {
							return false;
						}
					}
					// checks to see if the item works
					if (itemstring == null) {
						if (player.getItemInHand().getType() != null) {
							itemstring = Integer.toString(player.getItemInHand().getTypeId());
							durability = player.getItemInHand().getDurability();
						} else {
							player.sendMessage("Hold an item in your hand, or use this syntax :");
							return false;
						}
					}
					try {
						givetypeid = Integer.parseInt(itemstring);
					} catch (NumberFormatException e) {
						if (LinGift.OI != null) { // get the OddItem name
							try {
								givetypeid = OddItem.getItemStack(itemstring).getTypeId();
							} catch (IllegalArgumentException ex) {
								errormsg = "Did you mean : " + ex.getMessage()
										+ " ?";
							}
						} else { // get the ENUM name
							try {
								givetypeid = Material.getMaterial(itemstring.toUpperCase()).getId();
							} catch (NullPointerException n) {
								errormsg = "The item '"
										+ itemstring.toUpperCase()
										+ "' does not exist.";
							}
						}
					}

					// allows offline transfers
					Player recipient = Bukkit.getServer().getPlayer(playername);

					// Checks to see if you have enough
					itemsarray = player.getInventory().all(Material.getMaterial(givetypeid));
					int playerHasInInventory = 0;

					for (ItemStack itemstack : itemsarray.values()) {
						if ((itemstack.getDurability() == durability || 
								plugin.tools.contains(givetypeid)) && itemstack.getAmount() > 0) {
							playerHasInInventory = playerHasInInventory	+ itemstack.getAmount();
						}

					}

					// Checks to see if players are close enough
					if (recipient != null) {
						if (player.getWorld() == recipient.getWorld()) {
							if (maxRadius > 0) {
								int totaldistance = 0;
								int x1 = player.getLocation().getBlockX();
								int y1 = player.getLocation().getBlockY();
								int z1 = player.getLocation().getBlockZ();
								int x2 = recipient.getLocation().getBlockX();
								int y2 = recipient.getLocation().getBlockY();
								int z2 = recipient.getLocation().getBlockZ();

								totaldistance = ((x1 - x2) ^ 2 + (y1 - y2) ^ 2 + (z1 - z2) ^ 2);

								if (totaldistance >= (maxRadius ^ 2)) {
									errormsg = "That player too far away.";
								}
							}
						} else if (crossWorldSend == false) {
							errormsg = "That player is not in the same world.";
						}
					} else {
						if (!allowOfflineSend) {
							errormsg = "That player is not online.";
						}
					}

					// outputs error
					if (giveamount > playerHasInInventory) {
						errormsg = "You do not have that item with that amount.";
					}
					if (errormsg.length() > 0) {
						player.sendMessage(ChatColor.RED + errormsg);
					}
					else { // start the transfer
						short tmp_durability = durability;
						int tmp_amount = giveamount;
						for (ItemStack itemstack : itemsarray.values()) {
							if (itemstack.getDurability() == tmp_durability) {
								if (itemstack.getAmount() <= tmp_amount) {
									tmp_amount = tmp_amount - itemstack.getAmount();
									player.getInventory().removeItem(itemstack);
								} else if (itemstack.getAmount() > tmp_amount) {
									player.getInventory().removeItem(itemstack);
									player.getInventory().addItem(new ItemStack(givetypeid, (itemstack.getAmount() - tmp_amount), durability));
									tmp_amount = 0;
								}
							}
						}

						plugin.SM.sendToPlayer(player, recipient, givetypeid, durability, giveamount - tmp_amount, playername);

						int amount_left = giveamount - tmp_amount;
						if (tmp_amount > 0) { 
							//if tool, send the ones that are not damaged at all
							for (ItemStack itemstack : itemsarray.values()) {
								if (itemstack.getDurability() == 0) {
									if (itemstack.getAmount() <= tmp_amount) {
										tmp_amount = tmp_amount - itemstack.getAmount();
										player.getInventory().removeItem(itemstack);
									} else if (itemstack.getAmount() > tmp_amount) {
										player.getInventory().removeItem(itemstack);
										player.getInventory().addItem(new ItemStack(givetypeid, (itemstack.getAmount() - tmp_amount), durability));
										tmp_amount = 0;
									}
								}
								if (tmp_amount == 0)
									break;
							}
							plugin.SM.sendToPlayer(player, recipient, givetypeid, (byte) 0, amount_left - tmp_amount, playername);
						}

						amount_left = giveamount - tmp_amount;
						while (tmp_amount > 0) { 
							//if tool, send the ones that are differently damaged
							short tmpDurability = (byte) 0;
							for (ItemStack itemstack : itemsarray.values()) {
								tmpDurability = itemstack.getDurability();
								break;
							}
							for (ItemStack itemstack : itemsarray.values()) {
								if (itemstack.getDurability() == tmpDurability) {
									if (itemstack.getAmount() <= tmp_amount) {
										tmp_amount = tmp_amount - itemstack.getAmount();
										player.getInventory().removeItem(itemstack);
									} else if (itemstack.getAmount() > tmp_amount) {
										player.getInventory().removeItem(itemstack);
										player.getInventory().addItem(new ItemStack(givetypeid, (itemstack.getAmount() - tmp_amount), durability));
										tmp_amount = 0;
									}
								}
								if (tmp_amount == 0)
									break;
							}
							plugin.SM.sendToPlayer(player, recipient, givetypeid, tmpDurability, amount_left - tmp_amount, playername);
						}
						if (paying) {
							if (useEco) {
								economy.withdrawPlayer(player, fee);
								player.sendMessage("The transaction cost you : " + economy.format(fee));
							}
						}
					}
				} else {
					return false;
				}
			}
		} else {
			sender.sendMessage("This is a player only command.");
		}
		return true;
	}
}
