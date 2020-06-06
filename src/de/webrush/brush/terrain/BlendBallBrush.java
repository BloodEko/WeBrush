package de.webrush.brush.terrain;

import java.util.HashMap;
import java.util.Map;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;

import de.webrush.util.ChangeTracker;

/**
 * Smooths terrain. Similar to /b bb from VoxelSniper.
 * 
 * Todo: debug
 */
public class BlendBallBrush implements Brush {

    private final int WORLD_HEIGHT = 256;
    
    @Override
    public void build(EditSession session, BlockVector3 pos, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        ChangeTracker tracker = new ChangeTracker(session);
        build(tracker, pos, pattern, size);
        tracker.writeToSession();
    }
    
    public void build(ChangeTracker tracker, BlockVector3 pos, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        final int brushSize = (int) size + 1;
        double brushSizeSquared = size * size;
        // all changes are initially performed into a buffer to prevent the
        // results bleeding into each other
        //BlockBuffer buffer = new BlockBuffer(new Vector3i(-brushSize, -brushSize, -brushSize), new Vector3i(brushSize, brushSize, brushSize));

        int tx = pos.getX();
        int ty = pos.getY();
        int tz = pos.getZ();

        Map<BlockState, Integer>      frequency = new HashMap<>();
        Map<BlockVector3, BlockState> buffer    = new HashMap<>();

        for (int x = -brushSize; x <= brushSize; x++) {
            int x0 = x + tx;
            for (int y = -brushSize; y <= brushSize; y++) {
                int y0 = y + ty;
                for (int z = -brushSize; z <= brushSize; z++) {
                    if (x * x + y * y + z * z >= brushSizeSquared) {
                        continue;
                    }
                    int z0 = z + tz;
                    int highest = 1;
                    BlockState currentState = tracker.get(BlockVector3.at(x0, y0, z0));
                    BlockState highestState = currentState;
                    frequency.clear();
                    boolean tie = false;
                    for (int ox = -1; ox <= 1; ox++) {
                        for (int oz = -1; oz <= 1; oz++) {
                            for (int oy = -1; oy <= 1; oy++) {
                                if (oy + y0 < 0 || oy + y0 > WORLD_HEIGHT) {
                                    continue;
                                }
                                BlockState state = tracker.get(BlockVector3.at(x0 + ox, y0 + oy, z0 + oz));
                                Integer count = frequency.get(state);
                                if (count == null) {
                                    count = 1;
                                } else {
                                    count++;
                                }
                                if (count > highest) {
                                    highest = count;
                                    highestState = state;
                                    tie = false;
                                } else if (count == highest) {
                                    tie = true;
                                }
                                frequency.put(state, count);
                            }
                        }
                    }
                    if (!tie && currentState != highestState) {
                        buffer.put(BlockVector3.at(x, y, z), highestState);
                    }
                }
            }
        }


        // apply the buffer to the world
        for (int x = -brushSize; x <= brushSize; x++) {
            int x0 = x + tx;
            for (int y = -brushSize; y <= brushSize; y++) {
                int y0 = y + ty;
                for (int z = -brushSize; z <= brushSize; z++) {
                    int z0 = z + tz;
                    if (buffer.containsKey(BlockVector3.at(x, y, z))) {
                        tracker.setHard(BlockVector3.at(x0, y0, z0), buffer.get(BlockVector3.at(x, y, z)));
                    }
                }
            }
        }
        
    }

}
