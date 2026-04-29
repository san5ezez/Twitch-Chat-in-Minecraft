package com.twitchchat.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class TwitchChatClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("twitchchat");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Twitch Chat Bridge загружен (Fabric 26.1 / Loader 0.18.5)!");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            TwitchCommand.register(dispatcher)
        );
    }
}
