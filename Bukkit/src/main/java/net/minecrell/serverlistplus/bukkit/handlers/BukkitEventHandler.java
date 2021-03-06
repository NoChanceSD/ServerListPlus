/*
 *        _____                     __    _     _   _____ _
 *       |   __|___ ___ _ _ ___ ___|  |  |_|___| |_|  _  | |_ _ ___
 *       |__   | -_|  _| | | -_|  _|  |__| |_ -|  _|   __| | | |_ -|
 *       |_____|___|_|  \_/|___|_| |_____|_|___|_| |__|  |_|___|___|
 *
 *  ServerListPlus - http://git.io/slp
 *  Copyright (c) 2014, Minecrell <https://github.com/Minecrell>
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

package net.minecrell.serverlistplus.bukkit.handlers;

import net.minecrell.serverlistplus.bukkit.BukkitPlugin;
import net.minecrell.serverlistplus.core.favicon.FaviconSource;
import net.minecrell.serverlistplus.core.status.ResponseFetcher;
import net.minecrell.serverlistplus.core.status.StatusResponse;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

public class BukkitEventHandler extends StatusHandler {
    private PingListener listener;

    public BukkitEventHandler(BukkitPlugin plugin) {
        super(plugin);
    }

    public final class PingListener implements Listener {
        private PingListener() {}

        @EventHandler
        public void onServerListPing(final ServerListPingEvent event) {
            StatusResponse response = bukkit.getCore().createRequest(event.getAddress()).createResponse(
                    bukkit.getCore().getStatus(), new ResponseFetcher() {
                        @Override
                        public Integer getOnlinePlayers() {
                            return event.getNumPlayers();
                        }

                        @Override
                        public Integer getMaxPlayers() {
                            return event.getMaxPlayers();
                        }

                        @Override
                        public int getProtocolVersion() {
                            return -1;
                        }
                    });

            // Description
            String message = response.getDescription();
            if (message != null)
                event.setMotd(message);

            // Max players
            Integer max = response.getMaxPlayers();
            if (max != null)
                event.setMaxPlayers(max);

            // Favicon
            FaviconSource favicon = response.getFavicon();
            if (favicon != null) {
                CachedServerIcon icon = bukkit.getFavicon(favicon);
                if (icon != null)
                    try {
                        event.setServerIcon(icon);
                    } catch (UnsupportedOperationException ignored) {}
            }
        }
    }

    @Override
    public boolean register() {
        if (listener == null) {
            bukkit.registerListener(this.listener = new PingListener());
            return true;
        } else
            return false;
    }

    @Override
    public boolean unregister() {
        if (listener != null) {
            bukkit.unregisterListener(listener);
            this.listener = null;
            return true;
        } else
            return false;
    }
}
