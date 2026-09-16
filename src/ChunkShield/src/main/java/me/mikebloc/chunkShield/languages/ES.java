package me.mikebloc.chunkShield.languages;

import me.mikebloc.chunkShield.main;
import org.bukkit.command.CommandSender;
import static org.bukkit.Bukkit.getLogger;

public class ES
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// main Class Messages.
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Invalid entity type in config:
    public static void main_ConsoleInvalidConfigEntity(String key)
    {
        getLogger().warning("Tipo de entidad no válido en la configuración: " + key);
    }
    // Invalid entity type in named-entity-limits:
    public static void main_ConsoleInvalidConfigNamedEntity(String key)
    {
        getLogger().warning("Tipo de entidad no válido en named-entity-limits: " + key);
    }
    // Invalid block type in config:
    public static void main_ConsoleInvalidConfigBlock(String key)
    {
        getLogger().warning("Tipo de bloque no válido en la configuración: " + key);
    }
    // Invalid Action. No Permission.
    public static void main_NoPermission(CommandSender sender)
    {
        sender.sendMessage("§cAcción no válida. Sin permiso.");
    }
    // §7[§6ChunkShield§7] §aConfig reloaded.
    public static void main_ConfigReloaded(CommandSender sender)
    {
        sender.sendMessage("§7[§6ChunkShield§7] §aLa configuración se ha actualizado.");
    }
    // Stats Subcommand
    public static void main_Subcommand_Stats(CommandSender sender)
    {
        sender.sendMessage("§a■ §7- - - - - - - - - - - - - - - - - - - - - - - - - §a■"
                + "\n§a■ §6ChunkShield Stats §aDesde la última reinicio§7:" + "\n§a■"
                + "\n§a■ §3" + main.Global.chunkCount + " §6Áreas analizadas"
                + "\n§a■ §3" + main.Global.blocksPrevented + " §eBloques eliminados"
                + "\n§a■ §3" + main.Global.entitiesRemoved + " §6Entidades eliminadas"
                + "\n§a■ §3" + main.Global.vehiclesPrevented + " §eVehículos eliminadas"
                + "\n§a■ §7- - - - - - - - - - - - - - - - - - - - - - - - - §a■");
    }
}
