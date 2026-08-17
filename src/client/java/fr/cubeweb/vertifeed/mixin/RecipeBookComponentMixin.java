package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.RecipeBookAccess;
import fr.cubeweb.vertifeed.RecipeBookPageAccess;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = RecipeBookComponent.class, remap = false)
public class RecipeBookComponentMixin implements RecipeBookAccess {
	@Shadow
	private int width;

	@Shadow
	private int height;

	@Shadow
	private int xOffset;

	@Shadow
	private boolean widthTooNarrow;

	@Shadow
	private EditBox searchBox;

	@Shadow
	protected CycleButton<Boolean> filterButton;

	@Shadow
	@Final
	private RecipeBookPage recipeBookPage;

	@Shadow
	@Final
	private List<RecipeBookTabButton> tabButtons;

	@Shadow
	private ScreenRectangle magnifierIconPlacement;

	@Override
	public void vertifeed$reposition(int width, int height, boolean widthTooNarrow) {
		int oldXo = (this.width - 147) / 2 - this.xOffset;
		int oldYo = (this.height - 166) / 2;
		this.width = width;
		this.height = height;
		this.widthTooNarrow = widthTooNarrow;
		this.xOffset = widthTooNarrow ? 0 : 86;
		int xo = (this.width - 147) / 2 - this.xOffset;
		int yo = (this.height - 166) / 2;
		if (this.searchBox != null) {
			this.searchBox.setPosition(xo + 25, yo + 13);
			this.magnifierIconPlacement = ScreenRectangle.of(
				ScreenAxis.HORIZONTAL, xo + 8, this.searchBox.getY(), this.searchBox.getX() - xo, this.searchBox.getHeight()
			);
		}
		if (this.filterButton != null) {
			this.filterButton.setPosition(xo + 110, yo + 12);
		}
		if (this.recipeBookPage != null) {
			RecipeBookPageAccess page = (RecipeBookPageAccess) this.recipeBookPage;
			page.vertifeed$reposition(xo, yo);
			page.vertifeed$offsetOverlay(xo - oldXo, yo - oldYo);
		}

		int tabX = xo - 30;
		int tabY = yo + 3;
		int index = 0;
		for (RecipeBookTabButton tab : this.tabButtons) {
			if (tab.visible) {
				tab.setPosition(tabX, tabY + 27 * index++);
			}
		}
	}

	@Override
	public boolean vertifeed$isSearchFocused() {
		return this.searchBox != null && this.searchBox.isFocused();
	}

	@Override
	public void vertifeed$setSearchFocused(boolean focused) {
		if (this.searchBox != null) {
			this.searchBox.setFocused(focused);
		}
	}
}
