package de.webrush;

import com.sk89q.worldedit.math.BlockVector3;

/**
 * Iterates through all blocks in a sphere.
 * Applies the function on matching locations.
 */
public class ShapeCycler {
    
    private final BrushFunction callback;
    private final double size;
    
    public ShapeCycler(BrushFunction function, double size) {
        this.callback = function;
        this.size     = size;
    }
    
    public void run(BlockVector3 vec) {
        run(vec, true);
    }
    
    public void run(BlockVector3 pos, boolean filled) {

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

                    callback.apply(pos.add(x, y, z));
                    callback.apply(pos.add(-x, y, z));
                    callback.apply(pos.add(x, -y, z));
                    callback.apply(pos.add(x, y, -z));
                    callback.apply(pos.add(-x, -y, z));
                    callback.apply(pos.add(x, -y, -z));
                    callback.apply(pos.add(-x, y, -z));
                    callback.apply(pos.add(-x, -y, -z));
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
    
}
