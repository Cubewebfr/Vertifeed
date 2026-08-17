package fr.cubeweb.vertifeed.mixin;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.cubeweb.vertifeed.GameRendererSwap;
import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = GameRenderer.class, remap = false)
public abstract class GameRendererMixin implements GameRendererSwap {
	@Shadow
	@Final
	@Mutable
	private RenderTarget mainRenderTarget;

	@Shadow
	@Final
	private GlobalSettingsUniform globalSettingsUniform;

	@Shadow
	@Final
	private GuiRenderer guiRenderer;

	@Shadow
	@Final
	public Camera mainCamera;

	@Shadow
	@Final
	private Projection hudProjection;

	@Shadow
	@Final
	private ProjectionMatrixBuffer hud3dProjectionMatrixBuffer;

	@Shadow
	public abstract GameRenderState gameRenderState();

	@Invoker("renderItemInHand")
	abstract void vertifeed$invokeRenderItemInHand(CameraRenderState cameraState, float deltaPartialTick, Matrix4fc modelViewMatrix);

	@Override
	public RenderTarget vertifeed$getMainTarget() {
		return this.mainRenderTarget;
	}

	@Override
	public void vertifeed$setMainTarget(RenderTarget target) {
		this.mainRenderTarget = target;
	}

	@Override
	public GlobalSettingsUniform vertifeed$uniforms() {
		return this.globalSettingsUniform;
	}

	@Override
	public GuiRenderer vertifeed$guiRenderer() {
		return this.guiRenderer;
	}

	@Override
	public void vertifeed$drawHand(DeltaTracker deltaTracker) {
		GameRenderState renderState = this.gameRenderState();
		CameraRenderState cameraState = renderState.levelRenderState.cameraRenderState;
		if (cameraState.hudFov <= 0.01F) {
			cameraState.hudFov = 70.0F;
		}

		boolean wasHidden = renderState.guiRenderState.isHudHidden;
		renderState.guiRenderState.isHudHidden = false;
		try {
			this.hudProjection.setupPerspective(
				0.05F,
				100.0F,
				cameraState.hudFov,
				renderState.windowRenderState.width,
				renderState.windowRenderState.height
			);
			RenderSystem.setProjectionMatrix(this.hud3dProjectionMatrixBuffer.getBuffer(this.hudProjection), ProjectionType.PERSPECTIVE);
			RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.mainRenderTarget.getDepthTexture(), 0.0);
			this.vertifeed$invokeRenderItemInHand(
				cameraState,
				this.mainCamera.getCameraEntityPartialTicks(deltaTracker),
				cameraState.viewRotationMatrix
			);
		} finally {
			renderState.guiRenderState.isHudHidden = wasHidden;
		}
	}

	@Inject(
		method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER)
	)
	private void vertifeed$afterMainLevel(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		VerticalFeed.afterMainLevelRendered(Minecraft.getInstance(), deltaTracker);
	}

	@Inject(
		method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V")
	)
	private void vertifeed$beforeGui(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		VerticalFeed.beforeGuiRendered(Minecraft.getInstance(), deltaTracker);
	}

	@Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("RETURN"))
	private void vertifeed$afterMainRender(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		VerticalFeed.afterMainFramePresented(Minecraft.getInstance());
	}
}
