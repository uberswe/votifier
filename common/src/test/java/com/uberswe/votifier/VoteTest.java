package com.uberswe.votifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoteTest {

    @Test
    void constructorAndGetters() {
        Vote vote = new Vote("TestService", "PlayerOne", "127.0.0.1", "1700000000");
        assertEquals("TestService", vote.getServiceName());
        assertEquals("PlayerOne", vote.getUsername());
        assertEquals("127.0.0.1", vote.getAddress());
        assertEquals("1700000000", vote.getTimestamp());
    }

    @Test
    void emptyStrings() {
        Vote vote = new Vote("", "", "", "");
        assertEquals("", vote.getServiceName());
        assertEquals("", vote.getUsername());
        assertEquals("", vote.getAddress());
        assertEquals("", vote.getTimestamp());
    }

    @Test
    void specialCharacters() {
        Vote vote = new Vote("Service With Spaces", "Player_Name-123", "::1", "2024-01-01T00:00:00Z");
        assertEquals("Service With Spaces", vote.getServiceName());
        assertEquals("Player_Name-123", vote.getUsername());
        assertEquals("::1", vote.getAddress());
        assertEquals("2024-01-01T00:00:00Z", vote.getTimestamp());
    }

    @Test
    void unicodeCharacters() {
        Vote vote = new Vote("Srv\u00e9r", "\u00fc\u00df\u00e9r", "10.0.0.1", "now");
        assertEquals("Srv\u00e9r", vote.getServiceName());
        assertEquals("\u00fc\u00df\u00e9r", vote.getUsername());
    }
}
