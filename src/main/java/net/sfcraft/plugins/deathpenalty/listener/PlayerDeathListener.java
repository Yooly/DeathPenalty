package net.sfcraft.plugins.deathpenalty.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.meowj.langutils.lang.LanguageHelper;
import com.meowj.langutils.locale.LocaleHelper;

import net.milkbowl.vault.economy.Economy;
import net.sfcraft.plugins.deathpenalty.DeathPenalty;
import net.sfcraft.plugins.deathpenalty.util.StringUtil;

public class PlayerDeathListener implements Listener {
	private final DeathPenalty plugin;
	private final Economy economy;

	public PlayerDeathListener(DeathPenalty instance) {
		plugin = instance;
		economy = plugin.getEconomy();
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		boolean haveLanguageUtils = Bukkit.getPluginManager().getPlugin("LangUtils") != null;
		String locale = haveLanguageUtils ? LocaleHelper.getPlayerLanguage(player) : "zh_CN";
		List<String> noPluginWorlds = plugin.getSetting().getWorlds();
		String playerWorld = player.getWorld().getName();
		for (String string : noPluginWorlds) {
			if(string.equalsIgnoreCase(playerWorld)){
				return;
			}
		}
		event.setKeepInventory(true);	//设置死亡不掉落
		List<ItemStack> dropItem = dropItem(player, event);
		double dropMoney = dropMoney(player);
		int droppedExp = dropExp(player, event);
		if((dropItem != null && !dropItem.isEmpty()) || dropMoney != 0 || droppedExp != 0){
			player.sendMessage(plugin.getSetting().getMessage_deathDrop());
			if(dropItem != null && !dropItem.isEmpty())
				for (ItemStack itemStack : dropItem) {
					String itemName = haveLanguageUtils ? LanguageHelper.getItemDisplayName(itemStack, locale) : itemStack.getType().name();
					player.sendMessage(StringUtil.appendStr("§a", itemName, "§r : ", itemStack.getAmount()));
				}
			if(dropMoney != 0)
				player.sendMessage(StringUtil.appendStr("§a", plugin.getSetting().getMessage_money(),"§r : ", dropMoney));
			if(droppedExp != 0)
				player.sendMessage(StringUtil.appendStr("§a", plugin.getSetting().getMessage_exp(),"§r : ", droppedExp));
		}
		
	}

	/**
	 * 掉落物品
	 * 
	 * @param player
	 * @param event
	 */
	private List<ItemStack> dropItem(final Player player, final PlayerDeathEvent event) {
		if (player.hasPermission("DeathPenalty.item") || player.isOp()) {
			return null;
		}
		int dropItemAcount = event.getDrops().size();
		int dropItemNum = plugin.getSetting().getItem(); // 掉落物品格数
		if(dropItemNum == 0){
			return null;
		}
		List<ItemStack> dropItems = new ArrayList<ItemStack>();
		if (dropItemNum >= dropItemAcount) {
			dropItems = event.getDrops();
		} else {
			while (true) {
				if (dropItems.size() == dropItemNum || dropItems.size() == dropItemAcount) {
					break;
				}
				int randomNumber = (int) Math.round(Math.random() * (dropItemAcount - 1));
				ItemStack dropItem = event.getDrops().get(randomNumber);
				if (!dropItems.contains(dropItem)) {
					dropItems.add(dropItem);
				}
			}
		}
		HashMap<Integer, ItemStack> a = player.getInventory().removeItem(dropItems.toArray(new ItemStack[dropItems.size()]));
		dropItems.removeAll(a.values());	//装备栏的物品没法删除，只好不掉落了
		Location location = player.getLocation();
		for (ItemStack itemStack : dropItems) {
	        location.getWorld().dropItemNaturally(location, itemStack);//掉落物品
		}
		return dropItems;
	}

	/**
	 * 掉落金钱,需前置插件vault,及任意一款经济插件
	 * 
	 * @param player
	 * @return
	 */
	private double dropMoney(final Player player) {
		if (player.hasPermission("DeathPenalty.money") || player.isOp()) {
			return 0;
		}
		if(this.economy == null){
			return 0;
		}
		double playerMoney = this.economy.getBalance(player);
		String dropMoneySetting = plugin.getSetting().getMoney();
		double dropMoney = 0;
		if(dropMoneySetting.indexOf("%") != -1){
			dropMoney = playerMoney * Double.parseDouble(dropMoneySetting.replace("%", "")) / 100;
		} else {
			dropMoney = Double.parseDouble(dropMoneySetting);
		}
		if (this.economy.has(player, dropMoney)) {
			this.economy.withdrawPlayer(player, dropMoney);
		} else {
			dropMoney = 0;
		}
		return dropMoney;
	}

	/**
	 * 掉落经验
	 * @param player
	 * @param event
	 * @return
	 */
	private int dropExp(final Player player, final PlayerDeathEvent event) {
		if (player.hasPermission("DeathPenalty.exp") || player.isOp()) {
			event.setKeepLevel(true);
			event.setDroppedExp(0);
			return 0;
		}
		int totalExp = player.getTotalExperience();
		int droppedExp = 0;
		String dropExpSetting = plugin.getSetting().getExp();
		if(dropExpSetting.indexOf("%") != -1){
			droppedExp = (int)(totalExp * Double.parseDouble(dropExpSetting.replace("%", ""))) / 100;
		} else {
			droppedExp = Integer.parseInt(dropExpSetting);
		}
		event.setDroppedExp(droppedExp);
		event.setNewExp(totalExp - droppedExp);
		return droppedExp;
	}
}
