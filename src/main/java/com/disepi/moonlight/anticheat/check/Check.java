package com.disepi.moonlight.anticheat.check;

import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import com.disepi.moonlight.anticheat.Moonlight;
import com.disepi.moonlight.anticheat.player.PlayerData;

public class Check {

    public String name, detection; // Nombre y descripción del chequeo
    public float maxViolationScale; // Límite de violaciones antes del castigo
    public int checkId; // ID único

    // Constructor
    public Check(String name, String detection, float maxViolationScale) {
        this.name = name;
        this.detection = detection;
        this.maxViolationScale = maxViolationScale;
        this.checkId = Moonlight.checkAmount++;
    }

    // ⚠️ Ya no envía mensajes al staff
    public void fail(Player p, String debug) {
        // Antes: enviaba aviso al staff con Moonlight.sendMessageToModerators()
        // Ahora: solo registra internamente, sin mostrar nada
        // Si quieres dejar trazas en consola para debug, descomenta esta línea:
        // System.out.println("[Moonlight] " + p.getName() + " failed " + this.name + " [" + debug + "]");
    }

    // Teletransporta al jugador a la última posición segura
    public void lagback(Player p, PlayerData d, Vector3 pos) {
        d.resetMove = true;
        d.teleportPos = pos;
        d.isTeleporting = true;
        p.teleport(pos);
    }

    public void lagback(Player p, PlayerData d) {
        this.lagback(p, d, d.lastGroundPos);
    }

    // Suma violación y evalúa castigo
    public void violate(Player player, PlayerData data, float amount, boolean punish) {
        data.violationMap[this.checkId] += amount;

        if (punish && getViolationScale(data) > this.maxViolationScale) {
            punish(player, data);
        }
    }

    // Reduce violaciones
    public void reward(PlayerData data, float amount) {
        data.violationMap[this.checkId] -= amount;
        if (data.violationMap[this.checkId] < 0)
            data.violationMap[this.checkId] = 0;
    }

    public float getViolationScale(PlayerData data) {
        return data.violationMap[this.checkId];
    }

    // Aplica castigo según tipo de hack
    public void punish(Player p, PlayerData d) {
        // Solo cancela Fly, Speed o Timer
        if (isMovementRelated(this.name)) {
            lagback(p, d);
            fail(p, "Cancelled movement: " + this.name);
        }
        // Los demás tipos (KillAura, AutoClicker, etc.) no hacen nada visible
    }

    // Verifica si el hack está relacionado con movimiento
    private boolean isMovementRelated(String name) {
        String lower = name.toLowerCase();
        return lower.contains("fly") || lower.contains("speed") || lower.contains("timer");
    }

    // Métodos sobrescribibles para las detecciones específicas
    public void check(MovePlayerPacket e, PlayerData d, Player p) {}
    public void check(EntityDamageByEntityEvent e, PlayerData d, Player p) {}
    public void check(PlayerActionPacket e, PlayerData d, Player p) {}
    public void check(InventoryTransactionPacket e, PlayerData d, Player p) {}
    public void check(MobEquipmentPacket e, PlayerData d, Player p) {}
}
