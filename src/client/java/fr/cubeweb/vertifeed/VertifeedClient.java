package fr.cubeweb.vertifeed;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class VertifeedClient implements ClientModInitializer {
	private static KeyMapping toggleKey;
	private static KeyMapping menuKey;

	@Override
	public void onInitializeClient() {
		VerticalFeed.init();

		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.vertifeed.toggle",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F8,
			KeyMapping.Category.MISC
		));
		menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.vertifeed.menu",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F7,
			KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				boolean enabled = !VerticalFeed.config().enabled;
				VerticalFeed.setEnabled(enabled);
				tell(client, enabled ? "Vertical NDI on (" + VerticalFeed.ndiSourceName() + ")" : "Vertical NDI off");
			}

			while (menuKey.consumeClick()) {
				VerticalFeed.openSettings(client);
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
			ClientCommands.literal("vertifeed")
				.executes(context -> {
					VerticalFeed.openSettings(Minecraft.getInstance());
					return 1;
				})
				.then(ClientCommands.literal("menu").executes(context -> {
					VerticalFeed.openSettings(Minecraft.getInstance());
					return 1;
				}))
				.then(ClientCommands.literal("status").executes(context -> {
					tell(Minecraft.getInstance(), status(VerticalFeed.config()));
					return 1;
				}))
				.then(ClientCommands.literal("on").executes(context -> {
					VerticalFeed.setEnabled(true);
					tell(Minecraft.getInstance(), "Vertical NDI on");
					return 1;
				}))
				.then(ClientCommands.literal("off").executes(context -> {
					VerticalFeed.setEnabled(false);
					tell(Minecraft.getInstance(), "Vertical NDI off");
					return 1;
				}))
				.then(ClientCommands.literal("hud").executes(context -> {
					VertifeedConfig config = VerticalFeed.config();
					config.includeHud = !config.includeHud;
					config.save();
					tell(Minecraft.getInstance(), "HUD " + (config.includeHud ? "on" : "off"));
					return 1;
				}))
				.then(ClientCommands.literal("cursor").executes(context -> {
					VertifeedConfig config = VerticalFeed.config();
					config.drawCursor = !config.drawCursor;
					config.save();
					tell(Minecraft.getInstance(), "Cursor " + (config.drawCursor ? "on" : "off"));
					return 1;
				}))
				.then(ClientCommands.literal("fov")
					.then(ClientCommands.argument("degrees", FloatArgumentType.floatArg(30.0F, 150.0F)).executes(context -> {
						VertifeedConfig config = VerticalFeed.config();
						config.overrideFov = true;
						config.fov = FloatArgumentType.getFloat(context, "degrees");
						config.save();
						tell(Minecraft.getInstance(), "Vertical FOV " + config.fov);
						return 1;
					})))
				.then(ClientCommands.literal("size")
					.then(ClientCommands.argument("width", IntegerArgumentType.integer(160, 3840))
						.then(ClientCommands.argument("height", IntegerArgumentType.integer(160, 3840)).executes(context -> {
							VertifeedConfig config = VerticalFeed.config();
							config.width = IntegerArgumentType.getInteger(context, "width");
							config.height = IntegerArgumentType.getInteger(context, "height");
							config.save();
							tell(Minecraft.getInstance(), "Vertical size " + config.width + "x" + config.height);
							return 1;
						}))))
				.then(ClientCommands.literal("reload").executes(context -> {
					VerticalFeed.reload();
					tell(Minecraft.getInstance(), "Reloaded " + VertifeedConfig.path());
					return 1;
				}))
		));
	}

	private static String status(VertifeedConfig config) {
		return "Vertical NDI "
			+ (config.enabled ? "on" : "off")
			+ " | " + VerticalFeed.ndiSourceName()
			+ " | " + config.width + "x" + config.height
			+ " | FOV " + (config.overrideFov ? config.fov : "game")
			+ " | HUD " + (config.includeHud ? "on" : "off")
			+ " | cursor " + (config.drawCursor ? "on" : "off");
	}

	private static void tell(Minecraft minecraft, String message) {
		if (minecraft.player != null) {
			minecraft.gui.hud.getChat().addClientSystemMessage(Component.literal("[Vertifeed] " + message));
		} else {
			Vertifeed.LOGGER.info(message);
		}
	}
}
