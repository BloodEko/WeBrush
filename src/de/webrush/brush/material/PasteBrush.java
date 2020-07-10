package de.webrush.brush.material;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
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

/**
 * Caches the clipboard on loading and pastes
 * it by clicking without air and with an offset.
 */
public class PasteBrush implements Brush {
    
    private final SchematicProvider provider;
    private final int yoff;
    private final boolean rotate;
    
    private ClipboardHolder holder;
    private Clipboard clipboard;
    
    public PasteBrush(SchematicProvider provider, int yoff, boolean rotate) throws EmptyClipboardException {
        this.provider = provider;
        this.yoff     = yoff;
        this.rotate   = rotate;
    }
    
    
    @Override
    public void build(EditSession session, BlockVector3 click, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
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
    
    private void setClipboard() {
        try {
            holder = provider.getHolder();
            clipboard = holder.getClipboard();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }
    }
    
    /**
     * Abstract base for various different strategies
     * to provide a clipboard which can be pasted.
     */
    public static abstract class SchematicProvider {
        
        public static SchematicProvider create(LocalSession session, String source) throws EmptyClipboardException, IOException {
            if (source == null) {
                return new ClipboardProvider(session);
            }
            if (source.equals("/")) {
                return new FolderProvider(getSchematicFile(""));
            }
            if (source.endsWith("/")) {
                File folder = getSchematicFile(source);
                return new FolderProvider(folder);
            }
            if (!source.endsWith(".schem")) {
                source += ".schem";
            }
            return new FileProvider(getSchematicFile(source));
        }
        
        private static File getSchematicFile(String destination) {
            String path = WeBrush.getWorldEdit().getDataFolder().getAbsolutePath() + "/schematics/";
            return new File(path + destination);
        }
        
        public Clipboard loadClipBoard(File file) throws FileNotFoundException, IOException {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            ClipboardReader reader = format.getReader(new FileInputStream(file));
            return reader.read();
        }
        
        public abstract ClipboardHolder getHolder() throws IOException;
    }
    
    /**
     * Returns the copied clipboard.
     */
    public static class ClipboardProvider extends SchematicProvider {
        private final ClipboardHolder holder;
        
        public ClipboardProvider(LocalSession session) throws EmptyClipboardException {
            this.holder = session.getClipboard();
        }
        
        public ClipboardHolder getHolder() {
            return holder;
        }
        
        public String toString() {
            return "clipboard";
        }
    }
    
    /**
     * Provides a file as clipboard.
     */
    public static class FileProvider extends SchematicProvider {
        private final ClipboardHolder holder;
        
        public FileProvider(File file) throws IOException {
            this.holder   = new ClipboardHolder(loadClipBoard(file));
        }
        public ClipboardHolder getHolder() {
            return holder;
        }
        public String toString() {
            return "file";
        }
    }
    
    /**
     * Provides a random clipboard from one of the files.
     * Caches the results, so IO is only done once per file.
     */
    public static class FolderProvider extends SchematicProvider {
        private final File[] files;
        private final Map<File, ClipboardHolder> cache = new HashMap<>();
        
        public FolderProvider(File folder) {
            this.files = getSchematicFiles(folder);
        }
        
        private File[] getSchematicFiles(File folder) {
            return folder.listFiles((file, name) -> name.endsWith(".schem"));
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
        
        public String toString() {
            return "folder";
        }
    }
    
}
