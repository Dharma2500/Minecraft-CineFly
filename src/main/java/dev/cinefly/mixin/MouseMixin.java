package dev.cinefly.mixin;

import dev.cinefly.CineFlyClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Redirect(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;changeLookDirection(DD)V"
            )
    )
    private void cinefly$redirectMouseLook(Entity originalEntity, double deltaX, double deltaY) {
        if (CineFlyClient.isActive()) {
            CineFlyClient.rotateCamera(deltaX, deltaY);
        } else {
            originalEntity.changeLookDirection(deltaX, deltaY);
        }
    }
}
