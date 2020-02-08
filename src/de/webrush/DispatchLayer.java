package de.webrush;

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
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extension.factory.parser.pattern.SingleBlockPatternParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.webrush.brush.craftscript.ErodeBrush;
import de.webrush.brush.craftscript.FillBrush;
import de.webrush.brush.craftscript.VineBrush;
import de.webrush.brush.own.CubeBrush;
import de.webrush.brush.own.PreciseSphereBrush;
import de.webrush.brush.own.TestBrush;
import de.webrush.brush.voxelsniper.BlendBallBrush;
import de.webrush.brush.voxelsniper.BlendBallErosion;
import de.webrush.brush.voxelsniper.ErosionBrush;
import de.webrush.brush.voxelsniper.OverlayBrush;
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
        brushes.put("erosion", new LoadErosion());
        brushes.put("over",    new LoadOverlay());
        
        //own
        brushes.put("test",    new LoadTest());
        brushes.put("cube",    new LoadCube());
        brushes.put("sphere",  new LoadPreciseSphere());
    }
    
    //
    // CraftScripts
    //
    
    public static class LoadErode extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int maxFaces   = getIntOrDefault(args, 1, 2);
            int iterations = getIntOrDefault(args, 2, 1);
            
            initBrush(player, session, null, 2, 
                      new ErodeBrush(maxFaces, iterations), "worldedit.brush.erode", "Erode",
                    " MaxFaces:"   + maxFaces
                  + " Iterations:" + iterations);
        }
    }
    
    public static class LoadFill extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int maxFaces   = getIntOrDefault(args, 1, 2);
            int iterations = getIntOrDefault(args, 2, 1);
            
            initBrush(player, session, null, 2, 
                      new FillBrush(maxFaces, iterations), "worldedit.brush.fill", "Fill",
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
                      new VineBrush(chance, length), "worldedit.brush.vine", "Vine",
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
                      new BlendBallBrush(), "worldedit.brush.blendball", "BlendBall", "");
        }
    }
    
    public static class LoadBlendballErosion extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int blendradius  = getIntOrDefault(args, 1, 1);
            int erosionFaces = getIntOrDefault(args, 2, 6);
            int eresionRecur = getIntOrDefault(args, 3, 0);
            int fillFaces    = getIntOrDefault(args, 4, 1);
            int fillRecur    = getIntOrDefault(args, 5, 1);
            int size         = 5;

            BlendBallBrush blend   = new BlendBallBrush();
            ErosionBrush   erosion = new ErosionBrush(erosionFaces, eresionRecur, fillFaces, fillRecur);
            
            initBrush(player, session, null, size,
                      new BlendBallErosion(blend, erosion, blendradius), "worldedit.brush.erosion", "BlendBallErosion",
                    " BlendSize:"    + (size + blendradius)
                  + " erosionFaces:" + erosionFaces
                  + " eresionRecur:" + eresionRecur
                  + " fillFaces:"    + fillFaces
                  + " fillRecur:"    + fillRecur);
        }
    }
    
    public static class LoadErosion extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int erosionFaces = getIntOrDefault(args, 1, 6);
            int eresionRecur = getIntOrDefault(args, 2, 0);
            int fillFaces    = getIntOrDefault(args, 3, 1);
            int fillRecur    = getIntOrDefault(args, 4, 1);
            
            ErosionBrush brush = new ErosionBrush(erosionFaces, eresionRecur, fillFaces, fillRecur);
            
            initBrush(player, session, null, 5, 
                      brush, "worldedit.brush.erosion", "Erosion",
                    " erosionFaces:" + erosionFaces
                  + " eresionRecur:" + eresionRecur
                  + " fillFaces:"    + fillFaces
                  + " fillRecur:"    + fillRecur);
        }
    }
    
    
    public static class LoadOverlay extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern mat = getPatternOrdefault(session, player, args, 1, BlockTypes.DIRT.getDefaultState());
            int   depth = getIntOrDefault(args, 2, 3);
                        
            initBrush(player, session, mat, 5,
                      new OverlayBrush(depth), "worldedit.brush.over", "Overlay",
                    " Depth:" + depth 
                  + " Mat:"   + format(mat));
        }
    }
    
    //
    // Own
    //
    
    public static class LoadTest extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());

            initBrush(player, session, pattern, 5, 
                      new TestBrush(), "worldedit.brush.test", "Test", 
                    " Mat:"  + format(pattern));
        }
    }
    
    public static class LoadCube extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {          
            Pattern   pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());

            initBrush(player, session, pattern, 5, 
                      new CubeBrush(), "worldedit.brush.cube", "Cube", 
                    " Mat:" + format(pattern));
        }
    }
    
    public static class LoadPreciseSphere extends BaseLoader {

        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern   pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            
            initBrush(player, session, pattern, 5,
                      new PreciseSphereBrush(), "worldedit.brush.sphere", "Sphere",  
                    " Mat:" + format(pattern));
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
        
        
        public void initBrush(BukkitPlayer player, LocalSession session, Pattern pattern, double size,
                              Brush brush, String permission, String brushname, 
                              String brushdebug) throws InvalidToolBindException, MaxBrushRadiusException {
            
            checkSize(size);
            BrushTool tool = getTool(player, session);
            tool.setFill(pattern);
            tool.setSize(size);
            tool.setBrush(brush, permission);
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
