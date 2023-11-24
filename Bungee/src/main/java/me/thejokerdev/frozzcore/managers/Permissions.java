package me.thejokerdev.frozzcore.managers;

public enum Permissions {
    PROXYUTILS_ADMIN("proxyutils.admin"),
    STAFFCHAT_STAFF("staffchat.staff"),
    STAFFCHAT_JOIN("staffchat.join"),
    STAFFCHAT_LEAVE("staffchat.leave"),
    ABPS_BYPASS("abps.bypass"),
    ABPS_NOTIFY("abps.notify"),
    STREAMER("stream.msg")
    ;


    String perm;
    Permissions(String perm){
        this.perm = perm;
    }

    public String get() {
        return perm;
    }
}
