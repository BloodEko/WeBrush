package de.webrush;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import com.sk89q.worldedit.world.block.BlockTypes;

import de.webrush.brush.material.OverlayBrush;
import de.webrush.brush.material.PasteBrush;
import de.webrush.brush.material.PasteBrush.PasteParser;
import de.webrush.brush.material.PasteBrush.SchematicProvider;
import de.webrush.brush.material.SelectBrush;
import de.webrush.brush.material.TestBrush;
import de.webrush.brush.material.TreeBrush;
import de.webrush.brush.material.VineBrush;
import de.webrush.brush.shape.BartelLine;
import de.webrush.brush.shape.CubeBrush;
import de.webrush.brush.shape.LineBrush;
import de.webrush.brush.shape.SphereBrush;
import de.webrush.brush.terrain.BlendBallBrush;
import de.webrush.brush.terrain.BlendBallErosion;
import de.webrush.brush.terrain.ErodeBrush;
import de.webrush.brush.terrain.ErosionBrush;
import de.webrush.brush.terrain.FillBrush;
import de.webrush.brush.terrain.FlattenBrush;
import de.webrush.brush.terrain.SpikeBrush;
import de.webrush.util.PreSet;
import de.webrush.util.PreSetManager;
import de.webrush.util.Util;
import net.md_5.bungee.api.ChatColor;

public class DispatchLayer {
  
    public static Map<String, BrushLoader> brushes = new HashMap<>();
    
    static {
        //material
        brushes.put("over",  new LoadOverlay());
        brushes.put("paste", new LoadPasteBrush());
        brushes.put("test",  new LoadTest());
        brushes.put("tree",  new LoadTree());
        brushes.put("vine",  new LoadVine());
        
        //shape
        brushes.put("bartelLine", new LoadBartelLine());
        brushes.put("cube",       new LoadCube());
        brushes.put("line",       new LoadLineBrush());
        brushes.put("sphere",     new LoadSphereBrush());
        brushes.put("select",     new LoadSelectBrush());
        
        //terrain
        brushes.put("bb",      new LoadBlendBall());
        brushes.put("ebb",     new LoadBlendballErosion());
        brushes.put("erode",   new LoadErode());
        brushes.put("e",       new LoadErosion());
        brushes.put("fill",    new LoadFill());
        brushes.put("flatten", new LoadFlatten());
        brushes.put("spike",   new LoadSpike());
        
        brushes.put("preset",  new LoadPreset());
    }
    
    
    // -------- |
    // Material |
    // -------- |
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
    
    public static class LoadPasteBrush extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws Exception {
            String source  = getStringOrdefault(args, 1, PasteParser.CLIPBOARD);
            int     yoff   = getIntOrDefault(args, 2, 0);
            boolean rotate = getBooleanOrDefault(args, 3, true);
            SchematicProvider provider = PasteParser.create(session, source);
            
