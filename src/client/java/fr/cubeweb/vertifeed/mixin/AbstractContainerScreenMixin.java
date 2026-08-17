package fr.cubeweb.vertifeed.mixin;

import fr.cubeweb.vertifeed.ContainerLayout;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public class AbstractContainerScreenMixin implements ContainerLayout {
	@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

	@Shadow
	@Final
	protected int imageWidth;

	@Shadow
	@Final
	protected int imageHeight;

	@Override
	public int vertifeed$left() {
		return this.leftPos;
	}

	@Override
	public int vertifeed$top() {
		return this.topPos;
	}

	@Override
	public int vertifeed$imageWidth() {
		return this.imageWidth;
	}

	@Override
	public int vertifeed$imageHeight() {
		return this.imageHeight;
	}

	@Override
	public void vertifeed$setLeft(int left) {
		this.leftPos = left;
	}

	@Override
	public void vertifeed$setTop(int top) {
		this.topPos = top;
	}
}
