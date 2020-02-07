package de.bloodeko.worldeditbrushes.brushes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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
 * Places hanging Vines by a length and density from 0.0 to 1.0. <br>
 * Similar to /cs build vines from CraftScripts.
 * 
 * 
 * Todo: debug
 */
public class VineBrush implements Brush {

    private final double density;
    private final int    length;
    private final Random rand = new Random();
    private HashSet<BlockVector3> marked = new HashSet<>();
    
    
    public VineBrush(double density, int length) {
        this.density = density;
        this.length  = length;
    }
    
    @Override
    public void build(EditSession session, BlockVector3 vec, Pattern mat, double size) throws MaxChangedBlocksException {
        marked.clear();
        
        BrushFunction vinebrush = (x, y, z, distance) -> {

            BlockVector3 pos    = BlockVector3.at(x, y, z);
            BlockState curBlock = session.getBlock(pos);

            System.out.println("x:" + x + " y:" + y + " z:" + z);
            System.out.println(curBlock.getBlockType()  + "  != "  + BlockTypes.AIR);
            
            if (Math.random() > density) return;
            if (curBlock.getBlockType() != BlockTypes.AIR || marked.contains(pos)) return;
            //System.out.println("match first!");
            

            BlockState[] vines = new BlockState[4];
            vines[0] = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("east"),  true);
            vines[1] = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("west"),  true);
            vines[2] = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("south"), true);
            vines[3] = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("north"), true);

            BlockType[] blockFaces = new BlockType[4];
            blockFaces[0] = session.getBlock(pos.add(1,0,0)).getBlockType();
            blockFaces[1] = session.getBlock(pos.add(-1,0,0)).getBlockType();
            blockFaces[2] = session.getBlock(pos.add(0,0,1)).getBlockType();
            blockFaces[3] = session.getBlock(pos.add(0,0,-1)).getBlockType();

            List<Integer> solidSide = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                
                System.out.println("i:" + i + " x:" + x + " y:" + y + " z:" + z + " " + blockFaces[i] + "  " + (!blockFaces[i].getMaterial().isSolid()) + " ODER " + blockFaces[i] + " == " + mat.apply(BlockVector3.at(0,0,0)).getBlockType());
                
                if (!blockFaces[i].getMaterial().isSolid() || blockFaces[i] == mat.apply(BlockVector3.at(0,0,0)).getBlockType()) {
                    continue;
                }
                if (blockFaces[i] != BlockTypes.AIR) {
                    solidSide.add(i);
                    //System.out.println("add!" + i);
                }                                             
            }
            if ((solidSide.size() >= 1)) {
                //System.out.println("match SolidSize!");
                int randomSide   = solidSide.get(rand.nextInt(solidSide.size()));
                int randomLength = length;
                
                BlockState newVine = vines[randomSide];
                for (int extendVine = 0; extendVine <= randomLength; extendVine++) {
                    if (session.getBlock(pos.add(0,-(extendVine),0)).getBlockType() == BlockTypes.AIR) {
                        
                        try {
                            if (mat.apply(BlockVector3.at(0, 0, 0)).getBlockType() == BlockTypes.VINE) {
                                
                                System.out.println("before1:" +  session.getBlock(pos.add(0,-(extendVine),0)));
                                session.setBlock(pos.add(0,-(extendVine),0), newVine);
                                System.out.println("after1:" +  session.getBlock(pos.add(0,-(extendVine),0)));
                            }
                            else {
                                System.out.println("before2:" +  session.getBlock(pos.add(0,-(extendVine),0)));
                                session.setBlock(pos.add(0,-(extendVine),0), mat);
                                System.out.println("after2:" +  session.getBlock(pos.add(0,-(extendVine),0)));
                            }
                            marked.add(pos);
                        }
                        catch(MaxChangedBlocksException ex) {
                            ex.printStackTrace();
                        }
                        continue;
                    }
                    break;
                }
                //System.out.println("total:" + total);
            }
        };
        
        ShapeCycler cycler = new ShapeCycler(vinebrush, size);
        cycler.run(vec, null);
    }
    
}
