package de.bloodeko.worldeditbrushes;

import com.sk89q.worldedit.math.BlockVector3;

import de.bloodeko.worldeditbrushes.brushes.BrushFunction;

/**
 * Iterates through all blocks in a sphere.
 * Applies the function on matching locations.
 */
public class ShapeCycler {
    
    private final BrushFunction callback;
    private final double sizeX;
    private final double sizeY;
    private final double sizeZ;
    
    //private final int inDensity   = 1;
    //private final int outDensity  = 1;
    private final boolean enabled = true;
    
    public ShapeCycler(BrushFunction function, double size) {
        this(function, size, size , size);
    }
    
    private ShapeCycler(BrushFunction function, double xSize, double ySize, double zSize) {
        this.callback = function;
        this.sizeX    = xSize;
        this.sizeY    = ySize;
        this.sizeZ    = zSize;
    }
    
    public void run2(BlockVector3 vec, boolean filled) {
        
        double radiusX = sizeX + 0.5;
        double radiusY = sizeY + 0.5;
        double radiusZ = sizeZ + 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);
        
        int totalSet        = 0;
        int totalIterations = 0;
        
        int totalX = 0;
        int totalY = 0;
        int totalZ = 0;

        
        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            totalX++;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                totalY++;
                double nextZn = 0;
                forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;
                    totalZ++;
                    totalIterations++;
                    
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
                    
                    double px = vec.getX() + x;
                    double py = vec.getY() + y;
                    double pz = vec.getZ() + z;
                    
                    double mx = vec.getX() - x;
                    double my = vec.getY() - y;
                    double mz = vec.getZ() - z;
                    
                    this.callback.apply(px, py, pz, distanceSq);
                    this.callback.apply(mx, py, pz, distanceSq);
                    this.callback.apply(px, my, pz, distanceSq);
                    this.callback.apply(px, py, mz, distanceSq);
                    this.callback.apply(mx, my, pz, distanceSq);
                    this.callback.apply(px, my, mz, distanceSq);
                    this.callback.apply(mx, py, mz, distanceSq);
                    this.callback.apply(mx, my, mz, distanceSq);
                    
                    totalSet += 8;
                }
            }
        }
        
        System.out.println("sizeX:"   + sizeX);
        System.out.println("radiusX:" + radiusX);
        System.out.println("radiusY:" + radiusY);
        System.out.println("radiusZ:" + radiusZ);
                   
        System.out.println("invRadiusX:" + invRadiusX);
        System.out.println("invRadiusY:" + invRadiusY);
        System.out.println("invRadiusZ:" + invRadiusZ);
        
        System.out.println("ceilRadiusX:" + ceilRadiusX);
        System.out.println("ceilRadiusY:" + ceilRadiusY);
        System.out.println("ceilRadiusZ:" + ceilRadiusZ);
        System.out.println("totalSet: "         + totalSet);
        System.out.println("totalIterations: "  + totalIterations);
        System.out.println("totalX: "    + totalX);
        System.out.println("totalY: "    + totalY);
        System.out.println("totalZ: "    + totalZ);
        
    }
    
    public void run(BlockVector3 vec, Boolean filled) {
        
        if (!this.enabled) return;
        filled = filled == null ? true : filled;
        
        int setTotal    = 0; 
        int callsTtotal = 0;
        int xCall       = 0;
        int yCall       = 0;
        int zCall       = 0;
        
        int bx = vec.getBlockX();
        int by = vec.getBlockY();
        int bz = vec.getBlockZ();
        
        final double radiusX = this.sizeX/2 + 0.5;
        final double radiusY = this.sizeY/2 + 0.5;
        final double radiusZ = this.sizeZ/2 + 0.5;
        
        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final double ceilRadiusX = Math.ceil(radiusX);
        final double ceilRadiusY = Math.ceil(radiusY);
        final double ceilRadiusZ = Math.ceil(radiusZ);
        //var watch = new StopWatch(true);
        
        double xn, yn, zn;
        double px, nx, py, ny, pz, nz;

        // ellipsoid function copied and converted from worldedit sphere/ellipse function
        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            xCall++;
            xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                yCall++;
                yn =  nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                    zCall++;
                    zn =  nextZn;
                    nextZn = (z + 1) * invRadiusZ;
                    
                    callsTtotal++;
                    
                    double lenSq = lengthSq(xn, yn, zn);
                    if (lenSq > 1) {
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
                    
                    px = x + bx;
                    nx = -x + bx;
                    py = y + by;
                    ny = -y + by;
                    pz = z + bz;
                    nz = -z + bz;
                    
                    /*
                    System.out.println("x" + x);
                    System.out.println("y" + y);
                    System.out.println("z" + z);
                    System.out.println("");
                    
                    System.out.println("bx" + bx);
                    System.out.println("by" + by);
                    System.out.println("bz" + bz);
                    System.out.println("");
                    
                    System.out.println("px:" + px);
                    System.out.println("nx:" + nx);
                    System.out.println("py:" + py);
                    System.out.println("");
                    
                    System.out.println("NY:" + ny);
                    System.out.println("pz:" + pz);
                    System.out.println("nz:" + nz);
                    System.out.println("-----");
                    */
                    
                    this.callback.apply(px, py, pz, lenSq);
                    this.callback.apply(nx, py, pz, lenSq);
                    this.callback.apply(px, ny, pz, lenSq);
                    this.callback.apply(px, py, nz, lenSq);
                    this.callback.apply(nx, ny, pz, lenSq);
                    this.callback.apply(px, ny, nz, lenSq);
                    this.callback.apply(nx, py, nz, lenSq);
                    this.callback.apply(nx, ny, nz, lenSq);

                    setTotal+=8;                    
                }
            }
        }
        
        if (WorldEditBrushes.debug) {
            System.out.println("bx:" + bx);
            System.out.println("by:" + by);
            System.out.println("bz:" + bz);
            
            System.out.println("sizeX:"   + sizeX);
            System.out.println("radiusX:" + radiusX);
            System.out.println("radiusY:" + radiusY);
            System.out.println("radiusZ:" + radiusZ);
                       
            System.out.println("invRadiusX:" + invRadiusX);
            System.out.println("invRadiusY:" + invRadiusY);
            System.out.println("invRadiusZ:" + invRadiusZ);
            
            System.out.println("ceilRadiusX:" + ceilRadiusX);
            System.out.println("ceilRadiusY:" + ceilRadiusY);
            System.out.println("ceilRadiusZ:" + ceilRadiusZ);
            System.out.println("affectedTotal: " + setTotal);
            System.out.println("callsTotal: "    + callsTtotal);
            System.out.println("xCall: "    + xCall);
            System.out.println("yCall: "    + yCall);
            System.out.println("zCall: "    + zCall);
        }
        
    }
    
    private double lengthSq(double x, double y, double z) {
        return (x*x) + (y*y) + (z*z);
    }
    
}
