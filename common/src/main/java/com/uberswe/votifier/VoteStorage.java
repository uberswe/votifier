package com.uberswe.votifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoteStorage {
    private final Path storageFile;
    private final Map<String, List<Vote>> pendingVotes = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, List<Vote>>>() {}.getType();

    public VoteStorage(Path configDir) {
        this.storageFile = configDir.resolve("pending_votes.json");
        load();
    }

    private void load() {
        if (Files.exists(storageFile)) {
            try (Reader reader = Files.newBufferedReader(storageFile)) {
                Map<String, List<Vote>> loaded = GSON.fromJson(reader, TYPE);
                if (loaded != null) {
                    pendingVotes.putAll(loaded);
                }
            } catch (IOException e) {
                Constants.LOG.error("Failed to load pending votes", e);
            }
        }
    }

    public synchronized void addVote(Vote vote) {
        pendingVotes.computeIfAbsent(vote.getUsername().toLowerCase(), k -> new ArrayList<>()).add(vote);
        save();
    }

    public synchronized List<Vote> getAndRemoveVotes(String playerName) {
        List<Vote> votes = pendingVotes.remove(playerName.toLowerCase());
        if (votes != null) {
            save();
        }
        return votes != null ? votes : Collections.emptyList();
    }

    private void save() {
        try {
            Files.createDirectories(storageFile.getParent());
            try (Writer writer = Files.newBufferedWriter(storageFile)) {
                GSON.toJson(pendingVotes, TYPE, writer);
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to save pending votes", e);
        }
    }
}
