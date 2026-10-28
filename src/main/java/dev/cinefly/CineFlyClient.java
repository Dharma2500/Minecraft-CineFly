package dev.cinefly;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/** Client-only cinematic camera. The actual player is never moved by CineFly. */
public final class CineFlyClient implements ClientModInitializer {
    private static final double START_DISTANCE = 4.2;
    private static final double START_HEIGHT = 2.0;
    private static final double MOVE_SPEED = 0.24;
    private static final double FAST_MULTIPLIER = 3.0;
    private static final double SMOOTHING = 0.18;
    private static final float START_PITCH = 8.0f;

    private static KeyBinding toggleKey;
    private static boolean active;
    private static boolean previousHudHidden;
    private static Perspective previousPerspective;
    private static Entity previousCamera;
    private static ArmorStandEntity cameraAnchor;
    private static Vec3d cameraPosition = Vec3d.ZERO;
    private static Vec3d targetPosition = Vec3d.ZERO;
    private static float cameraYaw;
    private static float cameraPitch;
    private static float targetYaw;
    private static float targetPitch;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cinefly.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.create(Identifier.of("cinefly", "general"))
        ));
        ClientTickEvents.END_CLIENT_TICK.register(CineFlyClient::tick);
    }

    private static void tick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            if (active) stop(client);
            else start(client);
        }
        if (!active) return;

        if (client.player == null || client.world == null || cameraAnchor == null) {
            stop(client);
            return;
        }

        updateTargetFromInput(client);
        cameraPosition = cameraPosition.lerp(targetPosition, SMOOTHING);
        cameraYaw += wrapDegrees(targetYaw - cameraYaw) * (float) SMOOTHING;
        cameraPitch += (targetPitch - cameraPitch) * (float) SMOOTHING;

        // Armor stands use their feet as the entity position; offset so the camera
        // eye sits at the requested cinematic camera coordinate.
        cameraAnchor.setPosition(cameraPosition.x, cameraPosition.y - cameraAnchor.getStandingEyeHeight(), cameraPosition.z);
        cameraAnchor.setYaw(cameraYaw);
        cameraAnchor.setPitch(cameraPitch);
        cameraAnchor.setHeadYaw(cameraYaw);
    }

    private static void start(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        previousCamera = client.getCameraEntity();
        previousHudHidden = client.options.hudHidden;
        previousPerspective = client.options.getPerspective();
        ClientWorld world = client.world;
        cameraAnchor = new ArmorStandEntity(EntityType.ARMOR_STAND, world);

        float playerYaw = client.player.getYaw();
        double yawRadians = Math.toRadians(playerYaw);
        Vec3d playerPos = client.player.getEntityPos();
        targetPosition = new Vec3d(
                playerPos.x + Math.sin(yawRadians) * START_DISTANCE,
                playerPos.y + START_HEIGHT,
                playerPos.z - Math.cos(yawRadians) * START_DISTANCE
        );
        // Start at the player's current eye position, then ease into the cinematic
        // framing instead of snapping the camera to its offset in one frame.
        cameraPosition = playerPos.add(0.0, client.player.getStandingEyeHeight(), 0.0);
        cameraYaw = playerYaw;
        targetYaw = playerYaw;
        cameraPitch = client.player.getPitch();
        targetPitch = START_PITCH;

        cameraAnchor.setPosition(cameraPosition.x, cameraPosition.y - cameraAnchor.getStandingEyeHeight(), cameraPosition.z);
        cameraAnchor.setYaw(cameraYaw);
        cameraAnchor.setPitch(cameraPitch);
        cameraAnchor.setHeadYaw(cameraYaw);

        active = true;
        // Prevent Minecraft from applying a second third-person camera offset.
        client.options.setPerspective(Perspective.FIRST_PERSON);
        client.options.hudHidden = true;
        client.setCameraEntity(cameraAnchor);
    }

    private static void stop(MinecraftClient client) {
        active = false;
        if (client != null) {
            client.options.hudHidden = previousHudHidden;
            if (previousPerspective != null) client.options.setPerspective(previousPerspective);
            Entity restore = client.player != null ? client.player : previousCamera;
            if (restore != null) client.setCameraEntity(restore);
        }
        cameraAnchor = null;
        previousCamera = null;
        previousPerspective = null;
    }

    private static void updateTargetFromInput(MinecraftClient client) {
        double yawRadians = Math.toRadians(targetYaw);
        Vec3d forward = new Vec3d(-Math.sin(yawRadians), 0, Math.cos(yawRadians));
        Vec3d right = new Vec3d(Math.cos(yawRadians), 0, Math.sin(yawRadians));
        Vec3d movement = Vec3d.ZERO;

        if (client.options.forwardKey.isPressed()) movement = movement.add(forward);
        if (client.options.backKey.isPressed()) movement = movement.subtract(forward);
        if (client.options.rightKey.isPressed()) movement = movement.add(right);
        if (client.options.leftKey.isPressed()) movement = movement.subtract(right);
        if (client.options.jumpKey.isPressed()) movement = movement.add(0, 1, 0);
        if (client.options.sneakKey.isPressed()) movement = movement.add(0, -1, 0);

        if (movement.lengthSquared() > 0) {
            double speed = MOVE_SPEED * (client.options.sprintKey.isPressed() ? FAST_MULTIPLIER : 1.0);
            targetPosition = targetPosition.add(movement.normalize().multiply(speed));
        }
    }

    /** Called by the mouse mixin while CineFly is active. */
    public static boolean isActive() {
        return active;
    }

    /** Applies Minecraft's mouse-look deltas to the independent camera. */
    public static void rotateCamera(double deltaX, double deltaY) {
        if (!active) return;
        // Mouse.updateMouse has already applied the user's sensitivity setting.
        targetYaw += (float) (deltaX * 0.15);
        targetPitch += (float) (deltaY * 0.15);
        targetPitch = MathHelper.clamp(targetPitch, -89.0f, 89.0f);
    }

    private static float wrapDegrees(float degrees) {
        return MathHelper.wrapDegrees(degrees);
    }
}
