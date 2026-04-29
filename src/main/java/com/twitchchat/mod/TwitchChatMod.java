package com.twitchchat.mod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = TwitchChatMod.MODID, dist = Dist.CLIENT)
public class TwitchChatMod {

    public static final String MODID = "twitchchat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public TwitchChatMod(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Twitch Chat Bridge загружен (client-side)!");
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        TwitchCommand.register(event.getDispatcher());
    }
}
