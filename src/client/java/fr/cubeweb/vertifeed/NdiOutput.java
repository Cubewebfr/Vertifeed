package fr.cubeweb.vertifeed;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import me.walkerknapp.devolay.Devolay;
import me.walkerknapp.devolay.DevolayFrameFormatType;
import me.walkerknapp.devolay.DevolayFrameFourCCType;
import me.walkerknapp.devolay.DevolaySender;
import me.walkerknapp.devolay.DevolayVideoFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public class NdiOutput implements AutoCloseable {
	private final AtomicBoolean captureInFlight = new AtomicBoolean();
	private DevolaySender sender;
	private DevolayVideoFrame videoFrame;
	private ByteBuffer pixels;
	private String sourceName;
	private int width;
	private int height;
	private boolean available;

	public NdiOutput() {
		try {
			Devolay.loadLibraries();
			this.available = true;
		} catch (Throwable t) {
			Vertifeed.LOGGER.error("NDI runtime is missing. Install it from https://ndi.link/NDIRedistV6 and restart Minecraft.", t);
			this.available = false;
		}
	}

	public boolean isAvailable() {
		return this.available;
	}

	public synchronized void ensureSender(String sourceName, int width, int height) {
		if (!this.available) {
			return;
		}

		if (this.sender != null && sourceName.equals(this.sourceName) && this.width == width && this.height == height) {
			return;
		}

		closeSender();
		this.sourceName = sourceName;
		this.width = width;
		this.height = height;
		this.sender = new DevolaySender(sourceName, null, false, false);
		this.videoFrame = new DevolayVideoFrame();
		this.videoFrame.setResolution(width, height);
		this.videoFrame.setFourCCType(DevolayFrameFourCCType.BGRA);
		this.videoFrame.setFormatType(DevolayFrameFormatType.PROGRESSIVE);
		this.videoFrame.setFrameRate(60, 1);
		this.videoFrame.setAspectRatio((float) width / (float) height);
		this.videoFrame.setLineStride(width * 4);
		this.pixels = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN);
		Vertifeed.LOGGER.info("NDI sender ready: {} ({}x{})", sourceName, width, height);
	}

	public void captureVertical(RenderTarget target, Minecraft minecraft, VertifeedConfig config) {
		if (!this.available || this.sender == null || !this.captureInFlight.compareAndSet(false, true)) {
			return;
		}

		try {
			Screenshot.takeScreenshot(target, image -> onImage(image, minecraft, config, false));
		} catch (Throwable t) {
			this.captureInFlight.set(false);
			Vertifeed.LOGGER.warn("Failed to start vertical NDI capture", t);
		}
	}

	public void captureCropped(RenderTarget target, Minecraft minecraft, VertifeedConfig config) {
		if (!this.available || this.sender == null || !this.captureInFlight.compareAndSet(false, true)) {
			return;
		}

		try {
			Screenshot.takeScreenshot(target, image -> onImage(image, minecraft, config, true));
		} catch (Throwable t) {
			this.captureInFlight.set(false);
			Vertifeed.LOGGER.warn("Failed to start vertical NDI capture", t);
		}
	}

	private void onImage(NativeImage image, Minecraft minecraft, VertifeedConfig config, boolean crop) {
		try (image) {
			sendFrame(image, minecraft, config, crop);
		} catch (Throwable t) {
			Vertifeed.LOGGER.warn("Failed to send vertical NDI frame", t);
		} finally {
			this.captureInFlight.set(false);
		}
	}

	private synchronized void sendFrame(NativeImage image, Minecraft minecraft, VertifeedConfig config, boolean crop) {
		if (this.sender == null || this.pixels == null) {
			return;
		}

		VerticalComposer.send(this.pixels, this.width, this.height, image.getPixelsABGR(), image.getWidth(), image.getHeight(), minecraft, config, crop);
		this.videoFrame.setData(this.pixels);
		this.sender.sendVideoFrameAsync(this.videoFrame);
	}

	private void closeSender() {
		if (this.videoFrame != null) {
			this.sender.sendVideoFrameAsync(null);
			this.videoFrame.close();
			this.videoFrame = null;
		}

		if (this.sender != null) {
			this.sender.close();
			this.sender = null;
		}

		this.pixels = null;
	}

	@Override
	public synchronized void close() {
		closeSender();
	}
}
