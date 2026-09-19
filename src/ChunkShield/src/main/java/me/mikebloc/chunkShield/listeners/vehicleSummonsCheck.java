package me.mikebloc.chunkShield.listeners;

import me.mikebloc.chunkShield.languages.ES;
import me.mikebloc.chunkShield.languages.RU;
import me.mikebloc.chunkShield.main;
import me.mikebloc.chunkShield.languages.EN;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
        World world = e.getVehicle().getWorld();

        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleGlobal.theVehicle = e.getVehicle();
            vehicleRadiusScan(world);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void vehicleMoveCheck (VehicleMoveEvent e)
    {
        World world = e.getVehicle().getWorld();

        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleGlobal.theVehicle = e.getVehicle();
            vehicleRadiusScan(world);
        }
    }

    public int x;
    public int y;
    public int z;
    /////////////////////////////////////////////////////////////////////////////
    private void vehicleRadiusScan(World world)
    {
        main.Global.Entity_vehicleCount = 0;
        if (main.Global.configCollectiveVehicletLimit > 0)
        {
            int total = 0;
            for (Entity entity : vehicleGlobal.theVehicle.getNearbyEntities(main.Global.configRadiusLimit, main.Global.configRadiusLimit, main.Global.configRadiusLimit))
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

            if (main.Global.Entity_vehicleCount != 0)
            {
                if (main.Global.configToggleAlertVehicleLimit)
                {
                    ClickEvent copyCoords = ClickEvent.copyToClipboard(x + " " + y + " " + z);

                    // English Message Workflow
                    if(main.Global.configLanguageType == 1)
                    {
                        HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(EN.ClickCopy, NamedTextColor.GREEN));
                        Component primaryMessage = EN.vehicleSummonsCheck_alertVehicleLimit(copyCoords, hoverCoords);
                        EN.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                    }
                    // Spanish Message Workflow
                    else if(main.Global.configLanguageType == 2)
                    {
                        HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(ES.ClickCopy, NamedTextColor.GREEN));
                        Component primaryMessage = ES.vehicleSummonsCheck_alertVehicleLimit(copyCoords, hoverCoords);
                        ES.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                    }
                    // Russian Message Workflow
                    else if(main.Global.configLanguageType == 3)
                    {
                        HoverEvent<?> hoverCoords = HoverEvent.showText(Component.text(RU.ClickCopy, NamedTextColor.GREEN));
                        Component primaryMessage = RU.vehicleSummonsCheck_alertVehicleLimit(copyCoords, hoverCoords);
                        RU.sendMessageMethod(world, x, z, y, copyCoords, hoverCoords, primaryMessage);
                    }
                }
            }
        }
    }
}