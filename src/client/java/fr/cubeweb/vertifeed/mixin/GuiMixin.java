package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class, remap = false)
public class GuiMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/client/DeltaTracker;ZZ)V", at = @At("HEAD"), cancellable = true)
	private void vertifeed$skipDuringWorldPass(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci) {
		if (VerticalFeed.skipGuiExtract()) {
			ci.cancel();
		}
	}
}
