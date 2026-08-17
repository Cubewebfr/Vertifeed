package fr.cubeweb.vertifeed.mixin;

import com.mojang.blaze3d.platform.Window;
import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiGraphicsExtractor.class, remap = false)
public class GuiGraphicsExtractorMixin {
	@Inject(method = "applyCursor", at = @At("HEAD"), cancellable = true)
	private void vertifeed$keepGameCursor(Window window, CallbackInfo ci) {
		if (VerticalFeed.isPassing()) {
			ci.cancel();
		}
	}
}
