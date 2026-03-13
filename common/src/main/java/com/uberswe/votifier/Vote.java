package com.uberswe.votifier;

public class Vote {
    private final String serviceName;
    private final String username;
    private final String address;
    private final String timestamp;

    public Vote(String serviceName, String username, String address, String timestamp) {
        this.serviceName = serviceName;
        this.username = username;
        this.address = address;
        this.timestamp = timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getUsername() {
        return username;
    }

    public String getAddress() {
        return address;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
