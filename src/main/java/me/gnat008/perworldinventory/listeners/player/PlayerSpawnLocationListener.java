/*
 * Copyright (C) 2014-2016  EbonJaguar
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.listeners.player;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.DataWriter;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.process.InventoryChangeProcess;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import javax.inject.Inject;

public class PlayerSpawnLocationListener implements Listener {

    private DataWriter dataWriter;
    private GroupManager groupManager;
    private InventoryChangeProcess process;

    @Inject
    PlayerSpawnLocationListener(DataWriter dataWriter, GroupManager groupManager, InventoryChangeProcess process) {
        this.dataWriter = dataWriter;
        this.groupManager = groupManager;
        this.process = process;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        if (!Settings.getBoolean("load-data-on-join"))
            return;

        Player player = event.getPlayer();
        String spawnWorld = event.getSpawnLocation().getWorld().getName();

        if (Settings.getBoolean("debug-mode"))
            PerWorldInventory.printDebug("Player '" + player.getName() + "' joining! Spawning in world '" + spawnWorld + "'. Getting last logout location");

        Location lastLogout = dataWriter.getLogoutData(player);
        if (lastLogout != null) {
            if (Settings.getBoolean("debug-mode"))
                PerWorldInventory.printDebug("Logout location found for player '" + player.getName() + "'!");

            if (!lastLogout.getWorld().getName().equals(spawnWorld)) {
                Group spawnGroup = groupManager.getGroupFromWorld(spawnWorld);
                Group logoutGroup = groupManager.getGroupFromWorld(lastLogout.getWorld().getName());

                process.processWorldChangeOnSpawn(player, logoutGroup, spawnGroup);
            }
        }
    }
}
