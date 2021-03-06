package me.frep.thotpatrol.checks.combat.criticals;

import me.frep.thotpatrol.ThotPatrol;
import me.frep.thotpatrol.checks.Check;
import me.frep.thotpatrol.packets.events.PacketAttackEvent;
import me.frep.thotpatrol.utils.UtilCheat;
import me.frep.thotpatrol.utils.UtilMath;
import me.frep.thotpatrol.utils.UtilPlayer;
import me.frep.thotpatrol.utils.UtilTime;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CriticalsC extends Check {

    private Map<UUID, Long> lastAttack = new HashMap<>();
    private Map<UUID, Integer> verbose = new HashMap<>();

    public CriticalsC(ThotPatrol ThotPatrol) {
        super("CriticalsC", "Criticals (Type C)", ThotPatrol);
        setEnabled(true);
        setBannable(true);
        setMaxViolations(5);
    }

    //todo i dont even know if this works anymore, but if it does then it should catch most criticals

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        double deltaY = Math.abs(UtilMath.getVerticalDistance(e.getTo(), e.getFrom()));
        int count = verbose.getOrDefault(p.getUniqueId(), 0);
        int ping = getThotPatrol().getLag().getPing(p);
        double tps = getThotPatrol().getLag().getTPS();
        if (p.getAllowFlight()
            || p.hasPotionEffect(PotionEffectType.JUMP)
            || UtilCheat.slabsNear(p.getLocation())
            || UtilTime.elapsed(lastAttack.getOrDefault(p.getUniqueId(), 0L), 100)
            || !UtilTime.elapsed(getThotPatrol().lastDamage.getOrDefault(p.getUniqueId(), 0L), 2000)
            || e.getPlayer().getEyeLocation().clone().add(0, .5, 0).getBlock().getType().isSolid()
            || e.getPlayer().getEyeLocation().clone().add(0, 1, 0).getBlock().getType().isSolid()) {
            return;
        }
        if (p.isOnGround() && UtilPlayer.isOnGround(p) && deltaY > 0 && deltaY < .15) {
            count++;
        }
        if (count > 7) {
            count = 0;
            getThotPatrol().logCheat(this, p, "Packet | Ping: " + ping + " | TPS: " + tps);
            getThotPatrol().logToFile(p, this, "Packet", "DeltaY: " + deltaY + " | TPS: " + tps + " | Ping: " + ping);
        }
        verbose.put(p.getUniqueId(), count);
    }

    @EventHandler
    public void onAttack(PacketAttackEvent e) {
        if (e.getEntity() instanceof Player) {
            lastAttack.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }
}