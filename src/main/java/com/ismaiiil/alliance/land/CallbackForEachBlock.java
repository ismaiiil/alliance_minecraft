package com.ismaiiil.alliance.land;

import org.bukkit.Location;
import org.bukkit.Material;

public interface CallbackForEachBlock {

    void block(Location highlightLocation, Material blockMaterial);

}
