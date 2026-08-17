package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.VertifeedSettingsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PauseScreen.class, remap = false)
public abstract class PauseScreenMixin extends Screen {
	@Shadow
	private boolean showPauseMenu;

	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void vertifeed$addSettingsButton(CallbackInfo ci) {
		if (!this.showPauseMenu) {
			return;
		}

		this.addRenderableWidget(Button.builder(Component.translatable("menu.vertifeed"), button ->
			this.minecraft.gui.setScreen(new VertifeedSettingsScreen(this))
		).bounds(8, 8, 98, 20).build());
	}
}
