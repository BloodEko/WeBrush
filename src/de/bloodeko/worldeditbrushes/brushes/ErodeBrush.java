package de.bloodeko.worldeditbrushes.brushes;

import java.util.ArrayList;

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
 * Erodes Terrain away based on faces and iterations. <br>
 * Similar to /cs build erode from CraftSripts. <br>
 * Use size:2 faces:2 iterations:1 for finer path creation. 
 */
public class ErodeBrush implements Brush {
    
    @SuppressWarnings("unused")
    private final int iterations;
    private final int maxFaces;
    
    public ErodeBrush(int iterations, int maxFaces) {
        this.iterations = iterations;
        this.maxFaces   = maxFaces;
    }
    
    
    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        makeErosion(editSession, position, size);
    }
    
    
    private void makeErosion(EditSession session, BlockVector3 vec, double size) throws MaxChangedBlocksException {
        
        // IS INT & BLOCKSTATE
        ArrayList<Object>     blocks    = new ArrayList<>();
        ArrayList<BlockType>  blackList = new ArrayList<>();
        blackList.add(BlockTypes.AIR);
        blackList.add(BlockTypes.WATER);
        blackList.add(BlockTypes.LAVA);
        
        BlockType[] blockFaces = new BlockType[6];
        
        BrushFunction erode = (x, y, z, distance) -> {
            
            BlockState curBlockId = session.getBlock(BlockVector3.at(x, y, z));
            if (blackList.contains(curBlockId.getBlockType())) {
                return;
            }
            
            int blockCnt = 0; 
            //check around the six sides of the current loop block position
            blockFaces[0] = session.getBlock(BlockVector3.at(x+1,y,z)).getBlockType();
            blockFaces[1] = session.getBlock(BlockVector3.at(x-1,y,z)).getBlockType();
            blockFaces[2] = session.getBlock(BlockVector3.at(x,y,z+1)).getBlockType();
            blockFaces[3] = session.getBlock(BlockVector3.at(x,y,z-1)).getBlockType();
            blockFaces[4] = session.getBlock(BlockVector3.at(x,y+1,z)).getBlockType();
            blockFaces[5] = session.getBlock(BlockVector3.at(x,y-1,z)).getBlockType();
            
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
                blocks.add(x);
                blocks.add(y);
                blocks.add(z);
                if (sideBlock != BlockTypes.AIR)  blocks.add(sideBlock.getDefaultState());
                else blocks.add(BlockTypes.AIR.getDefaultState());
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
}
