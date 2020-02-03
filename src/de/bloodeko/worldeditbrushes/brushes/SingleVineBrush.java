package de.bloodeko.worldeditbrushes.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import net.md_5.bungee.api.ChatColor;

public class SingleVineBrush implements Brush {

    @Override
    public void build(EditSession session, BlockVector3 vec, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        BlockState vine = BlockTypes.VINE.getDefaultState().with(BlockTypes.VINE.getProperty("north"), true);
        
        for (int i = 0; i < 10; i++) {
            BlockVector3 pos = vec.add(0, -i , 0);
            session.setBlock(pos, vine);
        }
    }
    
    public static void load(Player player, LocalSession session) throws WorldEditException {
        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        
        tool.setFill(null);
        tool.setSize(1);
        tool.setBrush(new SingleVineBrush(), "worldedit.brush.vine");

        player.printInfo(TextComponent.of(ChatColor.LIGHT_PURPLE + "Set SingleVine brush Size: 1"));
    }

}
