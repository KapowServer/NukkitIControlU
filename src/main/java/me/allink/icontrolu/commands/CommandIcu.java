package me.allink.icontrolu.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;
import me.allink.icontrolu.utilities.PlayerList;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class CommandIcu implements CommandExecutor {
    private void controlCommand(final Player controller, final String label, final String[] args) {
        if (args.length == 1) {
            controller.sendMessage(TextFormat.RED + "Usage: /" + label + " control <player>");
        } else {
            Player target = Server.getInstance().getPlayer(args[1]);

            if (target == null && args[1].matches("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
                Optional<Player> hypotheticalTarget = Server.getInstance().getPlayer(UUID.fromString(args[1]));

                if(hypotheticalTarget.isPresent()) {
                    target = hypotheticalTarget.get();
                }
            }

            if (target != null) {
                if (target == controller) {
                    controller.sendMessage("You are already controlling yourself");
                } else if (PlayerList.getTarget(controller.getUniqueId()) != null) {
                    controller.sendMessage("You are already controlling \"" + target.getName() + "\"");
                } else if (PlayerList.getController(target.getUniqueId()) != null) {
                    controller.sendMessage("Player \"" + target.getName() + "\" is already being controlled");
                } else if (PlayerList.getTarget(target.getUniqueId()) != null) {
                    controller.sendMessage("Player \"" + target.getName() + "\" is already controlling another player");
                } else if (!controller.canSee(target)) {
                    controller.sendMessage("You may not control this player");
                } else {
                    controller.teleport(target.getLocation());

                    controller.getInventory().setContents(target.getInventory().getContents());

                    PlayerList.setTarget(controller.getUniqueId(), target);
                    PlayerList.setController(target.getUniqueId(), controller);

                    controller.sendMessage("You are now controlling \"" + target.getName() + "\"");
                }
            } else {
                controller.sendMessage("Player \"" + args[1] + "\" not found");
            }
        }
    }

    private void stopCommand(final Player controller) {
        final Player target = PlayerList.getTarget(controller.getUniqueId());

        if (target != null) {
            target.teleportImmediate(controller.getLocation().add(0, target.getHeight()));

            PlayerList.removeTarget(controller.getUniqueId());
            PlayerList.removeController(target.getUniqueId());

            Timer t = new Timer();

            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    for (Player player: Server.getInstance().getOnlinePlayers().values()) {
                        player.showPlayer(controller);
                    }

                    controller.removeEffect(Effect.INVISIBILITY);
                    controller.sendMessage("You are now visible");
                }
            }, 10000);

            controller.sendMessage("You are no longer controlling \"" + target.getName() + "\". You are invisible for 10 seconds.");
        } else {
            controller.sendMessage("You are not controlling anyone at the moment");
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Command has to be run by a player");
            return true;
        }

        final Player controller = (Player) sender;

        if (args.length == 0) {
            controller.sendMessage(TextFormat.RED + "Usage: /" + label + " <control|stop>");
        } else if (args[0].equalsIgnoreCase("control")) {
            controlCommand(controller, label, args);
        } else if (args[0].equalsIgnoreCase("stop")) {
            stopCommand(controller);
        }
        return true;
    }
}
