package me.allink.icontrolu;


import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;
import me.allink.icontrolu.utilities.PlayerList;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class Tick extends TimerTask {
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        for (Player target: Server.getInstance().getOnlinePlayers().values()) {
            final Player controller = PlayerList.getController(target.getUniqueId());

            if (controller != null) {
                for (int i = 0; i < controller.getInventory().getSize(); i++) {
                    if (controller.getInventory().getItem(i) != null) {
                        if (!controller.getInventory().getItem(i).equals(target.getInventory().getItem(i))) {
                            target.getInventory().setItem(i, controller.getInventory().getItem(i));
                        }
                    } else {
                        target.getInventory().setItem(i, null);
                    }
                }

                if (target.getHealth() > 0) {
                    target.teleportImmediate(controller.getLocation().subtract(0,controller.getHeight() - 0.2));
                }

                target.setAllowFlight(controller.getAllowFlight());
                target.getFoodData().setLevel(controller.getFoodData().getLevel());

                if (controller.getMaxHealth() > 0) {
                    target.setMaxHealth(controller.getMaxHealth());
                    target.setHealth(controller.getHealth());
                }

                target.setLevel(controller.getLevel());
                target.setSneaking(controller.isSneaking());
                target.setSprinting(controller.isSprinting());

                for (Player player: Server.getInstance().getOnlinePlayers().values()) {
                    player.hidePlayer(controller);
                }

                final int duration = 99999;
                final int amplifier = 0;
                final boolean ambient = false;
                final boolean particles = false;

                Effect effect = Effect.getEffect(Effect.INVISIBILITY);
                effect.setDuration(duration);
                effect.setAmplifier(amplifier);
                effect.setAmbient(ambient);
                effect.setVisible(particles);

                controller.addEffect(effect);
            }
        }
    }
}

class ControlPlayer implements Listener {
    @EventHandler
    private void onPlayerChatEvent(final PlayerChatEvent event) {
        final Player player = event.getPlayer();

        if (PlayerList.getController(player.getUniqueId()) != null) {
            if (event.getMessage().startsWith("§iControlUChat§")) {
                final int prefixLength = "§iControlUChat§".length();

                event.setMessage(
                        event.getMessage().substring(prefixLength)
                );
            } else {
                event.setCancelled(true);
            }

        } else if (PlayerList.getTarget(player.getUniqueId()) != null) {
            final Player target = PlayerList.getTarget(player.getUniqueId());

            target.chat("§iControlUChat§" + event.getMessage());  // Add prefix to prevent messages from being cancelled

            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onEntityDamage(final EntityDamageEvent event) {
        final Entity playerEntity = event.getEntity();
        final Player playerExact = Server.getInstance().getPlayerExact(playerEntity.getName());

        if(playerExact != null) {
            if (PlayerList.getTarget(playerExact.getUniqueId()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onPlayerAnimation(final PlayerAnimationEvent event) {
        final Player player = event.getPlayer();

        if (PlayerList.getController(player.getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();

        if (PlayerList.getController(player.getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();

        if (PlayerList.getController(player.getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (PlayerList.getController(player.getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        final Player player = event.getPlayer();

        if(player != null) {
            if(PlayerList.getController(player.getUniqueId()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        final Player player = event.getPlayer();

        if(player != null) {
            if(PlayerList.getController(player.getUniqueId()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        if (PlayerList.getController(player.getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        for (Player otherPlayer: Server.getInstance().getOnlinePlayers().values()) {
            if (PlayerList.getController(player.getUniqueId()) != null
                    && PlayerList.getController(player.getUniqueId()).equals(otherPlayer)) {
				/*
				  Target disconnects
				  */
                PlayerList.removeTarget(otherPlayer.getUniqueId());
                PlayerList.removeController(player.getUniqueId());

                final Player controller = otherPlayer;
                final int tickDelay = 200;

                Timer t = new Timer();

                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        for (Player allPlayers: Server.getInstance().getOnlinePlayers().values()) {
                            allPlayers.showPlayer(controller);
                        }

                        controller.removeEffect(Effect.INVISIBILITY);
                        controller.sendMessage("You are now visible");
                    }
                }, 10000);

                otherPlayer.sendMessage("The player you were controlling has disconnected. You are invisible for 10 seconds.");

            } else if (PlayerList.getTarget(player.getUniqueId()) != null
                    && PlayerList.getTarget(player.getUniqueId()).equals(otherPlayer)) {
				/*
				  Controller disconnects
				  */
                PlayerList.removeTarget(player.getUniqueId());
                PlayerList.removeController(otherPlayer.getUniqueId());
            }
        }
    }

    @EventHandler
    private void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();

        if (PlayerList.getController(player.getUniqueId()) != null) {
            final Player controller = PlayerList.getController(player.getUniqueId());

            controller.teleport(player.getLocation());
        }
    }
}

