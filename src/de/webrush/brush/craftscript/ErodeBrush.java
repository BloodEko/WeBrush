package de.webrush.brush.craftscript;

import java.util.ArrayList;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.webrush.ChangeTracker;
import de.webrush.Shaper;
import de.webrush.Shaper.BrushFunction;

/**
 * Erodes Terrain away based on faces and iterations. <br>
 * Similar to /cs build erode from CraftSripts. <br>
 * Use size:2 faces:2 iterations:1 for finer path creation. 
 */
public class ErodeBrush implements Brush {
    
    public static ArrayList<BlockType> blackList;
    
    static {
        blackList = new ArrayList<>();
        blackList.add(BlockTypes.AIR);
        blackList.add(BlockTypes.WATER);
        blackList.add(BlockTypes.LAVA);
    }
    
    private final int maxFaces;
    private final int iterations;
    
    public ErodeBrush(int maxFaces, int iterations) {
        this.maxFaces   = maxFaces;
        this.iterations = iterations;
    }
    
    
    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        makeErosion(editSession, position, size);
    }
    
    
    private void makeErosion(EditSession session, BlockVector3 click, double size) throws MaxChangedBlocksException {
        
        ChangeTracker tracker    = new ChangeTracker(session);
        BlockType[]   blockFaces = new BlockType[6];
        
        BrushFunction erode = vec -> {
            if (blackList.contains(tracker.get(vec).getBlockType())) {
                return;
            }
            
            //get all block-sides
            blockFaces[0] = tracker.get(vec.add(1,0,0)).getBlockType();
            blockFaces[1] = tracker.get(vec.add(-1,0,0)).getBlockType();
            blockFaces[2] = tracker.get(vec.add(0,0,1)).getBlockType();
            blockFaces[3] = tracker.get(vec.add(0,0,-1)).getBlockType();
            blockFaces[4] = tracker.get(vec.add(0,1,0)).getBlockType();
            blockFaces[5] = tracker.get(vec.add(0,-1,0)).getBlockType();
            
            BlockType sideBlock = null; 
            int       blockCnt  = 0;
            
            for (int i = 0; i < blockFaces.length; i++) {
                if (blackList.contains(blockFaces[i])) { //matches for AIR most time
                    blockCnt++;
                    
                    if (i < 4) { //If water/lava is found in one of the side positions then make the new block the same
                        if (blockFaces[i] == BlockTypes.WATER || blockFaces[i] == BlockTypes.LAVA) {
                            sideBlock = blockFaces[i];
                        }
                    }
                }
            }

            if (blockCnt >= maxFaces) {
                BlockState temp = sideBlock == null ? BlockTypes.AIR.getDefaultState() : sideBlock.getDefaultState();
                tracker.setSoft(vec, temp);
            }
        };
        
        for (int i = 0; i < iterations; i++) {
            Shaper.runSphere(erode, click, size);
            tracker.flushSoft();
        }
        tracker.writeToSession();
    }
    
}
