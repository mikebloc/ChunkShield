package me.mikebloc.chunkShield.listeners;

import me.mikebloc.chunkShield.main;
import me.mikebloc.chunkShield.languages.EN;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class entitySummonsCheck implements Listener
{
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e)
    {
        int x = (int) e.getEntity().getLocation().getX();
        int z = (int) e.getEntity().getLocation().getZ();
        int y = (int) e.getEntity().getLocation().getY();

        World world = e.getEntity().getWorld();
        Entity entity1 = e.getEntity();

        if (main.Global.configToggleEntityCheck)
        {
            if (main.Global.configToggleEntityCheck_50)
            {
                if (ThreadLocalRandom.current().nextBoolean()) Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), () -> entityCheck(world, entity1, x, z, y), 1L);
            }
            else Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), () -> entityCheck(world, entity1, x, z, y), 1L);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private static void entityCheck(World world, Entity entity1, int x, int z, int y)
    {
        int entitySummonNamedCount = 0;
        int entitySummonUnNamedCount = 0;

        Chunk chunk = entity1.getLocation().getChunk();

        boolean Removed;
        boolean Named;

        int length = chunk.getEntities().length;
        if (length > main.Global.configMinEntityWarning && main.Global.configToggleAlertChunkWarning)
        {
            Removed = false;
            Named = false;

            EntityType type = entity1.getType(); //can be ignored, just makes the method stop bitching.
            EntityAlertMessages(x, z, y, world, type, Removed, Named, length, entitySummonNamedCount, entitySummonUnNamedCount);
        }

        //Op 1: Did the owner clear their lists?
        if (main.Global.theEntityLimits.isEmpty() && main.Global.theNamedEntityLimits.isEmpty()) return;

        // ===== ENTITIES ===== //
        Map<EntityType, List<Entity>> namedEntityList   = new HashMap<>();
        Map<EntityType, List<Entity>> unnamedEntityList = new HashMap<>();

        //Op 2: Grab entities in chunk.
        for (Entity entity : chunk.getEntities())
        {
            //If the chunk contains no listed entities, move on.
            if (!main.Global.theEntityLimits.containsKey(entity.getType())) continue;

            boolean isNamed = entity.customName() != null;
            (isNamed ? namedEntityList : unnamedEntityList)
                    .computeIfAbsent(entity.getType(), k -> new ArrayList<>())
                    .add(entity);
        }

        //Stopper 4: Were there even any listed mobs?
        if (namedEntityList.isEmpty() && unnamedEntityList.isEmpty()) return;


        //Op 3: Grab for any entities that are not named.
        // Unnamed Limits
        for (Map.Entry<EntityType, List<Entity>> entry : unnamedEntityList.entrySet())
        {
            EntityType type = entry.getKey();
            Integer noNameOBJ = main.Global.theEntityLimits.get(type);
            List<Entity> list = entry.getValue();
            int limit = noNameOBJ;
            if (list.size() > limit)
            {
                int toRemove = list.size() - limit;
                for (entitySummonUnNamedCount = 0; entitySummonUnNamedCount < toRemove; entitySummonUnNamedCount++)
                {
                    list.get(entitySummonUnNamedCount).remove();
                    if (entitySummonUnNamedCount < 10)
                    {
                        if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, list.get(entitySummonUnNamedCount).getLocation().toCenterLocation(), 4);
                    }
                    main.Global.entitiesRemoved++;
                }
                if (entitySummonUnNamedCount != 0)
                {
                    if (type != EntityType.FALLING_BLOCK)
                    {
                        Removed = true;
                        Named = false;
                        if (main.Global.configToggleAlertEntityLimit) EntityAlertMessages(x, z, y, world, type, Removed, Named, length, entitySummonNamedCount, entitySummonUnNamedCount);
                    }
                }
                else
                {
                    Removed = false;
                    Named = false;
                    if (main.Global.configToggleAlertEntityLimit) EntityAlertMessages(x, z, y, world, type, Removed, Named, length, entitySummonNamedCount, entitySummonUnNamedCount);
                }
            }
        }

        // Named Limits
        for (Map.Entry<EntityType, List<Entity>> entry : namedEntityList.entrySet())
        {
            EntityType type = entry.getKey();
            Integer namedOBJ = main.Global.theNamedEntityLimits.get(type);
            List<Entity> list = entry.getValue();
            int cap = namedOBJ;
            if (list.size() > cap)
            {
                int toRemove = list.size() - cap;
                for (entitySummonNamedCount = 0; entitySummonNamedCount < toRemove; entitySummonNamedCount++)
                {
                    list.get(entitySummonNamedCount).remove();
                    if (entitySummonNamedCount < 10)
                    {
                        if(main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, list.get(entitySummonNamedCount).getLocation().toCenterLocation(), 4);
                    }
                    main.Global.entitiesRemoved++;
                }
                if (entitySummonNamedCount != 0)
                {
                    if (main.Global.configToggleAlertEntityLimit)
                    {
                        Removed = true;
                        Named = true;
                        EntityAlertMessages(x, z, y, world, type, Removed, Named, length, entitySummonNamedCount, entitySummonUnNamedCount);
                    }
                }
                else
                {
                    Removed = false;
                    Named = false;
                    if (main.Global.configToggleAlertEntityLimit) EntityAlertMessages(x, z, y, world, type, Removed, Named, length, entitySummonNamedCount, entitySummonUnNamedCount);
                }
            }
        }
    }

    public static void EntityAlertMessages(int x, int z, int y, World world, EntityType type, boolean Removed, boolean Named, int length, int entitySummonNamedCount, int entitySummonUnNamedCount)
    {
        ClickEvent copyCoords = ClickEvent.copyToClipboard(x + " " + y + " " + z);

        if (Removed)
        {
            if (Named)
            {
                // English Message Workflow
                if(main.Global.configLanguageType == 1)
                {
                    HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
                    Component primaryMessage = EN.entitySummonsCheck_EntityAlert_Named(type, entitySummonNamedCount, copyCoords, hoverCoords);
                    EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                }
            }
            else
            {
                // English Message Workflow
                if(main.Global.configLanguageType == 1)
                {
                    HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
                    Component primaryMessage = EN.entitySummonsCheck_EntityAlert_UnNamed(type, entitySummonUnNamedCount, copyCoords, hoverCoords);
                    EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                }
            }
        }
        else
        {
            // English Message Workflow
            if(main.Global.configLanguageType == 1)
            {
                HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
                Component primaryMessage = EN.entitySummonsCheck_EntityAlert_NotRemoved(length, copyCoords, hoverCoords);
                EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
            }
        }
    }




}
