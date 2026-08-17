package fr.cubeweb.vertifeed;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

public class VertifeedSlider extends AbstractSliderButton {
	private final Component prefix;
	private final double min;
	private final double max;
	private final DoubleConsumer apply;
	private final DoubleFunction<String> format;

	public VertifeedSlider(
		int x,
		int y,
		int width,
		int height,
		Component prefix,
		double min,
		double max,
		double current,
		DoubleConsumer apply,
		DoubleFunction<String> format
	) {
		super(x, y, width, height, prefix, Mth.clamp((current - min) / (max - min), 0.0, 1.0));
		this.prefix = prefix;
		this.min = min;
		this.max = max;
		this.apply = apply;
		this.format = format;
		this.updateMessage();
	}

	public double current() {
		return this.min + this.value * (this.max - this.min);
	}

	@Override
	protected void updateMessage() {
		this.setMessage(Component.literal(this.prefix.getString() + ": " + this.format.apply(this.current())));
	}

	@Override
	protected void applyValue() {
		this.apply.accept(this.current());
	}
}
