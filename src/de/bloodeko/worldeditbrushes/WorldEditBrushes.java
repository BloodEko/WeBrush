package de.bloodeko.worldeditbrushes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxRadiusException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import de.bloodeko.worldeditbrushes.DispatchLayer.BrushLoader;
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
