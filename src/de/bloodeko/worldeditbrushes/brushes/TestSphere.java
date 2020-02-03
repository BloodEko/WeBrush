package de.bloodeko.worldeditbrushes.brushes;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;


public class TestSphere implements Brush {
    
    
    @Override
    public void build(EditSession session, BlockVector3 vec, Pattern pattern, double size) throws MaxChangedBlocksException {
        List<BlockVector3> blocks     = new ArrayList<>();
        int                calls      = 0;
        int                dublicates = 0;

        int bx = vec.getBlockX();
        int by = vec.getBlockY();
        int bz = vec.getBlockZ();
        int radius = (int) size;
        boolean hollow = false;
        
        for (int x = bx - radius; x <= bx + radius; x++) {
            for (int y = by - radius; y <= by + radius; y++) {
                for (int z = bz - radius; z <= bz + radius; z++) {

                    double distance = ((bx-x) * (bx-x) + ((bz-z) * (bz-z)) + ((by-y) * (by-y)));

                    if (distance < radius * radius && !(hollow && distance < ((radius - 1) * (radius - 1)))) {

                        BlockVector3 loc = BlockVector3.at(x, y, z);
                        
                        calls++;
                        if (blocks.contains(loc)) {
                            dublicates++;
                            continue;
                        }
                        
                        blocks.add(loc);
                    }

                }
            }
        }
        
        for (BlockVector3 loc : blocks) {
            session.setBlock(loc, pattern);
        }

        System.out.println("blocks:"     + blocks.size());
        System.out.println("calls:"      + calls);
        System.out.println("dublicates:" + dublicates);
        
    }
      
}
