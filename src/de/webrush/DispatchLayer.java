package de.webrush;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxBrushRadiusException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extension.factory.parser.pattern.SingleBlockPatternParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.webrush.brush.material.OverlayBrush;
import de.webrush.brush.material.PasteBrush;
import de.webrush.brush.material.TestBrush;
import de.webrush.brush.material.TreeBrush;
import de.webrush.brush.material.VineBrush;
import de.webrush.brush.shape.BartelLine;
import de.webrush.brush.shape.CubeBrush;
import de.webrush.brush.shape.LineBrush;
import de.webrush.brush.shape.PreciseSphereBrush;
import de.webrush.brush.terrain.BlendBallBrush;
import de.webrush.brush.terrain.BlendBallErosion;
import de.webrush.brush.terrain.ErodeBrush;
import de.webrush.brush.terrain.ErosionBrush;
import de.webrush.brush.terrain.FillBrush;
import net.md_5.bungee.api.ChatColor;

public class DispatchLayer {
  
    public static Map<String, BrushLoader> brushes = new HashMap<>();
    
    static {
        //craftscripts
        brushes.put("erode",   new LoadErode());
        brushes.put("fill",    new LoadFill());
        brushes.put("vine",    new LoadVine());
        
        //voxelsniper
        brushes.put("bb",      new LoadBlendBall());
        brushes.put("ebb",     new LoadBlendballErosion());
        brushes.put("e",       new LoadErosion());
        brushes.put("over",    new LoadOverlay());
        brushes.put("tree",    new TreeLoader());
        
        //own
        brushes.put("test",       new LoadTest());
        brushes.put("cube",       new LoadCube());
        brushes.put("sphere",     new LoadPreciseSphere());
        brushes.put("bartelLine", new LoadBartelLine());
        brushes.put("line",       new LoadLineBrush());
        brushes.put("paste",      new LoadPasteBrush());
    }
    
    //
    // CraftScripts
    //
    
    public static class LoadErode extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int maxFaces   = getIntOrDefault(args, 1, 2);
            int iterations = getIntOrDefault(args, 2, 1);
            
