package com.ismaiiil.alliance.features.rtp;

import com.ismaiiil.alliance.commands.AllCommandsConstants;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class TPLink {
    public static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public UUID createdBy;
    public long timeCreated;
    public ArrayList<UUID> listUsedBy = new ArrayList<>();
    public Location destination;
    public String linkID;


    public TPLink(Player player,Location destination){
        createdBy = player.getUniqueId();
        timeCreated = System.currentTimeMillis();
        this.destination = destination;
        linkID = RandomStringUtils.random(15,chars);
    }

    public boolean hasExpired(){
        long tpLinkExpireTime = 30000;
        return (System.currentTimeMillis() - timeCreated) > tpLinkExpireTime;
    }

    public Component buildTPLinkShareMessage(){
        var tpLinkCommand = AllCommandsConstants.RTP + " " + linkID;
        return Component.text("Click ")
                .append( Component.text("here ")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.copyToClipboard(tpLinkCommand))
                        .decoration(TextDecoration.UNDERLINED, true)
                        .decoration(TextDecoration.BOLD, true)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy")))
                )
                .append( Component.text("to copy shareable TP command, "))
                .append( Component.text("Press CTRL+V in chat to paste "))
                ;
    }

}
