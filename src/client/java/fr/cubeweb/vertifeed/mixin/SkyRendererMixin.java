package fr.cubeweb.vertifeed.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SkyRenderer.class, remap = false)
public class SkyRendererMixin {
	@Redirect(
		method = "*",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/SkyRenderer;renderTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;",
			opcode = Opcodes.GETFIELD
		)
	)
	private RenderTarget vertifeed$currentTarget(SkyRenderer instance) {
		return Minecraft.getInstance().gameRenderer.mainRenderTarget();
	}
}
