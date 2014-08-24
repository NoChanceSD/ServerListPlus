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

import net.minecrell.serverlistplus.server.logging.ConsoleLogger;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {
    private static final String USAGE = "Usage: ServerListPlusServer [IP]<:Port>";

    public static int create(String[] args) {
        if (args.length == 0 || args.length > 1)
            return printError(USAGE);

        String[] address = args[0].split(":", 2);
        if (address.length != 2) return printError(USAGE);

        String host = address[0]; if (host.isEmpty() || host.equals("*")) host = null;
        int port = Integer.parseInt(address[1]);

        InetSocketAddress socket = host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(port);
        if (socket.getAddress() == null)
            return printError("Unknown host: " + host);

        Logger logger = new ConsoleLogger(ServerListPlusServer.class);

        try {
            new ServerListPlusServer(socket, logger).start();
            return 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to start server!", e);
            return -1;
        }
    }

    private static int printError(String message) {
        System.err.println(message);
        return -1;
    }

    public static void main(String[] args) {
        System.exit(create(args));
    }
}
