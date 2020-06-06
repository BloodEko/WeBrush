package de.webrush.util;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.math.BlockVector3;

/**
 * Uses the sphere function from WorldEdit to iterate
 * through the locations. Calls the function on these.
 */
public class Shaper {
    
    private final BrushFunction brush;
    private final double        size;
    
    private Shaper(BrushFunction brush, double size) {
        this.brush = brush;
        this.size  = size;
    }
    
    
    public static void runSphere(BrushFunction brush, BlockVector3 at, double size) {
        new Shaper(brush, size).runSphere(at, true);
    }
    
    private void runSphere(BlockVector3 pos, boolean filled) {

        final double radiusX = size/2 + 0.5;
        final double radiusY = size/2 + 0.5;
        final double radiusZ = size/2 + 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (!filled) {
                        if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                            continue;
                        }
                    }

                    brush.apply(pos.add(x, y, z));
                    brush.apply(pos.add(-x, y, z));
                    brush.apply(pos.add(x, -y, z));
                    brush.apply(pos.add(x, y, -z));
                    brush.apply(pos.add(-x, -y, z));
                    brush.apply(pos.add(x, -y, -z));
                    brush.apply(pos.add(-x, y, -z));
                    brush.apply(pos.add(-x, -y, -z));
                }
            }
        }      
    }
    
    private double lengthSq(double x, double y, double z) {
        return (x*x) + (y*y) + (z*z);
    }
    
    public interface BrushFunction {
        public void apply(BlockVector3 pos);
    }
    
    /**
     * Gets all blocks for a precise disk. <br>
     * Affected blocks will be in the radius (size/2 + 0.5).
     */
    public static List<BlockVector3> getDisc(BlockVector3 pos, double radius) {
        List<BlockVector3> list = new ArrayList<>();
        
        int size   = (int) radius;
        int startX = pos.getX() - size;
        int startZ = pos.getZ() - size;
        
        int endX   = pos.getX() + size;
        int endZ   = pos.getZ() + size;
        int y      = pos.getY();
        
        radius     = radius/2 + 0.5;
        radius     = radius*radius;
        
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                
                if (Math.pow(pos.getX() - x, 2) + Math.pow(pos.getZ() - z, 2) <= radius) {
                    list.add(BlockVector3.at(x, y, z));
                }
                
            }
        }
        return list;
    }
    
}
