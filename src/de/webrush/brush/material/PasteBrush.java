package de.webrush.brush.material;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Direction;

import de.webrush.WeBrush;
import net.md_5.bungee.api.ChatColor;

/**
 * Caches the clipboard on loading and pastes
 * it by clicking without air and with an offset.
 */
public class PasteBrush implements Brush {
    
    private final BukkitPlayer player;
    private final SchematicProvider provider;
    private final int yoff;
    private final boolean rotate;
    
    private ClipboardHolder holder;
    private Clipboard clipboard;
    
    public PasteBrush(BukkitPlayer player, SchematicProvider provider, int yoff, boolean rotate) throws EmptyClipboardException {
        this.player   = player;
        this.provider = provider;
        this.yoff     = yoff;
        this.rotate   = rotate;
    }
    
    
    @Override
    public void build(EditSession session, BlockVector3 click, Pattern pattern, double size) {
        try {
            
            setClipboard();
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
        catch(IOException ex) {
            player.print(ChatColor.RED + "Error! File no longer accessible. " + ex.getMessage());
        }
        catch(Exception ex) {
            player.print(ChatColor.RED + "Error! Check console...");
            ex.printStackTrace();
        }
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
    
    private void setClipboard() throws IOException {
        holder    = provider.getHolder();
        clipboard = holder.getClipboard();
    }
    
    
    /**
     * Abstract base for various different strategies
     * to provide a clipboard which can be pasted.
     */
    public static abstract class SchematicProvider {
        
        private final String display;
        
        public SchematicProvider(String display) {
            this.display = display;
        }
        
        public String toString() {
            return display;
        }
        
        /**
         * Loads the file as clipboard.
         */
        public Clipboard loadClipBoard(File file) throws IOException {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                return reader.read();
            }
        }
        
        public abstract ClipboardHolder getHolder() throws IOException;
    }
    
    /**
     * Holds a Clipboard, returns it.
     */
    public static class ClipboardProvider extends SchematicProvider {
        private final ClipboardHolder holder;
        
        public ClipboardProvider(LocalSession session) throws EmptyClipboardException {
            super("clipboard");
            holder = session.getClipboard();
        }
        
        public ClipboardProvider(File file) throws IOException {
            super(PasteParser.getFileDisplay(file));
            this.holder = new ClipboardHolder(loadClipBoard(file));
        }
        
        public ClipboardHolder getHolder() {
            return holder;
        }
    }
    
    /**
     * Holds a list of files, randomly selects one, 
     * reads it to a clipboard, caches it, returns it.
     */
    public static class FolderProvider extends SchematicProvider {
        private final File[] files;
        private final Map<File, ClipboardHolder> cache = new HashMap<>();
        
        public FolderProvider(File folder) {
            super(PasteParser.getFolderDisplay(folder));
            files = getSchematicFiles(folder);
        }
        
        private File[] getSchematicFiles(File folder) {
            return folder.listFiles(PasteParser.FILEMASK);
        }
        
        public ClipboardHolder getHolder() throws IOException {
            File file = getRandomFile();
            if (cache.containsKey(file)) {
                return cache.get(file);
            }
            ClipboardHolder schem = new ClipboardHolder(loadClipBoard(file));
            cache.put(file, schem);
            return schem;
        }
        
        private File getRandomFile() {
            int index = new Random().nextInt(files.length);
            return files[index];
        }
    }
    
    
    /**
     * Parses the player input to an concrete SchematicProvider.
     * Also ensures files and folders exist and are in the correct format.
     */
    public static class PasteParser {
        public static final FilenameFilter FILEMASK = (file, name) -> name.endsWith(".schem");
        public static final String CLIPBOARD = "-clipboard";
        public static final String RANDOM    = "-random";
        
        public static SchematicProvider create(LocalSession session, String source) throws EmptyClipboardException, IOException {
            if (source.equals(CLIPBOARD)) {
                return new ClipboardProvider(session);
            }
            if (source.equals(RANDOM)) {
                return new FolderProvider(getSchematicFile(""));
            }
            if (source.endsWith("/")) {
                return new FolderProvider(getSchematicFile(source));
            }
            if (!source.endsWith(".schem")) {
                source += ".schem";
            }
            return new ClipboardProvider(getSchematicFile(source));
        }
        
        /**
         * Returns the File in the schematics directory.
         * Might throw an IllegalArgumentException.
         */
        public static File getSchematicFile(String destination) {
            String path = getRootPath() + "/schematics/";
            File   file = new File(path + destination);
            if (destination.endsWith("/")) validateFolder(file);
            else                           validateFile(file);
            return file;
        }
        
        /** 
         * Shortens the file-path.
         */
        public static String getFileDisplay(File file) {
            String prefix = getRootPath();
            String full   = file.getAbsolutePath();
            return full.substring(prefix.length(), full.length());
        }
        
        /**
         * Shortens the folder-path.
         */
        public static String getFolderDisplay(File file) {
            return getFileDisplay(file) + File.separator;
        }
        
        /**
         * Ensures that the file is a directory and contains 
         * schematic files and has an allowed path.
         */
        public static void validateFolder(File folder) {
            validatePath(folder);
            if (!folder.isDirectory()) {
                throw new BrushException("Folder not found! " + getFolderDisplay(folder));
            }
            File[] files = folder.listFiles(FILEMASK);
            if (files == null || files.length == 0) {
                throw new BrushException("Folder is empty! " + getFolderDisplay(folder));
            }
        }
        
        /**
         * Ensures that the file exists and has an allowed path.
         */
        public static void validateFile(File file) {
            validatePath(file);
            if (!file.exists()) {
                throw new BrushException("File not found! " + getFileDisplay(file));
            }
        }
        
        /**
         * Ensures that the path is a child of the root.
         */
        public static void validatePath(File file) {
            String path = file.toPath().normalize().toString();
            if (!path.startsWith(getRootPath())) {
                throw new BrushException("File is on an unsafe path.");
            }
        }
        
        /**
         * Returns the root path for the worldEdit folder.
         */
        public static String getRootPath() {
            return WeBrush.getWorldEdit().getDataFolder().getAbsolutePath();
        }
    }
    
    
    /**
     * Builds a list of the schematic folder entries.
     * Is able to read folders and files, but also sub-folders.
     */
    public static class PasteTabCompleter {

        
        public static List<String> query(String arg) {
            List<String> list = new ArrayList<>();
            if (PasteParser.CLIPBOARD.startsWith(arg)) list.add(PasteParser.CLIPBOARD);
            if (PasteParser.RANDOM.startsWith(arg)) list.add(PasteParser.RANDOM);
            
            try {
                String path  = getSubPath(arg);
                String token = getSubToken(arg);
                File[] files = getSubFolder(path);
                
                for (File file : files) {
                    if (file.getName().startsWith(token) 
                    && !file.getName().contains(" ")) {
                        if (file.isDirectory()) {
                            list.add(path + file.getName() + "/");
                        } else if (file.getName().endsWith(".schem")) {
                            list.add(path + file.getName());
                        }
                    }
                }
            } catch(BrushException | InvalidPathException ex) {}
            return list;
        }
        
        /**
         * Returns the entries of the sub folder.
         */
        private static File[] getSubFolder(String path) {
            File  folder = PasteParser.getSchematicFile(path);
            File[] files = folder.listFiles();
            if (files == null) return new File[0];
            return files;
        }
        
        /**
         * Returns the path to a folder for a given argument.
         * If it is no folder, returns an empty string to match default.
         */
        private static String getSubPath(String path) {
            int index = path.lastIndexOf("/");
            if (index == -1) return "";
            return path.substring(0, index + 1);
        }
        
        /**
         * Returns the name of the folder/file in the sub directory.
         * If it is in the root, returns the input.
         */
        private static String getSubToken(String path) {
            int index = path.lastIndexOf('/');
            if (index == -1) return path;
            return path.substring(index + 1, path.length());
        }
    }
    
    /**
     * Indicates that wrong input was given to the Brush.
     */
    public static class BrushException extends RuntimeException {
        private static final long serialVersionUID = -1801406364876214202L;

        public BrushException(String message) {
            super(message);
        }
    }
    
}
