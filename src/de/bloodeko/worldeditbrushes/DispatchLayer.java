package de.bloodeko.worldeditbrushes;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxBrushRadiusException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.extension.factory.parser.pattern.SingleBlockPatternParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.bloodeko.worldeditbrushes.brushes.BlendBallBrush;
import de.bloodeko.worldeditbrushes.brushes.CubeBrush;
import de.bloodeko.worldeditbrushes.brushes.ErodeBrush;
import de.bloodeko.worldeditbrushes.brushes.FillBrush;
import de.bloodeko.worldeditbrushes.brushes.TestBrush;
import de.bloodeko.worldeditbrushes.brushes.VineBrush;
import net.md_5.bungee.api.ChatColor;


public class DispatchLayer {
  
    public static Map<String, BrushLoader> brushes = new HashMap<>();
    
    static {
        brushes.put("erode", new LoadErode());
        brushes.put("fill",  new LoadFill());
        brushes.put("vine",  new LoadVine());
        brushes.put("test",  new LoadTest());
        brushes.put("cube",  new LoadCube());
        brushes.put("bb",    new LoadBlendBall());
    }
    
    
    public static class LoadErode extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int size       = getIntOrDefault(args, 1, 5);
            int iterations = getIntOrDefault(args, 2, 1);
            int maxFaces   = getIntOrDefault(args, 3, 3);
            
            checkSize(size);
            BrushTool tool = getTool(player, session);
            
            tool.setFill(null);
            tool.setSize(size);
            tool.setBrush(new ErodeBrush(iterations, maxFaces), "worldedit.brush.erode");
            print(player, "Erode",
                " Size:"       + size
              + " Iterations:" + iterations
              + " MaxFaces:"   + maxFaces);
        }
    }
    
    public static class LoadFill extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int size       = getIntOrDefault(args, 1, 5);
            int iterations = getIntOrDefault(args, 2, 1);
            int maxFaces   = getIntOrDefault(args, 3, 3);
            
            checkSize(size);
            BrushTool tool = getTool(player, session);
            
            tool.setFill(null);
            tool.setSize(size);
            tool.setBrush(new FillBrush(iterations, maxFaces), "worldedit.brush.fill");
            print(player, "Fill",
                " Size:"       + size
              + " Iterations:" + iterations
              + " MaxFaces:"   + maxFaces);
        }
    }
    
    public static class LoadVine extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.VINE.getDefaultState());
            int     size    = getIntOrDefault(args, 2, 5);
            double  chance  = getDoubleOrDefault(args, 3, 0.5);
            int     length  = getIntOrDefault(args, 4, 5);
            
            checkSize(size);
            BrushTool tool = getTool(player, session);
            
            tool.setFill(pattern);
            tool.setSize(size);
            tool.setBrush(new VineBrush(chance, length), "worldedit.brush.vine");

            print(player, "Vine",
                " Mat:"    + format(pattern)
              + " Size:"   + size
              + " Chance:" + chance
              + " Length:" + length);
        }
    }
    
    public static class LoadTest extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            int     size    = getIntOrDefault(args, 2, 5);
            
            checkSize(size);
            BrushTool tool = getTool(player, session);

            tool.setFill(pattern);
            tool.setSize(size);
            tool.setBrush(new TestBrush(), "worldedit.brush.test");

            print(player, "Test",
                " Mat:"  + format(pattern)
              + " Size:" + size);
        }
    }
    
    public static class LoadCube extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern pattern   = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            int     width     = getIntOrDefault(args, 2, 5);
            boolean spherical = getBooleanOrDefault(args, 3, false);
            
            checkSize(width);
            BrushTool tool = getTool(player, session);

            tool.setFill(pattern);
            tool.setSize(width);
            tool.setBrush(new CubeBrush(width, spherical), "worldedit.brush.test");

            print(player, "Cube",
                " Mat:"       + format(pattern)
              + " Width:"     + width
              + " Spherical:" + spherical);
        }
    }
    
    public static class LoadBlendBall extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            BrushTool tool = getTool(player, session);

            tool.setFill(null);
            tool.setSize(3);
            tool.setBrush(new BlendBallBrush(), "worldedit.brush.blendball");

            print(player, "BlendBall",
                " Size:"      + 5);
        }
    }
    
    
    public static interface BrushLoader {
        public abstract void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException;
    }
    
    
    public static abstract class BaseLoader implements BrushLoader {

        public boolean getBooleanOrDefault(String[] args, int i, boolean or) {
            return args.length > i ? Boolean.parseBoolean(args[i]) : or;
        }
        
        public int getIntOrDefault(String[] args, int index, int or) {
            return args.length > index ? Integer.parseInt(args[index]) : or;
        }
        
        public double getDoubleOrDefault(String[] args, int index, double or) {
            return args.length > index ? Double.parseDouble(args[index]) : or;
        }
        
        public Pattern getPatternOrdefault(LocalSession session, BukkitPlayer player, String[] args, int index, Pattern or) throws InputParseException {
            if (args.length <= index) {
                return or;
            }
            SingleBlockPatternParser parser = new SingleBlockPatternParser(WorldEdit.getInstance());
            ParserContext context = new ParserContext();
            context.setActor(player);
            context.setWorld(player.getWorld());
            context.setSession(session);
            return parser.parseFromInput(args[index], context);
        }
        
        public void checkSize(double size) throws MaxBrushRadiusException {
            WorldEdit.getInstance().checkMaxBrushRadius(size);
        }
        
        public BrushTool getTool(BukkitPlayer player, LocalSession session) throws InvalidToolBindException {
            return session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        }
        
        public void print(BukkitPlayer player, String name, String message) {
            player.printInfo(TextComponent.of(ChatColor.LIGHT_PURPLE + "Set " + name + "brush. " + message));
        }
        
        public String format(Pattern pattern) {
            return StringUtils.substringAfter(pattern.toString(), "minecraft:");
        }

        @Override
        public abstract void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException;
    }
}
