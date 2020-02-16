package de.webrush;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;

/**
 * Virtually tracks changes on locations, writes them to the EditSession later on. <br>
 * 1. to guarantee correct ordering. <br>
 * 2. to make 1 single operation, which is important for FAWE and so. <br>
 * <br>
 * Soft changes can be set, flushed. <br>
 * Hard changes can be set, read and flushed. <br>
 */
public class ChangeTracker {
    
    private EditSession session;
    private Map<BlockVector3, BlockState> softChanges = new HashMap<>();
    private Map<BlockVector3, BlockState> hardChanges = new HashMap<>();
    
    /**
     * The created tracker object cannot be reused.
     */
    public ChangeTracker(EditSession session) {
        this.session = session;
    }

    /**
     * Writes a soft change to the map.
     */
    public void setSoft(BlockVector3 at, BlockState to) {
        softChanges.put(at, to);
    }

    /**
     * Writes a hard change to the map.
     */
    public void setHard(BlockVector3 at, BlockState to) {
        hardChanges.put(at, to);
    }
    
    /**
     * Queries a hard change for the location. <br>
     * If none is found queries the EditSession/world.
     */
    public BlockState get(BlockVector3 at) {
        BlockState val = hardChanges.get(at);
        if (val == null) {
            return session.getBlock(at);
        }
        return val;
    }
    
    /**
     * Moves all soft changes to hard changes.
     */
    public void flushSoft() {
        hardChanges.putAll(softChanges);
        softChanges.clear();
    }
    
    /**
     * Copies all hard changes to the EditSession. <br>
     * The EditSession is an virtual list, which is later
     * on applied to the world.
     */
    public void writeToSession() throws MaxChangedBlocksException {
        for (Entry<BlockVector3, BlockState> entry : hardChanges.entrySet()) {
            session.setBlock(entry.getKey(), entry.getValue());
        }
    }
}
