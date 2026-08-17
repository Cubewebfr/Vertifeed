package fr.cubeweb.vertifeed;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GlobalSettingsUniform;

public interface GameRendererSwap {
	RenderTarget vertifeed$getMainTarget();

	void vertifeed$setMainTarget(RenderTarget target);

	GlobalSettingsUniform vertifeed$uniforms();

	GuiRenderer vertifeed$guiRenderer();

	void vertifeed$drawHand(DeltaTracker deltaTracker);
}
