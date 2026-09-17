package me.mikebloc.chunkShield.listeners;

import me.mikebloc.chunkShield.languages.ES;
import me.mikebloc.chunkShield.languages.RU;
import me.mikebloc.chunkShield.main;
import me.mikebloc.chunkShield.languages.EN;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Gate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

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

        @NotNull String playerName = e.getPlayer().getName();

        // Patch to prevent unnecessary portal destruction.
        if (block.getType() == Material.END_PORTAL_FRAME)
        {
            if (main.Global.configToggleEndPortalFix) againstEndPortalCheck(e, player, secondHand, inventory);
            else
            {
                if (main.Global.configToggleBlockCheck_50)
                {
                    if (ThreadLocalRandom.current().nextBoolean()) blockChunkCheck(chunk, placedType, x, y, z, playerName, material, block, player);
                }
                else blockChunkCheck(chunk, placedType, x, y, z, playerName, material, block, player);
            }

        }
        else if (main.Global.configToggleBlockCheck)
        {
            if (main.Global.configToggleBlockCheck_50)
            {
                if (ThreadLocalRandom.current().nextBoolean()) blockChunkCheck(chunk, placedType, x, y, z, playerName, material, block, player);
            }
            else blockChunkCheck(chunk, placedType, x, y, z, playerName, material, block, player);
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
        if (player.hasPermission("chunkShield.blockBypass")) return;
        if (e.getBlockAgainst().getType() == Material.END_PORTAL_FRAME)
        {
            // Main Hand Check
            if (player.getInventory().getItemInMainHand().getType() == Material.ENDER_EYE)
            {
                // Check1
                if (secondHand.getType() == Material.END_PORTAL_FRAME)
                {
                    e.setCancelled(true);
                    main.Global.blocksPrevented++;
                    inventory.setItemInOffHand(null);
                    inventory.remove(Material.END_PORTAL_FRAME);
                }
            }
            // Offhand Check
            else if (player.getInventory().getItemInOffHand().getType() == Material.ENDER_EYE)
            {
                // Check1
                if (player.getInventory().getItemInMainHand().getType() == Material.END_PORTAL_FRAME)
                {
                    e.setCancelled(true);
                    main.Global.blocksPrevented++;
                    inventory.remove(Material.END_PORTAL_FRAME);
                }
            }
        }
        else
        {
            e.setCancelled(true);
            main.Global.blocksPrevented++;
            inventory.remove(Material.END_PORTAL_FRAME);
            if (secondHand.getType() == Material.END_PORTAL_FRAME) inventory.setItemInOffHand(null);
        }
    }
    /////////////////////////////////////////////////////////////////////////////
    private static void blockChunkCheck(Chunk chunk, Material placedType, int x, int y, int z, String playerName, Material material, Block block, Player player)
    {
        if (player.hasPermission("chunkShield.blockBypass")) return;
        // 1.0.5 Fix. If the list contains the block placed then check, otherwise don't.
        if (main.Global.theBlockLimits.containsKey(material))
        {
            World world = chunk.getWorld();
            World.Environment environment = chunk.getWorld().getEnvironment();

            // Patch to make sure we don't destroy natural Bedrock.
            if (environment == World.Environment.NORMAL)
            {
                int height;
                if (world.getMinHeight() < -5) height = -59;
                else height = 5;

                main.Global.minY = height;
                main.Global.maxY = world.getMaxHeight();
            }
            else if (environment == World.Environment.NETHER)
            {
                main.Global.minY = 5;
                main.Global.maxY = 122;
            }
            else if (environment == World.Environment.THE_END)
            {
                main.Global.minY = world.getMinHeight();
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
                            
                            if (player.getGameMode() != GameMode.CREATIVE)b.breakNaturally();
                            else b.setType(Material.AIR, false);
                            if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, b.getLocation().toCenterLocation(), 4);

                        }
                        else
                        {
                            if (main.Global.configToggleAlertBlockLimit) alertBLOCKLimitReached(x, y, z, playerName, b, world);

                            // 1.0.7 Fix - Material.REDSTONE_WIRE is not considered TRUE in Material.isItem().
                            // This fix has been updated to handle any non-item materials from an added list above.
                            // Such as STRING being transformed to TRIPWIRE
                            if (Global.nonItemBlocks.contains(b.getType()) && main.Global.theBlockLimits.containsKey(b.getType()))
                            {
                                if (player.getGameMode() == GameMode.CREATIVE) b.setType(Material.AIR);
                                else b.breakNaturally();
                                if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, b.getLocation().toCenterLocation(), 4);
                            }
                            else
                            {
                                b.setType(Material.STONE, false); // no physics to avoid cascades

                                player.getServer().getLogger().warning("!!! --- NON-ITEM BLOCK --- !!!  ");
                                player.getServer().getLogger().warning("■");
                                player.getServer().getLogger().warning(b.getType() + " just turned into Stone.");
                                player.getServer().getLogger().warning("Location: " + world.getName() + " [" + x + ", " + y + ", " + z + "]");
                                player.getServer().getLogger().warning("Report this message to developer to be fixed.");
                                player.getServer().getLogger().warning("ERROR: " + b.getType() + " is a non-item block.");
                                player.getServer().getLogger().warning("■");
                                player.getServer().getLogger().warning("!!! --- NON-ITEM BLOCK --- !!!  ");
                            }
                        }
                        main.Global.blocksPrevented++;
                    }
                }
            }
        }
        // Collective Check (Doors | Trapdoors | Gates)
        else if (block.getBlockData() instanceof Door || block.getBlockData() instanceof TrapDoor || block.getBlockData() instanceof Gate)
        {
            if (main.Global.configCollectiveDoorLimit >= 0)
            {
                World world = chunk.getWorld();
                World.Environment environment = chunk.getWorld().getEnvironment();

                if (environment == World.Environment.NORMAL)
                {
                    int height1;
                    if (world.getMinHeight() < -5) height = -62;
                    else height = 2;

                    main.Global.minY = height1;
                    main.Global.maxY = world.getMaxHeight();
                }
                else if (environment == World.Environment.NETHER)
                {
                    //Slightly lower to prevent doors and gates.
                    main.Global.minY = 2;
                    main.Global.maxY = 125;
                }
                else if (environment == World.Environment.THE_END)
                {
                    main.Global.minY = world.getMinHeight();
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
                            BlockData data = b1.getBlockData();
                            if (data instanceof Door d)
                            {
                                if (d.getHalf() == Door.Half.TOP) continue;
                                doorCount++;
                                if (doorCount > main.Global.configCollectiveDoorLimit)
                                {
                                    if (main.Global.configToggleAlertBlockLimit) alertDOORLimitReached(x, y, z, playerName, world);

                                    if (player.getGameMode() != GameMode.CREATIVE)b1.breakNaturally();
                                    else
                                    {
                                        b1.setType(Material.AIR, false);
                                        // 1.0.10 Fix - Top Half of doors would remain when placed/limited in Creative.
                                        Block b2 = world.getBlockAt(b1.getX(), b1.getY()+1, b1.getZ());
                                        b2.setType(Material.AIR, false);
                                    }
                                    if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, b1.getLocation().toCenterLocation(), 4);
                                    main.Global.blocksPrevented++;
                                }
                            }
                            else if (data instanceof TrapDoor || data instanceof Gate)
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
        ClickEvent copyCoords = ClickEvent.copyToClipboard(x + " " + y + " " + z);

        // English Message Workflow
        if(main.Global.configLanguageType == 1)
        {
            HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
            Component primaryMessage = EN.blockPlaceCheck_alertBlockLimitReached(playerName, b, copyCoords, hoverCoords);
            EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
        }
        // Spanish Message Workflow
        else if(main.Global.configLanguageType == 2)
        {
            HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(ES.ClickCopy, NamedTextColor.GREEN));
            Component primaryMessage = ES.blockPlaceCheck_alertBlockLimitReached(playerName, b, copyCoords, hoverCoords);
            ES.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
        }
        // Russian Message Workflow
        else if(main.Global.configLanguageType == 3)
        {
            HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(RU.ClickCopy, NamedTextColor.GREEN));
            Component primaryMessage = RU.blockPlaceCheck_alertBlockLimitReached(playerName, b, copyCoords, hoverCoords);
            RU.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
        }
    }

    private static void alertDOORLimitReached(int x, int y, int z, String playerName, World world)
    {
        ClickEvent copyCoords = ClickEvent.copyToClipboard(x + " " + y + " " + z);

        if(main.Global.configLanguageType == 1)
        {
            // English Message Workflow
            HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
            Component primaryMessage = EN.blockPlaceCheck_alertDoorLimitReached(playerName, copyCoords, hoverCoords);
            EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
        }
        else if(main.Global.configLanguageType == 2)
        {
            // Spanish Message Workflow
            HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(ES.ClickCopy, NamedTextColor.GREEN));
            Component primaryMessage = ES.blockPlaceCheck_alertDoorLimitReached(playerName, copyCoords, hoverCoords);
            ES.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
        }
        else if(main.Global.configLanguageType == 3)
        {
            // Russian Message Workflow
            HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(RU.ClickCopy, NamedTextColor.GREEN));
            Component primaryMessage = RU.blockPlaceCheck_alertDoorLimitReached(playerName, copyCoords, hoverCoords);
            RU.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
        }
    }
}
