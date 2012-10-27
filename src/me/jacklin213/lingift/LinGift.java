package me.jacklin213.lingift;

import info.somethingodd.OddItem.OddItem;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
//bukkit stuff
import me.jacklin213.lingift.listeners.LGPlayerListener;
import me.jacklin213.lingift.utils.LGConfigWriter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class LinGift extends JavaPlugin {

	public LGPlayerListener lgpl = new LGPlayerListener(this);
	public LGConfigWriter lgcw = new LGConfigWriter(this);
	public static Logger log = Logger.getLogger("Minecraft");
	public static OddItem OI = null;
	public static File dataFolder;
	public static Economy economy = null;
	
	private int maxradius = 0;
	private boolean allowoffline = false;
	private boolean crossWorld = true;
	private boolean useEco = false;
	private ArrayList<Integer> tools = new ArrayList<Integer>(Arrays.asList(
			256, 257, 258, 259, 267, 268, 269, 270, 271, 272, 273, 274, 275,
			276, 277, 278, 279, 283, 284, 285, 286, 290, 291, 292, 293, 294,
			298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310,
			311, 312, 313, 314, 315, 316, 317, 346, 359)); // tools that can be damaged
	private FileConfiguration config;
	double fee = 0;
	
	public void onDisable() {
		log.info("[LinGift] Disabled");
	}

	public void onEnable() {
		dataFolder = new File("plugins" + File.separator + "LinGift"
				+ File.separator);
		LGConfigWriter.createConfig();
		config = getConfig();
		//Assign variables to config stuff
		maxradius = config.getInt("max-range", 0);
		allowoffline = config.getBoolean("allow-offline", false);
		fee = config.getDouble("transaction-fee", 0);
		crossWorld = config.getBoolean("allow-cross-world-sending", true);
		useEco = config.getBoolean("use-economy", false);
		// Get the information from the plugin.yml file.
		PluginDescriptionFile pdfFile = this.getDescription();
		//Checks depedencies
		OI = (OddItem) getServer().getPluginManager().getPlugin("OddItem");
		if (OI != null) {
			log.info("[LinGift] Successfully connected with OddItem");
		}

		if (useEco) {
			if (!setupEconomy()) {
				log.info(String.format(
						"[%s] - Disabled due to no Vault dependency found!",
						getDescription().getName()));
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}

		// Print that the plugin has been enabled!
		log.info("[LinGift] version : " + pdfFile.getVersion()
				+ " by jacklin213 & (Former Author) nitnelave is enabled!");
	}

	private Boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}
	
	//Command process
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		boolean canUseCommand = true;
		boolean paying = false;

		HashMap<Integer, ? extends ItemStack> itemsarray = new HashMap<Integer, ItemStack>();

		if (sender instanceof Player) {
			Player player = (Player) sender;

			String permissions_config = null;
			permissions_config = config.getString("use-permissions", "OP")
					.trim();

			double money = 0;
			if (useEco)
				money = economy.getBalance(player.getName());

			if (!permissions_config.equalsIgnoreCase("false")) {
				if (permissions_config.equalsIgnoreCase("permissions")) {
					canUseCommand = player.hasPermission("LinGift.send");
				} else if (permissions_config.equalsIgnoreCase("OP"))
					canUseCommand = player.isOp();

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
						if (player.getItemInHand().getTypeId() != 0) {
							itemstring = Integer.toString(player
									.getItemInHand().getTypeId());
							durability = player.getItemInHand().getDurability();
						} else {
							player.sendMessage("Hold an item in your hand, or use this syntax :");
							return false;
						}
					}
					try {
						givetypeid = Integer.parseInt(itemstring);
					} catch (NumberFormatException e) {
						if (OI != null) { // get the OddItem name
							try {
								givetypeid = OddItem.getItemStack(itemstring)
										.getTypeId();
							} catch (IllegalArgumentException ex) {
								errormsg = "Did you mean : " + ex.getMessage()
										+ " ?";
							}
						} else { // get the ENUM name

							try {
								givetypeid = Material.getMaterial(
										itemstring.toUpperCase()).getId();
							} catch (NullPointerException n) {
								errormsg = "The item '"
										+ itemstring.toUpperCase()
										+ "' does not exist.";
							}
						}
					}

					// allows offline transfers
					Player recipient = getServer().getPlayer(playername);

					// Checks to see if you have enough
					itemsarray = player.getInventory().all(
							Material.getMaterial(givetypeid));
					int playerHasInInventory = 0;

					for (Entry<Integer, ? extends ItemStack> entry : itemsarray
							.entrySet()) {
						ItemStack value = entry.getValue();

						if ((value.getDurability() == durability || tools
								.contains(givetypeid)) && value.getAmount() > 0) {

							playerHasInInventory = playerHasInInventory
									+ value.getAmount();
						}

					}

					// Checks to see if players are close enough
					if (recipient != null) {
						if (player.getWorld() == recipient.getWorld()) {

							if (maxradius > 0) {

								int totaldistance = 0;

								int x1 = player.getLocation().getBlockX();

								int y1 = player.getLocation().getBlockY();

								int z1 = player.getLocation().getBlockZ();

								int x2 = recipient.getLocation().getBlockX();

								int y2 = recipient.getLocation().getBlockY();

								int z2 = recipient.getLocation().getBlockZ();

								totaldistance = ((x1 - x2) ^ 2 + (y1 - y2) ^ 2
										+ (z1 - z2) ^ 2);

								if (totaldistance >= (maxradius ^ 2)) {

									errormsg = "That player too far away.";

								}

							}
						} else if (crossWorld == false)
							errormsg = "That player is not in the same world.";
					} else {
						if (!allowoffline) {
							errormsg = "That player is not online.";
						}
					}

					// outputs error
					if (giveamount > playerHasInInventory) {
						errormsg = "You do not have that item with that amount.";
					}
					if (errormsg.length() > 0) {
						player.sendMessage(ChatColor.GRAY + errormsg);
					}

					// start the transfer
					else {

						short tmp_durability = durability;
						int tmp_amount = giveamount;
						for (Entry<Integer, ? extends ItemStack> entry : itemsarray
								.entrySet()) {
							ItemStack value = entry.getValue();
							if (value.getDurability() == tmp_durability) {
								if (value.getAmount() <= tmp_amount) {
									tmp_amount = tmp_amount - value.getAmount();
									player.getInventory().removeItem(value);
								} else if (value.getAmount() > tmp_amount) {
									player.getInventory().removeItem(value);
									player.getInventory().addItem(
											new ItemStack(givetypeid, (value
													.getAmount() - tmp_amount),
													durability));
									tmp_amount = 0;
								}
							}
						}

						sendToPlayer(player, recipient, givetypeid, durability,
								giveamount - tmp_amount, playername);

						int amount_left = giveamount - tmp_amount;
						if (tmp_amount > 0) { // tool, send the ones that are
												// not damaged at all
							for (Entry<Integer, ? extends ItemStack> entry : itemsarray
									.entrySet()) {
								ItemStack value = entry.getValue();
								if (value.getDurability() == 0) {
									if (value.getAmount() <= tmp_amount) {
										tmp_amount = tmp_amount
												- value.getAmount();
										player.getInventory().removeItem(value);
									} else if (value.getAmount() > tmp_amount) {
										player.getInventory().removeItem(value);
										player.getInventory()
												.addItem(
														new ItemStack(
																givetypeid,
																(value.getAmount() - tmp_amount),
																durability));
										tmp_amount = 0;
									}
								}
								if (tmp_amount == 0)
									break;
							}
							sendToPlayer(player, recipient, givetypeid,
									(byte) 0, amount_left - tmp_amount,
									playername);
						}

						amount_left = giveamount - tmp_amount;
						while (tmp_amount > 0) { // tool, send the ones that are
													// differently damaged
							short tmpDurability = (byte) 0;
							for (Entry<Integer, ? extends ItemStack> entry : itemsarray
									.entrySet()) {
								tmpDurability = entry.getValue()
										.getDurability();
								break;
							}
							for (Entry<Integer, ? extends ItemStack> entry : itemsarray
									.entrySet()) {
								ItemStack value = entry.getValue();
								if (value.getDurability() == tmpDurability) {
									if (value.getAmount() <= tmp_amount) {
										tmp_amount = tmp_amount
												- value.getAmount();
										player.getInventory().removeItem(value);
									} else if (value.getAmount() > tmp_amount) {
										player.getInventory().removeItem(value);
										player.getInventory()
												.addItem(
														new ItemStack(
																givetypeid,
																(value.getAmount() - tmp_amount),
																durability));
										tmp_amount = 0;
									}
								}
								if (tmp_amount == 0)
									break;
							}
							sendToPlayer(player, recipient, givetypeid,
									tmpDurability, amount_left - tmp_amount,
									playername);

						}
						if (paying) {
							if (useEco) {
								economy.withdrawPlayer(player.getName(), fee);
								player.sendMessage("The transaction cost you : "
										+ economy.format(fee));
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

	public void sendToPlayer(Player sender, Player recipient, int givetypeid,
			short durability, int giveamount, String playername) {
		// player is not online, store in offline.txt
		if (recipient == null || !recipient.isOnline()) {
			LGConfigWriter.writeOffline(sender, playername, givetypeid, durability,
					giveamount, false);
		}
		// both online, do in real time
		else {
			sendOnline(sender, recipient, givetypeid, durability, giveamount);
		}
	}

	private void sendOnline(Player sender, Player recipient, int givetypeid,
			short durability, int giveamount) {
		// make sure that the receiving player's inventory isn't full
		if (recipient.getInventory().firstEmpty() >= 0) {

			// remove the item
			int amount_left = giveamount;
			int stack_size = Material.getMaterial(givetypeid).getMaxStackSize();
			if (givetypeid == 357)
				stack_size = 8;
			while (amount_left > 0) {
				if (recipient.getInventory().firstEmpty() == -1)
					break;
				recipient.getInventory().addItem(
						new ItemStack(givetypeid, Math.min(amount_left,
								stack_size), durability));
				amount_left -= Math.min(amount_left, stack_size);
			}

			String materialname = Material.getMaterial(givetypeid).toString()
					.toLowerCase().replace("_", " ");
			if (giveamount > 1) {
				if (materialname.endsWith("s") || materialname.endsWith("z"))
					materialname = materialname + "es";
				else
					materialname = materialname + "s";
			}

			sender.sendMessage(ChatColor.GRAY + "You gave " + ChatColor.GREEN
					+ recipient.getName() + " " + ChatColor.GRAY + giveamount
					+ " " + ChatColor.RED + materialname);
			recipient.sendMessage(ChatColor.GREEN + sender.getName()
					+ ChatColor.GRAY + " gave you " + giveamount + " "
					+ ChatColor.RED + materialname);
			if (amount_left > 0) {
				LGConfigWriter.writeOffline(sender, recipient.getName(), givetypeid,
						durability, amount_left, false);
				sender.sendMessage(ChatColor.GREEN
						+ recipient.getName()
						+ "'s "
						+ ChatColor.GRAY
						+ " inventory is full. Only part of the items were sent.");
				recipient
						.sendMessage(ChatColor.GREEN
								+ sender.getName()
								+ ChatColor.GRAY
								+ " tried to send you something, but you have no space left. Try to reconnect with some space.");
			}
		} else {
			sender.sendMessage(ChatColor.GREEN + recipient.getName() + "'s "
					+ ChatColor.GRAY + " inventory is full. Try again later.");
			recipient
					.sendMessage(ChatColor.GREEN
							+ sender.getName()
							+ ChatColor.GRAY
							+ " tried to send you something, but you have no space left. Try to reconnect with some space.");
		}
	}

	//end of class
}