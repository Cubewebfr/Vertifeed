package fr.cubeweb.vertifeed;

public interface RecipeBookScreenAccess {
	void vertifeed$layoutBook(int width, int height, boolean widthTooNarrow);

	boolean vertifeed$widthTooNarrow();

	boolean vertifeed$isSearchFocused();

	void vertifeed$setSearchFocused(boolean focused);
}
