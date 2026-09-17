package me.mikebloc.chunkShield.listeners;

import me.mikebloc.chunkShield.main;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public final class vehicleSummonsCheck implements Listener
{
    public static class vehicleGlobal
    {
        public static Vehicle theVehicle;
    }

    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent e)
    {
        int x = (int) e.getVehicle().getLocation().getX();
        int y = (int) e.getVehicle().getLocation().getY();
        int z = (int) e.getVehicle().getLocation().getZ();

        World world = e.getVehicle().getWorld();

        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleGlobal.theVehicle = e.getVehicle();
            vehicleRadiusScan(world, x, y, z);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void vehicleMoveCheck (VehicleMoveEvent e)
    {
        int x = (int) e.getVehicle().getLocation().getX();
        int y = (int) e.getVehicle().getLocation().getY();
        int z = (int) e.getVehicle().getLocation().getZ();

        World world = e.getVehicle().getWorld();

        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleGlobal.theVehicle = e.getVehicle();
            vehicleRadiusScan(world, x, y, z);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private void vehicleRadiusScan(World world, int x, int y, int z)
    {
        main.Global.Entity_vehicleCount = 0;
        // ---- Category totals: BOATS & MINE CARTS
        if (main.Global.configCollectiveVehicletLimit > 0)
        {
            int total = 0;
            for (Entity entity : vehicleGlobal.theVehicle.getNearbyEntities(main.Global.configRadiusLimit, main.Global.configRadiusLimit, main.Global.configRadiusLimit))
            {
                if (entity instanceof Boat || entity instanceof Minecart)
                {
                    // This could be simpler but it wont cooperate otherwise.
                    total++;
                    int toRemove = main.Global.configCollectiveVehicletLimit - total;
                    if (toRemove < 1)
                    {
                        entity.remove();
                        main.Global.Entity_vehicleCount++;
                        entity.getLocation();
                        if (main.Global.Entity_vehicleCount < 10 && main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, entity.getLocation().toCenterLocation(), 4);
                        main.Global.vehiclesPrevented++;
                    }
                }
            }

            if (main.Global.Entity_vehicleCount > 1)
            {
                if (main.Global.configToggleAlertVehicleLimit)
                {
                    TextComponent STYLE = new TextComponent("§c■ - - - - - - - - - - - - - - - - - - - - - - - - - ■");
                    TextComponent message = new TextComponent("§c■ " + "§6Vehicle Limit §ewas reached at§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■");
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
        }
    }
}