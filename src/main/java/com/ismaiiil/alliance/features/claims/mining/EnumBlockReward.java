package com.ismaiiil.alliance.features.claims.mining;

import org.bukkit.Material;

import java.util.*;

public class EnumBlockReward {

    private static final HashMap<Material, Double> values = new HashMap<>();

    static {
        values.put(Material.STONE, 0.01);
        values.put(Material.COBBLESTONE, 0.01);
        values.put(Material.DIRT, 0.005);
        values.put(Material.GRASS_BLOCK, 0.005);
        values.put(Material.IRON_ORE, 0.1);
        values.put(Material.COAL_ORE, 0.05);
        values.put(Material.REDSTONE_ORE, 1.0);
        values.put(Material.GOLD_ORE, 1.0);
        values.put(Material.LAPIS_ORE, 2.0);
        values.put(Material.DIAMOND_ORE, 10.0);
        values.put(Material.ANCIENT_DEBRIS, 15.0);
    }

    public static Double getMatValue(Material material){
        if (values.containsKey(material)){
            return values.get(material);
        }
        return 0.0;
    }

    public static boolean isValued(Material material){
        return values.containsKey(material);
    }
}
