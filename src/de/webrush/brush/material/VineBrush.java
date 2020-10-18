package de.webrush.brush.material;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.webrush.util.ChangeTracker;
import de.webrush.util.Shaper;
import de.webrush.util.Shaper.BrushFunction;

/**
 * Places hanging Vines by a length and density from 0.0 to 1.0. <br>
 * Similar to /cs build vines from CraftScripts. <br>
 * <br>
 */
public class VineBrush implements Brush {

    private final double density;
    private final int    length;
    private final Random rand = new Random();
    private final Set<BlockVector3> marked = new HashSet<>();
    
    
    public VineBrush(double density, int length) {
        this.density = density;
        this.length  = length;
    }
    
    @Override
    public void build(EditSession session, BlockVector3 click, Pattern mat, double size) throws MaxChangedBlocksException {
        marked.clear();
        
        BlockState[] vines = new BlockState[4];
        vines[0] = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("east"),  true);
        vines[1] = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("west"),  true);
        vines[2] = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("south"), true);
        vines[3] = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("north"), true);
        
        ChangeTracker tracker  = new ChangeTracker(session);
        BlockType     material = mat.applyBlock(BlockVector3.at(0,0,0)).getBlockType();
        
        BrushFunction vinebrush = vec -> {
            if (Math.random() > density) return;
            if (BlockTypes.AIR != tracker.get(vec).getBlockType() || marked.contains(vec)) return;

            BlockType[] blockFaces = new BlockType[4];
            blockFaces[0] = tracker.get(vec.add(1,0,0)).getBlockType();
            blockFaces[1] = tracker.get(vec.add(-1,0,0)).getBlockType();
            blockFaces[2] = tracker.get(vec.add(0,0,1)).getBlockType();
            blockFaces[3] = tracker.get(vec.add(0,0,-1)).getBlockType();

            List<Integer> solidSides = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                if (!blockFaces[i].getMaterial().isSolid() || blockFaces[i] == material) {
                    continue;
                }
                if (blockFaces[i] != BlockTypes.AIR) {
                    solidSides.add(i);
                }                                             
            }
            if (solidSides.size() >= 1) {
                int randomSide   = solidSides.get(rand.nextInt(solidSides.size()));
                int randomLength = rand.nextInt(length);
                
                for (int extendVine = 0; extendVine <= randomLength; extendVine++) {
                    BlockVector3 vineLoc = vec.add(0, -(extendVine), 0);
                    
                    if (BlockTypes.AIR == tracker.get(vineLoc).getBlockType()) {
                        if (BlockTypes.VINE == material) {
                            tracker.setHard(vineLoc, vines[randomSide]);
                        } else {
                            tracker.setHard(vineLoc, material.getDefaultState());
                        }
                        marked.add(vec);
                    } else {
                        break;
                    }
                }
            }
        };
        
        Shaper.runSphere(vinebrush, click, size);
        tracker.writeToSession();
    }  
}
