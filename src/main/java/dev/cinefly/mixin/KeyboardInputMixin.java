package dev.cinefly.mixin;

import dev.cinefly.CineFlyClient;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Shadow public float movementForward;
    @Shadow public float movementSideways;
    @Shadow public boolean jumping;
    @Shadow public boolean sneaking;

    @Inject(method = "tick", at = @At("TAIL"))
    private void cinefly$freezePlayerMovement(CallbackInfo ci) {
        if (!CineFlyClient.isActive()) return;
        movementForward = 0.0F;
        movementSideways = 0.0F;
        jumping = false;
        sneaking = false;
    }
}
