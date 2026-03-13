package com.uberswe.votifier;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.List;

public class Votifier implements DedicatedServerModInitializer {
    private VotifierServer votifierServer;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            String playerName = handler.getPlayer().getGameProfile().getName();
            onPlayerJoin(server, playerName);
        });
    }

    private void onServerStarting(MinecraftServer server) {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("votifier");
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

    private void onServerStopping(MinecraftServer server) {
        if (votifierServer != null) {
            votifierServer.stop();
        }
    }

    private void onPlayerJoin(MinecraftServer server, String playerName) {
        if (votifierServer == null) return;

        List<Vote> votes = votifierServer.getVoteStorage().getAndRemoveVotes(playerName);
        if (!votes.isEmpty()) {
            Constants.LOG.info("Processing {} pending vote(s) for {}", votes.size(), playerName);
            for (Vote vote : votes) {
                for (String command : votifierServer.getConfig().getCommands()) {
                    String cmd = command.replace("{player}", playerName);
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), cmd);
                }
            }
        }
    }
}
