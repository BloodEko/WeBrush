package de.bloodeko.worldeditbrushes.brushes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.BlockMaterial;

/**
 * Performs an eroding/filling erosion on the terrain.
 * Similar to /b e from VoxelSniper.
 * 
 * <pre>
 * Melt     = 2, 1, 5, 1
 * Fill     = 5, 1, 2, 1
 * Smooth   = 3, 1, 3, 1
 * LiftUp   = 6, 0, 1, 1
 * LiftDown = 1, 1, 6, 0
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
     
        BlockChangeTracker blockChangeTracker = new BlockChangeTracker(session);
        BlockVector3       targetBlockVector  = pos;

        for (int i = 0; i < erodeRec; ++i) {
            erosionIteration(session, (int) size, blockChangeTracker, targetBlockVector);
        }

        for (int i = 0; i < fillRec; ++i) {
            fillIteration(session, (int) size, blockChangeTracker, targetBlockVector);
        }

        
        for (Entry<BlockVector3, BlockState> entry : blockChangeTracker.getAll().entrySet()) {
            session.setBlock(entry.getKey(), entry.getValue());
        }
    }

    private void fillIteration(EditSession session, int size, BlockChangeTracker blockChangeTracker, BlockVector3 pos)
    {
        
        final int currentIteration = blockChangeTracker.nextIteration();
        for (int x = pos.getX() - size; x <= pos.getX() + size; ++x)
        {
            for (int z = pos.getZ() - size; z <= pos.getZ() + size; ++z)
            {
                for (int y = pos.getY() - size; y <= pos.getY() + size; ++y)
                {
                    BlockVector3 currentPosition = BlockVector3.at(x, y, z);
                    if (currentPosition.distanceSq(pos) <= size*size)
                    {
                        BlockState currentBlock = blockChangeTracker.get(currentPosition, currentIteration);
                        BlockMaterial currentMat = currentBlock.getBlockType().getMaterial();

                        if (!(currentMat.isAir() || currentMat.isLiquid()))
                        {
                            continue;
                        }

                        int count = 0;

                        final Map<BlockType, Integer> blockCount = new HashMap<>();

                        for (final BlockVector3 vector : FACES_TO_CHECK)
                        {
                            final BlockVector3  relativePosition = currentPosition.add(vector);
                            final BlockState    relativeBlock = blockChangeTracker.get(relativePosition, currentIteration);
                            final BlockType     relativeType  = relativeBlock.getBlockType();

                            if (!(relativeType.getMaterial().isAir() || relativeType.getMaterial().isLiquid()))
                            {
                                count++;
                                if (blockCount.containsKey(relativeType))
                                {
                                    blockCount.put(relativeType, blockCount.get(relativeType) + 1);
                                }
                                else
                                {
                                    blockCount.put(relativeType, 1);
                                }
                            }
                        }

                        BlockType currentMaterial = BlockTypes.AIR;
                        int amount = 0;

                        for (final BlockType wrapper : blockCount.keySet())
                        {
                            final Integer currentCount = blockCount.get(wrapper);
                            if (amount <= currentCount)
                            {
                                currentMaterial = wrapper;
                                amount = currentCount;
                            }
                        }

                        if (count >= fillFaces)
                        {
                            blockChangeTracker.put(currentPosition, currentMaterial.getDefaultState(), currentIteration);
                        }
                    }
                }
            }
        }
    }

    private void erosionIteration(EditSession session, int size, BlockChangeTracker blockChangeTracker, BlockVector3 pos)
    {
        
        final int currentIteration = blockChangeTracker.nextIteration();
        for (int x = pos.getX() - size; x <= pos.getX() + size; ++x)
        {
            for (int z = pos.getZ() - size; z <= pos.getZ() + size; ++z)
            {
                for (int y = pos.getY() - size; y <= pos.getY() + size; ++y)
                {
                    final BlockVector3 currentPosition = BlockVector3.at(x, y, z);
                    if (currentPosition.distanceSq(pos) <= size*size)
                    {
                        final BlockState    currentBlock = blockChangeTracker.get(currentPosition, currentIteration);
                        final BlockMaterial currentMat   = currentBlock.getBlockType().getMaterial();

                        if (currentMat.isAir() || currentMat.isLiquid())
                        {
                            continue;
                        }

                        int count = 0;
                        for (final BlockVector3 vector : FACES_TO_CHECK)
                        {
                            final BlockVector3  relativePosition = currentPosition.add(vector);
                            final BlockState    relativeBlock = blockChangeTracker.get(relativePosition, currentIteration);
                            final BlockMaterial relativeMat  = relativeBlock.getBlockType().getMaterial();

                            if (relativeMat.isAir() || relativeMat.isLiquid())
                            {
                                count++;
                            }
                        }

                        if (count >= erodeFaces)
                        {
                            blockChangeTracker.put(currentPosition, BlockTypes.AIR.getDefaultState(), currentIteration);
                        }
                    }
                }
            }
        }
    }
    
    
    private static final class BlockChangeTracker {
        
        private final Map<Integer, Map<BlockVector3, BlockState>> blockChanges = new HashMap<>();
        private final Map<BlockVector3, BlockState> flatChanges                = new HashMap<>();
        private final EditSession session;
        private int nextIterationId = 0;

        public BlockChangeTracker(EditSession session) {
            this.session = session;
        }

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

        public void put(BlockVector3 position, BlockState changedBlock, int iteration) {
            if (!this.blockChanges.containsKey(iteration)) {
                this.blockChanges.put(iteration, new HashMap<BlockVector3, BlockState>());
            }

            this.blockChanges.get(iteration).put(position, changedBlock);
            this.flatChanges.put(position, changedBlock);
        }
    }
}
