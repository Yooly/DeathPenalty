package net.sfcraft.plugins.deathpenalty.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.sfcraft.plugins.deathpenalty.DeathPenalty;

public class CommandManager implements CommandExecutor, TabCompleter{
	private final DeathPenalty plugin;

	public CommandManager(DeathPenalty instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("DeathPenalty")) {
            return false;
        }
		if (args.length != 1) {
			sender.sendMessage("[DeathPenalty] Use '/dp reload' to reolad config");
            return true;
        }
		if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("DeathPenalty.reload") || !sender.isOp()) {
				sender.sendMessage("[DeathPenalty] I'm sorry, but you do not have permossion to perform this command.");
			}
			plugin.reloadConfig();
			plugin.loadConfig();
			sender.sendMessage("[DeathPenalty] Configuration has been reloaded.");
			return true;
		}
		return true;
	}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    	if (!cmd.getName().equalsIgnoreCase("DeathPenalty")) {
            return null;
        }
    	for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }
    	if(args.length == 1){
        	List<String> completions = new ArrayList<String>();
        	completions.add("reload");
        	return completions;
    	}
    	return null;
    }
}
