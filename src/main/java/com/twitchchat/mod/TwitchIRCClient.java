package com.twitchchat.mod;

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

public class TwitchIRCClient {

    private static final String TWITCH_IRC_HOST = "irc.chat.twitch.tv";
    private static final int TWITCH_IRC_PORT = 6667;

    private static final String ANON_NICK = "justinfan" + (int)(Math.random() * 80000 + 1000);
    private static final String ANON_PASS = "SCHMOOPIIE";

    private static final Pattern MSG_PATTERN =
        Pattern.compile(":([^!]+)![^@]+@[^.]+\\.tmi\\.twitch\\.tv PRIVMSG #(\\S+) :(.+)");

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private String currentChannel = "";

    public boolean isRunning() { return running.get(); }
    public String getCurrentChannel() { return currentChannel; }

    public boolean connect(String channel) {
        if (running.get()) disconnect();
        currentChannel = channel.toLowerCase().trim().replaceAll("^#", "");
        try {
            socket = new Socket(TWITCH_IRC_HOST, TWITCH_IRC_PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer.println("PASS " + ANON_PASS);
            writer.println("NICK " + ANON_NICK);
            writer.println("JOIN #" + currentChannel);
            running.set(true);
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "TwitchIRC-Reader");
                t.setDaemon(true);
                return t;
            });
            executor.submit(this::readLoop);
            TwitchChatMod.LOGGER.info("Подключено к Twitch IRC: #{}", currentChannel);
            return true;
        } catch (IOException e) {
            TwitchChatMod.LOGGER.error("Ошибка подключения: {}", e.getMessage());
            cleanup();
            return false;
        }
    }

    public void disconnect() {
        running.set(false);
        try { if (writer != null) writer.println("PART #" + currentChannel); } catch (Exception ignored) {}
        cleanup();
    }

    private void cleanup() {
        try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        if (executor != null) executor.shutdownNow();
        writer = null; reader = null; socket = null; executor = null;
    }

    private void readLoop() {
        try {
            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                handleLine(line);
            }
        } catch (IOException e) {
            if (running.get()) {
                TwitchChatMod.LOGGER.warn("Соединение разорвано: {}", e.getMessage());
                sendToClient(Component.literal("§c[TwitchChat] Соединение с Twitch разорвано!"));
                running.set(false);
                cleanup();
            }
        }
    }

    private void handleLine(String line) {
        if (line.startsWith("PING")) {
            writer.println("PONG " + line.substring(5));
            return;
        }
        Matcher matcher = MSG_PATTERN.matcher(line);
        if (matcher.matches()) {
            String username = matcher.group(1);
            String message  = matcher.group(3).trim();
            Component chatMessage = Component.empty()
                .append(Component.literal("§d[Twitch] "))
                .append(Component.literal("§e" + username + "§r"))
                .append(Component.literal(": §f" + message));
            sendToClient(chatMessage);
        }
    }

    private void sendToClient(Component message) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.execute(() -> mc.player.sendSystemMessage(message));
        }
    }
}
