package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.OverlayRecipeAccess;
import fr.cubeweb.vertifeed.RecipeBookPageAccess;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = RecipeBookPage.class, remap = false)
public class RecipeBookPageMixin implements RecipeBookPageAccess {
	@Shadow
	@Final
	private List<RecipeButton> buttons;

	@Shadow
	private ImageButton forwardButton;

	@Shadow
	private ImageButton backButton;

	@Shadow
	@Final
	private OverlayRecipeComponent overlay;

	@Override
	public void vertifeed$reposition(int xo, int yo) {
		for (int i = 0; i < this.buttons.size(); i++) {
			this.buttons.get(i).setPosition(xo + 11 + 25 * (i % 5), yo + 31 + 25 * (i / 5));
		}
		if (this.forwardButton != null) {
			this.forwardButton.setPosition(xo + 93, yo + 137);
		}
		if (this.backButton != null) {
			this.backButton.setPosition(xo + 38, yo + 137);
		}
	}

	@Override
	public void vertifeed$offsetOverlay(int dx, int dy) {
		if (this.overlay != null) {
			((OverlayRecipeAccess) this.overlay).vertifeed$offset(dx, dy);
		}
	}
}
