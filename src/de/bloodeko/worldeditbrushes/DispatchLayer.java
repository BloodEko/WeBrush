package de.bloodeko.worldeditbrushes;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxBrushRadiusException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

import de.bloodeko.worldeditbrushes.brushes.CubeBrush;
import de.bloodeko.worldeditbrushes.brushes.ErodeBrush;
import de.bloodeko.worldeditbrushes.brushes.FillBrush;
import de.bloodeko.worldeditbrushes.brushes.TestBrush;
import de.bloodeko.worldeditbrushes.brushes.TestSphere;
import de.bloodeko.worldeditbrushes.brushes.VineBrush;
import net.md_5.bungee.api.ChatColor;


public class DispatchLayer {

    public static void loadErodeBrush(Player player, LocalSession session, Pattern pattern, double size, int iterations, int maxFaces) throws WorldEditException {
        
        WorldEdit.getInstance().checkMaxBrushRadius(size);
        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        
        tool.setFill(pattern);
        tool.setSize(size);
        tool.setBrush(new ErodeBrush(iterations, maxFaces), "worldedit.brush.erode");

        player.printInfo(TextComponent.of(ChatColor.LIGHT_PURPLE + "Set Erode brush." 
                                   + " Size:"       + (int) size
                                   + " Iterations:" + iterations
                                   + " MaxFaces:"   + maxFaces));
    }
    
    
    public static void loadFillBrush(Player player, LocalSession session, Pattern pattern, double size, int iterations, int maxFaces) throws WorldEditException {
        
        WorldEdit.getInstance().checkMaxBrushRadius(size);
        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        
        tool.setFill(pattern);
        tool.setSize(size);
        tool.setBrush(new FillBrush(iterations, maxFaces), "worldedit.brush.fill");

        player.printInfo(TextComponent.of(ChatColor.LIGHT_PURPLE + "Set Fill brush." 
                                   + " Size:"       + (int) size
                                   + " Iterations:" + iterations
                                   + " MaxFaces:"   + maxFaces));
    }
    
    public static void loadVineBrush(Player player, LocalSession session, Pattern pattern, double size, double density, int length) throws WorldEditException {
        
        WorldEdit.getInstance().checkMaxBrushRadius(size);
        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        
        tool.setFill(pattern);
        tool.setSize(size);
        tool.setBrush(new VineBrush(density, length), "worldedit.brush.vine");

        player.printInfo(TextComponent.of(ChatColor.LIGHT_PURPLE + "Set Vine brush." 
                                   + " Size:"       + (int) size
                                   + " Mat:"        + pattern
                                   + " Density:"    + density
                                   + " Length:"   + length));
    }  
    
    public static void loadTest(Player player, LocalSession session, Pattern pattern, double size) throws WorldEditException {
        WorldEdit.getInstance().checkMaxBrushRadius(size);
        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        
        tool.setFill(pattern);
        tool.setSize(size);
        tool.setBrush(new TestBrush(), "worldedit.brush.test");

        player.printInfo(TextComponent.of(ChatColor.LIGHT_PURPLE + "Set Test brush." 
                                   + " Size:"       + (int) size
                                   + " Mat:"        + pattern));
    }


    public static void loadTestSphere(Player player, LocalSession session, Pattern pattern, int size) throws WorldEditException {
        WorldEdit.getInstance().checkMaxBrushRadius(size);
        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        
        tool.setFill(pattern);
        tool.setSize(size);
        tool.setBrush(new TestSphere(), "worldedit.brush.sphere");

        player.printInfo(TextComponent.of(ChatColor.LIGHT_PURPLE + "Set TestSphere brush." 
                                   + " Size:"       + (int) size
                                   + " Mat:"        + pattern));
    }


    public static void loadCuboid(Player player, LocalSession session, Pattern mat, int width, int height, boolean spherical) throws WorldEditException {
        WorldEdit.getInstance().checkMaxBrushRadius(width);
        WorldEdit.getInstance().checkMaxBrushRadius(height);
        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        
        tool.setFill(mat);
        tool.setSize(width);
        tool.setBrush(new CubeBrush(width, height, spherical), "worldedit.brush.cuboid");

        player.printInfo(TextComponent.of(ChatColor.LIGHT_PURPLE + "Set cuboid brush."
                                   + " mat:"    + mat
                                   + " width:"  + width
                                   + " height:" + height
                                   + " spherical:"   + spherical));
    } 
}
