package de.webrush.brush.terrain;

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
        
        ChangeTracker   tracker    = new ChangeTracker(session);
        BlockType[]     blockFaces = new BlockType[6];
        
        BrushFunction erode = vec -> {
            if (isWaterOrAir(tracker.get(vec).getBlockType())) {
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
                if (isWaterOrAir(blockFaces[i])) {
                    if (isSide(i) && blockFaces[i].getMaterial().isLiquid()) {
                        sideBlock = blockFaces[i];
                    }
                    blockCnt++;
                }
            }

            if (blockCnt >= maxFaces) {
                tracker.setSoft(vec, getAirOrDefault(sideBlock));
            }
        };
        
        for (int i = 0; i < iterations; i++) {
            Shaper.runSphere(erode, click, size);
            tracker.flushSoft();
        }
        tracker.writeToSession();
    }
  
    private boolean isWaterOrAir(BlockType type) {
        return type == BlockTypes.AIR 
            || type == BlockTypes.WATER 
            || type == BlockTypes.LAVA;
    }
    
    private boolean isSide(int index) {
        return index < 4;
    }
    
    private BlockState getAirOrDefault(BlockType type) {
        return type == null ? BlockTypes.AIR.getDefaultState() : type.getDefaultState();
    }
}
