package com.github.games647.fastlogin.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.FloodgateAPI;
import org.geysermc.floodgate.FloodgatePlayer;

import com.github.games647.fastlogin.bukkit.BukkitLoginSession;
import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import com.github.games647.fastlogin.core.StoredProfile;
import com.github.games647.fastlogin.core.hooks.AuthPlugin;

public class FloodgateAuthTask implements Runnable {

    private final FastLoginBukkit plugin;
    private final Player player;

    public FloodgateAuthTask(FastLoginBukkit plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        FloodgatePlayer floodgatePlayer = FloodgateAPI.getPlayer(player.getUniqueId());
        plugin.getLog().info(
                "Player {} is connecting through Geyser Floodgate.",
                player.getName());
        String allowNameConflict = plugin.getCore().getConfig().getString("allowFloodgateNameConflict");
        // check if the Bedrock player is linked to a Java account 
        boolean isLinked = floodgatePlayer.fetchLinkedPlayer() != null;
        if (allowNameConflict.equalsIgnoreCase("linked") && !isLinked) {
            plugin.getLog().info(
                    "Bedrock Player {}'s name conflits an existing Java Premium Player's name",
                    player.getName());
            
            // kicking must be synchronous
            // https://www.spigotmc.org/threads/asynchronous-player-kick-problem.168580/
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                public void run() {
                    player.kickPlayer("This name is allready in use by a Premium Java Player");
                }
            });

        }
        if (!allowNameConflict.equalsIgnoreCase("true")
                && !allowNameConflict.equalsIgnoreCase("linked")
                && !allowNameConflict.equalsIgnoreCase("false")) {
            plugin.getLog().error(
                    "Invalid value detected for 'allowFloodgateNameConflict' in FasttLogin/config.yml. Aborting login of Player {}",
                    player.getName());
            return;
        }
        
        AuthPlugin<Player> authPlugin = plugin.getCore().getAuthPluginHook();

        String autoLoginFloodgate = plugin.getCore().getConfig().getString("autoLoginFloodgate");
        boolean autoRegisterFloodgate = plugin.getCore().getConfig().getBoolean("autoRegisterFloodgate");
        
        boolean isRegistered;
        try {
            isRegistered = authPlugin.isRegistered(player.getName());
        } catch (Exception e) {
            plugin.getLog().error(
                    "An error has occured while checking if player {} is registered",
                    player.getName());
            return;
        }
        
        if (!isRegistered && !autoRegisterFloodgate) {
            plugin.getLog().info(
                    "Auto registration is disabled for Floodgate players in config.yml");
            return;
        }
        
        // logging in from bedrock for a second time threw an error with UUID
        StoredProfile profile = plugin.getCore().getStorage().loadProfile(player.getName());
        if (profile == null) {
            profile = new StoredProfile(player.getUniqueId(), player.getName(), true, player.getAddress().toString());
        }

        BukkitLoginSession session = new BukkitLoginSession(player.getName(), isRegistered, profile);
        
        // enable auto login based on the value of 'autoLoginFloodgate' in config.yml
        session.setVerified(autoLoginFloodgate.equalsIgnoreCase("true")
                || (autoLoginFloodgate.equalsIgnoreCase("linked") && isLinked));

        // run login task
        Runnable forceLoginTask = new ForceLoginTask(plugin.getCore(), player, session);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, forceLoginTask);
    }

}
