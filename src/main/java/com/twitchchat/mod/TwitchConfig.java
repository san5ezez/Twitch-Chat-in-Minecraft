package com.twitchchat.mod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class TwitchConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("twitchchat.json");

    public String channel = "";
    public boolean autostart = false;
    public String soundNick = "";
    public String language = "ru"; // ru / en

    private static TwitchConfig instance;

    public static TwitchConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (Reader r = new FileReader(file)) {
                instance = GSON.fromJson(r, TwitchConfig.class);
                if (instance == null) instance = new TwitchConfig();
            } catch (Exception e) {
                TwitchChatClient.LOGGER.warn("Не удалось загрузить конфиг: {}", e.getMessage());
                instance = new TwitchConfig();
            }
        } else {
            instance = new TwitchConfig();
            save();
        }
    }

    public static void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            TwitchChatClient.LOGGER.warn("Не удалось сохранить конфиг: {}", e.getMessage());
        }
    }
}
