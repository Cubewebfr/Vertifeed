package fr.cubeweb.vertifeed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class VertifeedConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public boolean enabled = false;
	public String sourceName = "";
	public int width = 1080;
	public int height = 1920;
	public boolean overrideFov = true;
	public float fov = 110.0F;
	public boolean includeHud = true;
	public boolean drawCursor = true;
	public int hudScale = 4;
	public int frameSkip = 1;
	public HudLayer hotbar = HudLayer.hotbar();
	public HudLayer chat = HudLayer.chat();
	public HudLayer debug = HudLayer.debug();
	public HudLayer menus = HudLayer.menus();

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("vertifeed.json");
	}

	public static VertifeedConfig load() {
		Path file = path();
		if (Files.isRegularFile(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				VertifeedConfig config = GSON.fromJson(reader, VertifeedConfig.class);
				if (config != null) {
					config.sanitize();
					return config;
				}
			} catch (IOException e) {
				Vertifeed.LOGGER.warn("Could not read {}", file, e);
			}
		}

		VertifeedConfig config = new VertifeedConfig();
		config.save();
		return config;
	}

	public void save() {
		sanitize();
		Path file = path();

		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			Vertifeed.LOGGER.warn("Could not write {}", file, e);
		}
	}

	public void sanitize() {
		width = Math.max(160, width);
		height = Math.max(160, height);
		fov = Math.clamp(fov, 30.0F, 150.0F);
		frameSkip = Math.max(1, Math.min(8, frameSkip));
		if (hudScale < 2) {
			hudScale = 4;
		} else {
			hudScale = Math.min(6, hudScale);
		}
		if (sourceName == null) {
			sourceName = "";
		} else if (sourceName.equals("Minecraft Vertical")) {
			// Legacy default — NDI now uses the same "Minecraft - IGN" name as the window.
			sourceName = "";
		}
		if (hotbar == null) {
			hotbar = HudLayer.hotbar();
		}
		if (chat == null) {
			chat = HudLayer.chat();
		}
		if (debug == null) {
			debug = HudLayer.debug();
		}
		if (menus == null) {
			menus = HudLayer.menus();
		}
		hotbar.sanitize();
		chat.sanitize();
		debug.sanitize();
		menus.sanitize();
	}

	public HudLayer layer(HudLayer.Id id) {
		return switch (id) {
			case HOTBAR -> hotbar;
			case CHAT -> chat;
			case DEBUG -> debug;
			case MENUS -> menus;
		};
	}

	public static class HudLayer {
		public boolean visible = true;
		public float x;
		public float y;
		public float scale = 1.0F;

		public HudLayer() {
		}

		public HudLayer(boolean visible, float x, float y, float scale) {
			this.visible = visible;
			this.x = x;
			this.y = y;
			this.scale = scale;
		}

		public void sanitize() {
			x = Math.clamp(x, 0.0F, 1.0F);
			y = Math.clamp(y, 0.0F, 1.0F);
			scale = Math.clamp(scale, 0.25F, 3.0F);
		}

		public static HudLayer hotbar() {
			return new HudLayer(true, 0.50F, 0.93F, 1.0F);
		}

		public static HudLayer chat() {
			return new HudLayer(true, 0.03F, 0.78F, 1.0F);
		}

		public static HudLayer debug() {
			return new HudLayer(true, 0.02F, 0.03F, 1.0F);
		}

		public static HudLayer menus() {
			return new HudLayer(true, 0.50F, 0.50F, 1.0F);
		}

		public enum Id {
			HOTBAR,
			CHAT,
			DEBUG,
			MENUS
		}
	}
}
