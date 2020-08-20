
package net.imglib2.labkit.segmentation;

import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = SegmentationPlugin.class)
public class PixelClassificationPlugin implements SegmentationPlugin {

	@Parameter
	Context context;

	@Override
	public String getTitle() {
		return "Labkit Pixel Classification";
	}

	@Override
	public Segmenter createSegmenter() {
		return new TrainableSegmentationSegmenter(context);
	}

	public static SegmentationPlugin create() {
		Context context = SingletonContext.getInstance();
		PixelClassificationPlugin plugin = new PixelClassificationPlugin();
		context.inject(plugin);
		return plugin;
	}
}
