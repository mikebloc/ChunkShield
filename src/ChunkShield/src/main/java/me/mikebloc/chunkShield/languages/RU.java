package me.mikebloc.chunkShield.languages;

import me.mikebloc.chunkShield.main;
import org.bukkit.command.CommandSender;
import static org.bukkit.Bukkit.getLogger;

public class RU
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// main Class Messages.
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Invalid entity type in config:
    public static void main_ConsoleInvalidConfigEntity(String key)
    {
        getLogger().warning("Неверный тип объекта в конфигурации: " + key);
    }
    // Invalid entity type in named-entity-limits:
    public static void main_ConsoleInvalidConfigNamedEntity(String key)
    {
        getLogger().warning("Неверный тип объекта named-entity-limits: " + key);
    }
    // Invalid block type in config:
    public static void main_ConsoleInvalidConfigBlock(String key)
    {
        getLogger().warning("Неверный тип блока в конфигурации: " + key);
    }
    // Invalid Action. No Permission.
    public static void main_NoPermission(CommandSender sender)
    {
        sender.sendMessage("§cНедопустимое действие. Нет разрешения.");
    }
    // §7[§6ChunkShield§7] §aConfig reloaded.
    public static void main_ConfigReloaded(CommandSender sender)
    {
        sender.sendMessage("§7[§6ChunkShield§7] §aКонфигурация была перезагружена.");
    }
    // Stats Subcommand
    public static void main_Subcommand_Stats(CommandSender sender)
    {
        sender.sendMessage("§a■ §7- - - - - - - - - - - - - - - - - - - - - - - - - §a■"
                + "\n§a■ §6ChunkShield Stats §aС момента последней перезагрузки§7:" + "\n§a■"
                + "\n§a■ §3" + main.Global.chunkCount + " §6Обследованные территории"
                + "\n§a■ §3" + main.Global.blocksPrevented + " §eTЗаблокированные блоки"
                + "\n§a■ §3" + main.Global.entitiesRemoved + " §6Заблокированные субъекты"
                + "\n§a■ §3" + main.Global.vehiclesPrevented + " §eУбранные транспортные средства"
                + "\n§a■ §7- - - - - - - - - - - - - - - - - - - - - - - - - §a■");
    }
}
