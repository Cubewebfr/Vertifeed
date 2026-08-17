package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.GameModeSwitcherAccess;
import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GameModeSwitcherScreen.class, remap = false)
public class GameModeSwitcherScreenMixin implements GameModeSwitcherAccess {
	@Shadow
	private List<GameModeSwitcherScreen.GameModeSlot> slots;

	@Shadow
	private int firstMouseX;

	@Shadow
	private int firstMouseY;

	@Shadow
	private boolean setFirstMousePos;

	@Override
	public void vertifeed$layout(int width, int height) {
		int allWidth = this.slots.size() * 31 - 5;
		for (int i = 0; i < this.slots.size(); i++) {
			this.slots.get(i).setPosition(width / 2 - allWidth / 2 + i * 31, height / 2 - 31);
		}
	}

	@Override
	public boolean vertifeed$overflows(int width, int height) {
		for (GameModeSwitcherScreen.GameModeSlot slot : this.slots) {
			if (slot.getX() < 0 || slot.getY() < 0 || slot.getX() + slot.getWidth() > width || slot.getY() + slot.getHeight() > height) {
				return true;
			}
		}
		return false;
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void vertifeed$keepSelection(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (VerticalFeed.isPassing()) {
			this.firstMouseX = mouseX;
			this.firstMouseY = mouseY;
			this.setFirstMousePos = true;
		}
	}
}
