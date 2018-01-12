package net.imglib2.labkit;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.color.ColorMapProvider;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;

import java.util.Arrays;
import java.util.function.Consumer;

public class ImageLabelingModel implements LabelingModel {

	final RandomAccessibleInterval<? extends NumericType<?>> rawData;

	private ColorMapProvider colorProvider;

	private Holder<Labeling> labelingHolder;

	private Notifier<Runnable> dataChangedNotifier = new Notifier<>();

	ImageLabelingModel(RandomAccessibleInterval<? extends NumericType<?>> image) {
		this.rawData = image;
		Labeling labeling = new Labeling(Arrays.asList("background", "foreground"), image);
		this.labelingHolder = new CheckedHolder(labeling);
		colorProvider = new ColorMapProvider(labelingHolder);
	}

	public RandomAccessibleInterval<? extends NumericType<?>> image() {
		return rawData;
	}

	// -- LabelingModel methods --

	@Override
	public ColorMapProvider colorMapProvider() {
		return colorProvider;
	}

	@Override
	public Holder<Labeling> labeling() {
		return labelingHolder;
	}

	@Override
	public void requestRepaint() {
		dataChangedNotifier.forEach(Runnable::run);
	}

	@Override
	public Notifier<Runnable> dataChangedNotifier() {
		return dataChangedNotifier;
	}

	private static class CheckedHolder implements Holder<Labeling> {

		Notifier<Consumer<Labeling>> notifier = new Notifier<>();

		Labeling value;

		CheckedHolder(Labeling value) {
			this.value = value;
		}

		@Override
		public void set(Labeling value) {
			if(! Intervals.equals(value, this.value))
				throw new IllegalArgumentException();
			this.value = value;
			notifier.forEach(listener -> listener.accept(value));
		}

		@Override
		public Labeling get() {
			return value;
		}

		@Override
		public Notifier<Consumer<Labeling>> notifier() {
			return notifier;
		}
	}
}