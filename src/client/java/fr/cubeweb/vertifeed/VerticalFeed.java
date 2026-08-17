package fr.cubeweb.vertifeed;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class VerticalFeed {
	private static VertifeedConfig config = new VertifeedConfig();
	private static NdiOutput output;
	private static int frameCounter;
	private static boolean captureThisFrame;
	private static boolean warnedMissingNdi;
	private static boolean passing;
	private static boolean skipGuiExtract;
	private static boolean isolatedFailed;
	private static boolean isolatedReady;
	private static RenderTarget verticalTarget;
	private static RenderTarget verticalOutline;
	private static int guiWidth = 270;
	private static int guiHeight = 480;
	private static int guiScale = 4;
	private static int mainGuiWidth = 640;
	private static int mainGuiHeight = 360;
	private static final List<EntityRenderState> savedEntities = new ArrayList<>();
	private static final List<BlockEntityRenderState> savedBlockEntities = new ArrayList<>();
	private static final List<BlockBreakingRenderState> savedBreaking = new ArrayList<>();
	private static final List<ParticleGroupRenderState> savedParticles = new ArrayList<>();
	private static BlockOutlineRenderState savedBlockOutline;
	private static int savedScreenWidth;
	private static int savedScreenHeight;
	private static int savedContainerLeft;
	private static int savedContainerTop;
	private static boolean savedContainerLayout;
	private static boolean savedRecipeBook;
	private static boolean savedWidthTooNarrow;
	private static boolean savedSearchFocused;
	private static boolean savedWidgetOffset;
	private static int savedWidgetDx;
	private static int savedWidgetDy;
	private static Object hoverTarget;
	private static String hoverMessage;
	private static int hoverIndex = -1;
	private static double hoverLocalX;
	private static double hoverLocalY;
	private static boolean hasMouseOverride;
	private static double overrideMouseX;
	private static double overrideMouseY;
	private static boolean ndiCursorReady;
	private static double ndiCursorGuiX;
	private static double ndiCursorGuiY;

	public static void init() {
		config = VertifeedConfig.load();
	}

	public static void reload() {
		config = VertifeedConfig.load();
	}

	private static NdiOutput output() {
		if (output == null) {
			output = new NdiOutput();
		}

		return output;
	}

	public static VertifeedConfig config() {
		return config;
	}

	public static boolean isPassing() {
		return passing;
	}

	public static boolean skipGuiExtract() {
		return skipGuiExtract;
	}

	public static boolean remapGuiMouse() {
		return true;
	}

	public static boolean hasMouseOverride() {
		return hasMouseOverride;
	}

	public static double overrideMouseX() {
		return overrideMouseX;
	}

	public static double overrideMouseY() {
		return overrideMouseY;
	}

	public static void snapshotFeatures(LevelRenderState state) {
		savedEntities.clear();
		savedEntities.addAll(state.entityRenderStates);
		savedBlockEntities.clear();
		savedBlockEntities.addAll(state.blockEntityRenderStates);
		savedBreaking.clear();
		savedBreaking.addAll(state.blockBreakingRenderStates);
		savedParticles.clear();
		savedParticles.addAll(state.particlesRenderState.particles);
		savedBlockOutline = state.blockOutlineRenderState;
	}

	public static void restoreFeatures(LevelRenderState state) {
		state.entityRenderStates.clear();
		state.entityRenderStates.addAll(savedEntities);
		state.blockEntityRenderStates.clear();
		state.blockEntityRenderStates.addAll(savedBlockEntities);
		state.blockBreakingRenderStates.clear();
		state.blockBreakingRenderStates.addAll(savedBreaking);
		state.particlesRenderState.particles.clear();
		state.particlesRenderState.particles.addAll(savedParticles);
		state.blockOutlineRenderState = savedBlockOutline;
	}

	public static void clearFeatureSnapshot() {
		savedEntities.clear();
		savedBlockEntities.clear();
		savedBreaking.clear();
		savedParticles.clear();
		savedBlockOutline = null;
	}

	public static int passWidth() {
		return config.width;
	}

	public static int passHeight() {
		return config.height;
	}

	public static int passGuiWidth() {
		return guiWidth;
	}

	public static int passGuiHeight() {
		return guiHeight;
	}

	public static int passGuiScale() {
		return guiScale;
	}

	public static String playerDisplayName() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null && minecraft.getUser() != null) {
			String name = minecraft.getUser().getName();
			if (name != null && !name.isBlank()) {
				return name;
			}
		}
		return "Player";
	}

	public static String brandTitle() {
		return "Minecraft - " + playerDisplayName();
	}

	public static String ndiSourceName() {
		String custom = config.sourceName;
		if (custom != null && !custom.isBlank()) {
			return custom;
		}
		return brandTitle();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static void setEnabled(boolean enabled) {
		config.enabled = enabled;
		config.save();
	}

	public static void openSettings(Minecraft minecraft) {
		Screen current = minecraft.gui.screen();
		if (current instanceof VertifeedSettingsScreen) {
			return;
		}

		minecraft.gui.setScreen(new VertifeedSettingsScreen(current));
	}

	public static void afterMainLevelRendered(Minecraft minecraft, DeltaTracker deltaTracker) {
		if (minecraft.level == null) {
			return;
		}

		captureIsolatedFrame(minecraft, deltaTracker, true);
	}

	public static void beforeGuiRendered(Minecraft minecraft, DeltaTracker deltaTracker) {
		if (minecraft.level != null) {
			return;
		}

		captureIsolatedFrame(minecraft, deltaTracker, false);
	}

	private static void captureIsolatedFrame(Minecraft minecraft, DeltaTracker deltaTracker, boolean hasLevel) {
		captureThisFrame = false;
		isolatedReady = false;
		if (!shouldCapture(minecraft) || isolatedFailed) {
			return;
		}

		NdiOutput ndi = output();
		if (!ndi.isAvailable()) {
			return;
		}

		if (++frameCounter % config.frameSkip != 0) {
			return;
		}

		captureThisFrame = true;
		config.sanitize();
		mainGuiWidth = Math.max(1, minecraft.getWindow().getGuiScaledWidth());
		mainGuiHeight = Math.max(1, minecraft.getWindow().getGuiScaledHeight());
		refreshGuiScale(minecraft);
		shrinkGuiScaleTo(minWidthFor(minecraft.gui.screen()));
		if (ensureVerticalTarget()) {
			ndi.ensureSender(ndiSourceName(), config.width, config.height);
			return;
		}
		try {
			if (hasLevel) {
				renderIsolatedView(minecraft, deltaTracker);
			} else {
				renderIsolatedMenu(minecraft, deltaTracker);
			}
			isolatedReady = true;
			ndi.captureVertical(verticalTarget, minecraft, config);
		} catch (Throwable t) {
			isolatedFailed = true;
			passing = false;
			skipGuiExtract = false;
			Vertifeed.LOGGER.error("Vertical world pass failed; falling back to a 16:9 crop so the main view stays intact.", t);
		}
	}

	public static void afterMainFramePresented(Minecraft minecraft) {
		if (!shouldCapture(minecraft) || !captureThisFrame || isolatedReady) {
			return;
		}

		NdiOutput ndi = output();
		if (!ndi.isAvailable()) {
			if (!warnedMissingNdi) {
				warnedMissingNdi = true;
				Vertifeed.LOGGER.error("Vertical NDI is enabled but the NDI runtime is not installed.");
			}
			return;
		}

		config.sanitize();
		ndi.ensureSender(ndiSourceName(), config.width, config.height);
		ndi.captureCropped(minecraft.gameRenderer.mainRenderTarget(), minecraft, config);
	}

	private static void renderIsolatedView(Minecraft minecraft, DeltaTracker deltaTracker) {
		GameRenderer renderer = minecraft.gameRenderer;
		GameRendererSwap swap = (GameRendererSwap) renderer;
		RenderTarget mainTarget = swap.vertifeed$getMainTarget();
		if (verticalTarget == null || verticalTarget.getColorTexture() == null || verticalTarget.getDepthTexture() == null) {
			throw new IllegalStateException("Vertical render target is not ready");
		}
		ndiEnsure();

		GameRenderState renderState = renderer.gameRenderState();
		WindowRenderState windowState = renderState.windowRenderState;
		CameraRenderState cameraState = renderState.levelRenderState.cameraRenderState;
		int savedWidth = windowState.width;
		int savedHeight = windowState.height;
		int savedGuiScale = windowState.guiScale;
		Matrix4f savedProjection = new Matrix4f(cameraState.projectionMatrix);
		LevelRendererSwap levelSwap = (LevelRendererSwap) minecraft.levelRenderer;
		RenderTarget realOutline = levelSwap.vertifeed$getOutlineTarget();

		passing = true;
		skipGuiExtract = true;
		swap.vertifeed$setMainTarget(verticalTarget);
		levelSwap.vertifeed$setOutlineTarget(verticalOutline());
		if (swap.vertifeed$getMainTarget() != verticalTarget) {
			passing = false;
			skipGuiExtract = false;
			levelSwap.vertifeed$setOutlineTarget(realOutline);
			throw new IllegalStateException("Could not swap the main render target for the vertical pass");
		}

		try {
			windowState.width = config.width;
			windowState.height = config.height;
			windowState.guiScale = guiScale;
			applyVerticalProjection(cameraState);
			if (cameraState.hudFov <= 0.01F) {
				cameraState.hudFov = 70.0F;
			}
			updateGlobalSettings(swap, renderState, deltaTracker, minecraft, config.width, config.height);
			renderer.renderLevel(deltaTracker);
			blitVerticalOutline(renderState.levelRenderState);
			swap.vertifeed$drawHand(deltaTracker);

			if (config.includeHud) {
				skipGuiExtract = false;
				applyPortraitScreen(minecraft.gui.screen());
				fitPortraitGui(minecraft.gui.screen(), windowState);
				minecraft.gui.extractRenderState(deltaTracker, true, true);
				swap.vertifeed$guiRenderer().render();
			}
		} finally {
			skipGuiExtract = false;
			passing = false;
			clearFeatureSnapshot();
			cameraState.projectionMatrix.set(savedProjection);
			windowState.width = savedWidth;
			windowState.height = savedHeight;
			windowState.guiScale = savedGuiScale;
			restorePortraitScreen(minecraft.gui.screen());
			levelSwap.vertifeed$setOutlineTarget(realOutline);
			swap.vertifeed$setMainTarget(mainTarget);
			try {
				updateGlobalSettings(swap, renderState, deltaTracker, minecraft, savedWidth, savedHeight);
				minecraft.gui.extractRenderState(deltaTracker, true, true);
			} catch (Throwable t) {
				Vertifeed.LOGGER.warn("Failed to restore the main view after the vertical pass", t);
			}
		}
	}

	private static void renderIsolatedMenu(Minecraft minecraft, DeltaTracker deltaTracker) {
		GameRenderer renderer = minecraft.gameRenderer;
		GameRendererSwap swap = (GameRendererSwap) renderer;
		RenderTarget mainTarget = swap.vertifeed$getMainTarget();
		if (verticalTarget == null || verticalTarget.getColorTexture() == null || verticalTarget.getDepthTexture() == null) {
			throw new IllegalStateException("Vertical render target is not ready");
		}
		ndiEnsure();

		GameRenderState renderState = renderer.gameRenderState();
		WindowRenderState windowState = renderState.windowRenderState;
		int savedWidth = windowState.width;
		int savedHeight = windowState.height;
		int savedGuiScale = windowState.guiScale;

		passing = true;
		skipGuiExtract = true;
		swap.vertifeed$setMainTarget(verticalTarget);
		if (swap.vertifeed$getMainTarget() != verticalTarget) {
			passing = false;
			skipGuiExtract = false;
			throw new IllegalStateException("Could not swap the main render target for the vertical menu pass");
		}

		try {
			windowState.width = config.width;
			windowState.height = config.height;
			windowState.guiScale = guiScale;
			updateGlobalSettings(swap, renderState, deltaTracker, minecraft, config.width, config.height);
			RenderSystem.getDevice()
				.createCommandEncoder()
				.clearColorAndDepthTextures(
					verticalTarget.getColorTexture(),
					renderState.guiRenderState.clearColorOverride,
					verticalTarget.getDepthTexture(),
					0.0
				);
			skipGuiExtract = false;
			applyPortraitScreen(minecraft.gui.screen());
			fitPortraitGui(minecraft.gui.screen(), windowState);
			minecraft.gui.extractRenderState(deltaTracker, false, true);
			swap.vertifeed$guiRenderer().render();
		} finally {
			skipGuiExtract = false;
			passing = false;
			windowState.width = savedWidth;
			windowState.height = savedHeight;
			windowState.guiScale = savedGuiScale;
			restorePortraitScreen(minecraft.gui.screen());
			swap.vertifeed$setMainTarget(mainTarget);
			try {
				updateGlobalSettings(swap, renderState, deltaTracker, minecraft, savedWidth, savedHeight);
				minecraft.gui.extractRenderState(deltaTracker, false, true);
			} catch (Throwable t) {
				Vertifeed.LOGGER.warn("Failed to restore the main menu after the vertical pass", t);
			}
		}
	}

	private static void applyPortraitScreen(Screen screen) {
		savedContainerLayout = false;
		savedRecipeBook = false;
		savedWidgetOffset = false;
		if (screen == null) {
			return;
		}

		savedScreenWidth = screen.width;
		savedScreenHeight = screen.height;
		if (usesUniformMouseScale(screen)) {
			clearMouseOverride();
		} else {
			captureHoverTarget(screen);
		}
		if (screen instanceof GameModeSwitcherAccess switcher) {
			screen.width = guiWidth;
			screen.height = guiHeight;
			switcher.vertifeed$layout(guiWidth, guiHeight);
			rememberNdiCursor();
			return;
		}
		if (resizesInPlace(screen)) {
			screen.resize(guiWidth, guiHeight);
			if (!usesUniformMouseScale(screen)) {
				pinHoverTarget(screen);
			}
			rememberNdiCursor();
			return;
		}

		if (screen instanceof AbstractContainerScreen<?> container) {
			ContainerLayout layout = (ContainerLayout) container;
			savedContainerLeft = layout.vertifeed$left();
			savedContainerTop = layout.vertifeed$top();
			savedContainerLayout = true;
			screen.width = guiWidth;
			screen.height = guiHeight;
			if (screen instanceof RecipeBookScreenAccess bookScreen) {
				savedRecipeBook = true;
				savedWidthTooNarrow = bookScreen.vertifeed$widthTooNarrow();
				savedSearchFocused = bookScreen.vertifeed$isSearchFocused();
				boolean tooNarrow = guiWidth < 379;
				bookScreen.vertifeed$layoutBook(guiWidth, guiHeight, tooNarrow);
				if (savedSearchFocused) {
					bookScreen.vertifeed$setSearchFocused(true);
				}
			}

			int newLeft = Math.max(0, (guiWidth - layout.vertifeed$imageWidth()) / 2);
			int newTop = Math.max(0, (guiHeight - layout.vertifeed$imageHeight()) / 2);
			offsetScreenWidgets(screen, newLeft - savedContainerLeft, newTop - savedContainerTop);
			layout.vertifeed$setLeft(newLeft);
			layout.vertifeed$setTop(newTop);
			pinHoverTarget(screen);
			rememberNdiCursor();
			return;
		}

		offsetScreenWidgets(screen, (guiWidth - savedScreenWidth) / 2, (guiHeight - savedScreenHeight) / 2);
		savedWidgetOffset = true;
		screen.width = guiWidth;
		screen.height = guiHeight;
		pinHoverTarget(screen);
		rememberNdiCursor();
	}

	private static void restorePortraitScreen(Screen screen) {
		clearMouseOverride();
		if (screen == null || savedScreenWidth <= 0 || savedScreenHeight <= 0) {
			return;
		}

		if (screen instanceof GameModeSwitcherAccess switcher) {
			screen.width = savedScreenWidth;
			screen.height = savedScreenHeight;
			switcher.vertifeed$layout(savedScreenWidth, savedScreenHeight);
			return;
		}
		if (resizesInPlace(screen)) {
			screen.resize(savedScreenWidth, savedScreenHeight);
			return;
		}

		if (savedContainerLayout && screen instanceof AbstractContainerScreen<?> container) {
			ContainerLayout layout = (ContainerLayout) container;
			screen.width = savedScreenWidth;
			screen.height = savedScreenHeight;
			if (savedRecipeBook && screen instanceof RecipeBookScreenAccess bookScreen) {
				bookScreen.vertifeed$layoutBook(savedScreenWidth, savedScreenHeight, savedWidthTooNarrow);
				if (savedSearchFocused) {
					bookScreen.vertifeed$setSearchFocused(true);
				}
			}
			offsetScreenWidgets(screen, savedContainerLeft - layout.vertifeed$left(), savedContainerTop - layout.vertifeed$top());
			layout.vertifeed$setLeft(savedContainerLeft);
			layout.vertifeed$setTop(savedContainerTop);
			savedContainerLayout = false;
			savedRecipeBook = false;
			clearWidgetOffset();
			return;
		}

		if (savedWidgetOffset) {
			offsetScreenWidgets(screen, -savedWidgetDx, -savedWidgetDy);
			screen.width = savedScreenWidth;
			screen.height = savedScreenHeight;
			clearWidgetOffset();
			return;
		}

		if (screen.width != savedScreenWidth || screen.height != savedScreenHeight) {
			screen.resize(savedScreenWidth, savedScreenHeight);
		}
	}

	private static void captureHoverTarget(Screen screen) {
		hoverTarget = null;
		hoverMessage = null;
		hoverIndex = -1;
		double x = mainGuiMouseX();
		double y = mainGuiMouseY();
		for (GuiEventListener child : screen.children()) {
			if (child instanceof AbstractSelectionList<?> list) {
				for (Object entry : list.children()) {
					if (entry instanceof GuiEventListener listener && listener.isMouseOver(x, y) && entry instanceof LayoutElement element) {
						rememberHover(entry, element, x, y, -1);
						return;
					}
				}
			}
		}
		int index = 0;
		for (GuiEventListener child : screen.children()) {
			if (child instanceof AbstractWidget widget) {
				if (widget.isMouseOver(x, y)) {
					rememberHover(widget, widget, x, y, index);
					return;
				}
				index++;
			}
		}
	}

	private static void rememberHover(Object target, LayoutElement element, double x, double y, int index) {
		hoverTarget = target;
		hoverIndex = index;
		hoverLocalX = x - element.getX();
		hoverLocalY = y - element.getY();
		if (target instanceof AbstractWidget widget) {
			hoverMessage = widget.getMessage().getString();
		}
	}

	private static void pinHoverTarget(Screen screen) {
		LayoutElement element = resolveHoverTarget(screen);
		if (element == null) {
			hasMouseOverride = false;
			return;
		}

		hasMouseOverride = true;
		overrideMouseX = element.getX() + hoverLocalX;
		overrideMouseY = element.getY() + hoverLocalY;
	}

	private static LayoutElement resolveHoverTarget(Screen screen) {
		if (hoverTarget instanceof LayoutElement element && isStillOnScreen(screen, hoverTarget)) {
			return element;
		}
		if (hoverMessage != null && !hoverMessage.isEmpty()) {
			for (GuiEventListener child : screen.children()) {
				if (child instanceof AbstractWidget widget && hoverMessage.equals(widget.getMessage().getString())) {
					return widget;
				}
			}
		}
		if (hoverIndex >= 0) {
			int index = 0;
			for (GuiEventListener child : screen.children()) {
				if (child instanceof AbstractWidget widget) {
					if (index == hoverIndex) {
						return widget;
					}
					index++;
				}
			}
		}
		return null;
	}

	private static boolean isStillOnScreen(Screen screen, Object target) {
		for (GuiEventListener child : screen.children()) {
			if (child == target) {
				return true;
			}
			if (child instanceof AbstractSelectionList<?> list) {
				for (Object entry : list.children()) {
					if (entry == target) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static void rememberNdiCursor() {
		Minecraft minecraft = Minecraft.getInstance();
		if (hasMouseOverride) {
			ndiCursorGuiX = overrideMouseX;
			ndiCursorGuiY = overrideMouseY;
		} else {
			ndiCursorGuiX = guiMouseX(minecraft.mouseHandler.xpos());
			ndiCursorGuiY = guiMouseY(minecraft.mouseHandler.ypos());
		}
		ndiCursorReady = true;
	}

	private static void clearMouseOverride() {
		hasMouseOverride = false;
		hoverTarget = null;
		hoverMessage = null;
		hoverIndex = -1;
	}

	private static double mainGuiMouseX() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.mouseHandler.xpos() * mainGuiWidth / Math.max(1, minecraft.getWindow().getScreenWidth());
	}

	private static double mainGuiMouseY() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.mouseHandler.ypos() * mainGuiHeight / Math.max(1, minecraft.getWindow().getScreenHeight());
	}

	private static boolean resizesInPlace(Screen screen) {
		return screen instanceof ChatScreen
			|| screen instanceof TitleScreen
			|| screen instanceof PauseScreen
			|| screen instanceof SelectWorldScreen
			|| screen instanceof JoinMultiplayerScreen
			|| screen instanceof DebugOptionsScreen
			|| screen instanceof AdvancementsScreen
			|| screen instanceof SocialInteractionsScreen
			|| screen instanceof PackSelectionScreen
			|| screen instanceof StatsScreen
			|| screen instanceof VertifeedSettingsScreen;
	}

	private static void offsetScreenWidgets(Screen screen, int dx, int dy) {
		if (dx == 0 && dy == 0) {
			return;
		}

		savedWidgetOffset = true;
		savedWidgetDx += dx;
		savedWidgetDy += dy;
		for (GuiEventListener child : screen.children()) {
			if (child instanceof AbstractWidget widget) {
				widget.setPosition(widget.getX() + dx, widget.getY() + dy);
			}
		}
	}

	private static void clearWidgetOffset() {
		savedWidgetOffset = false;
		savedWidgetDx = 0;
		savedWidgetDy = 0;
	}

	private static RenderTarget verticalOutline() {
		if (verticalOutline != null
			&& verticalOutline.width == config.width
			&& verticalOutline.height == config.height
			&& verticalOutline.getColorTexture() != null) {
			return verticalOutline;
		}

		RenderSystem.assertOnRenderThread();
		if (verticalOutline != null) {
			verticalOutline.destroyBuffers();
		}
		verticalOutline = new TextureTarget("Vertifeed Outline", config.width, config.height, true, GpuFormat.RGBA8_UNORM);
		return verticalOutline;
	}

	private static void blitVerticalOutline(LevelRenderState levelRenderState) {
		if (levelRenderState == null || !levelRenderState.shouldShowEntityOutlines) {
			return;
		}
		if (verticalOutline == null || verticalTarget == null || verticalOutline.getColorTextureView() == null || verticalTarget.getColorTextureView() == null) {
			return;
		}

		verticalOutline.blitAndBlendToTexture(verticalTarget.getColorTextureView(), verticalTarget.getDepthTextureView());
	}

	private static void applyVerticalProjection(CameraRenderState cameraState) {
		float fov = config.overrideFov ? config.fov : Minecraft.getInstance().gameRenderer.mainCamera().getFov();
		Projection projection = new Projection();
		projection.setupPerspective(0.05F, cameraState.depthFar, fov, config.width, config.height);
		projection.getMatrix(cameraState.projectionMatrix);
	}

	private static void ndiEnsure() {
		output().ensureSender(ndiSourceName(), config.width, config.height);
	}

	public static double guiMouseX(double screenX) {
		Minecraft minecraft = Minecraft.getInstance();
		double screenW = Math.max(1, minecraft.getWindow().getScreenWidth());
		double mainX = screenX * mainGuiWidth / screenW;
		Screen screen = minecraft.gui.screen();
		if (usesLeftAlignedMouse(screen)) {
			return mainX;
		}
		if (usesUniformMouseScale(screen)) {
			return mainX * guiWidth / Math.max(1, mainGuiWidth);
		}
		return mainX - mainGuiWidth / 2.0 + guiWidth / 2.0;
	}

	public static double guiMouseY(double screenY) {
		Minecraft minecraft = Minecraft.getInstance();
		double screenH = Math.max(1, minecraft.getWindow().getScreenHeight());
		double mainY = screenY * mainGuiHeight / screenH;
		Screen screen = minecraft.gui.screen();
		if (usesLeftAlignedMouse(screen)) {
			return mainY - mainGuiHeight + guiHeight;
		}
		if (usesUniformMouseScale(screen)) {
			return mainY * guiHeight / Math.max(1, mainGuiHeight);
		}
		if (screen instanceof TitleScreen) {
			return mainY - mainGuiHeight / 4.0 + guiHeight / 4.0;
		}
		return mainY - mainGuiHeight / 2.0 + guiHeight / 2.0;
	}

	private static boolean usesLeftAlignedMouse(Screen screen) {
		return screen instanceof ChatScreen;
	}

	/**
	 * Full-bleed menus (world list, multiplayer, packs, …) stretch with the screen.
	 * Center-anchored mapping makes the cursor drift across list rows.
	 */
	private static boolean usesUniformMouseScale(Screen screen) {
		return screen instanceof SelectWorldScreen
			|| screen instanceof JoinMultiplayerScreen
			|| screen instanceof PackSelectionScreen
			|| screen instanceof StatsScreen
			|| screen instanceof SocialInteractionsScreen
			|| screen instanceof DebugOptionsScreen
			|| screen instanceof AdvancementsScreen
			|| screen instanceof VertifeedSettingsScreen
			|| screen instanceof CreateWorldScreen
			|| screen instanceof OptionsScreen;
	}

	public static int cursorX(int outW, double screenX) {
		double x = ndiCursorReady ? ndiCursorGuiX : hasMouseOverride ? overrideMouseX : guiMouseX(screenX);
		return (int) Math.round(x * outW / Math.max(1, guiWidth));
	}

	public static int cursorY(int outH, double screenY) {
		double y = ndiCursorReady ? ndiCursorGuiY : hasMouseOverride ? overrideMouseY : guiMouseY(screenY);
		return (int) Math.round(y * outH / Math.max(1, guiHeight));
	}

	private static void refreshGuiScale(Minecraft minecraft) {
		guiScale = Math.max(2, Math.min(6, config.hudScale));
		while (guiScale > 2 && (config.width / guiScale < 270 || config.height / guiScale < 180)) {
			guiScale--;
		}
		guiWidth = Math.max(1, config.width / guiScale);
		guiHeight = Math.max(1, config.height / guiScale);
	}

	private static void shrinkGuiScaleTo(int minWidth) {
		while (guiScale > 1 && minWidth > 0 && config.width / guiScale < minWidth) {
			guiScale--;
			guiWidth = Math.max(1, config.width / guiScale);
			guiHeight = Math.max(1, config.height / guiScale);
		}
	}

	private static int minWidthFor(Screen screen) {
		if (screen instanceof PackSelectionScreen) {
			return 430;
		}
		if (screen instanceof CreateWorldScreen || screen instanceof OptionsScreen || screen instanceof VertifeedSettingsScreen) {
			return 310;
		}
		if (screen instanceof AdvancementsScreen) {
			return 252;
		}
		if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
			return 220;
		}
		if (screen instanceof SelectWorldScreen) {
			return 270;
		}
		if (screen instanceof AbstractContainerScreen<?>) {
			return 176;
		}
		return 0;
	}

	private static void fitPortraitGui(Screen screen, WindowRenderState windowState) {
		if (screen == null) {
			return;
		}

		while (guiScale > 1 && portraitOverflows(screen)) {
			restorePortraitScreen(screen);
			guiScale--;
			guiWidth = Math.max(1, config.width / guiScale);
			guiHeight = Math.max(1, config.height / guiScale);
			windowState.guiScale = guiScale;
			applyPortraitScreen(screen);
		}
	}

	private static boolean portraitOverflows(Screen screen) {
		if (screen instanceof GameModeSwitcherAccess switcher && switcher.vertifeed$overflows(guiWidth, guiHeight)) {
			return true;
		}

		for (GuiEventListener child : screen.children()) {
			if (child instanceof AbstractWidget widget && widget.visible && widgetOverflows(widget)) {
				return true;
			}
			if (child instanceof AbstractSelectionList<?> list) {
				for (Object entry : list.children()) {
					if (entry instanceof AbstractWidget widget && widget.visible && widgetOverflows(widget)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean widgetOverflows(AbstractWidget widget) {
		return widget.getX() < 0
			|| widget.getY() < 0
			|| widget.getX() + widget.getWidth() > guiWidth
			|| widget.getY() + widget.getHeight() > guiHeight;
	}

	private static void updateGlobalSettings(
		GameRendererSwap swap,
		GameRenderState renderState,
		DeltaTracker deltaTracker,
		Minecraft minecraft,
		int width,
		int height
	) {
		swap.vertifeed$uniforms()
			.update(
				width,
				height,
				renderState.optionsRenderState.glintStrength,
				minecraft.level == null ? 0L : minecraft.level.getGameTime(),
				deltaTracker,
				renderState.optionsRenderState.menuBackgroundBlurriness,
				renderState.levelRenderState.cameraRenderState.pos,
				renderState.optionsRenderState.textureFiltering == TextureFilteringMethod.RGSS
			);
	}

	private static boolean ensureVerticalTarget() {
		if (verticalTarget != null && verticalTarget.width == config.width && verticalTarget.height == config.height && verticalTarget.getColorTexture() != null) {
			return false;
		}

		RenderSystem.assertOnRenderThread();
		if (verticalTarget != null) {
			verticalTarget.resize(config.width, config.height);
		} else {
			verticalTarget = new MainTarget(config.width, config.height);
		}
		return true;
	}

	private static boolean shouldCapture(Minecraft minecraft) {
		return config.enabled && minecraft.gameRenderer != null && minecraft.isGameLoadFinished();
	}
}
