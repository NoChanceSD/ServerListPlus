/*
 *        _____                     __    _     _   _____ _
 *       |   __|___ ___ _ _ ___ ___|  |  |_|___| |_|  _  | |_ _ ___
 *       |__   | -_|  _| | | -_|  _|  |__| |_ -|  _|   __| | | |_ -|
 *       |_____|___|_|  \_/|___|_| |_____|_|___|_| |__|  |_|___|___|
 *
 *  ServerListPlus - http://git.io/slp
 *    > The most customizable server status ping plugin for Minecraft!
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

package net.minecrell.serverlistplus.server;

import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.favicon.FaviconSource;
import net.minecrell.serverlistplus.core.plugin.ServerListPlusPlugin;
import net.minecrell.serverlistplus.core.plugin.ServerType;
import net.minecrell.serverlistplus.core.status.StatusManager;
import net.minecrell.serverlistplus.core.util.InstanceStorage;
import net.minecrell.serverlistplus.server.network.NetworkManager;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.LoadingCache;

import static net.minecrell.serverlistplus.core.logging.Logger.ERROR;
import static net.minecrell.serverlistplus.core.logging.Logger.INFO;

public class ServerListPlusServer implements ServerListPlusPlugin {
    private final ServerListPlusCore core;
    private final Logger logger;

    private final NetworkManager network;

    public ServerListPlusServer(InetSocketAddress address, final Logger logger) {
        this.logger = logger;

        logger.log(INFO, "Loading...");
        this.core = new ServerListPlusCore(this, new ServerProfileManager());
        this.network = new NetworkManager(this, address);
        logger.log(INFO, "Successfully loaded!");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.log(INFO, "Stopping...");
                close();
            }
        });
    }

    public boolean start() {
        if (!core.getStatus().hasChanges()) {
            logger.log(ERROR, "No changes configured, nothing to do! Please add something in the configuration.");
            return false;
        }

        try {
            network.start(); return true;
        } catch (Exception e) {
            logger.log(ERROR, "Failed to start network manager!", e);
            return false;
        }
    }

    public boolean close() {
        try {
            network.close(); return true;
        } catch (Exception e) {
            logger.log(ERROR, "Failed to close network manager!", e);
            return false;
        }
    }

    @Override
    public ServerListPlusCore getCore() {
        return core;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.SERVER;
    }

    @Override
    public String getServerImplementation() {
        return "Unknown";
    }

    @Override
    public Path getPluginFolder() {
        return Paths.get("");
    }

    @Override
    public String getRandomPlayer() {
        return null;
    }

    @Override
    public Integer getOnlinePlayersAt(String location) {
        return null;
    }

    @Override
    public Cache<?, ?> getRequestCache() {
        return null;
    }

    @Override
    public LoadingCache<FaviconSource, ?> getFaviconCache() {
        return null;
    }

    @Override
    public String colorize(String s) {
        return s.replace('\u00A7', '&'); // TODO: Improve this
    }

    @Override
    public void initialize(ServerListPlusCore core) {

    }

    @Override
    public void reloadCaches(ServerListPlusCore core) {

    }

    @Override
    public void reloadFaviconCache(CacheBuilderSpec spec) {

    }

    @Override
    public void configChanged(InstanceStorage<Object> confs) {

    }

    @Override
    public void statusChanged(StatusManager status) {

    }
}
