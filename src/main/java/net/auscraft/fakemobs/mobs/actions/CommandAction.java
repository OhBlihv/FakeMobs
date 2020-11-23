package net.auscraft.fakemobs.mobs.actions;

import net.auscraft.skycore.util.BUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class CommandAction extends BaseAction
{
	
	public enum CommandExecutor
	{
		
		PLAYER,
		CONSOLE;
		
	}
	
	@RequiredArgsConstructor
	public class CommandObject
	{
		
		private final String command;
		private final CommandExecutor commandExecutor;
		
	}
	
	private final List<CommandObject> commands = new ArrayList<>();
	
	public CommandAction(ConfigurationSection configurationSection)
	{
		for(String command : configurationSection.getKeys(false))
		{
			ConfigurationSection commandSection = configurationSection.getConfigurationSection(command);
			
			String executorString = commandSection.getString("execute-as");
			CommandExecutor commandExecutor;
			try
			{
				commandExecutor = CommandExecutor.valueOf(executorString);
			}
			catch(IllegalArgumentException e)
			{
				commandExecutor = CommandExecutor.CONSOLE;
				BUtil.logInfo("Could not determine executor at " + commandSection.getCurrentPath() + ". Defaulting to CONSOLE.");
			}
			
			commands.add(new CommandObject(command, commandExecutor));
		}
	}
	
	@Override
	public void doAction(Player player)
	{
		for(CommandObject commandObject : commands)
		{
			String translatedLine = commandObject.command.replace("{player}", player.getName());
			
			if(commandObject.commandExecutor == CommandExecutor.PLAYER)
			{
				player.chat("/" + translatedLine);
			}
			else// if(commandExecutor == CommandExecutor.CONSOLE)
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), translatedLine);
			}
		}
	}
	
}