            initBrush(player, session, null, 2, 
                      new ErodeBrush(maxFaces, iterations), "Erode",
                    " MaxFaces:"   + maxFaces
                  + " Iterations:" + iterations);
        }
    }
    
    public static class LoadFill extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int maxFaces   = getIntOrDefault(args, 1, 2);
            int iterations = getIntOrDefault(args, 2, 1);
            
            initBrush(player, session, null, 2, 
                      new FillBrush(maxFaces, iterations), "Fill",
                    " MaxFaces:"   + maxFaces
                  + " Iterations:" + iterations);
        }
    }
    
    public static class LoadVine extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.VINE.getDefaultState());
            double  chance  = getDoubleOrDefault(args, 2, 0.5);
            int     length  = getIntOrDefault(args, 3, 5);
            
            initBrush(player, session, pattern, 5,
                      new VineBrush(chance, length), "Vine",
                    " Mat:"    + format(pattern)
                  + " Chance:" + chance
                  + " Length:" + length);
        }
    }
    
    //
    // VoxelSniper
    //
    
    public static class LoadBlendBall extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            initBrush(player, session, null, 5, 
                      new BlendBallBrush(), "BlendBall", "");
        }
    }
    
    public static class LoadBlendballErosion extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int blendEdge    = getIntOrDefault(args, 1, 1);
            int[] preset     = getPreset(args, 2, new int[] {6, 0, 1, 1});
            int erosionFaces = preset[0];
            int eresionRecur = preset[1];
            int fillFaces    = preset[2];
            int fillRecur    = preset[3];
            int size         = 5;

            BlendBallBrush blend   = new BlendBallBrush();
            ErosionBrush   erosion = new ErosionBrush(erosionFaces, eresionRecur, fillFaces, fillRecur);
            
            initBrush(player, session, null, size,
                      new BlendBallErosion(blend, erosion, blendEdge), "BlendBallErosion",
                    " BlendEdge:"    + blendEdge
                  + " erosionFaces:" + erosionFaces
                  + " eresionRecur:" + eresionRecur
                  + " fillFaces:"    + fillFaces
                  + " fillRecur:"    + fillRecur);
        }
    }
    
    public static class LoadErosion extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int[] preset     = getPreset(args, 1, new int[] {6, 0, 1, 1});
            int erosionFaces = preset[0];
            int eresionRecur = preset[1];
            int fillFaces    = preset[2];
            int fillRecur    = preset[3];
            
            ErosionBrush brush = new ErosionBrush(erosionFaces, eresionRecur, fillFaces, fillRecur);
            
            initBrush(player, session, null, 5, 
                      brush, "Erosion",
                    " erosionFaces:" + erosionFaces
                  + " eresionRecur:" + eresionRecur
                  + " fillFaces:"    + fillFaces
                  + " fillRecur:"    + fillRecur);
        }
    }
    
    
    public static class LoadOverlay extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            boolean natural = isNatural(args, 1);
            Pattern mat     = getPatternOrdefault(session, player, args, 1, BlockTypes.DIRT.getDefaultState());
            int     depth   = getIntOrDefault(args, 2, 3);
            
            initBrush(player, session, mat, 5,
                      new OverlayBrush(depth, natural), "Overlay",
                    " Mat:"   + (natural ? "natural" : format(mat))
                  + " Depth:" + depth);
        }
    }
    
    public static class TreeLoader extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            String   treename = getStringOrdefault(args, 1, "oak");
            TreeType tree     = TreeGenerator.lookup(treename);
            
            if (tree == null) {
                player.print(ChatColor.RED + "Tree not found: " + treename);
                return;
            }
            
            initBrush(player, session, null, 5,
                      new TreeBrush(tree), "Tree",
                    " Tree:" + treename);
        }
    }
    
    //
    // Own
    //
    
    public static class LoadTest extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());

            initBrush(player, session, pattern, 5, 
                      new TestBrush(), "Test", 
                    " Mat:"  + format(pattern));
        }
    }
    
    public static class LoadCube extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {          
            Pattern   pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());

            initBrush(player, session, pattern, 5, 
                      new CubeBrush(), "Cube", 
                    " Mat:" + format(pattern));
        }
    }
    
    public static class LoadPreciseSphere extends BaseLoader {

        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern   pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            
            initBrush(player, session, pattern, 5,
                      new PreciseSphereBrush(), "Sphere",  
                    " Mat:" + format(pattern));
        }
    }
    
    public static class LoadLineBrush extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            
            initBrush(player, session, pattern, 0,
                      new LineBrush(player, session), "Line",
                    " Mat:" + format(pattern));
        }
    }
    
    public static class LoadBartelLine extends BaseLoader {

        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern mat        = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            double  size       = getDoubleOrDefault(args, 2, 0.5);
            double  tension    = getDoubleOrDefault(args, 3, -0.2);
            double  bias       = getDoubleOrDefault(args, 4, 0);
            double  continuity = getDoubleOrDefault(args, 5, 0.5);
            double  quality    = getDoubleOrDefault(args, 6, 5);
            boolean fill       = getBooleanOrDefault(args, 7, true);
            
            player.print("Set splatter line."    +
                    " mat:"        + format(mat) + " size:"    + size + 
                    " tension:"    + tension     + " bias:"    + bias + 
                    " continuity:" + continuity  + " quality:" + quality + 
                    " fill:" + fill);
            
            checkSize(size);
            new BartelLine(player, session, mat, size, tension, bias, continuity, quality, fill).build();
        }
    }
    
    public static class LoadPasteBrush extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int     yoff   = getIntOrDefault(args, 1, 0);
            boolean rotate = getBooleanOrDefault(args, 2, false);
            
            initBrush(player, session, null, 0, 
                      new PasteBrush(session, yoff, rotate), "Schematic", 
                    " yoff:"   + yoff +
                    " rotate:" + rotate);
        }
    }
    
    
    
    public static interface BrushLoader {
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException;
    }
    
    
    
    public static class VoxelPreset {
        public static final int[] melt     = { 2, 1, 5, 1 };
        public static final int[] fill     = { 5, 1, 2, 1 };
        public static final int[] smooth   = { 3, 1, 3, 1 };
        public static final int[] liftUp   = { 6, 0, 1, 1 };
        public static final int[] liftDown = { 1, 1, 6, 0 };
        public static final List<String> names = new ArrayList<>();
        
        static {
            names.add("melt");
            names.add("fill");
            names.add("smooth");
            names.add("liftUp");
            names.add("liftDown");
        }
    }
    
    
    public static abstract class BaseLoader implements BrushLoader {

        public int[] getPreset(String[] args, int index, int[] or) {
            
            if (args.length > index) {
                switch(args[index]) {
                    case "melt":     return VoxelPreset.melt;
                    case "fill":     return VoxelPreset.fill;
                    case "smooth":   return VoxelPreset.smooth;
                    case "liftUp":   return VoxelPreset.liftUp;
                    case "liftDown": return VoxelPreset.liftDown;
                }
            }
            
            int[] array = new int[4];
            for (int i = 0; i < array.length; i++) {
                array[i] = getIntOrDefault(args, i + index, or[i]);
                
            }
            return array;
        }
        
        public String getStringOrdefault(String[] args, int index, String or) {
            return args.length > index ? args[index] : or;
        }
        
        public boolean getBooleanOrDefault(String[] args, int index, boolean or) {
            return args.length > index ? Boolean.parseBoolean(args[index]) : or;
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
            if (isNatural(args, index)) {
                return or;
            }
            SingleBlockPatternParser parser = new SingleBlockPatternParser(WorldEdit.getInstance());
            ParserContext context = new ParserContext();
            context.setActor(player);
            context.setWorld(player.getWorld());
            context.setSession(session);
            return parser.parseFromInput(args[index], context);
        }
        
        
        public boolean isNatural(String[] args, int index) {
            return args.length > index && args[index].equals("n");
        }
        
        
        public void checkSize(double size) throws MaxBrushRadiusException {
            WorldEdit.getInstance().checkMaxBrushRadius(size);
        }
        
        
        public void initBrush(BukkitPlayer player, LocalSession session, Pattern pattern, double size,
                              Brush brush, String brushname, 
                              String brushdebug) throws InvalidToolBindException, MaxBrushRadiusException {
            
            checkSize(size);
            BrushTool tool = getTool(player, session);
            tool.setFill(pattern);
            tool.setSize(size);
            tool.setBrush(brush, "webrush.admin");
            sendInfo(player, size, brushname, brushdebug);
        }
        
        
        public BrushTool getTool(BukkitPlayer player, LocalSession session) throws InvalidToolBindException {
            return session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        }
        
        
        public void sendInfo(BukkitPlayer player, double brushsize, String brushname, String debug) {
            ChatColor color = ChatColor.LIGHT_PURPLE;
            String    name  = "Set "   +       brushname;
            String    size  = "brush(" + (int) brushsize + ")";
            
            player.printInfo(TextComponent.of(color + name + size + debug));
        }
        
        
        public String format(Pattern pattern) {
            return StringUtils.substringAfter(pattern.toString(), "minecraft:");
        }

        
        @Override
        public abstract void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException;
    }
}
