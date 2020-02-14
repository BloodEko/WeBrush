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

import de.webrush.ShapeCycler;
import de.webrush.ShapeCycler.BrushFunction;

/**
 * Erodes Terrain away based on faces and iterations. <br>
 * Similar to /cs build erode from CraftSripts. <br>
 * Use size:2 faces:2 iterations:1 for finer path creation. 
 */
public class ErodeBrush implements Brush {
    
    private final int maxFaces;
    @SuppressWarnings("unused")
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
        
        // IS INT & BLOCKSTATE
        ArrayList<Object>     blocks    = new ArrayList<>();
        ArrayList<BlockType>  blackList = new ArrayList<>();
        blackList.add(BlockTypes.AIR);
        blackList.add(BlockTypes.WATER);
        blackList.add(BlockTypes.LAVA);
        
        BlockType[] blockFaces = new BlockType[6];
        
        BrushFunction erode = vec -> {
            
            BlockState curBlockId = session.getBlock(vec);
            if (blackList.contains(curBlockId.getBlockType())) {
                return;
            }
            
            int blockCnt = 0; 
            //check around the six sides of the current loop block position
            blockFaces[0] = session.getBlock(vec.add(1,0,0)).getBlockType();
            blockFaces[1] = session.getBlock(vec.add(-1,0,0)).getBlockType();
            blockFaces[2] = session.getBlock(vec.add(0,0,1)).getBlockType();
            blockFaces[3] = session.getBlock(vec.add(0,0,-1)).getBlockType();
            blockFaces[4] = session.getBlock(vec.add(0,1,0)).getBlockType();
            blockFaces[5] = session.getBlock(vec.add(0,-1,0)).getBlockType();
            
            BlockType sideBlock = BlockTypes.AIR;      //Search our blockFaces list for water or lava
            for (int i = 0; i < blockFaces.length; i++) {

                if (blackList.contains(blockFaces[i])) {
                    
                    blockCnt++;
                    if (i < 4) {      //If water/lava is found in one of the side positions then make the new block the same
                        if(blockFaces[i] == BlockTypes.WATER) {
                            sideBlock = BlockTypes.WATER;
                        }
                        else if(blockFaces[i] == BlockTypes.LAVA) {
                            sideBlock = BlockTypes.LAVA;
                        }
                    }
                }
            }


            if (blockCnt >= maxFaces) {
                blocks.add(vec.getX());
                blocks.add(vec.getY());
                blocks.add(vec.getZ());
                if (sideBlock != BlockTypes.AIR)  blocks.add(sideBlock.getDefaultState());
                else blocks.add(BlockTypes.AIR.getDefaultState());
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
}
