package me.mikebloc.chunkShield.languages;

import me.mikebloc.chunkShield.main;
import org.bukkit.command.CommandSender;
import static org.bukkit.Bukkit.getLogger;

public class EN
{

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// main Class Messages.
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Invalid entity type in config:
    public static void main_ConsoleInvalidConfigEntity(String key)
    {
        getLogger().warning("Invalid entity type in config: " + key);
    }
    // Invalid entity type in named-entity-limits:
    public static void main_ConsoleInvalidConfigNamedEntity(String key)
    {
        getLogger().warning("Invalid entity type in named-entity-limits: " + key);
    }
    // Invalid block type in config:
    public static void main_ConsoleInvalidConfigBlock(String key)
    {
        getLogger().warning("Invalid block type in config: " + key);
    }
    // Invalid Action. No Permission.
    public static void main_NoPermission(CommandSender sender)
    {
        sender.sendMessage("§cInvalid Action. No Permission.");
    }
    // §7[§6ChunkShield§7] §aConfig reloaded.
    public static void main_ConfigReloaded(CommandSender sender)
    {
        sender.sendMessage("§7[§6ChunkShield§7] §aConfig reloaded.");
    }
    // Stats Subcommand
    public static void main_Subcommand_Stats(CommandSender sender)
    {
        sender.sendMessage("§a■ §7- - - - - - - - - - - - - - - - - - - - - - - - - §a■"
                + "\n§a■ §6ChunkShield Stats since §aLast Restart§7:" + "\n§a■"
                + "\n§a■ §3" + main.Global.chunkCount + " §6Total Chunks Scanned"
                + "\n§a■ §3" + main.Global.blocksPrevented + " §eTotal Blocks Prevented"
                + "\n§a■ §3" + main.Global.entitiesRemoved + " §6Total Entities Prevented"
                + "\n§a■ §3" + main.Global.vehiclesPrevented + " §eTotal Vehicles Prevented"
                + "\n§a■ §7- - - - - - - - - - - - - - - - - - - - - - - - - §a■");
    }
}
