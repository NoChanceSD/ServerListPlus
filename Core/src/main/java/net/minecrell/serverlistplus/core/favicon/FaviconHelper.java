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

package net.minecrell.serverlistplus.core.favicon;

import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.config.PluginConf;
import net.minecrell.serverlistplus.core.util.Helper;
import net.minecrell.serverlistplus.core.util.TimeUnitValue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.USER_AGENT;
import static net.minecrell.serverlistplus.core.logging.Logger.DEBUG;
import static net.minecrell.serverlistplus.core.logging.Logger.WARN;

public final class FaviconHelper {
    private FaviconHelper() {}

    private static final int FAVICON_SIZE = 64;

    public static BufferedImage fromStream(InputStream in) throws IOException {
        return ImageIO.read(in);
    }

    private static InputStream openConnection(ServerListPlusCore core, URL url, String type) throws IOException {
        URLConnection con = url.openConnection();

        TimeUnitValue timeout = core.getConf(PluginConf.class).Favicon.Timeout;
        int time = (int) timeout.getUnit().toMillis(timeout.getValue());
        con.setConnectTimeout(time);
        con.setReadTimeout(time);

        con.addRequestProperty(USER_AGENT, core.getDisplayName());
        con.addRequestProperty(ACCEPT, type);

        return con.getInputStream();
    }

    public static BufferedImage fromURL(ServerListPlusCore core, URL url) throws IOException {
        try (InputStream in = openConnection(core, url, "image/png")) {
            return fromStream(in);
        }
    }

    private static final String SKIN_URL = "http://skins.minecraft.net/MinecraftSkins/%s.png";
    private static final String SKIN_UUID_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String STEVE_URL = "https://minecraft.net/images/steve.png";
    private static final String ALEX_URL = "https://minecraft.net/images/alex.png";

    private static final int HEAD_X = 8, HEAD_Y = 8;
    private static final int HELM_X = 40, HELM_Y = 8;
    private static final int HEAD_SIZE = 8;

    private static BufferedImage fromSkin(ServerListPlusCore core, URL url, boolean helm) throws IOException {
        BufferedImage skin = fromURL(core, url);
        if (helm) {
            Graphics2D g = skin.createGraphics();
            g.copyArea(HELM_X, HELM_Y, HEAD_SIZE, HEAD_SIZE, HEAD_X - HELM_X, HEAD_Y - HELM_Y);
            g.dispose();
        }

        return skin.getSubimage(HEAD_X, HEAD_Y, HEAD_SIZE, HEAD_SIZE);
    }

    public static BufferedImage fromSkin(ServerListPlusCore core, String name, boolean helm) throws IOException {
        try { // First try if it is already a valid URL
            return fromSkin(core, new URL(name), helm);
        } catch(MalformedURLException ignored) {}

        // Now try if it's an UUID (dashes required)
        try {
            return fromSkin(core, UUID.fromString(name), helm);
        } catch (IllegalArgumentException ignored) {}

        URL url;
        if (name.equalsIgnoreCase("char") || name.equalsIgnoreCase("steve")) url = new URL(STEVE_URL);
        else if (name.equalsIgnoreCase("alex")) url = new URL(ALEX_URL);
        else url = new URL(String.format(SKIN_URL, name));
        return fromSkin(core, url, helm);
    }

    private static String toHex(long num) {
        return Strings.padStart(Long.toHexString(num), 16, '0');
    }

    private static String toHexString(UUID uuid) {
        return toHex(uuid.getMostSignificantBits()) + toHex(uuid.getLeastSignificantBits());
    }

    public static BufferedImage fromSkin(ServerListPlusCore core, UUID uuid, boolean helm) throws IOException {
        JsonObject obj;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openConnection(core,
                new URL(SKIN_UUID_URL + toHexString(uuid)), "application/json")))) {
            obj = Helper.JSON.fromJson(reader, JsonObject.class);
        }

        JsonArray arr = obj.getAsJsonArray("properties");
        for (JsonElement ele : arr) {
            if (ele instanceof JsonObject) {
                obj = (JsonObject) ele;
                if (!obj.getAsJsonPrimitive("name").getAsString().equals("textures")) continue;
                String json = new String(BaseEncoding.base64().decode(
                        obj.getAsJsonPrimitive("value").getAsString()), StandardCharsets.UTF_8);
                obj = Helper.JSON.fromJson(json, JsonObject.class)
                        .getAsJsonObject("textures")
                        .getAsJsonObject("SKIN");
                return fromSkin(core, new URL(obj.getAsJsonPrimitive("url").getAsString()), helm);
            }
        }

        throw new IllegalStateException("Skin not found");
    }

    public static BufferedImage load(ServerListPlusCore core, FaviconSource source) throws IOException {
        return core.getConf(PluginConf.class).Favicon.ResizeStrategy.resize(source.getLoader().load(core,
                source.getSource()), FAVICON_SIZE, FAVICON_SIZE);
    }

    public static BufferedImage loadSafely(ServerListPlusCore core, FaviconSource source) {
        try { // Try loading the favicon
            return load(core, source);
        } catch (Exception e) {
            core.getLogger()
                    .log(WARN, "Unable to load favicon from {}: {} -> {}",
                            source.getLoader(), source.getSource(), Helper.causedException(e))
                    .log(DEBUG, e, "Unable to load favicon from {}: {}",
                            source.getLoader(), source.getSource());
            return null;
        }
    }
}
