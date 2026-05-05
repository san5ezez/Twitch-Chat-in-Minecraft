package com.twitchchat.mod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class Lang {

    private static final Map<String, String> RU = new HashMap<>();
    private static final Map<String, String> EN = new HashMap<>();

    static {
        // Русский
        RU.put("connection_lost",   "Соединение разорвано!");
        RU.put("sub_new",           "подписался на канал");
        RU.put("sub_resub",         "продлил подписку");
        RU.put("sub_gift",          "подарил подписку");
        RU.put("anon_gift",         "Аноним подарил подписку");
        RU.put("months",            "мес.");
        RU.put("mention",           "Вас упомянул");
        RU.put("badge_streamer",    "стример");
        RU.put("channel_set",       "Канал сохранён:");
        RU.put("channel_changed",   "Канал изменён:");
        RU.put("reconnected",       "переподключено!");
        RU.put("connect_start",     "Подключаюсь к");
        RU.put("connected",         "Подключено! Чат активен.");
        RU.put("connect_fail",      "Не удалось подключиться. Проверь интернет.");
        RU.put("set_channel_first", "Сначала укажи канал: /twitch channel <название>");
        RU.put("already_running",   "Уже запущено! Канал:");
        RU.put("stopped",           "Остановлено. Канал");
        RU.put("stopped2",          "отключён.");
        RU.put("not_running",       "Трансляция не запущена.");
        RU.put("reloading",         "Переподключаюсь к");
        RU.put("reload_ok",         "Переподключено!");
        RU.put("reload_fail",       "Не удалось переподключиться.");
        RU.put("no_channel",        "Канал не задан.");
        RU.put("autostart_on",      "Автозапуск: ВКЛ");
        RU.put("autostart_off",     "Автозапуск: ВЫКЛ");
        RU.put("sound_set",         "Звук при упоминании ника:");
        RU.put("lang_set",          "Язык установлен: Русский");
        RU.put("status_active",     "Активно");
        RU.put("status_stopped",    "Остановлено");
        RU.put("status_channel",    "Канал:");
        RU.put("status_autostart",  "Автозапуск:");
        RU.put("status_sound",      "Звук-ник:");
        RU.put("status_lang",       "Язык:");
        RU.put("not_set",           "не задан");
        RU.put("on",                "ВКЛ");
        RU.put("off",               "ВЫКЛ");
        RU.put("autostart_launch",  "Автозапуск: подключено к");
        RU.put("help_header",       "=== TwitchChat команды ===");

        // English
        EN.put("connection_lost",   "Connection lost!");
        EN.put("sub_new",           "subscribed to the channel");
        EN.put("sub_resub",         "resubscribed");
        EN.put("sub_gift",          "gifted a sub to");
        EN.put("anon_gift",         "An anonymous user gifted a sub to");
        EN.put("months",            "mo.");
        EN.put("mention",           "You were mentioned by");
        EN.put("badge_streamer",    "streamer");
        EN.put("channel_set",       "Channel saved:");
        EN.put("channel_changed",   "Channel changed:");
        EN.put("reconnected",       "reconnected!");
        EN.put("connect_start",     "Connecting to");
        EN.put("connected",         "Connected! Chat is active.");
        EN.put("connect_fail",      "Failed to connect. Check your internet.");
        EN.put("set_channel_first", "Set a channel first: /twitch channel <name>");
        EN.put("already_running",   "Already running! Channel:");
        EN.put("stopped",           "Stopped. Channel");
        EN.put("stopped2",          "disconnected.");
        EN.put("not_running",       "Relay is not running.");
        EN.put("reloading",         "Reconnecting to");
        EN.put("reload_ok",         "Reconnected!");
        EN.put("reload_fail",       "Failed to reconnect.");
        EN.put("no_channel",        "No channel set.");
        EN.put("autostart_on",      "Autostart: ON");
        EN.put("autostart_off",     "Autostart: OFF");
        EN.put("sound_set",         "Sound on mention for nick:");
        EN.put("lang_set",          "Language set: English");
        EN.put("status_active",     "Active");
        EN.put("status_stopped",    "Stopped");
        EN.put("status_channel",    "Channel:");
        EN.put("status_autostart",  "Autostart:");
        EN.put("status_sound",      "Sound nick:");
        EN.put("status_lang",       "Language:");
        EN.put("not_set",           "not set");
        EN.put("on",                "ON");
        EN.put("off",               "OFF");
        EN.put("autostart_launch",  "Autostart: connected to");
        EN.put("help_header",       "=== TwitchChat commands ===");
    }

    public static String get(String key) {
        boolean ru = "ru".equals(TwitchConfig.get().language);
        Map<String, String> map = ru ? RU : EN;
        return map.getOrDefault(key, key);
    }
}
