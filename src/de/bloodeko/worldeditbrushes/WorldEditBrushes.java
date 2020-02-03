package de.bloodeko.worldeditbrushes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxRadiusException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.factory.parser.pattern.SingleBlockPatternParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.bloodeko.worldeditbrushes.brushes.SingleVineBrush;
import net.md_5.bungee.api.ChatColor;


public class WorldEditBrushes extends JavaPlugin implements CommandExecutor {
     
    WorldEditPlugin worldEditPlugin;
    static boolean  debug;
    
    @Override
    public void onEnable() {
        this.getCommand("webrush").setExecutor(this);
        worldEditPlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Command not usable from console!");
            return true;
        }
        
        setBrush((Player) sender, args);
        return true;
    }
    
    private void setBrush(Player player, String[] args) {
        
        if (args.length == 0) {
            player.sendMessage(getHelpText());
            return;
        }
        
        try {
            LocalSession session = worldEditPlugin.getSession(player);

            
            switch(args[0].toLowerCase()) {
                case "erode": {
                    int size       = getIntOrDefault(args, 1, 5);
                    int iterations = getIntOrDefault(args, 2, 1);
                    int maxFaces   = getIntOrDefault(args, 3, 3);
                    
                    DispatchLayer.loadErodeBrush(BukkitAdapter.adapt(player), session, null, size, iterations, maxFaces);
                    break;
                }
                
                case "fill": {
                    int size       = getIntOrDefault(args, 1, 5);
                    int iterations = getIntOrDefault(args, 2, 1);
                    int maxFaces   = getIntOrDefault(args, 3, 3);
                    
                    DispatchLayer.loadFillBrush(BukkitAdapter.adapt(player), session, null, size, iterations, maxFaces);
                    break;
                }
                
                case "vine": {
                    Pattern mat    = getPatternOrdefault(session, player, args, 1, BlockTypes.VINE.getDefaultState());
                    int     size   = getIntOrDefault(args, 2, 5);
                    double  chance = getDoubleOrDefault(args, 3, 0.5);
                    int     length = getIntOrDefault(args, 4, 5);
                    
                    DispatchLayer.loadVineBrush(BukkitAdapter.adapt(player), session, mat, size, chance, length);
                    break;
                }
                
                case "vine2": {
                    SingleVineBrush.load(BukkitAdapter.adapt(player), session);
                    break;
                }
                
                case "test": {
                    Pattern mat    = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
                    int     size   = getIntOrDefault(args, 2, 5);
                    DispatchLayer.loadTest(BukkitAdapter.adapt(player), session, mat, size);
                    break;
                }
                
                case "sphere": {
                    Pattern mat    = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
                    int     size   = getIntOrDefault(args, 2, 5);
                    DispatchLayer.loadTestSphere(BukkitAdapter.adapt(player), session, mat, size);
                    break;
                }
                
                case "cube": {
                    Pattern mat    = getPatternOrdefault(session, player, args, 1, BlockTypes.STONE.getDefaultState());
                    int     width  = getIntOrDefault(args, 2, 5);
                    int     height = getIntOrDefault(args, 3, 5);
                    boolean spherical = getBooleanOrDefault(args, 4, false);
                    DispatchLayer.loadCuboid(BukkitAdapter.adapt(player), session, mat, width, height, spherical);
                    break;
                }
                
                case "debug": {
                    debug = !debug;
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug is now:" + debug);
                    break;
                }
                
                default:
                    player.sendMessage(getHelpText());
            }
        }
        catch (MaxRadiusException ex) {
            player.sendMessage(ChatColor.RED + "Brush size is too big. Check WE config.");
        }
        catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "Exception found. Check console...");
            ex.printStackTrace();
        }
    }

    private String getHelpText() {
        return ChatColor.GOLD + "[WorldEditBrush]" + ChatColor.WHITE
           + " > erode, fill, vine, vine2, test, sphere, cube";
    }
    
    private boolean getBooleanOrDefault(String[] args, int i, boolean or) {
        return args.length > i ? Boolean.parseBoolean(args[i]) : or;
    }
    
    private int getIntOrDefault(String[] args, int index, int or) {
        return args.length > index ? Integer.parseInt(args[index]) : or;
    }
    
    private double getDoubleOrDefault(String[] args, int index, double or) {
        return args.length > index ? Double.parseDouble(args[index]) : or;
    }
    
    private Pattern getPatternOrdefault(LocalSession session, Player player, String[] args, int index, Pattern or) throws InputParseException {
        if (args.length <= index) {
            return or;
        }
        SingleBlockPatternParser parser = new SingleBlockPatternParser(WorldEdit.getInstance());
        ParserContext context = new ParserContext();
        context.setActor(BukkitAdapter.adapt(player));
        context.setWorld(BukkitAdapter.adapt(player.getWorld()));
        context.setSession(session);
        return parser.parseFromInput(args[index], context);
    }
}
