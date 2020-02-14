package de.webrush.brush.craftscript;

import java.util.ArrayList;
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

import de.webrush.ShapeCycler;
import de.webrush.ShapeCycler.BrushFunction;

/**
 * Fills Terrain based required faces and iterations. <br>
 * Similar to /cs build fill from CraftSripts. <br>
 * Use size:2 faces:2 iterations:1 for finer path creation. 
 */
public class FillBrush implements Brush {
    
    private final int maxFaces;
    @SuppressWarnings("unused")
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
        
        // IS INT & BLOCKSTATE
        ArrayList<Object>     blocks    = new ArrayList<>();
        ArrayList<BlockType>  blackList = new ArrayList<>();
        blackList.add(BlockTypes.AIR);
        blackList.add(BlockTypes.WATER);
        blackList.add(BlockTypes.LAVA);
        
        BrushFunction erode = vec -> {
            
            BlockState curBlockId = session.getBlock(vec);
            if (!blackList.contains(curBlockId.getBlockType())) {
                return;
            }
            
            int          blockCnt   = 0;
            BlockState[] blockFaces = new BlockState[6];
            MaxFace      maxFace    = new MaxFace();

            blockFaces[0] = session.getBlock(vec.add(1,0,0));
            blockFaces[1] = session.getBlock(vec.add(-1,0,0));
            blockFaces[2] = session.getBlock(vec.add(0,0,1));
            blockFaces[3] = session.getBlock(vec.add(0,0,-1));
            blockFaces[4] = session.getBlock(vec.add(0,1,0));
            blockFaces[5] = session.getBlock(vec.add(0,-1,0));
            
            Map<BlockType, Counter> faces = new HashMap<BlockType, Counter>();
            
            for (int i = 0; i < blockFaces.length; i++) {

                if (!blackList.contains(blockFaces[i].getBlockType())) {
                    if (!faces.containsKey(blockFaces[i].getBlockType())) {
                        faces.put(blockFaces[i].getBlockType(), new Counter());
                    }
                    else {
                        Counter counter = faces.get(blockFaces[i].getBlockType());
                        counter.cnt++;
                    }
                    Counter counter = faces.get(blockFaces[i].getBlockType());
                    int cnt = counter.cnt;
                    if (cnt > maxFace.cnt) {
                        maxFace.block = blockFaces[i];
                        maxFace.cnt   = cnt;
                    }
                    blockCnt++;
                }
            }
            

            if (blockCnt >= maxFaces) {
                blocks.add(vec.getX());
                blocks.add(vec.getY());
                blocks.add(vec.getZ());
                blocks.add(maxFace.block);
            }
        };
        
        new ShapeCycler(erode, size).run(click);
        
        for (int i = 0; i < blocks.size(); i += 4) {
            int     valx    = (int) blocks.get(i);
            int     valy    = (int) blocks.get(i+1);
            int     valz    = (int) blocks.get(i+2);
            BlockState valType = (BlockState) blocks.get(i+3);
            session.setBlock(BlockVector3.at(valx, valy, valz), valType);
        }
        
    }
    
    private static class Counter {
        int       cnt;
        
        public Counter() {
            cnt   = 1;
        }
    }
    
    private class MaxFace {
        BlockState block;
        int        cnt;
    }
}


