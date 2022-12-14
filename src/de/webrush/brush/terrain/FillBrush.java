package de.webrush.brush.terrain;

import java.util.HashMap;
import java.util.Map;

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
 * Fills Terrain based required faces and iterations. <br>
 * Similar to /cs build fill from CraftSripts. <br>
 * Use size:2 faces:2 iterations:1 for finer path creation. 
 */
public class FillBrush implements Brush {
    
    private final int maxFaces;
    private final int iterations;
    
    public FillBrush(int maxFaces, int iterations) {
        this.maxFaces   = maxFaces;
        this.iterations = iterations;
    }
    
    
    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        makeFill(editSession, position, size);
    }
    
    
    private void makeFill(EditSession session, BlockVector3 click, double size) throws MaxChangedBlocksException {

        ChangeTracker   tracker    = new ChangeTracker(session);
        BlockState[]    blockFaces = new BlockState[6];
        
        BrushFunction erode = vec -> {
            if (!isWaterOrAir(tracker.get(vec).getBlockType())) {
                return;
            }

            //get all block-sides
            blockFaces[0] = tracker.get(vec.add(1,0,0));
            blockFaces[1] = tracker.get(vec.add(-1,0,0));
            blockFaces[2] = tracker.get(vec.add(0,0,1));
            blockFaces[3] = tracker.get(vec.add(0,0,-1));
            blockFaces[4] = tracker.get(vec.add(0,1,0));
            blockFaces[5] = tracker.get(vec.add(0,-1,0));
            
            Map<BlockType, Counter> map = new HashMap<BlockType, Counter>();
            int blockCnt = 0;
            BlockState maxFaceBlock = null;
            int        maxFaceCnt   = 0;
            
            for (int i = 0; i < blockFaces.length; i++) {
                BlockState face = blockFaces[i];
                BlockType  type = face.getBlockType();
                
                if (!isWaterOrAir(type)) {
                    Counter counter = map.get(type);
                    if (counter == null) {
                        counter = new Counter();
                        map.put(type, counter);
                    }
                    int cnt = counter.increment();
                    if (cnt > maxFaceCnt) {
                        maxFaceBlock = face;
                        maxFaceCnt   = cnt;
                    }
                    blockCnt++;
                }
            }
            
            if (blockCnt >= maxFaces) {
                tracker.setSoft(vec, maxFaceBlock);
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
    
    private static class Counter {
        private int val;
        
        public int increment() {
            val++;
            return val;
        }
    }
}


