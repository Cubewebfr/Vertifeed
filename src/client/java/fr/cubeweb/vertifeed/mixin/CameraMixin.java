package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Camera.class, remap = false)
public class CameraMixin {
	@Inject(method = "calculateFov", at = @At("HEAD"), cancellable = true)
	private void vertifeed$overrideFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
		if (VerticalFeed.isPassing() && VerticalFeed.config().overrideFov) {
			cir.setReturnValue(VerticalFeed.config().fov);
		}
	}
}
