package fr.cubeweb.vertifeed;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VertifeedSettingsScreen extends Screen {
	private static final Component TITLE = Component.translatable("vertifeed.menu.title");
	private static final OutputSize[] SIZES = {
		new OutputSize(720, 1280),
		new OutputSize(1080, 1920),
		new OutputSize(1440, 2560)
	};

	private final @Nullable Screen lastScreen;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private Page page = Page.FEED;
	private VertifeedConfig.HudLayer.Id selectedLayer = VertifeedConfig.HudLayer.Id.HOTBAR;

	public VertifeedSettingsScreen(@Nullable Screen lastScreen) {
		super(TITLE);
		this.lastScreen = lastScreen;
	}

	@Override
	protected void init() {
		this.layout.removeChildren();
		this.layout.addTitleHeader(TITLE, this.font);

		LinearLayout contents = LinearLayout.vertical().spacing(6);
		this.addFeedOptions(contents);

		this.layout.addToContents(contents);
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	private void addFeedOptions(LinearLayout contents) {
		VertifeedConfig config = VerticalFeed.config();
		int fieldWidth = Math.min(310, Math.max(150, this.width - 20));
		contents.addChild(CycleButton.onOffBuilder(config.enabled)
			.create(0, 0, fieldWidth, 20, Component.translatable("vertifeed.option.enabled"), (button, value) -> {
				config.enabled = value;
			}));
		contents.addChild(CycleButton.onOffBuilder(config.includeHud)
			.create(0, 0, fieldWidth, 20, Component.translatable("vertifeed.option.hud"), (button, value) -> {
				config.includeHud = value;
			}));
		contents.addChild(CycleButton.onOffBuilder(config.drawCursor)
			.create(0, 0, fieldWidth, 20, Component.translatable("vertifeed.option.cursor"), (button, value) -> {
				config.drawCursor = value;
			}));
		contents.addChild(CycleButton.onOffBuilder(config.overrideFov)
			.create(0, 0, fieldWidth, 20, Component.translatable("vertifeed.option.overrideFov"), (button, value) -> {
				config.overrideFov = value;
			}));
		contents.addChild(new VertifeedSlider(
			0,
			0,
			fieldWidth,
			20,
			Component.translatable("vertifeed.option.fov"),
			30.0,
			150.0,
			config.fov,
			value -> config.fov = (float) value,
			value -> String.valueOf(Math.round(value))
		));
		contents.addChild(CycleButton.builder(OutputSize::label, currentSize(config))
			.withValues(sizeValues(config))
			.create(0, 0, fieldWidth, 20, Component.translatable("vertifeed.option.size"), (button, value) -> {
				config.width = value.width;
				config.height = value.height;
			}));
		contents.addChild(new VertifeedSlider(
			0,
			0,
			fieldWidth,
			20,
			Component.translatable("vertifeed.option.hudScale"),
			2.0,
			6.0,
			config.hudScale,
			value -> config.hudScale = Math.max(2, (int) Math.round(value)),
			value -> String.valueOf(Math.max(2, Math.round(value)))
		));
		contents.addChild(new VertifeedSlider(
			0,
			0,
			fieldWidth,
			20,
			Component.translatable("vertifeed.option.frameskip"),
			1.0,
			4.0,
			config.frameSkip,
			value -> config.frameSkip = Math.max(1, (int) Math.round(value)),
			value -> String.valueOf(Math.max(1, Math.round(value)))
		));
		contents.addChild(new MultiLineTextWidget(Component.translatable("vertifeed.menu.feed.hint"), this.font)
			.setMaxWidth(fieldWidth)
			.setCentered(true));
	}

	private void addLayoutOptions(LinearLayout contents) {
		VertifeedConfig config = VerticalFeed.config();
		contents.addChild(CycleButton.builder(VertifeedSettingsScreen::layerLabel, this.selectedLayer)
			.withValues(VertifeedConfig.HudLayer.Id.values())
			.create(0, 0, 310, 20, Component.translatable("vertifeed.option.layer"), (button, value) -> {
				this.selectedLayer = value;
				this.rebuildWidgets();
			}));

		VertifeedConfig.HudLayer layer = config.layer(this.selectedLayer);
		contents.addChild(CycleButton.onOffBuilder(layer.visible)
			.create(0, 0, 310, 20, Component.translatable("vertifeed.option.visible"), (button, value) -> {
				layer.visible = value;
			}));
		contents.addChild(new VertifeedSlider(
			0,
			0,
			310,
			20,
			Component.translatable("vertifeed.option.x"),
			0.0,
			1.0,
			layer.x,
			value -> layer.x = (float) value,
			value -> percent(value)
		));
		contents.addChild(new VertifeedSlider(
			0,
			0,
			310,
			20,
			Component.translatable("vertifeed.option.y"),
			0.0,
			1.0,
			layer.y,
			value -> layer.y = (float) value,
			value -> percent(value)
		));
		contents.addChild(new VertifeedSlider(
			0,
			0,
			310,
			20,
			Component.translatable("vertifeed.option.scale"),
			0.25,
			2.5,
			layer.scale,
			value -> layer.scale = (float) value,
			value -> String.format("%.2f", value)
		));
		contents.addChild(Button.builder(Component.translatable("vertifeed.option.reset"), button -> {
			resetLayer(config, this.selectedLayer);
			this.rebuildWidgets();
		}).width(310).build());
		contents.addChild(new MultiLineTextWidget(Component.translatable("vertifeed.menu.layout.hint"), this.font)
			.setMaxWidth(310)
			.setCentered(true));
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		VerticalFeed.config().save();
		this.minecraft.gui.setScreen(this.lastScreen);
	}

	@Override
	public void removed() {
		VerticalFeed.config().save();
	}

	private static List<OutputSize> sizeValues(VertifeedConfig config) {
		List<OutputSize> sizes = new ArrayList<>(List.of(SIZES));
		OutputSize current = currentSize(config);
		if (!sizes.contains(current)) {
			sizes.add(0, current);
		}
		return sizes;
	}

	private static OutputSize currentSize(VertifeedConfig config) {
		for (OutputSize size : SIZES) {
			if (size.width == config.width && size.height == config.height) {
				return size;
			}
		}

		return new OutputSize(config.width, config.height);
	}

	private static void resetLayer(VertifeedConfig config, VertifeedConfig.HudLayer.Id id) {
		switch (id) {
			case HOTBAR -> config.hotbar = VertifeedConfig.HudLayer.hotbar();
			case CHAT -> config.chat = VertifeedConfig.HudLayer.chat();
			case DEBUG -> config.debug = VertifeedConfig.HudLayer.debug();
			case MENUS -> config.menus = VertifeedConfig.HudLayer.menus();
		}
	}

	private static Component layerLabel(VertifeedConfig.HudLayer.Id id) {
		return Component.translatable("vertifeed.layer." + id.name().toLowerCase());
	}

	private static String percent(double value) {
		return Math.round(value * 100.0) + "%";
	}

	private enum Page {
		FEED,
		LAYOUT;

		private Component label() {
			return Component.translatable("vertifeed.page." + this.name().toLowerCase());
		}
	}

	private record OutputSize(int width, int height) {
		private Component label() {
			return Component.literal(this.width + "x" + this.height);
		}
	}
}
