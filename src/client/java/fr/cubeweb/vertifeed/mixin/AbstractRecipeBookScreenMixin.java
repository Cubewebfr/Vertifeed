package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.RecipeBookAccess;
import fr.cubeweb.vertifeed.RecipeBookScreenAccess;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AbstractRecipeBookScreen.class, remap = false)
public class AbstractRecipeBookScreenMixin implements RecipeBookScreenAccess {
	@Shadow
	@Final
	private RecipeBookComponent<?> recipeBookComponent;

	@Shadow
	private boolean widthTooNarrow;

	@Override
	public void vertifeed$layoutBook(int width, int height, boolean widthTooNarrow) {
		this.widthTooNarrow = widthTooNarrow;
		((RecipeBookAccess) this.recipeBookComponent).vertifeed$reposition(width, height, widthTooNarrow);
	}

	@Override
	public boolean vertifeed$widthTooNarrow() {
		return this.widthTooNarrow;
	}

	@Override
	public boolean vertifeed$isSearchFocused() {
		return ((RecipeBookAccess) this.recipeBookComponent).vertifeed$isSearchFocused();
	}

	@Override
	public void vertifeed$setSearchFocused(boolean focused) {
		((RecipeBookAccess) this.recipeBookComponent).vertifeed$setSearchFocused(focused);
	}
}