            initBrush(player, session, null, 0, 
                      new PasteBrush(player, provider, yoff, rotate), "Paste", 
                    " source:" + provider +
                    " yoff:"   + yoff +
                    " rotate:" + rotate);
        }
    }
    
    public static class LoadTest extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int height  = getIntOrDefault(args, 1, 20);
            int blocks  = getIntOrDefault(args, 2, 1);
            
            initBrush(player, session, BlockTypes.STONE.getDefaultState(), 5, 
                      new TestBrush(height, blocks), "Test",
                    " height:"  + height
                  + " blocks:"  + blocks);
        }
    }
    
    public static class LoadTree extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            String   treename = getStringOrdefault(args, 1, "oak");
            TreeType tree     = TreeGenerator.lookup(treename);
            
            if (tree == null) {
                Util.printError(player, "Tree not found: " + treename);
                return;
            }
            
            initBrush(player, session, null, 5,
                      new TreeBrush(tree), "Tree",
                    " Tree:" + treename);
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
    
    
    // ------ |
    // Shape  |
    // ------ |
    public static class LoadBartelLine extends BaseLoader {

        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern mat        = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            double  size       = getDoubleOrDefault(args, 2, 0.5);
            double  tension    = getDoubleOrDefault(args, 3, -0.2);
            double  bias       = getDoubleOrDefault(args, 4, 0);
            double  continuity = getDoubleOrDefault(args, 5, 0.5);
            double  quality    = getDoubleOrDefault(args, 6, 5);
            boolean fill       = getBooleanOrDefault(args, 7, true);
            
            Util.printMessage(player,
                    "Set splatter line."    +
                    " mat:"        + format(mat) + " size:"    + size + 
                    " tension:"    + tension     + " bias:"    + bias + 
                    " continuity:" + continuity  + " quality:" + quality + 
                    " fill:" + fill);
            
            checkSize(size);
            new BartelLine(player, session, mat, size, tension, bias, continuity, quality, fill).build();
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
    
    public static class LoadLineBrush extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            
            initBrush(player, session, pattern, 0,
                      new LineBrush(player, session), "Line",
                    " Mat:" + format(pattern));
        }
    }
    
    public static class LoadSelectBrush extends BaseLoader {

        @Override
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            double radius = getAllowedRadius();
            initBrush(player, session, null, radius, 
                    new SelectBrush(player, session.getRegionSelector(player.getWorld()), session), 
                   "Select", "");
        }
        
        /**
         * Allows a size twice of the configured brush size.
         * Because the SelectBrush is only reading, which is faster.
         */
        @Override
        public void checkSize(double size) throws MaxBrushRadiusException {
            super.checkSize(size / 2);
        }
        
        /**
         * Returns the default radius of 40.
         * If this is larger than twice the allowed radius, minimizes it.
         */
        private double getAllowedRadius() {
            double radius = 40;
            if (getMaxRadius() <= 0) {
                return radius;
            }
            if (getMaxRadius() < (radius / 2)) {
                return getMaxRadius() * 2;
            }
            return radius;
        }
        
        /**
         * Returns the maximum configured brush size.
         */
        private double getMaxRadius() {
            return WorldEdit.getInstance().getConfiguration().maxBrushRadius;
        }
    }
    
    public static class LoadSphereBrush extends BaseLoader {

        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            Pattern   pattern = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
            
            initBrush(player, session, pattern, 5,
                      new SphereBrush(), "Sphere",  
                    " Mat:" + format(pattern));
        }
    }
    
    
    // ------- |
    // Terrain |
    // ------- |
    public static class LoadBlendBall extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            initBrush(player, session, null, 5, 
                      new BlendBallBrush(), "BlendBall", "");
        }
    }
    
    public static class LoadBlendballErosion extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            String name = getStringOrdefault(args, 1, null);
            VoxelPreset preset = VoxelPreset.get(name);
            if (preset == null) {
                preset = VoxelPreset.getDefault();
            } else {
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            int blendEdge    = getIntOrDefault(args, 1, 1);
            int erosionFaces = getIntOrDefault(args, 2, preset.erosionFaces);
            int erosionRecur = getIntOrDefault(args, 3, preset.erosionRecur);
            int fillFaces    = getIntOrDefault(args, 4, preset.fillFaces);
            int fillRecur    = getIntOrDefault(args, 5, preset.fillRecur);

            BlendBallBrush blend   = new BlendBallBrush();
            ErosionBrush   erosion = new ErosionBrush(erosionFaces, erosionRecur, fillFaces, fillRecur);
            
            initBrush(player, session, null, 5,
                      new BlendBallErosion(blend, erosion, blendEdge), "BlendBallErosion",
                    " blendEdge:"    + blendEdge
                  + " erosionFaces:" + erosionFaces
                  + " erosionRecur:" + erosionRecur
                  + " fillFaces:"    + fillFaces
                  + " fillRecur:"    + fillRecur);
        }
    }
    
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
      
    public static class LoadErosion extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            String name = getStringOrdefault(args, 1, null);
            VoxelPreset preset = VoxelPreset.get(name);
            if (preset == null) {
                preset = VoxelPreset.getDefault();
            } else {
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            int erosionFaces = getIntOrDefault(args, 1, preset.erosionFaces);
            int erosionRecur = getIntOrDefault(args, 2, preset.erosionRecur);
            int fillFaces    = getIntOrDefault(args, 3, preset.fillFaces);
            int fillRecur    = getIntOrDefault(args, 4, preset.fillRecur);
            
            ErosionBrush brush = new ErosionBrush(erosionFaces, erosionRecur, fillFaces, fillRecur);
            
            initBrush(player, session, null, 5, 
                      brush, "Erosion",
                    " erosionFaces:" + erosionFaces
                  + " erosionRecur:" + erosionRecur
                  + " fillFaces:"    + fillFaces
                  + " fillRecur:"    + fillRecur);
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
    
    public static class LoadFlatten extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int height = getIntOrDefault(args, 1, 20);
            int blocks = getIntOrDefault(args, 2, 1);
            
            initBrush(player, session, BlockTypes.STONE.getDefaultState(), 5, 
                      new FlattenBrush(height, blocks), "Flatten",
                      " height:" + height
                    + " blocks:" + blocks);
        }
    }
    
    public static class LoadSpike extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            int     height  = getIntOrDefault(args, 1, 20);
            int     blocks  = getIntOrDefault(args, 2, 1);
            
            initBrush(player, session, BlockTypes.STONE.getDefaultState(), 5, 
                      new SpikeBrush(height, blocks), "Spike",
                    " height:"  + height
                  + " blocks:"  + blocks);
        }
    }
    
    public static class LoadPreset extends BaseLoader {
        
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws WorldEditException {
            if (args.length <= 1) {
                Util.printMessage(player, "Args length too short!");
                return;
            }
            
            if (args[1].equalsIgnoreCase("reload")) {
                PreSetManager manager = WeBrush.getPreSetManager();
                manager.reload();
                
                int     counter = manager.map.keySet().size();
                boolean error   = manager.error;
                
                Util.printMessage(player, "Reloaded Presets. (" + counter + ")");
                if (error) {
                    Util.printMessage(player, "An error occured! Check console.");
                }
                return;
            }
            
            String key    = args[1];
            PreSet preSet = WeBrush.getPreSetManager().map.get(key);
            if (preSet == null) {
                Util.printError(player, "Preset not found.");
                return;
            }
            
            preSet.loadAll(() -> {
                Util.setHeldItemSlot(player, 0);
                Util.printMessage(player, "Loaded Preset:" + key);
            }, player.getPlayer());
        }
    }
    
    
    public static interface BrushLoader {
        public void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws Exception;
    }
    
    
    
    public static class VoxelPreset {
        public static final Map<String, VoxelPreset> map = new HashMap<>();
        
        static {
            map.put("melt",     new VoxelPreset(2, 1, 5, 1 ));
            map.put("fill",     new VoxelPreset(5, 1, 2, 1 ));
            map.put("smooth",   new VoxelPreset(3, 1, 3, 1 ));
            map.put("liftUp",   new VoxelPreset(6, 0, 1, 1 ));
            map.put("liftDown", new VoxelPreset(1, 1, 6, 0 ));
        }
        
        public final int erosionFaces;
        public final int erosionRecur;
        public final int fillFaces;
        public final int fillRecur;
        
        public VoxelPreset(int eFaces, int eRecur, int fFaces, int fRecur) {
            this.erosionFaces = eFaces;
            this.erosionRecur = eRecur;
            this.fillFaces = fFaces;
            this.fillRecur = fRecur;
        }
        
        public static VoxelPreset get(String name) {
            return map.get(name);
        }
        
        public static VoxelPreset getDefault() {
            return map.get("liftUp");
        }
    }
    
    
    public static abstract class BaseLoader implements BrushLoader {
        
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
            
            Util.printMessage(player, color + name + size + debug);
        }
        
        
        public String format(Pattern pattern) {
            return Util.after(pattern.toString(), "minecraft:");
        }

        
        @Override
        public abstract void loadBrush(BukkitPlayer player, LocalSession session, String[] args) throws Exception;
    }
}
