package me.mikebloc.chunkShield.listeners;

import me.mikebloc.chunkShield.main;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Door;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


public final class blockPlaceCheck implements Listener
{
    public static class Global
    {
        // Some blocks/items do not translate correctly when limited.
        // Ex: REDSTONE is not REDSTONE_WIRE
        public static List<Material> nonItemBlocks = List.of
                (
                        Material.REDSTONE_WIRE,
                        Material.TRIPWIRE
                );
    }



    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e)
    {
        Material placedType = e.getBlock().getType();
        Chunk chunk = e.getBlock().getChunk();
        Block block = e.getBlock();
        Material material = block.getType();
        Player player = e.getPlayer();
        PlayerInventory inventory = e.getPlayer().getInventory();
        ItemStack secondHand = inventory.getItemInOffHand();



        int x = e.getBlock().getX();
        int y = e.getBlock().getY();
        int z = e.getBlock().getZ();

        String playerName = e.getPlayer().getName();

        // Patch to prevent unnecessary portal destruction.
        if (block.getType() == Material.ENDER_PORTAL_FRAME)
        {
            if (main.Global.configToggleEndPortalFix) againstEndPortalCheck(e, player, secondHand, inventory);
            else
            {
                if (main.Global.configToggleBlockCheck_50)
                {
                    if (ThreadLocalRandom.current().nextBoolean()) blockChunkCheck(e, chunk, placedType, x, y, z, playerName, material, block, player);
                }
                else blockChunkCheck(e, chunk, placedType, x, y, z, playerName, material, block, player);
            }

        }
        else if (main.Global.configToggleBlockCheck)
        {
            if (main.Global.configToggleBlockCheck_50)
            {
                if (ThreadLocalRandom.current().nextBoolean()) blockChunkCheck(e, chunk, placedType, x, y, z, playerName, material, block, player);
            }
            else blockChunkCheck(e, chunk, placedType, x, y, z, playerName, material, block, player);
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    //No need for owner to block End Portal Frames
    //This is a fix/feature for if End Portal Frames get blocked.
    //If normally limited, it breaks end portals.
    //This however, prevents placing them without ruining portals.
    //Why the fix? If you interact with a portal, that can also trigger a removal if limited bc the frame block is updated.
    private static void againstEndPortalCheck(BlockPlaceEvent e, Player player, ItemStack secondHand, PlayerInventory inventory)
    {
        if (e.getBlockAgainst().getType() == Material.ENDER_PORTAL_FRAME)
        {
            // Main Hand Check
            if (player.getInventory().getItemInMainHand().getType() == Material.EYE_OF_ENDER)
            {
                // Check1
                if (secondHand.getType() == Material.ENDER_PORTAL_FRAME)
                {
                    if (player.hasPermission("chunkShield.blockBypass")) return;
                    e.setCancelled(true);
                    main.Global.blocksPrevented++;
                    inventory.setItemInOffHand(null);
                    inventory.remove(Material.ENDER_PORTAL_FRAME);
                }
            }
            // Offhand Check
            else if (player.getInventory().getItemInOffHand().getType() == Material.EYE_OF_ENDER)
            {
                // Check1
                if (player.getInventory().getItemInMainHand().getType() == Material.ENDER_PORTAL_FRAME)
                {
                    if (player.hasPermission("chunkShield.blockBypass")) return;
                    e.setCancelled(true);
                    main.Global.blocksPrevented++;
                    inventory.remove(Material.ENDER_PORTAL_FRAME);
                }
            }
        }
        else
        {
            if (player.hasPermission("chunkShield.blockBypass")) return;
            e.setCancelled(true);
            main.Global.blocksPrevented++;
            inventory.remove(Material.ENDER_PORTAL_FRAME);
            if (secondHand.getType() == Material.ENDER_PORTAL_FRAME) inventory.setItemInOffHand(null);
        }
    }
    /////////////////////////////////////////////////////////////////////////////
    private static void blockChunkCheck(BlockPlaceEvent e, Chunk chunk, Material placedType, int x, int y, int z, String playerName, Material material, Block block, Player player)
    {
        // 1.0.5 Fix. If the list contains the block placed then check, otherwise don't.
        if (main.Global.theBlockLimits.containsKey(material))
        {
            World world = chunk.getWorld();
            World.Environment environment = chunk.getWorld().getEnvironment();

            // Patch to make sure we don't destroy natural Bedrock.
            if (environment == World.Environment.NORMAL)
            {
                main.Global.minY = 5;
                main.Global.maxY = world.getMaxHeight();
            }
            else if (environment == World.Environment.NETHER)
            {
                main.Global.minY = 5;
                main.Global.maxY = 122;
            }
            else if (environment == World.Environment.THE_END)
            {
                main.Global.minY = 0;
                main.Global.maxY = 128;
            }

            // Iterate each configured block type and prune extras within this chunk
            for (Map.Entry<Material, Integer> rule : main.Global.theBlockLimits.entrySet())
            {
                Material target = rule.getKey();
                int limit = Math.max(0, rule.getValue());

                // Collect all blocks of this type in deterministic order
                List<Block> matches = new ArrayList<>();
                for (int yLevel = main.Global.minY; yLevel < main.Global.maxY; yLevel++)
                {
                    for (int xLevel = 0; xLevel < 16; xLevel++)
                    {
                        for (int zLevel = 0; zLevel < 16; zLevel++)
                        {
                            Block b = chunk.getBlock(xLevel, yLevel, zLevel);
                            if (b.getType() == target) matches.add(b);
                        }
                    }
                }

                // Remove extras beyond the limit
                if (matches.size() > limit)
                {
                    for (int i = limit; i < matches.size(); i++)
                    {
                        Block b = matches.get(i);
                        if (placedType.isItem())
                        {
                            if (main.Global.configToggleAlertBlockLimit) alertBLOCKLimitReached(x, y, z, playerName, b, world);

                            if (player.hasPermission("chunkShield.blockBypass")) return;
                            if (player.getGameMode() != GameMode.CREATIVE)b.breakNaturally();
                            else b.setType(Material.AIR, false);
                            if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, b.getLocation().toCenterLocation(), 4);

                        }
                        else
                        {
                            if (main.Global.configToggleAlertBlockLimit) alertBLOCKLimitReached(x, y, z, playerName, b, world);
                            if (player.hasPermission("chunkShield.blockBypass")) return;

                            // 1.0.7 Fix - Material.REDSTONE_WIRE is not considered TRUE in Material.isItem().
                            // This fix has been updated to handle any non-item materials from an added list above.
                            // Such as STRING being transformed to TRIPWIRE
                            if (Global.nonItemBlocks.contains(b.getType()) && main.Global.theBlockLimits.containsKey(b.getType()))
                            {
                                if (player.getGameMode() != GameMode.CREATIVE)b.breakNaturally();
                                else b.setType(Material.AIR, false);
                                if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, b.getLocation().toCenterLocation(), 4);
                            }
                            else b.setType(Material.STONE, false); // no physics to avoid cascades
                        }
                        main.Global.blocksPrevented++;
                    }
                }
            }
        }
        // ===== 2) BLOCKS: collective DOOR/TRAPDOOR cap =====
        else if (material.name().endsWith("_DOOR") || material.name().endsWith("DOOR") || material.name().endsWith("_GATE"))
        {
            if (main.Global.configCollectiveDoorLimit >= 0)
            {
                World world = chunk.getWorld();
                World.Environment environment = chunk.getWorld().getEnvironment();

                if (environment == World.Environment.NORMAL)
                {
                    main.Global.minY = -59;
                    main.Global.maxY = world.getMaxHeight();
                }
                else if (environment == World.Environment.NETHER)
                {
                    main.Global.minY = 2;
                    main.Global.maxY = 125;
                }
                else if (environment == World.Environment.THE_END)
                {
                    main.Global.minY = 0;
                    main.Global.maxY = world.getMaxHeight();
                }

                int doorCount = 0;

                // We will remove anything beyond the cap. Deterministic scan order.
                for (int yLevel = main.Global.minY; yLevel < main.Global.maxY; yLevel++)
                {
                    for (int xLevel = 0; xLevel < 16; xLevel++)
                    {
                        for (int zLevel = 0; zLevel < 16; zLevel++)
                        {
                            Block b1 = chunk.getBlock(xLevel, yLevel, zLevel);
                            if (b1.getType().name().endsWith("_DOOR"))
                            {
                                doorCount++;
                                if (doorCount > main.Global.configCollectiveDoorLimit)
                                {
                                    if (main.Global.configToggleAlertBlockLimit) alertDOORLimitReached(x, y, z, playerName, world);

                                    //removed 1.0.10 fix in this port. Discerning halves of doors is not possible.
                                    b1.breakNaturally();
                                    if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, b1.getLocation().toCenterLocation(), 4);
                                    main.Global.blocksPrevented++;
                                }
                            }
                            else if (b1.getType().name().endsWith("DOOR") || b1.getType().name().endsWith("_GATE"))
                            {
                                doorCount++;
                                if (doorCount > main.Global.configCollectiveDoorLimit)
                                {
                                    if (main.Global.configToggleAlertBlockLimit) alertDOORLimitReached(x, y, z, playerName, world);

                                    if (player.getGameMode() != GameMode.CREATIVE)b1.breakNaturally();
                                    else b1.setType(Material.AIR, false);
                                    if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, b1.getLocation().toCenterLocation(), 4);
                                    main.Global.blocksPrevented++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void alertBLOCKLimitReached(int x, int y, int z, String playerName, Block b, World world)
    {
        TextComponent STYLE = new TextComponent("§c■ - - - - - - - - - - - - - - - - - - - - - - - - - ■");
        TextComponent message = new TextComponent("§c■ " + "§6" + playerName + " §ereached " + "§c" + b.getType() +" §elimit at§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■");
        TextComponent sub = new TextComponent("§c■ " + "§6Location§7: §a" + world.getName() + " §7/ §6" + x + ", " + y + ", " + z);

        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            if (player.hasPermission("chunkShield.alerts")) player.spigot().sendMessage(STYLE);
            if (player.hasPermission("chunkShield.alerts")) player.spigot().sendMessage(message);
            if (player.hasPermission("chunkShield.alerts")) player.spigot().sendMessage(sub);
            if (player.hasPermission("chunkShield.alerts")) player.spigot().sendMessage(STYLE);

            Bukkit.getServer().getLogger().warning(STYLE.getText());
            Bukkit.getServer().getLogger().warning(message.getText());
            Bukkit.getServer().getLogger().warning(sub.getText());
            Bukkit.getServer().getLogger().warning(STYLE.getText());
        }
    }

    private static void alertDOORLimitReached(int x, int y, int z, String playerName, World world)
    {
        TextComponent STYLE = new TextComponent("§c■ - - - - - - - - - - - - - - - - - - - - - - - - - ■");
        TextComponent message = new TextComponent("§c■ " + "§6" + playerName + " §ereached " + "§cDoor Limit §eat§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■");
        TextComponent sub = new TextComponent("§c■ " + "§6Location§7: §a" + world.getName() + " §7/ §6" + x + ", " + y + ", " + z);

        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            if (player.hasPermission("chunkShield.alerts")) player.spigot().sendMessage(STYLE);
            if (player.hasPermission("chunkShield.alerts")) player.spigot().sendMessage(message);
            if (player.hasPermission("chunkShield.alerts")) player.spigot().sendMessage(sub);
            if (player.hasPermission("chunkShield.alerts")) player.spigot().sendMessage(STYLE);

            Bukkit.getServer().getLogger().warning(STYLE.getText());
            Bukkit.getServer().getLogger().warning(message.getText());
            Bukkit.getServer().getLogger().warning(sub.getText());
            Bukkit.getServer().getLogger().warning(STYLE.getText());
        }
    }
}
