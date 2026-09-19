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
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent e)
    {
        World world = e.getVehicle().getWorld();
        Entity vehicle = e.getVehicle();

        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleRadiusScan(world, vehicle);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void vehicleMoveCheck (VehicleMoveEvent e)
    {
        World world = e.getVehicle().getWorld();
        Entity vehicle = e.getVehicle();

        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleRadiusScan(world, vehicle);
        }
    }

    public int x;
    public int y;
    public int z;
    /////////////////////////////////////////////////////////////////////////////
    private void vehicleRadiusScan(World world, Entity vehicle)
    {
        main.Global.Entity_vehicleCount = 0;
        if (main.Global.configCollectiveVehicletLimit > 0)
        {
            int total = 0;
            for (Entity entity : vehicle.getNearbyEntities(main.Global.configRadiusLimit, main.Global.configRadiusLimit, main.Global.configRadiusLimit))
            {
                if (entity instanceof Boat || entity instanceof Minecart)
                {
                    total++;
                    int toRemove = main.Global.configCollectiveVehicletLimit - total;
                    if (toRemove < 1)
                    {
                        entity.remove();
                        x = (int) entity.getLocation().getX();
                        y = (int) entity.getLocation().getY();
                        z = (int) entity.getLocation().getZ();
                        main.Global.Entity_vehicleCount++;
                        entity.getLocation();
                        if (main.Global.Entity_vehicleCount < 10 && main.Global.configTogglePurgeEffect) world.spawnParticle(Particle.LAVA, entity.getLocation().toCenterLocation(), 4);
                        main.Global.vehiclesPrevented++;
                    }
                }
            }

            if (main.Global.Entity_vehicleCount > 1 && main.Global.configToggleAlertVehicleLimit)
            {
                TextComponent STYLE = new TextComponent("§c■ - - - - - - - - - - - - - - - - - - - - - - - - - ■");
                TextComponent message = new TextComponent("§c■ " + "§eRemoved §ax" + main.Global.Entity_vehicleCount + "§6Vehicle(s)§e.");
                TextComponent sub = new TextComponent("§c■ " + "§eLocation§7: §6" + world.getName() + " §7/ §a[" + x + ", " + y + ", " + z + "]");

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