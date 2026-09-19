package me.mikebloc.chunkShield;
import me.mikebloc.chunkShield.languages.EN;
import me.mikebloc.chunkShield.listeners.blockPlaceCheck;
import me.mikebloc.chunkShield.listeners.entitySummonsCheck;
import me.mikebloc.chunkShield.listeners.vehicleSummonsCheck;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class main extends JavaPlugin implements Listener
{
    private final Map<EntityType, Integer> entityLimits = new HashMap<>();
    private final Map<Material, Integer> blockLimits = new HashMap<>();
    private final Map<EntityType, Integer> namedEntityLimits = new HashMap<>();

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new blockPlaceCheck(), this);
        getServer().getPluginManager().registerEvents(new entitySummonsCheck(), this);
        getServer().getPluginManager().registerEvents(new vehicleSummonsCheck(), this);

        saveDefaultConfig();
        loadLimitsFromConfig();
        getLogger().info("ChunkShield enabled.");
        Objects.requireNonNull(getCommand("reloadconfig")).setExecutor(this);
        Objects.requireNonNull(getCommand("statscommand")).setExecutor(this);

        Global.configMinEntityLimit = this.getConfig().getInt("MinimumEntityRequirement");
        Global.configMinEntityWarning = this.getConfig().getInt("WarningRequirement");
        Global.configToggleScanChunkUponLoad = this.getConfig().getBoolean("Scan-Chunks-Upon-Loading");
        Global.configToggleScanChunkUponLoad_50 = this.getConfig().getBoolean("Scan-Chunks-Upon-Loading-50%");
        Global.configToggleBlockCheck = this.getConfig().getBoolean("BlockCheck");
        Global.configToggleBlockCheck_50 = this.getConfig().getBoolean("BlockCheck-50%");
        Global.configToggleEntityCheck = this.getConfig().getBoolean("EntityCheck");
        Global.configToggleEntityCheck_50 = this.getConfig().getBoolean("EntityCheck-50%");
        Global.configToggleScanChunkUponCrafting = this.getConfig().getBoolean("Scan-Chunk-Upon-Crafting");
        Global.configToggleScanChunkUponEntityDying = this.getConfig().getBoolean("Scan-Chunk-Upon-Entity-Dying");
        Global.configToggleScanChunkUponOpeningContainer = this.getConfig().getBoolean("Scan-Chunk-Upon-Opening-Container");
        Global.configToggleVehicleRadiusCheck = this.getConfig().getBoolean("VehicleRadiusCheck");
        Global.configToggleEndPortalFix = this.getConfig().getBoolean("end-portal-fix");
        Global.configCollectiveDoorLimit = this.getConfig().getInt("ChunkDoorLimit");
        Global.configCollectiveVehicletLimit = this.getConfig().getInt("ChunkVehicleLimit");
        Global.configRadiusLimit = this.getConfig().getInt("vehicle-radius");

        Global.configToggleAlertChunkScanned = this.getConfig().getBoolean("ChunkScanned");
        Global.configToggleAlertChunkWarning = this.getConfig().getBoolean("ChunkWarning");
        Global.configToggleAlertBlockLimit = this.getConfig().getBoolean("BlockLimits");
        Global.configToggleAlertEntityLimit = this.getConfig().getBoolean("EntityLimits");
        Global.configToggleAlertVehicleLimit = this.getConfig().getBoolean("VehicleLimits");

        Global.configTogglePurgeEffect = this.getConfig().getBoolean("PurgeEffects");
        Global.configLanguageType = 1;

    }
    ////////////////////////////////////////////////////////////////////////////
    public static class Global
    {
        public static int configRadiusLimit;
        public static int configCollectiveDoorLimit;
        public static boolean configToggleEndPortalFix;
        public static int configCollectiveVehicletLimit;

        public static int configMinEntityLimit;
        public static int configMinEntityWarning;
        public static boolean configToggleScanChunkUponLoad;
        public static boolean configToggleScanChunkUponLoad_50;
        public static boolean configToggleBlockCheck;
        public static boolean configToggleBlockCheck_50;
        public static boolean configToggleEntityCheck;
        public static boolean configToggleEntityCheck_50;
        public static boolean configToggleScanChunkUponCrafting;
        public static boolean configToggleScanChunkUponEntityDying;
        public static boolean configToggleScanChunkUponOpeningContainer;
        public static boolean configToggleVehicleRadiusCheck;

        public static Map<Material, Integer> theBlockLimits;
        public static Map<EntityType, Integer> theEntityLimits;
        public static Map<EntityType, Integer> theNamedEntityLimits;

        public static int minY = 0;
        public static int maxY = 0;
        public static long chunkCount = 0;
        public static long blocksPrevented = 0;
        public static long entitiesRemoved = 0;
        public static long vehiclesPrevented = 0;

        public static int Entity_vehicleCount = 0;

        public static boolean configToggleAlertChunkScanned;
        public static boolean configToggleAlertChunkWarning;
        public static boolean configToggleAlertBlockLimit;
        public static boolean configToggleAlertEntityLimit;
        public static boolean configToggleAlertVehicleLimit;

        public static boolean configTogglePurgeEffect;
        public static int configLanguageType;
    }

    ////////////////////////////////////////////////////////////////////////////
    private void loadLimitsFromConfig()
    {
        FileConfiguration config = getConfig();
        blockLimits.clear();
        entityLimits.clear();
        namedEntityLimits.clear();

        Global.configMinEntityLimit = this.getConfig().getInt("MinimumEntityRequirement");
        Global.configMinEntityWarning = this.getConfig().getInt("WarningRequirement");
        Global.configToggleScanChunkUponLoad = this.getConfig().getBoolean("Scan-Chunks-Upon-Loading");
        Global.configToggleScanChunkUponLoad_50 = this.getConfig().getBoolean("Scan-Chunks-Upon-Loading-50%");
        Global.configToggleBlockCheck = this.getConfig().getBoolean("BlockCheck");
        Global.configToggleBlockCheck_50 = this.getConfig().getBoolean("BlockCheck-50%");
        Global.configToggleEntityCheck = this.getConfig().getBoolean("EntityCheck");
        Global.configToggleEntityCheck_50 = this.getConfig().getBoolean("EntityCheck-50%");
        Global.configToggleScanChunkUponCrafting = this.getConfig().getBoolean("Scan-Chunk-Upon-Crafting");
        Global.configToggleScanChunkUponEntityDying = this.getConfig().getBoolean("Scan-Chunk-Upon-Entity-Dying");
        Global.configToggleScanChunkUponOpeningContainer = this.getConfig().getBoolean("Scan-Chunk-Upon-Opening-Container");
        Global.configToggleVehicleRadiusCheck = this.getConfig().getBoolean("VehicleRadiusCheck");
        Global.configToggleEndPortalFix = this.getConfig().getBoolean("end-portal-fix");
        Global.configCollectiveDoorLimit = this.getConfig().getInt("ChunkDoorLimit");
        Global.configCollectiveVehicletLimit = this.getConfig().getInt("ChunkVehicleLimit");
        Global.configRadiusLimit = this.getConfig().getInt("vehicle-radius");

        Global.configToggleAlertChunkScanned = this.getConfig().getBoolean("ChunkScanned");
        Global.configToggleAlertChunkWarning = this.getConfig().getBoolean("ChunkWarning");
        Global.configToggleAlertBlockLimit = this.getConfig().getBoolean("BlockLimits");
        Global.configToggleAlertEntityLimit = this.getConfig().getBoolean("EntityLimits");
        Global.configToggleAlertVehicleLimit = this.getConfig().getBoolean("VehicleLimits");

        Global.configTogglePurgeEffect = this.getConfig().getBoolean("PurgeEffects");
        Global.configLanguageType = this.getConfig().getInt("lang");

        ConfigurationSection section0 = config.getConfigurationSection("entity-limits");
        if (section0 != null)
        {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("entity-limits")).getKeys(false))
            {
                try
                {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    int eLimit = section0.getInt(key);
                    entityLimits.put(type, eLimit);
                }
                catch (IllegalArgumentException e)
                {
                    // TRANSLATE: Invalid entity type in config:
                    if (Global.configLanguageType == 1) EN.main_ConsoleInvalidConfigEntity(key);
                }
            }
            Global.theEntityLimits = entityLimits;
        }

        ConfigurationSection section1 = config.getConfigurationSection("block-limits");
        if (section1 != null)
        {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("block-limits")).getKeys(false))
            {
                try
                {
                    Material material = Material.valueOf(key.toUpperCase());
                    int bLimit = section1.getInt(key);
                    blockLimits.put(material, bLimit);

                    if (material.asItemType() instanceof Door || material.asItemType() instanceof TrapDoor || material.asItemType() instanceof Gate)
                    {
                        if (Global.configCollectiveVehicletLimit != -1)
                        {
                            getLogger().warning("!!! --- CONFIG ERROR --- !!!  ");
                            getLogger().warning("■");
                            getLogger().warning(key + " is already covered by CollectiveDoorLimit.");
                            getLogger().warning("CollectiveDoorLimit covers all doors/gates/trapdoors.");
                            getLogger().warning("If this is intended, set CollectiveDoorLimit to -1 to ignore.");
                            getLogger().warning("■");
                            getLogger().warning("!!! --- CONFIG ERROR --- !!!  ");
                        }
                    }

                    if (material == Material.REDSTONE)
                    {
                        getLogger().warning("!!! --- CONFIG ERROR --- !!!  ");
                        getLogger().warning("■");
                        getLogger().warning(key + " does not limit Placed REDSTONE_WIRE.");
                        getLogger().warning("Must use: REDSTONE_WIRE");
                        getLogger().warning("If this is intended, ignore.");
                        getLogger().warning("■");
                        getLogger().warning("!!! --- CONFIG ERROR --- !!!  ");
                    }
                    if (material == Material.END_PORTAL_FRAME)
                    {
                        getLogger().warning("!!! --- CONFIG ERROR --- !!!  ");
                        getLogger().warning("■");
                        getLogger().warning(key + " will break natural End Portals.");
                        getLogger().warning("Enable 'end-portal-fix' in config instead.");
                        getLogger().warning("If this is intended, ignore.");
                        getLogger().warning("■");
                        getLogger().warning("!!! --- CONFIG ERROR --- !!!  ");
                    }
                }
                catch (IllegalArgumentException e)
                {
                    // TRANSLATE: Invalid block type in config:
                    if (Global.configLanguageType == 1) EN.main_ConsoleInvalidConfigBlock(key);
                }
            }
            Global.theBlockLimits = blockLimits;
        }

        ConfigurationSection section2 = getConfig().getConfigurationSection("named-entity-limits");
        if (section2 != null)
        {
            for (String key : section2.getKeys(false))
            {
                try
                {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    int limit = section2.getInt(key);
                    namedEntityLimits.put(type, limit);
                }
                catch (IllegalArgumentException e)
                {
                    // TRANSLATE: Invalid entity type in named-entity-limits:
                    if (Global.configLanguageType == 1) EN.main_ConsoleInvalidConfigNamedEntity(key);
                }
            }
            Global.theNamedEntityLimits = namedEntityLimits;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // 20% - InventoryOpenEvent
    // 10% - CraftItemEvent
    // 20% - EntityDeathEvent
    // 100% - ChunkLoadEvent
    /////////////////////////////////////////////////////////////////////////////

    // Cords are forced into INT because the Chunk Cords in messages get long and or ugly.
    @EventHandler
    public void openInventory1(InventoryOpenEvent e)
    {
        int x = (int) e.getPlayer().getLocation().getX();
        int z = (int) e.getPlayer().getLocation().getZ();
        int y = (int) e.getPlayer().getLocation().getY();

        World world = e.getPlayer().getWorld();

        if (Global.configToggleScanChunkUponOpeningContainer)
        {
            Chunk chunk = e.getPlayer().getChunk();
            // 10% chance to check chunk upon opening any chest, barrel, enderChest.
            int roll = ThreadLocalRandom.current().nextInt(1, 11); // 10% chance.
            if (roll == 10)
            {chunkCleansingCheck(world, chunk, x, z, y);}
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void craftItem1(CraftItemEvent e)
    {
        int x = (int) e.getWhoClicked().getLocation().getX();
        int z = (int) e.getWhoClicked().getLocation().getZ();
        int y = (int) e.getWhoClicked().getLocation().getY();

        World world = e.getWhoClicked().getWorld();

        if (Global.configToggleScanChunkUponCrafting)
        {
            // 10% chance to check chunk upon crafting something.
            Chunk chunk = e.getWhoClicked().getChunk();
            int roll = ThreadLocalRandom.current().nextInt(1, 11); // 10% chance.
            if (roll == 10) chunkCleansingCheck(world, chunk, x, z, y);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void entityDeath1(EntityDeathEvent e)
    {
        int x = (int) e.getEntity().getLocation().getX();
        int z = (int) e.getEntity().getLocation().getZ();
        int y = (int) e.getEntity().getLocation().getY();

        World world = e.getEntity().getWorld();

        if (Global.configToggleScanChunkUponEntityDying)
        {
            // 10% chance to check chunk upon an Entity dying.
            Chunk chunk = e.getEntity().getChunk();
            int roll = ThreadLocalRandom.current().nextInt(1, 11); // 10% chance.
            if (roll == 10) chunkCleansingCheck(world, chunk, x, z, y);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void chunkLoad1(ChunkLoadEvent e)
    {
        int x = e.getChunk().getX() * 16;
        int z = e.getChunk().getZ() * 16;
        int y = 80;

        World world = e.getWorld();

        if (Global.configToggleScanChunkUponLoad)
        {
            if (Global.configToggleScanChunkUponLoad_50)
            {
                if (ThreadLocalRandom.current().nextBoolean())
                {
                    if (e.isNewChunk())return;
                    Chunk chunk = e.getChunk();
                    chunkCleansingCheck(world, chunk, x, z, y);
                }
            }
            else
            {
                if (e.isNewChunk())return;
                Chunk chunk = e.getChunk();
                chunkCleansingCheck(world, chunk, x, z, y);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private void chunkCleansingCheck(World world, Chunk chunk, int x, int z, int y)
    {
        int length = chunk.getEntities().length;
        //Stopper 1: Chunk void of entities?
        if (length <1) return;

        //Stopper 2: Is entity count less than the configured minimum?
        if (length < Global.configMinEntityLimit) return;

        //Stopper 3: Did the owner clear their lists?
        if (Global.theEntityLimits.isEmpty() && Global.theNamedEntityLimits.isEmpty()) return;

        int chunkLoadRemovedNamedCount = 0;
        int chunkLoadRemovedUnNamedCount = 0;

        // ===== ENTITIES ===== //
        Map<EntityType, List<Entity>> namedMap   = new HashMap<>();
        Map<EntityType, List<Entity>> unnamedMap = new HashMap<>();

        for (Entity entity : chunk.getEntities())
        {
            //If the chunk contains no listed entities, move on.
            EntityType type = entity.getType();
            if (!Global.theEntityLimits.containsKey(type)) continue;

            boolean isNamed = entity.customName() != null;
            (isNamed ? namedMap : unnamedMap)
                    .computeIfAbsent(type, k -> new ArrayList<>())
                    .add(entity);
        }

        //Stopper 4: Were there even any listed mobs?
        if (namedMap.isEmpty() && unnamedMap.isEmpty()) return;

        Global.chunkCount++;

        //Op 3: Grab for any entities that are not named.
        // Unnamed Limits
        for (Map.Entry<EntityType, List<Entity>> entry : unnamedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer noNameOBJ = Global.theEntityLimits.get(type);
            if (noNameOBJ == null) continue;
            int limit = noNameOBJ;
            List<Entity> list = entry.getValue();
            if (list.size() > limit)
            {
                int toRemove = list.size() - limit;
                for (chunkLoadRemovedUnNamedCount = 0; chunkLoadRemovedUnNamedCount < toRemove; chunkLoadRemovedUnNamedCount++)
                {
                    list.get(chunkLoadRemovedUnNamedCount).remove();
                }

                if (chunkLoadRemovedUnNamedCount != 0)
                {
                    if (main.Global.configToggleAlertEntityLimit)
                    {
                        if (type != EntityType.FALLING_BLOCK)
                        {
                            ClickEvent<ClickEvent.Payload.Text> copyCoords = ClickEvent.copyToClipboard(x + " " + y + " " + z);
                            // TRANSLATE: Removed x10 ZOMBIE_VILLAGER.
                            // English Message Workflow
                            if(main.Global.configLanguageType == 1)
                            {
                                HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
                                Component primaryMessage = EN.entitySummonsCheck_EntityAlert_UnNamed(type, chunkLoadRemovedUnNamedCount, copyCoords, hoverCoords);
                                EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                            }
                        }
                    }
                }
            }
        }

        // Named Limits
        for (Map.Entry<EntityType, List<Entity>> entry : namedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer namedOBJ = Global.theNamedEntityLimits.get(type);
            if (namedOBJ == null) continue;
            int cap = namedOBJ;
            List<Entity> list = entry.getValue();
            if (list.size() > cap)
            {
                int toRemove = list.size() - cap;
                for (chunkLoadRemovedNamedCount = 0; chunkLoadRemovedNamedCount < toRemove; chunkLoadRemovedNamedCount++)
                {
                    list.get(chunkLoadRemovedNamedCount).remove();
                }

                if (chunkLoadRemovedNamedCount != 0)
                {
                    if (Global.configToggleAlertEntityLimit)
                    {
                        ClickEvent<ClickEvent.Payload.Text> copyCoords = ClickEvent.copyToClipboard(x + " " + y + " " + z);
                        // TRANSLATE: Removed x10 Named ZOMBIE_VILLAGER.
                        // English Message Workflow
                        if(main.Global.configLanguageType == 1)
                        {
                            HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
                            Component primaryMessage = EN.entitySummonsCheck_EntityAlert_Named(type, chunkLoadRemovedNamedCount, copyCoords, hoverCoords);
                            EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                        }
                    }
                }
            }
        }

        int totality = chunkLoadRemovedNamedCount + chunkLoadRemovedUnNamedCount;

        // Totality tallies all removed listed entities.
        // If there are still a bunch of unlisted entities that surpass the warning level.
        // Let the server know.

        if (totality == 0)
        {
            if (length > Global.configMinEntityWarning && Global.configToggleAlertChunkWarning)
            {
                ClickEvent<ClickEvent.Payload.Text> copyCoords = ClickEvent.copyToClipboard(x + " " + y + " " + z);
                // TRANSLATE: CHUNK WARNING: Found x10 entities.
                // English Message Workflow
                if(main.Global.configLanguageType == 1)
                {
                    HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
                    Component primaryMessage = EN.entitySummonsCheck_EntityAlert_NotRemoved(length, copyCoords, hoverCoords);
                    EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                }
            }
        }
        else
        {
            if (Global.configToggleAlertChunkScanned)
            {
                ClickEvent<ClickEvent.Payload.Text> copyCoords = ClickEvent.copyToClipboard(x + " " + y + " " + z);
                // TRANSLATE: A Loaded Chunk met 6 conditions & removed x10 entities.
                // English Message Workflow
                if(main.Global.configLanguageType == 1)
                {
                    HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
                    Component primaryMessage = EN.main_AlertChunkScanRemovalSuccess(totality, copyCoords, hoverCoords);
                    EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args)
    {
        if (args.length == 0 || args[0].equalsIgnoreCase("help"))
        {
            sender.sendMessage("§a■ §7- - - - - - - - - - - - - - - - - - - - - - - - - §a■"
                                + "\n§a■ §6ChunkShield Commands§7:" + "\n§a■"
                                + "\n§a■ §e/chunkshield §areload"
                                + "\n§a■ §e/chunkshield §astats"
                                + "\n§a■ §7- - - - - - - - - - - - - - - - - - - - - - - - - §a■");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload"))
        {
            if (sender.hasPermission("chunkShield.reload"))
            {
                reloadConfig();
                loadLimitsFromConfig();
                // TRANSLATE: §7[§6ChunkShield§7] §aConfig reloaded.
                if (Global.configLanguageType == 1) EN.main_ConfigReloaded(sender);
            }
            else
            {
                // TRANSLATE: Invalid Action. No Permission.
                if (Global.configLanguageType == 1) EN.main_NoPermission(sender);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("stats"))
        {
            if (sender.hasPermission("chunkShield.info"))
            {
                // TRANSLATE: ChunkShield Stats breakdown.
                if (Global.configLanguageType == 1) EN.main_Subcommand_Stats(sender);
            }
            else
            {
                // TRANSLATE: Invalid Action. No Permission.
                if (Global.configLanguageType == 1) EN.main_NoPermission(sender);
            }
            return true;
        }

        sender.sendMessage("§cUnknown subcommand.");
        return true;
    }
}