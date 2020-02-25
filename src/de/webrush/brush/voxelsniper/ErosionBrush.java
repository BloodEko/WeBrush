package de.webrush.brush.voxelsniper;

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
import com.sk89q.worldedit.world.registry.BlockMaterial;

import de.webrush.ChangeTracker;

/**
 * Performs an eroding/filling erosion on the terrain.
 * Similar to /b e from VoxelSniper.
 * 
 * <pre>
 * Melt     = 2, 1, 5, 1
 * Fill     = 5, 1, 2, 1
 * LiftUp   = 6, 0, 1, 1
 * LiftDown = 1, 1, 6, 0
 * Smooth   = 3, 1, 3, 1
 * Clean    = 6, 1, 6, 1
 * <pre>
 */
public class ErosionBrush implements Brush {

    private static final BlockVector3[] FACES_TO_CHECK = {
            BlockVector3.at(0, 0, 1), BlockVector3.at(0, 0, -1), 
            BlockVector3.at(0, 1, 0), BlockVector3.at(0, -1, 0),
            BlockVector3.at(1, 0, 0), BlockVector3.at(-1, 0, 0)
    };
    
    public final int erodeFaces;
    public final int erodeRec;
    public final int fillFaces;
    public final int fillRec;
    
    public ErosionBrush(int erodeFaces, int erodeRec, int fillFaces, int fillRec) {
        this.erodeFaces = erodeFaces;
        this.erodeRec   = erodeRec;
        this.fillFaces  = fillFaces;
        this.fillRec    = fillRec;
    }

    @Override
    public void build(EditSession session, BlockVector3 pos, Pattern pattern, double size)
            throws MaxChangedBlocksException {
    
        ChangeTracker tracker = new ChangeTracker(session);
        build(tracker, pos, pattern, size);
        tracker.writeToSession();
    }
    
    
    public void build(ChangeTracker tracker, BlockVector3 pos, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        for (int i = 0; i < erodeRec; i++) {
            erosionIteration((int) size, tracker, pos);
            tracker.flushSoft();
        }
        
        for (int i = 0; i < fillRec; i++) {
            fillIteration((int) size, tracker, pos);
            tracker.flushSoft();
        }
    }

    private void fillIteration(int size, ChangeTracker tracker, BlockVector3 pos) {
        
        int startX = pos.getX() - size;
        int startY = pos.getY() - size;
        int startZ = pos.getZ() - size;
        int endX = pos.getX() + size;
        int endY = pos.getY() + size;
        int endZ = pos.getZ() + size;
        
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                for (int y = startY; y <= endY; y++) {
                    
                    BlockVector3 currentPosition = BlockVector3.at(x, y, z);
                    if (currentPosition.distanceSq(pos) > size*size) {
                        continue;
                    }
                    
                    BlockState currentBlock  = tracker.get(currentPosition);
                    BlockMaterial currentMat = currentBlock.getBlockType().getMaterial();

                    if (!(currentMat.isAir() || currentMat.isLiquid())) {
                        continue;
                    }

                    final Map<BlockType, Integer> blockCount = new HashMap<>();
                    int count = 0;

                    for (final BlockVector3 vector : FACES_TO_CHECK) {
                        final BlockVector3  relativePosition = currentPosition.add(vector);
                        final BlockState    relativeBlock = tracker.get(relativePosition);
                        final BlockType     relativeType  = relativeBlock.getBlockType();

                        if (!(relativeType.getMaterial().isAir() || relativeType.getMaterial().isLiquid())) {
                            count++;
                            if (blockCount.containsKey(relativeType)) {
                                blockCount.put(relativeType, blockCount.get(relativeType) + 1);
                            } else {
                                blockCount.put(relativeType, 1);
                            }
                        }
                    }

                    BlockType currentMaterial = BlockTypes.AIR;
                    int amount = 0;

                    for (final BlockType wrapper : blockCount.keySet()) {
                        final Integer currentCount = blockCount.get(wrapper);
                        if (amount <= currentCount) {
                            currentMaterial = wrapper;
                            amount = currentCount;
                        }
                    }

                    if (count >= fillFaces) {
                        tracker.setSoft(currentPosition, currentMaterial.getDefaultState());
                    }
                }
            }
        }
    }

    private void erosionIteration(int size, ChangeTracker tracker, BlockVector3 pos) {
        
        int startX = pos.getX() - size;
        int startY = pos.getY() - size;
        int startZ = pos.getZ() - size;
        int endX = pos.getX() + size;
        int endY = pos.getY() + size;
        int endZ = pos.getZ() + size;
        
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                for (int y = startY; y <= endY; y++) {
                    
                    BlockVector3 currentPosition = BlockVector3.at(x, y, z);
                    if (currentPosition.distanceSq(pos) > size*size) {
                        continue;
                    }
                    
                    BlockState    currentBlock = tracker.get(currentPosition);
                    BlockMaterial currentMat   = currentBlock.getBlockType().getMaterial();

                    if (currentMat.isAir() || currentMat.isLiquid()) {
                        continue;
                    }

                    int count = 0;
                    for (final BlockVector3 vector : FACES_TO_CHECK) {
                        final BlockVector3  relativePosition = currentPosition.add(vector);
                        final BlockState    relativeBlock = tracker.get(relativePosition);
                        final BlockMaterial relativeMat  = relativeBlock.getBlockType().getMaterial();

                        if (relativeMat.isAir() || relativeMat.isLiquid()) {
                            count++;
                        }
                    }

                    if (count >= erodeFaces) {
                        tracker.setSoft(currentPosition, BlockTypes.AIR.getDefaultState());
                    }
                }
            }
        }
    }
    
    /*
    private static final class BlockChangeTracker {
        
        private final Map<Integer, Map<BlockVector3, BlockState>> blockChanges = new HashMap<>();
        private final Map<BlockVector3, BlockState> flatChanges                = new HashMap<>();
        private final EditSession session;
        private int nextIterationId = 0;

        public BlockChangeTracker(EditSession session) {
            this.session = session;
        }

        // reads from all last iterations, blockchanges
        // if no change found, reads from session.
        public BlockState get(BlockVector3 position, int iteration) {
            BlockState changedBlock = null;

            for (int i = iteration - 1; i >= 0; --i) {
                if (this.blockChanges.containsKey(i) && this.blockChanges.get(i).containsKey(position)) {
                    changedBlock = this.blockChanges.get(i).get(position);
                    return changedBlock;
                }
            }
            changedBlock = session.getBlock(position);
            return changedBlock;
        }

        public Map<BlockVector3, BlockState> getAll() {
            return this.flatChanges;
        }

        public int nextIteration() {
            return this.nextIterationId++;
        }

        // writes to current iteration, blockchanges
        // writes go global, flat changes
        public void put(BlockVector3 position, BlockState changedBlock, int iteration) {
            if (!this.blockChanges.containsKey(iteration)) {
                this.blockChanges.put(iteration, new HashMap<BlockVector3, BlockState>());
            }

            this.blockChanges.get(iteration).put(position, changedBlock);
            this.flatChanges.put(position, changedBlock);
        }
    }
    */
    
}
