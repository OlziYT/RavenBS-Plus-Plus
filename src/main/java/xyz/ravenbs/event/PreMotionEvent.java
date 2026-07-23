package xyz.ravenbs.event;

public class PreMotionEvent {
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private boolean sprinting;
    private boolean sneaking;
    private boolean cancelled;
    
    // Static flag for render overrides
    private static boolean setRenderYaw = false;

    public PreMotionEvent(double posX, double posY, double posZ, float yaw, float pitch, boolean onGround, boolean sprinting, boolean sneaking) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.sprinting = sprinting;
        this.sneaking = sneaking;
    }

    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }

    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }

    public double getPosZ() { return posZ; }
    public void setPosZ(double posZ) { this.posZ = posZ; }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }

    public boolean isSprinting() { return sprinting; }
    public void setSprinting(boolean sprinting) { this.sprinting = sprinting; }

    public boolean isSneaking() { return sneaking; }
    public void setSneaking(boolean sneaking) { this.sneaking = sneaking; }
    
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public static boolean setRenderYaw() {
        return setRenderYaw;
    }

    public static void setRenderYaw(boolean value) {
        setRenderYaw = value;
    }
}
