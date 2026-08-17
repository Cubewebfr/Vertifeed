package fr.cubeweb.vertifeed;

import net.minecraft.client.Minecraft;

import java.nio.ByteBuffer;

final class VerticalComposer {
	private static final int CURSOR_OUTLINE = 0xFF000000;
	private static final int CURSOR_FILL = 0xFFFFFFFF;
	private static final byte[][] CURSOR = {
		{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0},
		{1, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0},
		{1, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0},
		{1, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0},
		{1, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0},
		{1, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0},
		{1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0},
		{1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 0},
		{1, 2, 2, 1, 2, 2, 1, 0, 0, 0, 0},
		{1, 2, 1, 0, 1, 2, 2, 1, 0, 0, 0},
		{1, 1, 0, 0, 1, 2, 2, 1, 0, 0, 0},
		{1, 0, 0, 0, 0, 1, 2, 2, 1, 0, 0},
		{0, 0, 0, 0, 0, 1, 2, 2, 1, 0, 0},
		{0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0}
	};

	private VerticalComposer() {
	}

	static void send(
		ByteBuffer out,
		int outW,
		int outH,
		int[] src,
		int srcW,
		int srcH,
		Minecraft minecraft,
		VertifeedConfig config,
		boolean crop
	) {
		int[] dest = new int[outW * outH];
		int sx = 0;
		int sy = 0;
		int sw = srcW;
		int sh = srcH;
		if (crop) {
			Crop box = cropBox(srcW, srcH);
			sx = box.x;
			sy = box.y;
			sw = box.w;
			sh = box.h;
		}
		blit(dest, outW, outH, src, srcW, srcH, sx, sy, sw, sh);

		if (config.drawCursor && !minecraft.mouseHandler.isMouseGrabbed()) {
			int x = VerticalFeed.cursorX(outW, minecraft.mouseHandler.xpos());
			int y = VerticalFeed.cursorY(outH, minecraft.mouseHandler.ypos());
			if (crop) {
				double mx = minecraft.mouseHandler.xpos() * srcW / Math.max(1, minecraft.getWindow().getScreenWidth());
				double my = minecraft.mouseHandler.ypos() * srcH / Math.max(1, minecraft.getWindow().getScreenHeight());
				x = (int) Math.round((mx - sx) * outW / (double) sw);
				y = (int) Math.round((my - sy) * outH / (double) sh);
			}
			int pixel = Math.max(1, Math.round(outH / 1920.0F));
			for (int row = 0; row < CURSOR.length; row++) {
				for (int col = 0; col < CURSOR[row].length; col++) {
					byte cell = CURSOR[row][col];
					if (cell == 0) {
						continue;
					}
					fillRect(dest, outW, outH, x + col * pixel, y + row * pixel, pixel, pixel, cell == 1 ? CURSOR_OUTLINE : CURSOR_FILL);
				}
			}
		}

		out.clear();
		for (int abgr : dest) {
			out.put((byte) (abgr >> 16));
			out.put((byte) (abgr >> 8));
			out.put((byte) abgr);
			out.put((byte) (abgr >> 24));
		}
		out.flip();
	}

	private static Crop cropBox(int srcW, int srcH) {
		int cropW = Math.max(1, srcH * 9 / 16);
		int cropH = srcH;
		if (cropW > srcW) {
			cropW = srcW;
			cropH = Math.max(1, srcW * 16 / 9);
		}
		return new Crop((srcW - cropW) / 2, (srcH - cropH) / 2, cropW, cropH);
	}

	private static void blit(int[] dest, int destW, int destH, int[] src, int srcW, int srcH, int sx, int sy, int sw, int sh) {
		for (int y = 0; y < destH; y++) {
			int srcY = sy + y * sh / destH;
			if (srcY < 0 || srcY >= srcH) {
				continue;
			}
			int srcRow = srcY * srcW;
			int destRow = y * destW;
			for (int x = 0; x < destW; x++) {
				int srcX = sx + x * sw / destW;
				if (srcX >= 0 && srcX < srcW) {
					dest[destRow + x] = src[srcRow + srcX];
				}
			}
		}
	}

	private static void fillRect(int[] dest, int destW, int destH, int x, int y, int w, int h, int color) {
		for (int row = 0; row < h; row++) {
			int destY = y + row;
			if (destY < 0 || destY >= destH) {
				continue;
			}
			int destRow = destY * destW;
			for (int col = 0; col < w; col++) {
				int destX = x + col;
				if (destX >= 0 && destX < destW) {
					dest[destRow + destX] = color;
				}
			}
		}
	}

	private record Crop(int x, int y, int w, int h) {
	}
}
