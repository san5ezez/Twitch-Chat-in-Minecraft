package com.twitchchat.mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class TwitchCommand {

    private static final TwitchIRCClient IRC = new TwitchIRCClient();
    private static String savedChannel = "";

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(cmd("twitch"));
        dispatcher.register(cmd("twich"));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> cmd(String name) {
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal(name)
            .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("channel")
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("channelName", StringArgumentType.word())
                    .executes(TwitchCommand::setChannel)))
            .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("start")
                .executes(TwitchCommand::startRelay))
            .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("stop")
                .executes(TwitchCommand::stopRelay))
            .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("status")
                .executes(TwitchCommand::showStatus));
    }

    public static void chat(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null)
            mc.execute(() -> mc.player.sendSystemMessage(Component.literal(text)));
    }

    private static int setChannel(CommandContext<FabricClientCommandSource> ctx) {
        savedChannel = StringArgumentType.getString(ctx, "channelName")
            .toLowerCase().trim().replaceAll("^#", "");
        if (IRC.isRunning()) {
            IRC.disconnect();
            if (IRC.connect(savedChannel))
                chat("§d[TwitchChat] §fКанал изменён: §e#" + savedChannel + " §aпереподключено!");
            else
                chat("§c[TwitchChat] Канал сохранён §e#" + savedChannel + "§c, подключиться не удалось.");
        } else {
            chat("§d[TwitchChat] §fКанал сохранён: §e#" + savedChannel + " §7| /twitch start чтобы начать");
        }
        return 1;
    }

    private static int startRelay(CommandContext<FabricClientCommandSource> ctx) {
        if (savedChannel.isEmpty()) {
            chat("§c[TwitchChat] Сначала укажи канал: §e/twitch channel <название>");
            return 0;
        }
        if (IRC.isRunning()) {
            chat("§c[TwitchChat] Уже запущено! Канал: §e#" + savedChannel);
            return 0;
        }
        chat("§d[TwitchChat] §7Подключаюсь к §e#" + savedChannel + "§7...");
        if (IRC.connect(savedChannel)) {
            chat("§d[TwitchChat] §aПодключено к §e#" + savedChannel + "§a! Чат активен.");
            return 1;
        } else {
            chat("§c[TwitchChat] Не удалось подключиться. Проверь интернет.");
            return 0;
        }
    }

    private static int stopRelay(CommandContext<FabricClientCommandSource> ctx) {
        if (!IRC.isRunning()) {
            chat("§c[TwitchChat] Трансляция не запущена.");
            return 0;
        }
        IRC.disconnect();
        chat("§d[TwitchChat] §cОстановлено. Канал §e#" + savedChannel + "§c отключён.");
        return 1;
    }

    private static int showStatus(CommandContext<FabricClientCommandSource> ctx) {
        if (IRC.isRunning())
            chat("§d[TwitchChat] §aАктивно §7| Канал: §e#" + savedChannel);
        else if (!savedChannel.isEmpty())
            chat("§d[TwitchChat] §cОстановлено §7| Канал: §e#" + savedChannel + " §7| /twitch start");
        else
            chat("§d[TwitchChat] §cНе настроено §7| /twitch channel <название>");
        return 1;
    }
}
