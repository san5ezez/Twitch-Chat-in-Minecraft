package com.twitchchat.mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class TwitchCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(build("twitch"));
        dispatcher.register(build("twich"));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> build(String name) {
        return lit(name)
            .then(lit("channel").then(arg("channelName").executes(TwitchCommand::setChannel)))
            .then(lit("start").executes(TwitchCommand::startRelay))
            .then(lit("stop").executes(TwitchCommand::stopRelay))
            .then(lit("reload").executes(TwitchCommand::reload))
            .then(lit("status").executes(TwitchCommand::showStatus))
            .then(lit("autostart")
                .then(lit("on").executes(ctx -> setAutostart(ctx, true)))
                .then(lit("off").executes(ctx -> setAutostart(ctx, false))))
            .then(lit("sound").then(arg("nick").executes(TwitchCommand::setSound)))
            .then(lit("language")
                .then(lit("ru").executes(ctx -> setLanguage(ctx, "ru")))
                .then(lit("en").executes(ctx -> setLanguage(ctx, "en"))))
            .then(lit("help").executes(TwitchCommand::showHelp));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> lit(String s) {
        return LiteralArgumentBuilder.literal(s);
    }
    private static RequiredArgumentBuilder<FabricClientCommandSource, String> arg(String name) {
        return RequiredArgumentBuilder.argument(name, StringArgumentType.word());
    }

    public static void chat(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null)
            mc.execute(() -> mc.player.sendSystemMessage(Component.literal(text)));
    }

    private static int setChannel(CommandContext<FabricClientCommandSource> ctx) {
        String ch = StringArgumentType.getString(ctx, "channelName")
            .toLowerCase().trim().replaceAll("^#", "");
        TwitchConfig cfg = TwitchConfig.get();
        cfg.channel = ch;
        TwitchConfig.save();
        if (TwitchChatClient.IRC.isRunning()) {
            TwitchChatClient.IRC.disconnect();
            if (TwitchChatClient.IRC.connect(ch))
                chat("§d[TwitchChat] §f" + Lang.get("channel_changed") + " §e#" + ch + " §a" + Lang.get("reconnected"));
            else
                chat("§c[TwitchChat] " + Lang.get("connect_fail"));
        } else {
            chat("§d[TwitchChat] §f" + Lang.get("channel_set") + " §e#" + ch + " §7| /twitch start");
        }
        return 1;
    }

    private static int startRelay(CommandContext<FabricClientCommandSource> ctx) {
        TwitchConfig cfg = TwitchConfig.get();
        if (cfg.channel.isEmpty()) { chat("§c[TwitchChat] " + Lang.get("set_channel_first")); return 0; }
        if (TwitchChatClient.IRC.isRunning()) { chat("§c[TwitchChat] " + Lang.get("already_running") + " §e#" + cfg.channel); return 0; }
        chat("§d[TwitchChat] §7" + Lang.get("connect_start") + " §e#" + cfg.channel + "§7...");
        if (TwitchChatClient.IRC.connect(cfg.channel)) {
            chat("§d[TwitchChat] §a" + Lang.get("connected")); return 1;
        } else {
            chat("§c[TwitchChat] " + Lang.get("connect_fail")); return 0;
        }
    }

    private static int stopRelay(CommandContext<FabricClientCommandSource> ctx) {
        if (!TwitchChatClient.IRC.isRunning()) { chat("§c[TwitchChat] " + Lang.get("not_running")); return 0; }
        String ch = TwitchConfig.get().channel;
        TwitchChatClient.IRC.disconnect();
        chat("§d[TwitchChat] §c" + Lang.get("stopped") + " §e#" + ch + " §c" + Lang.get("stopped2"));
        return 1;
    }

    private static int reload(CommandContext<FabricClientCommandSource> ctx) {
        TwitchConfig cfg = TwitchConfig.get();
        if (cfg.channel.isEmpty()) { chat("§c[TwitchChat] " + Lang.get("no_channel")); return 0; }
        chat("§d[TwitchChat] §7" + Lang.get("reloading") + " §e#" + cfg.channel + "§7...");
        if (TwitchChatClient.IRC.isRunning()) TwitchChatClient.IRC.disconnect();
        if (TwitchChatClient.IRC.connect(cfg.channel)) {
            chat("§d[TwitchChat] §a" + Lang.get("reload_ok")); return 1;
        } else {
            chat("§c[TwitchChat] " + Lang.get("reload_fail")); return 0;
        }
    }

    private static int setAutostart(CommandContext<FabricClientCommandSource> ctx, boolean val) {
        TwitchConfig cfg = TwitchConfig.get();
        cfg.autostart = val;
        TwitchConfig.save();
        chat("§d[TwitchChat] §f" + Lang.get(val ? "autostart_on" : "autostart_off"));
        return 1;
    }

    private static int setSound(CommandContext<FabricClientCommandSource> ctx) {
        String nick = StringArgumentType.getString(ctx, "nick");
        TwitchConfig cfg = TwitchConfig.get();
        cfg.soundNick = nick;
        TwitchConfig.save();
        chat("§d[TwitchChat] §f" + Lang.get("sound_set") + " §e" + nick);
        return 1;
    }

    private static int setLanguage(CommandContext<FabricClientCommandSource> ctx, String lang) {
        TwitchConfig cfg = TwitchConfig.get();
        cfg.language = lang;
        TwitchConfig.save();
        chat("§d[TwitchChat] §f" + Lang.get("lang_set"));
        return 1;
    }

    private static int showStatus(CommandContext<FabricClientCommandSource> ctx) {
        TwitchConfig cfg = TwitchConfig.get();
        boolean running = TwitchChatClient.IRC.isRunning();
        chat("§d=== TwitchChat ===");
        chat("§7" + Lang.get("status_channel") + " §e" + (cfg.channel.isEmpty() ? Lang.get("not_set") : "#" + cfg.channel));
        chat("§7" + Lang.get("status_active") + ": " + (running ? "§a" + Lang.get("on") : "§c" + Lang.get("off")));
        chat("§7" + Lang.get("status_autostart") + " " + (cfg.autostart ? "§a" + Lang.get("on") : "§c" + Lang.get("off")));
        chat("§7" + Lang.get("status_sound") + " §e" + (cfg.soundNick.isEmpty() ? Lang.get("not_set") : cfg.soundNick));
        chat("§7" + Lang.get("status_lang") + " §e" + ("ru".equals(cfg.language) ? "Русский" : "English"));
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {
        chat("§d" + Lang.get("help_header"));
        chat("§e/twitch channel <name>");
        chat("§e/twitch start / stop");
        chat("§e/twitch reload");
        chat("§e/twitch autostart on/off");
        chat("§e/twitch sound <nick>");
        chat("§e/twitch language ru/en");
        chat("§e/twitch status");
        return 1;
    }
}
