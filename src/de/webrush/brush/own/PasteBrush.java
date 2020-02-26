package de.webrush.brush.own;

import java.util.Random;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Direction;

/**
 * Caches the clipboard on loading and pastes
 * it by clicking without air and with an offset.
 */
public class PasteBrush implements Brush {

    private final ClipboardHolder holder;
    private final Clipboard clipboard;
    private final int yoff;
    private boolean rotate;
    
    
    public PasteBrush(LocalSession local, int yoff, boolean rotate) throws EmptyClipboardException {
        this.holder    = local.getClipboard();
        this.clipboard = holder.getClipboard();
        this.yoff      = yoff;
        this.rotate    = rotate;
    }
    
    
    @Override
    public void build(EditSession session, BlockVector3 click, Pattern pattern, double size)
            throws MaxChangedBlocksException {

        if (rotate) {
            rotate();
        }
        
        BlockVector3 oldOrigin = clipboard.getOrigin();
        BlockVector3 newOrigin = getOrigin();
        
        clipboard.setOrigin(newOrigin);
        Operation operation = holder.createPaste(session)
                .to(click)
                .ignoreAirBlocks(true)
                .copyEntities(true)
                .build();
        Operations.completeLegacy(operation);
        clipboard.setOrigin(oldOrigin);
    }
      
    
    private BlockVector3 getOrigin() {
        Vector3 center = clipboard.getRegion().getCenter();
        double  height = clipboard.getRegion().getMinimumPoint().getY() - (1 + yoff);
        return BlockVector3.at(center.getX(), height, center.getZ());
    }
    
    
    private void rotate() {
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(-getRandomRotation());
        
        Vector3 vec = getRandomDirection().toBlockVector().abs().multiply(-2).add(1, 1, 1).toVector3();
        transform = transform.scale(vec);
        
        holder.setTransform(holder.getTransform().combine(transform));
    }
    
    
    private int getRandomRotation() {
        switch (new Random().nextInt(3)) {
            case 0:  return 90;
            case 1:  return 180;
            default: return 270;
        }
    }
    
    private Direction getRandomDirection() {
        boolean north = new Random().nextBoolean();
        return  north ? Direction.NORTH : Direction.WEST;
    }   
    
}
