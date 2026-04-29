package com.twitchchat.mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TwitchCommand {

    private static final TwitchIRCClient CLIENT = new TwitchIRCClient();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("twitch")
                .then(Commands.literal("channel")
                    .then(Commands.argument("channelName", StringArgumentType.word())
                        .executes(TwitchCommand::setChannel)
                    )
                )
                .then(Commands.literal("start")
                    .executes(TwitchCommand::startRelay)
                )
                .then(Commands.literal("stop")
                    .executes(TwitchCommand::stopRelay)
                )
                .then(Commands.literal("status")
                    .executes(TwitchCommand::showStatus)
                )
        );

        dispatcher.register(
            Commands.literal("twich")
                .then(Commands.literal("channel")
                    .then(Commands.argument("channelName", StringArgumentType.word())
                        .executes(TwitchCommand::setChannel)
                    )
                )
                .then(Commands.literal("start")
                    .executes(TwitchCommand::startRelay)
                )
                .then(Commands.literal("stop")
                    .executes(TwitchCommand::stopRelay)
                )
                .then(Commands.literal("status")
                    .executes(TwitchCommand::showStatus)
                )
        );
    }

    // Отправить сообщение только в локальный чат — только этот игрок видит
    public static void sendToLocalChat(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.execute(() -> mc.player.sendSystemMessage(message));
        }
    }

    private static int setChannel(CommandContext<CommandSourceStack> ctx) {
        String channelName = StringArgumentType.getString(ctx, "channelName");
        boolean wasRunning = CLIENT.isRunning();

        if (wasRunning) {
            CLIENT.disconnect();
        }

        sendToLocalChat(Component.literal(
            "§d[TwitchChat] §fКанал установлен: §e#" + channelName
        ));

        if (wasRunning) {
            boolean connected = CLIENT.connect(channelName);
            if (connected) {
                sendToLocalChat(Component.literal(
                    "§d[TwitchChat] §aПодключено к §e#" + channelName + "§a!"
                ));
            } else {
                sendToLocalChat(Component.literal(
                    "§c[TwitchChat] Не удалось подключиться к #" + channelName
                ));
            }
        }
        return 1;
    }

    private static int startRelay(CommandContext<CommandSourceStack> ctx) {
        String channel = CLIENT.getCurrentChannel();

        if (channel == null || channel.isEmpty()) {
            sendToLocalChat(Component.literal(
                "§c[TwitchChat] Сначала укажи канал: §e/twitch channel <название>"
            ));
            return 0;
        }

        if (CLIENT.isRunning()) {
            sendToLocalChat(Component.literal(
                "§c[TwitchChat] Уже запущено! Канал: §e#" + channel
            ));
            return 0;
        }

        sendToLocalChat(Component.literal(
            "§d[TwitchChat] §7Подключаюсь к §e#" + channel + "§7..."
        ));

        boolean connected = CLIENT.connect(channel);
        if (connected) {
            sendToLocalChat(Component.literal(
                "§d[TwitchChat] §aПодключено к §e#" + channel + "§a! Чат активен."
            ));
            return 1;
        } else {
            sendToLocalChat(Component.literal(
                "§c[TwitchChat] Не удалось подключиться. Проверь интернет."
            ));
            return 0;
        }
    }

    private static int stopRelay(CommandContext<CommandSourceStack> ctx) {
        if (!CLIENT.isRunning()) {
            sendToLocalChat(Component.literal(
                "§c[TwitchChat] Трансляция не запущена."
            ));
            return 0;
        }

        String channel = CLIENT.getCurrentChannel();
        CLIENT.disconnect();

        sendToLocalChat(Component.literal(
            "§d[TwitchChat] §cОстановлено. Канал §e#" + channel + "§c отключён."
        ));
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        boolean running = CLIENT.isRunning();
        String channel = CLIENT.getCurrentChannel();

        if (running) {
            sendToLocalChat(Component.literal(
                "§d[TwitchChat] §aАктивно §7| Канал: §e#" + channel
            ));
        } else if (channel != null && !channel.isEmpty()) {
            sendToLocalChat(Component.literal(
                "§d[TwitchChat] §cОстановлено §7| Канал: §e#" + channel +
                " §7| /twitch start чтобы начать"
            ));
        } else {
            sendToLocalChat(Component.literal(
                "§d[TwitchChat] §cНе настроено §7| /twitch channel <название>"
            ));
        }
        return 1;
    }
}
