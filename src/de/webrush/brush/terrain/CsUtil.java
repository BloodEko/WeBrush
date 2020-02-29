package de.webrush.brush.terrain;

import java.util.ArrayList;

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

public class CsUtil {

    public static final ArrayList<BlockType> unsolidList = new ArrayList<>();
    
    static {
        unsolidList.add(BlockTypes.AIR);
        unsolidList.add(BlockTypes.WATER);
        unsolidList.add(BlockTypes.LAVA);
    }
}
