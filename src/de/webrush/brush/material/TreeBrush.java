package de.webrush.brush.material;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;

/**
 * Generates a tree on dirt/grass.
 */
public class TreeBrush implements Brush {

    private TreeType tree;
    
    public TreeBrush(TreeType tree) {
        this.tree = tree;
    }
      
    @Override
    public void build(EditSession session, BlockVector3 pos, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        tree.generate(session, pos.add(0, 1, 0));
    }

}
