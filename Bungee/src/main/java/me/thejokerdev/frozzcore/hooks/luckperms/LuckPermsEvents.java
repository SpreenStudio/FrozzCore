package me.thejokerdev.frozzcore.hooks.luckperms;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.hooks.webhooks.WebHook;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.actionlog.Action;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.log.LogPublishEvent;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LuckPermsEvents {
    private final BungeeMain plugin;
    private final LuckPerms luckPerms;

    public LuckPermsEvents(BungeeMain plugin) {
        this.plugin = plugin;
        this.luckPerms = plugin.getApi();

        register();
    }

    public void register(){
        EventBus eventBus = luckPerms.getEventBus();

        eventBus.subscribe(LogPublishEvent.class, e ->{
            WebHook webHook = plugin.getWebhookManager().getPermissions();
            Action loggedAction = e.getEntry();
            Instant timestamp = loggedAction.getTimestamp();
            String time = "<t:%s:f>";
            String timeFormatted = String.format(time, timestamp.getEpochSecond());
            String description = loggedAction.getDescription();
            boolean isRemoveAction = description.contains("unset") || description.contains("remove") || description.contains("delete") || description.contains("clear");
            webHook.setTitle("Action Log")
                    .addField("Timestamp", timeFormatted, true)
                    .addField("Source", loggedAction.getSource().getName() + " (" + loggedAction.getSource().getUniqueId() + ")", true)
                    .addField("Target", loggedAction.getTarget().getName() + (loggedAction.getTarget().getUniqueId().isPresent() ? " (" + getTargetString(loggedAction.getTarget().getUniqueId()) + ")" : ""), true)
                    .addField("Target Type", loggedAction.getTarget().getType().toString(), true)
                    .addField("Description", description, false);
            webHook.setColor(isRemoveAction ? "#ff0000" : "#00ff00");
            webHook.setTimestamp(true);
            webHook.execute();
        });
    }

    public void unregister(){
        Set<EventSubscription<LogPublishEvent>> subscriptions = luckPerms.getEventBus().getSubscriptions(LogPublishEvent.class);
        subscriptions.forEach(e -> {
            e.close();
            plugin.log("&cUnregistered LuckPerms event: " + e.getEventClass().getSimpleName() + " (" + e.getHandler() +", "+ e.getEventClass()+")");
        });
        subscriptions.clear();
    }

    private static String getTargetString(Optional<UUID> target) {
        return target.map(UUID::toString).orElse("None");
    }
}