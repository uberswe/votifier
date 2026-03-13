package com.uberswe.votifier;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;

@Mod(Constants.MOD_ID)
public class Votifier {
    private VotifierServer votifierServer;

    public Votifier() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get().resolve("votifier");
            VotifierConfig config = VotifierConfig.load(configDir);
            RSAKeyManager keyManager = new RSAKeyManager();
            keyManager.load(configDir);
            VoteStorage voteStorage = new VoteStorage(configDir);

            Constants.LOG.info("Votifier public key (paste this into your server list website):");
            Constants.LOG.info(keyManager.getPublicKeyBase64());
            Constants.LOG.info("Votifier v2 token: {}", config.getToken());

            votifierServer = new VotifierServer(config, keyManager, voteStorage);
            votifierServer.start();
        } catch (Exception e) {
            Constants.LOG.error("Failed to start Votifier", e);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (votifierServer != null) {
            votifierServer.stop();
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (votifierServer == null) return;
        MinecraftServer server = event.getPlayer().getServer();
        if (server == null) return;

        String playerName = event.getPlayer().getGameProfile().getName();
        List<Vote> votes = votifierServer.getVoteStorage().getAndRemoveVotes(playerName);
        if (!votes.isEmpty()) {
            Constants.LOG.info("Processing {} pending vote(s) for {}", votes.size(), playerName);
            for (Vote vote : votes) {
                for (String command : votifierServer.getConfig().getCommands()) {
                    String cmd = command.replace("{player}", playerName);
                    server.getCommands().performCommand(server.createCommandSourceStack(), cmd);
                }
            }
        }
    }
}
