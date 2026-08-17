package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {
	@Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
	private void vertifeed$windowTitle(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(VerticalFeed.brandTitle());
	}
}
