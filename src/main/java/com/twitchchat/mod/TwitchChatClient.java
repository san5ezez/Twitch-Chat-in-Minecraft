package com.twitchchat.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class TwitchChatClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("twitchchat");
    public static final TwitchIRCClient IRC = new TwitchIRCClient();
    private static boolean autostartDone = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Twitch Chat in Minecraft v2.0 loaded!");
        TwitchConfig.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            TwitchCommand.register(dispatcher));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!autostartDone && client.player != null) {
                autostartDone = true;
                TwitchConfig cfg = TwitchConfig.get();
                if (cfg.autostart && !cfg.channel.isEmpty() && !IRC.isRunning())
                    if (IRC.connect(cfg.channel))
                        TwitchCommand.chat("§d[TwitchChat] §a" + Lang.get("autostart_launch") + " §e#" + cfg.channel);
            }
            if (autostartDone && Minecraft.getInstance().player == null)
                autostartDone = false;
        });
    }
}
