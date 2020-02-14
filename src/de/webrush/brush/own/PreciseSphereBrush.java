package de.webrush.brush.own;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

import de.webrush.ShapeCycler;
import de.webrush.ShapeCycler.BrushFunction;

/**
 * An more precise sphere brush.
 * Affects the same area as Fill/Erode from CraftScripts do.
 */
public class PreciseSphereBrush implements Brush {

    @Override
    public void build(EditSession session, BlockVector3 click, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        List<BlockVector3> list = new ArrayList<>();  
        
        BrushFunction function = vec -> {
            list.add(vec);
        };
        
        new ShapeCycler(function, size).run(click);
        
        for (BlockVector3 vec : list) {
            session.setBlock(vec, pattern);
        }
    }
    
}
