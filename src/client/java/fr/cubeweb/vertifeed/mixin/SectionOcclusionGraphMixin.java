package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.VerticalFeed;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ChunkLoadingRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SectionOcclusionGraph.class, remap = false)
public class SectionOcclusionGraphMixin {
	@Inject(
		method = "update(Lnet/minecraft/client/renderer/state/level/CameraRenderState;ILnet/minecraft/client/renderer/state/level/ChunkLoadingRenderState;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void vertifeed$skipOcclusion(CameraRenderState camera, int fov, ChunkLoadingRenderState chunkLoadingRenderState, CallbackInfo ci) {
		if (VerticalFeed.isPassing()) {
			ci.cancel();
		}
	}
}
