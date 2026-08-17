package fr.cubeweb.vertifeed.mixin;

import com.mojang.blaze3d.platform.Window;
import fr.cubeweb.vertifeed.VerticalFeed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Window.class, remap = false)
public class WindowMixin {
	@Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
	private void vertifeed$passGuiWidth(CallbackInfoReturnable<Integer> cir) {
		if (VerticalFeed.isPassing()) {
			cir.setReturnValue(VerticalFeed.passGuiWidth());
		}
	}

	@Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
	private void vertifeed$passGuiHeight(CallbackInfoReturnable<Integer> cir) {
		if (VerticalFeed.isPassing()) {
			cir.setReturnValue(VerticalFeed.passGuiHeight());
		}
	}

	@Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true)
	private void vertifeed$passGuiScale(CallbackInfoReturnable<Integer> cir) {
		if (VerticalFeed.isPassing()) {
			cir.setReturnValue(VerticalFeed.passGuiScale());
		}
	}
}
