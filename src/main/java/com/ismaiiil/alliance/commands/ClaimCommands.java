package com.ismaiiil.alliance.commands;

import com.ismaiiil.alliance.AlliancePlugin;
import com.ismaiiil.alliance.land.AllianceRegionManager;
import com.ismaiiil.alliance.scoreboard.AllianceScoreboardManager;
import com.ismaiiil.alliance.scoreboard.EnumObjective;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@Command("claims")
@Permission("alliance.claims.basic")
public class ClaimCommands {

    @Default
    public static void claims(CommandSender sender){
        sender.sendMessage("--- claims help ---");
        sender.sendMessage("/claims - Show this help");
        sender.sendMessage("/claims list - displays all the regions you claimed");
        sender.sendMessage("/claims delete <regionName> - delete the region <regionName>");
    }

    @Subcommand("list")
    @Permission("alliance.claims.list")
    public static void claimsList(Player player){
        var regions = AlliancePlugin.getInstance().defaultRegionManager.getRegions();
        ArrayList<String> ownedRegions = new ArrayList<>();

        for (var region:regions.values()) {
            if (region.isOwner(WorldGuardPlugin.inst().wrapPlayer(player))){
                ownedRegions.add(region.getId());
            }
        }

        player.sendMessage(Component.text("Your owned regions: ")
                .color(NamedTextColor.BLUE)
                .append( Component.text(ownedRegions.toString()))
        );

    }

    @Subcommand("delete")
    @Permission("alliance.claims.delete")
    public static void claimsDelete(Player player, @AStringArgument String regionId){
        var defaultRm = AlliancePlugin.getInstance().defaultRegionManager;
        var regions = defaultRm.getRegions();
        var playerData = AlliancePlugin.getPlayerData(player);

        ArrayList<String> ownedRegions = new ArrayList<>();

        for (var region:regions.values()) {
            if (region.isOwner(WorldGuardPlugin.inst().wrapPlayer(player))){
                ownedRegions.add(region.getId());
            }
        }

        if (ownedRegions.contains(regionId)){
            var region = (ProtectedCuboidRegion) defaultRm.getRegion(regionId);
            if (region == null){
                player.sendMessage(Component.text("Region " + regionId + " does note exist anymore")
                        .color(NamedTextColor.RED)
                );
                return;
            }

            var blocksToReset = AllianceRegionManager.getBorderBlocks(region, player,(hl, bm) -> { });

            AlliancePlugin.getInstance().defaultRegionManager.removeRegion(regionId);

            player.sendMessage(Component.text("Deleted Region " + regionId)
                    .color(NamedTextColor.GREEN)
            );

            var area = AllianceRegionManager.calculateArea(region);

            playerData.balance += area;
            playerData.usedBalance -= area;

            AllianceRegionManager.borderCache.reset(player,blocksToReset).thenApply(
                    empty ->{
                        AllianceScoreboardManager.updateAllPlayerScores(player, EnumObjective.BALANCE);
                        return null;
                    });
        }else{
            player.sendMessage(Component.text("You do not own this region " + regionId + ", or it does not exist")
                    .color(NamedTextColor.RED)
            );
        }

    }

}
