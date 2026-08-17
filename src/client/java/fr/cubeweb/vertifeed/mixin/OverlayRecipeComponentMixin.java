package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.OverlayRecipeAccess;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = OverlayRecipeComponent.class, remap = false)
public class OverlayRecipeComponentMixin implements OverlayRecipeAccess {
	@Shadow
	private int x;

	@Shadow
	private int y;

	@Shadow
	private boolean isVisible;

	@Shadow
	@Final
	private List<?> recipeButtons;

	@Override
	public void vertifeed$offset(int dx, int dy) {
		if (!this.isVisible || dx == 0 && dy == 0) {
			return;
		}

		this.x += dx;
		this.y += dy;
		for (Object button : this.recipeButtons) {
			if (button instanceof AbstractWidget widget) {
				widget.setPosition(widget.getX() + dx, widget.getY() + dy);
			}
		}
	}
}
