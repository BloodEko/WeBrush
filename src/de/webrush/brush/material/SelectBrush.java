package de.webrush.brush.material;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import net.md_5.bungee.api.ChatColor;

/**
 * A brush which allows to make smart selections of buildings.
 * The bottom block is clicked, then it expands N/W/E/S/U.
 */
public class SelectBrush implements Brush {
    private final RegionSelector selector;
    private final LocalSession localSession;
    private final BukkitPlayer player;
    
    public SelectBrush(BukkitPlayer player, RegionSelector selector, LocalSession localSession) {
        this.selector = selector;
        this.player = player;
        this.localSession = localSession;
    }

    @Override
    public void build(EditSession session, BlockVector3 position, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        long growingMs = System.currentTimeMillis();
        GrowingSelector growing = new GrowingSelector(session, position, (int) size);
        growing.grow();
        growingMs = System.currentTimeMillis() - growingMs;
        
        SelectorLimits limits = ActorSelectorLimits.forActor(player);
        selector.clear();
        selector.selectPrimary(growing.pos1, limits);
        selector.selectSecondary(growing.pos2, limits);
        
        //copy blocks to clipboard
        long copyMs = System.currentTimeMillis();
        try {
            checkRegionBounds(selector.getRegion(), localSession);
            BlockArrayClipboard clipboard = new BlockArrayClipboard(selector.getRegion());
            clipboard.setOrigin(localSession.getPlacementPosition(player));
            ForwardExtentCopy copy = new ForwardExtentCopy(session, selector.getRegion(), clipboard, growing.pos1);
            copy.setCopyingEntities(false);
            copy.setCopyingBiomes(false);
            Operations.completeLegacy(copy);
            localSession.setClipboard(new ClipboardHolder(clipboard));
        } catch (IncompleteRegionException ex) {
            ex.printStackTrace();
        }
        copyMs = System.currentTimeMillis() - copyMs;
        
        // Selected and copied X blocks. (100, 50, 100)
        BlockVector3 volumeVec = growing.pos2.subtract(growing.pos1).add(1, 1, 1);
        player.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Selected and copied " 
                                     + selector.getVolume() + " blocks. " + volumeVec 
                                     + " within " + growingMs + "+" + copyMs 
                                     + "ms (" + growing.reads + " reads)" );
        
