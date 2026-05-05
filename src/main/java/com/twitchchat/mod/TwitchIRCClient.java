package com.twitchchat.mod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.*;

@Environment(EnvType.CLIENT)
public class TwitchIRCClient {

    private static final String HOST = "irc.chat.twitch.tv";
    private static final int PORT = 6667;
    private static final String NICK = "justinfan" + (int)(Math.random() * 80000 + 1000);

    private static final Pattern TAG_MSG = Pattern.compile(
        "@([^ ]+) :([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv PRIVMSG #(\\S+) :(.+)"
    );
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
                sendToChat(Component.literal("§c[TwitchChat] " + Lang.get("connection_lost")));
                running.set(false);
                cleanup();
            }
        }
    }

    private void handle(String line) {
        if (line.startsWith("PING")) { writer.println("PONG " + line.substring(5)); return; }
        if (line.contains(" USERNOTICE ") || line.contains(" CLEARCHAT ") || line.contains(" NOTICE ")) return;

        Matcher m = TAG_MSG.matcher(line);
        if (m.matches()) {
            String tags  = m.group(1);
            String user  = m.group(2);
            String msg   = m.group(4).trim();
            String badge = parseBadges(tags);
            String prefix = badge.isEmpty() ? "" : badge + " ";
            sendToChat(Component.literal("§d[Twitch] " + prefix + "§e" + user + "§r: §f" + msg));
            checkMention(user, msg);
            return;
        }

        Matcher m2 = PLAIN_MSG.matcher(line);
        if (m2.matches()) {
            String user = m2.group(1);
            String msg  = m2.group(3).trim();
            sendToChat(Component.literal("§d[Twitch] §e" + user + "§r: §f" + msg));
            checkMention(user, msg);
        }
    }

    private void checkMention(String user, String msg) {
        TwitchConfig cfg = TwitchConfig.get();
        if (cfg.soundNick == null || cfg.soundNick.isEmpty()) return;
        if (msg.toLowerCase().contains(cfg.soundNick.toLowerCase())) {
            sendToChat(Component.literal("§b[TwitchChat] " + Lang.get("mention") + " §b" + user + "§e: §f" + msg));
            playSound();
        }
    }

    private void playSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        mc.execute(() -> mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 2.0f));
    }

    private String parseBadges(String tags) {
        String tagsPart = tags.contains(" ") ? tags.substring(0, tags.indexOf(' ')) : tags;
        String badgesValue = "";
        for (String tag : tagsPart.split(";")) {
            if (tag.startsWith("badges=")) { badgesValue = tag.substring(7); break; }
        }
        if (badgesValue.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        for (String badge : badgesValue.split(",")) {
            String name = badge.split("/")[0];
            String fmt = switch (name) {
                case "broadcaster" -> "§c[" + Lang.get("badge_streamer") + "]§r";
                case "moderator"   -> "§2[mod]§r";
                case "vip"         -> "§d[vip]§r";
                case "subscriber"  -> "§6[sub]§r";
                case "partner"     -> "§9[✓]§r";
                case "premium"     -> "§b[prime]§r";
                case "turbo"       -> "§5[turbo]§r";
                case "staff"       -> "§4[staff]§r";
                case "bits"        -> "§e[bits]§r";
                case "founder"     -> "§6[founder]§r";
                case "artist-badge"-> "§3[artist]§r";
                default            -> "";
            };
            if (!fmt.isEmpty()) { if (result.length() > 0) result.append(" "); result.append(fmt); }
        }
        return result.toString();
    }

    private void sendToChat(Component msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null)
            mc.execute(() -> mc.player.sendSystemMessage(msg));
    }
}
