package xyz.ravenbs.module.impl.combat;

public class KillAuraTarget {
    public double distance;
    public float health;
    public int hurttime;
    public double yawDelta;
    public int entityId;
    public boolean isEnemy;

    public KillAuraTarget(double distance, float health, int hurttime, double yawDelta, int entityId, boolean isEnemy) {
        this.distance = distance;
        this.health = health;
        this.hurttime = hurttime;
        this.yawDelta = yawDelta;
        this.entityId = entityId;
        this.isEnemy = isEnemy;
    }
}
