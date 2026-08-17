package fr.cubeweb.vertifeed;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface LevelRendererSwap {
	RenderTarget vertifeed$getOutlineTarget();

	void vertifeed$setOutlineTarget(RenderTarget target);
}
