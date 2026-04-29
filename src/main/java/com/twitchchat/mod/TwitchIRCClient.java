package com.twitchchat.mod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class TwitchIRCClient {

    private static final String HOST = "irc.chat.twitch.tv";
    private static final int PORT = 6667;
    private static final String NICK = "justinfan" + (int)(Math.random() * 80000 + 1000);

    // Паттерн для сообщений с тегами: @badges=...;...;... :nick!... PRIVMSG #ch :msg
    private static final Pattern TAG_MSG = Pattern.compile(
        "@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv PRIVMSG #(\\S+) :(.+)"
    );
    // Паттерн без тегов (fallback)
    private static final Pattern PLAIN_MSG = Pattern.compile(
        ":([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv PRIVMSG #(\\S+) :(.+)"
    );

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private String channel = "";

    public boolean isRunning() { return running.get(); }
    public String getChannel() { return channel; }

    public boolean connect(String ch) {
        if (running.get()) disconnect();
        channel = ch.toLowerCase().replaceAll("^#", "").trim();
        try {
            socket = new Socket(HOST, PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Запрашиваем теги — это даёт нам badges, color и др.
            writer.println("CAP REQ :twitch.tv/tags");
            writer.println("PASS SCHMOOPIIE");
            writer.println("NICK " + NICK);
            writer.println("JOIN #" + channel);

            running.set(true);
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "TwitchIRC");
                t.setDaemon(true);
                return t;
            });
            executor.submit(this::loop);
            return true;
        } catch (IOException e) {
            TwitchChatClient.LOGGER.error("Ошибка: {}", e.getMessage());
            cleanup();
            return false;
        }
    }

    public void disconnect() {
        running.set(false);
        try { if (writer != null) writer.println("PART #" + channel); } catch (Exception ignored) {}
        cleanup();
    }

    private void cleanup() {
        try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        if (executor != null) executor.shutdownNow();
        writer = null; reader = null; socket = null; executor = null;
    }

    private void loop() {
        try {
            String line;
            while (running.get() && (line = reader.readLine()) != null) handle(line);
        } catch (IOException e) {
            if (running.get()) {
                send(Component.literal("§c[TwitchChat] Соединение разорвано!"));
                running.set(false);
                cleanup();
            }
        }
    }

    private void handle(String line) {
        if (line.startsWith("PING")) {
            writer.println("PONG " + line.substring(5));
            return;
        }

        // Пробуем парсить с тегами
        Matcher m = TAG_MSG.matcher(line);
        if (m.matches()) {
            String tags    = m.group(1);
            String user    = m.group(2);
            String msg     = m.group(4).trim();
            String badges  = parseBadges(tags);
            String prefix  = badges.isEmpty() ? "" : badges + " ";
            send(Component.literal("§d[Twitch] " + prefix + "§e" + user + "§r: §f" + msg));
            return;
        }

        // Fallback без тегов
        Matcher m2 = PLAIN_MSG.matcher(line);
        if (m2.matches()) {
            String user = m2.group(1);
            String msg  = m2.group(3).trim();
            send(Component.literal("§d[Twitch] §e" + user + "§r: §f" + msg));
        }
    }

    // Парсим поле badges= и возвращаем красивые значки
    private String parseBadges(String tags) {
        StringBuilder result = new StringBuilder();

        String badgesValue = "";
        for (String tag : tags.split(";")) {
            if (tag.startsWith("badges=")) {
                badgesValue = tag.substring(7);
                break;
            }
        }

        if (badgesValue.isEmpty()) return "";

        for (String badge : badgesValue.split(",")) {
            String name = badge.split("/")[0];
            switch (name) {
                case "broadcaster" -> result.append("§c[стример]§r ");
                case "moderator"   -> result.append("§2[mod]§r ");
                case "vip"         -> result.append("§d[vip]§r ");
                case "subscriber"  -> result.append("§6[sub]§r ");
                case "partner"     -> result.append("§9[✓]§r ");
                case "staff"       -> result.append("§4[staff]§r ");
                case "turbo"       -> result.append("§5[turbo]§r ");
                case "premium"     -> result.append("§b[prime]§r ");
            }
        }

        return result.toString().trim();
    }

    private void send(Component msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null)
            mc.execute(() -> mc.player.sendSystemMessage(msg));
    }
}
