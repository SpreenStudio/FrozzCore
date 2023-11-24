package me.thejokerdev.frozzcore.redis.payload;

public enum Payload {
    SERVER_ADD,
    SERVER_REMOVE,
    JOIN,
    LEAVE,
    SERVER_CHANGE,
    MESSAGE,
    MESSAGE_COMPONENT,
    BUNGEE_COUNT,
    CONNECT_PLAYER;

    Payload() {
    }
}
