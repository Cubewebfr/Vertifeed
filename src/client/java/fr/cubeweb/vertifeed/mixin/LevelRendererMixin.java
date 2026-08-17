package fr.cubeweb.vertifeed.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import fr.cubeweb.vertifeed.LevelRendererSwap;
import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, remap = false)
public class LevelRendererMixin implements LevelRendererSwap {
	@Shadow
	@Final
	@Mutable
	private RenderTarget entityOutlineTarget;

	@Override
	public RenderTarget vertifeed$getOutlineTarget() {
		return this.entityOutlineTarget;
	}

	@Override
	public void vertifeed$setOutlineTarget(RenderTarget target) {
		this.entityOutlineTarget = target;
	}

	@Inject(method = "submitFeatures", at = @At("HEAD"))
	private void vertifeed$replayEntities(LevelRenderState state, SubmitNodeCollector collector, boolean renderOutline, CallbackInfo ci) {
		if (VerticalFeed.isPassing()) {
			VerticalFeed.restoreFeatures(state);
		} else if (VerticalFeed.isEnabled()) {
			VerticalFeed.snapshotFeatures(state);
		}
	}

	@Inject(method = "compileSections", at = @At("HEAD"), cancellable = true)
	private void vertifeed$skipCompile(CameraRenderState camera, CallbackInfo ci) {
		if (VerticalFeed.isPassing()) {
			ci.cancel();
		}
	}

	@Inject(method = "resize", at = @At("HEAD"), cancellable = true)
	private void vertifeed$skipResize(int width, int height, CallbackInfo ci) {
		if (VerticalFeed.isPassing()) {
			ci.cancel();
		}
	}
}
