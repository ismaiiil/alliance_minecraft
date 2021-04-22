package com.ismaiiil.alliance.features.claims.corner;


import com.sk89q.worldedit.math.BlockVector2;
import lombok.*;
import org.bukkit.block.Block;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Corner {
    public String regionId;
    public BlockVector2 oldCorner;
    public CornerTypes cornerTypes;

    public Corner(String regionId, Block oldCorner, CornerTypes cornerTypes) {
        this.regionId = regionId;
        this.oldCorner = BlockVector2.at(oldCorner.getX(), oldCorner.getZ());
        this.cornerTypes = cornerTypes;
    }

}