        if (growing.mightHaveMoreBlocks()) {
            player.getPlayer().sendMessage(ChatColor.RED + "There might be more blocks to select!");
        }
    }

    /**
     * Throws if the region would allocate a clipboard larger than the block change limit.
     *
     * @param region The region to check
     * @param session The session
     * @throws MaxChangedBlocksException if the volume exceeds the limit
     */
    private void checkRegionBounds(Region region, LocalSession session) throws MaxChangedBlocksException {
        int limit = session.getBlockChangeLimit();
        if (limit >= 0 && region.getBoundingBox().getVolume() >= limit) {
            throw new MaxChangedBlocksException(limit);
        }
    }
    
    /**
     * An selector entity which grows until all sides are air, 
     * or the maximum range is reached.
     */
    private static class GrowingSelector {
        private final EditSession session;
        private final BlockVector3 center;
        private final int range;
        
        private BlockVector3 pos1;
        private BlockVector3 pos2;
        public int reads;
        
        public GrowingSelector(EditSession session, BlockVector3 center, int range) {
            this.session = session;
            this.center = center;
            this.range = range;
            this.pos1 = center;
            this.pos2 = center;
        }
        
        /**
         * Grows the selection and tries to fully include the building.
         */
        public void grow() {
            while (true) { //Loop as long as blocks are found on sides within the maximum range.
                BlockVector3 block = null;
                boolean found = false;
                
                while (canExpandUp() && expandUp()) {
                    found = true;
                }
                while (canBroaden() && (block = searchEast()) != null) {
                    pos1 = BlockVector3.at(block.getX(), pos1.getY(), block.getZ());
                    found = true;
                }
                while (canLengthen() && (block = searchNorth()) != null) {
                    pos1 = BlockVector3.at(pos1.getX(), pos1.getY(), block.getZ());
                    found = true;
                }
                while (canBroaden() && (block = searchWest()) != null) {
                    pos2 = BlockVector3.at(block.getX(), pos2.getY(), pos2.getZ());
                    found = true;
                }
                while (canLengthen() && (block = searchSouth()) != null) {
                    pos2 = BlockVector3.at(pos2.getX(), pos2.getY(), block.getZ());
                    found = true;
                }
                
                if (!found) {
                    break;
                }
            }
        }
        
        /**
         * Returns true of the selector has reached it's maximum on any axis.
         */
        private boolean mightHaveMoreBlocks() {
            return !canExpandUp() || !canBroaden() || !canLengthen();
        }
        
        private boolean canBroaden() {
            return pos2.getX() - pos1.getX() < range*2;
        }
        
        private boolean canLengthen() {
            return pos2.getZ() - pos1.getZ() < range*2;
        }
        
        private boolean canExpandUp() {
            return pos2.getY() - center.getY() < range*2;
        }
        
        private BlockVector3 searchNorth() {
            return searchZ(pos1.getZ() - 1);
        }
        
        private BlockVector3 searchSouth() {
            return searchZ(pos2.getZ() + 1);
        }
        
        private BlockVector3 searchWest() {
            return searchX(pos2.getX() + 1);
        }
        
        private BlockVector3 searchEast() {
            return searchX(pos1.getX() -1);
        }
        
        /**
         * Tries to expands one layer up with an increased outline of 1.
         * Returns true, if a block was found in this area.
         */
        private boolean expandUp() {
            int y = pos2.getY() + 1;
            BlockVector3 at = searchBlock(
                               canBroaden()  ? pos1.getX()-1 : pos1.getX(), //east
                               canBroaden()  ? pos2.getX()+1 : pos2.getX(), //west
                               canLengthen() ? pos1.getZ()-1 : pos1.getZ(), //north
                               canLengthen() ? pos2.getZ()+1 : pos2.getZ(), //south
                               y, y);
            if (at == null) {
                return false;
            }
            
            int x1 = Math.min(pos1.getX(), at.getX());
            int x2 = Math.max(pos2.getX(), at.getX());
            int z1 = Math.min(pos1.getZ(), at.getZ());
            int z2 = Math.max(pos2.getZ(), at.getZ());
            
            BlockVector3 newPos1 = BlockVector3.at(x1, pos1.getY(), z1);
            BlockVector3 newPos2 = BlockVector3.at(x2, y, z2);
            
            if (!newPos1.equals(pos1)) {
                pos1 = newPos1;
            }
            if (!newPos2.equals(pos2)) {
                pos2 = newPos2;
            }
            return true;
        }
        
        /**
         * Returns the block found at this X line, or null.
         */
        private BlockVector3 searchX(int x) {
            int z1 = pos1.getZ();
            if (canLengthen()) z1--; //lower -1
            
            int z2 = pos2.getZ() + 1; //upper +1
            return searchBlock(x, x, z1, z2);
        }

        /**
         * Returns the block found at this Z line, or null.
         */
        private BlockVector3 searchZ(int z) {
            int x1 = pos1.getX() - 1; //lower -1
            int x2 = pos2.getX() + 1; //upper +1
            return searchBlock(x1, x2, z, z);
        }
        
        private BlockVector3 searchBlock(int x1, int x2, int z1, int z2) {
            return searchBlock(x1, x2, z1, z2, pos1.getY(), pos2.getY());
        }
        
        /**
         * Returns the location of a non-air block within the area, or null.
         * Coordinates are inclusive and x1, z1, y1 are the lower bounds.
         */
        private BlockVector3 searchBlock(int x1, int x2, int z1, int z2, int y1, int y2) {
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        BlockVector3 at = BlockVector3.at(x, y, z);
                        BlockType type = session.getBlock(at).getBlockType();
                        reads++;
                        if (!isAir(type)) {
                            return at;
                        }
                    }
                }
            }
            return null;
        }
        
        private boolean isAir(BlockType type) {
            return type == BlockTypes.AIR || type == BlockTypes.CAVE_AIR;
        }
    }
    
}
