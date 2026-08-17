package fr.cubeweb.vertifeed;

public interface RecipeBookAccess {
	void vertifeed$reposition(int width, int height, boolean widthTooNarrow);

	boolean vertifeed$isSearchFocused();

	void vertifeed$setSearchFocused(boolean focused);
}
