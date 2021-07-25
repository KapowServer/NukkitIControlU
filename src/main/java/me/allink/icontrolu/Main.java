package me.allink.icontrolu;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;
import me.allink.icontrolu.commands.CommandIcu;
import me.allink.icontrolu.utilities.PlayerList;

import java.util.Timer;

public class Main extends PluginBase {
    private static Main INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        /* Commands */
        ((PluginCommand<?>)this.getCommand("icu")).setExecutor(new CommandIcu());

        Timer tickTimer = new Timer();
        tickTimer.schedule(new Tick(), 0, 50);
        this.getServer().getPluginManager().registerEvents(new ControlPlayer(), this);
    }

    @Override
    public void onDisable() {
        for (Player controller: Server.getInstance().getOnlinePlayers().values()) {
            final Player target = PlayerList.getTarget(controller.getUniqueId());

            if (target != null) {
                for (Player player: Server.getInstance().getOnlinePlayers().values()) {
                    player.showPlayer(controller);
                }

                controller.removeEffect(Effect.INVISIBILITY);
                controller.sendMessage("You are no longer controlling \"" + target.getName() + "\" due to server reload");
            }
        }
    }

    public static Main getInstance() {
        return INSTANCE;
    }
}
