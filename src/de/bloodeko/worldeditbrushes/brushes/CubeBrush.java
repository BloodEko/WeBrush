package de.bloodeko.worldeditbrushes.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

public class CubeBrush implements Brush {

    private final int width;
    private final int height;
    private final boolean spherical;
    
    public CubeBrush(int width, int height, boolean spherical) {
        this.width  = width;
        this.height = height;
        this.spherical = spherical;
    }

    @Override
    public void build(EditSession session, BlockVector3 middle, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        int calls = 0;
        int set   = 0;
        
        int startX = middle.getX() - width / 2;
        int startY = middle.getY() - width / 2;
        int startZ = middle.getZ() - width / 2;
        
        int endX   = middle.getX() + width / 2;
        int endY   = middle.getY() + width / 2;
        int endZ   = middle.getZ() + width / 2;
        
        if (width % 2 == 1) {
            endX++;
            endY++;
            endZ++;
        }
        
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                for (int z = startZ; z < endZ; z++) {
                    calls++;
                    BlockVector3 vec = BlockVector3.at(x, y, z);
                    
                    if (spherical) {
                        double dx   = middle.getX() - x;
                        double dy   = middle.getY() - y;
                        double dz   = middle.getZ() - z;
                        double mul  = dx*dx + dy*dy + dz*dz;
                        double sqrt = Math.sqrt(mul);
                        
                        if (sqrt <= (size/2)) {
                            set++;
                            session.setBlock(vec, pattern);
                        }
                        continue;
                    }

                    set++;
                    session.setBlock(vec, pattern);
                }
            }
        }
        
        System.out.println("Cubecalls:" + calls);
        System.out.println("CubeSet:"   + set);
    }

}
