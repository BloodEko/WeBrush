package de.bloodeko.worldeditbrushes.brushes;

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

import de.bloodeko.worldeditbrushes.ShapeCycler;
import de.bloodeko.worldeditbrushes.ShapeCycler.BrushFunction;

/**
 * Fills Terrain based required faces and iterations. <br>
 * Similar to /cs build fill from CraftSripts. <br>
 * Use size:2 faces:2 iterations:1 for finer path creation. 
 */
public class FillBrush implements Brush {
    
    @SuppressWarnings("unused")
    private final int iterations;
    private final int maxFaces;
    
    public FillBrush(int iterations, int maxFaces) {
        this.iterations = iterations;
        this.maxFaces   = maxFaces;
    }
    
    
    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        makeFill(editSession, position, size);
    }
    
    
    private void makeFill(EditSession session, BlockVector3 vec, double size) throws MaxChangedBlocksException {
        
        // IS INT & BLOCKSTATE
        ArrayList<Object>     blocks    = new ArrayList<>();
        ArrayList<BlockType>  blackList = new ArrayList<>();
        blackList.add(BlockTypes.AIR);
        blackList.add(BlockTypes.WATER);
        blackList.add(BlockTypes.LAVA);
        
        BrushFunction erode = (x, y, z, distance) -> {
            
            BlockState curBlockId = session.getBlock(BlockVector3.at(x, y, z));
            if (!blackList.contains(curBlockId.getBlockType())) {
                return;
            }
            
            int          blockCnt   = 0;
            BlockState[] blockFaces = new BlockState[6];
            MaxFace      maxFace    = new MaxFace();

            blockFaces[0] = session.getBlock(BlockVector3.at(x+1,y,z));
            blockFaces[1] = session.getBlock(BlockVector3.at(x-1,y,z));
            blockFaces[2] = session.getBlock(BlockVector3.at(x,y,z+1));
            blockFaces[3] = session.getBlock(BlockVector3.at(x,y,z-1));
            blockFaces[4] = session.getBlock(BlockVector3.at(x,y+1,z));
            blockFaces[5] = session.getBlock(BlockVector3.at(x,y-1,z));
            
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
                blocks.add(x);
                blocks.add(y);
                blocks.add(z);
                blocks.add(maxFace.block);
            }
        };
        
        ShapeCycler cycler = new ShapeCycler(erode, size);
        cycler.run(vec, null);
        
        for (int i = 0; i < blocks.size(); i += 4) {
            double     valx    = (double) blocks.get(i);
            double     valy    = (double) blocks.get(i+1);
            double     valz    = (double) blocks.get(i+2);
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


