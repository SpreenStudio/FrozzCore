package me.thejokerdev.frozzcore.hooks.webhooks;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import lombok.Getter;
import lombok.Setter;
import me.thejokerdev.frozzcore.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;

@Getter
public class WebHook {
    private final BungeeMain plugin;
    private final String url;
    private String message;
    private String username;
    private String avatarUrl;
    private String title;
    private String description;
    private String color;
    private String footer;
    private String footerIcon;
    private String thumbnail;
    private String image;
    private String author;
    private String authorIcon;
    private String authorUrl;
    private boolean timestamp;

    public WebHook(BungeeMain plugin, String url) {
        this.plugin = plugin;
        this.url = url;
    }

    public WebHook(BungeeMain plugin, Configuration config){
        this.plugin = plugin;
        this.url = config.getString("url");
        this.message = config.getString("message");
        this.username = config.getString("username");
        this.avatarUrl = config.getString("avatar");
        this.title = config.getString("embed.title");
        this.description = config.getString("embed.description");
        this.color = config.getString("embed.color");
        this.footer = config.getString("embed.footer");
        this.footerIcon = config.getString("embed.footerIcon");
        this.thumbnail = config.getString("embed.thumbnail");
        this.image = config.getString("embed.image");
        this.author = config.getString("embed.author");
        this.authorIcon = config.getString("embed.authorIcon");
        this.authorUrl = config.getString("embed.authorUrl");
        this.timestamp = config.getBoolean("embed.timestamp");
    }

    public WebHook setMessage(String message) {
        this.message = message;
        return this;
    }

    public WebHook setUsername(String username) {
        this.username = username;
        return this;
    }

    public WebHook setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public WebHook setTitle(String title) {
        this.title = title;
        return this;
    }

    public WebHook setDescription(String description) {
        this.description = description;
        return this;
    }

    public WebHook setColor(String color) {
        this.color = color;
        return this;
    }

    public WebHook setFooter(String footer) {
        this.footer = footer;
        return this;
    }

    public WebHook setFooterIcon(String footerIcon) {
        this.footerIcon = footerIcon;
        return this;
    }

    public WebHook setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public WebHook setImage(String image) {
        this.image = image;
        return this;
    }

    public WebHook setAuthor(String author) {
        this.author = author;
        return this;
    }

    public WebHook setAuthorIcon(String authorIcon) {
        this.authorIcon = authorIcon;
        return this;
    }

    public WebHook setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
        return this;
    }

    public WebHook setTimestamp(boolean timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    private final ArrayList<WebhookEmbed.EmbedField> fields = new ArrayList<>();

    public WebHook addField(String name, String value, boolean inline){
        fields.add(new WebhookEmbed.EmbedField(inline, name, value));
        return this;
    }

    public void execute() {
        WebhookClient client = WebhookClient.withUrl(url);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        if (!this.username.isEmpty()){
            builder.setUsername(this.username);
        }
        if (!this.avatarUrl.isEmpty()){
            builder.setAvatarUrl(this.avatarUrl);
        }
        if (!this.message.isEmpty()){
            message = plugin.getUtils().ct(message);
            message = ChatColor.stripColor(message);
            builder.setContent(this.message);
        }

        TemporalAccessor timestamp = null;
        if (this.timestamp) {
            timestamp = java.time.Instant.now();
        }

        Color color = null;
        if (this.color != null) {
            color = Color.decode(this.color);
        } else {
            color = Color.decode("#2F3136");
        }

        if (!description.isEmpty()){
            description = plugin.getUtils().ct(description);
            description = ChatColor.stripColor(description);
        }

        WebhookEmbed embed;
        @NotNull WebhookEmbedBuilder builder1 = new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle(title, null))
                .setDescription(description)
                .setColor(color.hashCode())
                .setFooter(new WebhookEmbed.EmbedFooter(footer, footerIcon))
                .setThumbnailUrl(thumbnail)
                .setImageUrl(image)
                .setAuthor(new WebhookEmbed.EmbedAuthor(author, authorUrl, authorIcon))
                .setTimestamp(timestamp);

        if (!fields.isEmpty()){
            fields.forEach(builder1::addField);
        }

        if (this.embed){
            embed = builder1.build();

            builder.addEmbeds(embed);
        }
        client.send(builder.build());
        client.close();
    }

    @Setter
    boolean embed = true;

    public WebHook setEmbed(boolean embed) {
        this.embed = embed;
        return this;
    }

    /**
     * @return a clone of this WebHook
     */
    @Override
    public WebHook clone(){
        WebHook hook = new WebHook(plugin, url);
        hook.setMessage(message);
        hook.setUsername(username);
        hook.setAvatarUrl(avatarUrl);
        hook.setTitle(title);
        hook.setDescription(description);
        hook.setColor(color);
        hook.setFooter(footer);
        hook.setFooterIcon(footerIcon);
        hook.setThumbnail(thumbnail);
        hook.setImage(image);
        hook.setAuthor(author);
        hook.setAuthorIcon(authorIcon);
        hook.setAuthorUrl(authorUrl);
        hook.setTimestamp(timestamp);
        return hook;
    }
}
