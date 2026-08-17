package fr.cubeweb.vertifeed.mixin;

import com.mojang.blaze3d.platform.Window;
import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MouseHandler.class, remap = false)
public class MouseHandlerMixin {
	@Inject(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("HEAD"), cancellable = true)
	private static void vertifeed$guiMouseX(Window window, double x, CallbackInfoReturnable<Double> cir) {
		if (VerticalFeed.isPassing() && VerticalFeed.hasMouseOverride()) {
			cir.setReturnValue(VerticalFeed.overrideMouseX());
			return;
		}
		if (VerticalFeed.isPassing() && VerticalFeed.remapGuiMouse()) {
			cir.setReturnValue(VerticalFeed.guiMouseX(x));
		}
	}

	@Inject(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("HEAD"), cancellable = true)
	private static void vertifeed$guiMouseY(Window window, double y, CallbackInfoReturnable<Double> cir) {
		if (VerticalFeed.isPassing() && VerticalFeed.hasMouseOverride()) {
			cir.setReturnValue(VerticalFeed.overrideMouseY());
			return;
		}
		if (VerticalFeed.isPassing() && VerticalFeed.remapGuiMouse()) {
			cir.setReturnValue(VerticalFeed.guiMouseY(y));
		}
	}
}
