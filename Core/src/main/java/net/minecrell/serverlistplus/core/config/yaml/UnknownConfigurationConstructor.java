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

package net.minecrell.serverlistplus.core.config.yaml;

import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.config.UnknownConf;

import com.google.common.base.Throwables;

import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;

import static net.minecrell.serverlistplus.core.logging.Logger.WARN;

public class UnknownConfigurationConstructor extends CustomClassLoaderConstructor {
    private final ServerListPlusCore core;

    public UnknownConfigurationConstructor(ServerListPlusCore core) {
        super(core.getClass().getClassLoader());
        this.core = core;
    }

    @Override
    protected Class<?> getClassForNode(Node node) {
        try {
            return super.getClassForNode(node);
        } catch (YAMLException e) {
            core.getLogger().log(WARN, "Unknown configuration: {} -> {}", node.getTag().getValue(),
                    Throwables.getRootCause(e).getMessage());
            return UnknownConf.class;
        }
    }
}
