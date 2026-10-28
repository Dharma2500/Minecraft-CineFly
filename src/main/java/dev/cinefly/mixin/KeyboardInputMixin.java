package dev.cinefly.mixin;

import dev.cinefly.CineFlyClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Shadow public PlayerInput playerInput;

    @Inject(method = "tick", at = @At("TAIL"))
    private void cinefly$freezePlayerMovement(CallbackInfo ci) {
        if (CineFlyClient.isActive()) {
            playerInput = new PlayerInput(false, false, false, false, false, false, false);
        }
    }
}
