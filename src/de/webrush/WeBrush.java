package de.webrush;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxRadiusException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;

import de.webrush.DispatchLayer.BrushLoader;
import net.md_5.bungee.api.ChatColor;


public class WeBrush extends JavaPlugin implements CommandExecutor, TabCompleter {
     
    WorldEditPlugin worldEditPlugin;
    static boolean  debug;
    
    @Override
    public void onEnable() {
        this.getCommand("webrush").setExecutor(this);
        this.getCommand("webrush").setTabCompleter(this);
        worldEditPlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch(args.length) {
            case 1:
                return filterList(DispatchLayer.brushes.keySet(), args[0]);
            
            case 2:
                if (args[0].equals("tree")) {
                    return filterList(TreeType.getPrimaryAliases(), args[1]);
                }
                
            default:
                return null;
        }
    }
    
    public List<String> filterList(Collection<? extends String> collection, String arg) {
        List<String> list = new ArrayList<>();
        for (String key : collection) {
            if (key.startsWith(arg)) {
                list.add(key);
            }
        }
        return list;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Command not usable from console!");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(getHelpText());
            return true;
        }
        
        String arg0 = args[0].toLowerCase();
        if (arg0.equals("debug")) {
            debug = !debug;
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Debug is now:" + debug);
            return true;
        }
        
        setBrush((Player) sender, arg0, args);
        return true;
    }
    
    private void setBrush(Player player, String name, String[] args) {
        try {
            LocalSession session = worldEditPlugin.getSession(player);
            BrushLoader  loader  = DispatchLayer.brushes.get(name);
            if (loader == null) {
                player.sendMessage(getHelpText());
                return;
            }
            loader.loadBrush(BukkitAdapter.adapt(player), session, args);
        }
        catch (MaxRadiusException ex) {
            player.sendMessage(ChatColor.RED + "Brush size is too big. Check WE config.");
        }
        catch(NoMatchException ex) {
            player.sendMessage(ChatColor.RED + "Can't find material: " + ex.getMessage());
        }
        catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "Could not parse to number: " + ex.getMessage());
        }
        catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "Exception found. Check console...");
            ex.printStackTrace();
        }
    }

    public String getHelpText() {
        String cmds = "debug|" + String.join("|", DispatchLayer.brushes.keySet());
        return "/webrush <" + cmds + ">";
    }
}
