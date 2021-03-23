package de.webrush.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.webrush.WeBrush;
import de.webrush.util.PreSet.SlotOperation;

/**
 * The responsibility of this class is to load all PreSets from
 * file and make them accessible for the preset command.
 */
public class PreSetManager {

    public Map<String, PreSet> map = new HashMap<>();
    public boolean error = false;
    private int delay;
    
    public PreSetManager() {
        reload();
    }
    
    /**
     * Clears data and loads new preset data from file.
     */
    public void reload() {
        map.clear();
        error = false;
        loadPreSets();
    }
    
    /**
     * Loads all presets into the map.
     */
    private void loadPreSets() {
        try {
            loadDelay(getRootSection());
            for (ConfigurationSection presetSection : getSubSections(getRootSection())) {
                loadPreSet(presetSection);
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("[WeBrush] Could not load presets.");
            error = true;
        }
    }
    
    private void loadDelay(ConfigurationSection rootSection) {
        delay = rootSection.getInt("delay", 0);
    }
    
    /**
     * Loads the specified preset into the map.
     */
    private void loadPreSet(ConfigurationSection presetSection) {
        try {
            List<SlotOperation> slots = new ArrayList<>();
            for (ConfigurationSection slotSection : getSubSections(presetSection)) {
                slots.add(SlotOperation.valueOf(slotSection.getName(), slotSection));
            }
            
            PreSet set = new PreSet(slots, delay);
            map.put(presetSection.getName(), set);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("[WeBrush] Could not load preset:" + presetSection.getName());
            error = true;
        }
    }
    
    /** 
     * Gets all sub-sections of the specified section.
     */
    private List<ConfigurationSection> getSubSections(ConfigurationSection parent) {
        List<ConfigurationSection> list = new ArrayList<>();
        if (parent == null) {
            return list;
        }
        for (String key : parent.getKeys(false)) {
            ConfigurationSection add = parent.getConfigurationSection(key);
            if (add == null) {
                continue;
            }
            list.add(add);
        }
        return list;
    }
    
    /** 
     * Returns the root section named "preset".
     */
    private ConfigurationSection getRootSection() throws Exception {
        File                 file   = getPresetFile();
        YamlConfiguration    config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root   = config.getConfigurationSection("preset");
        return root;
    }
    
    /**
     * Returns the preset file, creates a new one if it doesn't exist.
     */
    private File getPresetFile() throws Exception {
        File file = new File(getMainFolder().getAbsolutePath() + File.separator + "preset.yml");
        if (!file.exists()) {
            WeBrush.getInstance().saveResource("preset.yml", false);
        }
        return file;
    }
    
    /**
     * Returns the main folder, creates a new one if it doesn't exist.
     */
    private File getMainFolder() {
        File folder = WeBrush.getInstance().getDataFolder();
        folder.mkdirs();
        return folder;
    }
     
}

