
package net.imglib2.labkit;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.segmentation.weka.PixelClassificationPlugin;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SegmentationUseCaseTest {

	@Test
	public void test() {
		ImgPlus<UnsignedByteType> image = new ImgPlus<>(ArrayImgs.unsignedBytes(new byte[] { 1, 1, 2,
			2 }, 2, 2));
		InputImage inputImage = new DatasetInputImage(image);
		SegmentationModel segmentationModel = new DefaultSegmentationModel(
			new Context(), inputImage);
		addLabels(segmentationModel.imageLabelingModel());
		SegmentationPlugin plugin = PixelClassificationPlugin.create();
		SegmentationItem segmenter = segmentationModel.segmenterList().addSegmenter(plugin);
		segmenter.train(Collections.singletonList(new ValuePair<>(image,
			segmentationModel.imageLabelingModel().labeling().get())));
		RandomAccessibleInterval<ShortType> result = segmenter.results(segmentationModel
			.imageLabelingModel())
			.segmentation();
		List<Integer> list = new ArrayList<>();
		Views.iterable(result).forEach(x -> list.add(x.getInteger()));
		assertEquals(Arrays.asList(1, 1, 0, 0), list);
	}

	private void addLabels(ImageLabelingModel imageLabelingModel) {
		Labeling labeling = imageLabelingModel.labeling().get();
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		ra.setPosition(new long[] { 0, 0 });
		ra.get().add(labeling.getLabel("foreground"));
		ra.setPosition(new long[] { 0, 1 });
		ra.get().add(labeling.getLabel("background"));
	}

	@Test
	public void testMultiChannel() throws InterruptedException {
		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(new byte[] { -1, 0, -1,
			0, -1, -1, 0, 0 }, 2, 2, 2);
		ImgPlus<UnsignedByteType> imgPlus = new ImgPlus<>(img, "Image",
			new AxisType[] { Axes.X, Axes.Y, Axes.CHANNEL });
		DatasetInputImage inputImage = new DatasetInputImage(imgPlus, BdvShowable
			.wrap(Views.hyperSlice(img, 2, 0)));

		Labeling labeling = getLabeling();
		SegmentationModel segmentationModel = new DefaultSegmentationModel(new Context(),
			inputImage);
		ImageLabelingModel imageLabelingModel = segmentationModel.imageLabelingModel();
		imageLabelingModel.labeling().set(labeling);
		SegmentationItem segmenter = segmentationModel.segmenterList().addSegmenter(
			PixelClassificationPlugin.create());
		segmenter.train(Collections.singletonList(new ValuePair<>(imgPlus,
			imageLabelingModel.labeling().get())));
		RandomAccessibleInterval<ShortType> result = segmenter.results(imageLabelingModel)
			.segmentation();
		Iterator<ShortType> it = Views.iterable(result).iterator();
		assertEquals(1, it.next().get());
		assertEquals(0, it.next().get());
		assertEquals(0, it.next().get());
		assertEquals(0, it.next().get());
		assertTrue(Intervals.equals(new FinalInterval(2, 2), result));
	}

	private Labeling getLabeling() {
		List<String> labels = Arrays.asList("b", "f");
		Interval interval = new FinalInterval(2, 2);
		Labeling labeling = Labeling.createEmpty(labels, interval);
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		ra.setPosition(new long[] { 0, 0 });
		ra.get().add(labeling.getLabel("f"));
		ra.setPosition(new long[] { 1, 0 });
		ra.get().add(labeling.getLabel("b"));
		ra.setPosition(new long[] { 0, 1 });
		ra.get().add(labeling.getLabel("b"));
		ra.setPosition(new long[] { 1, 1 });
		ra.get().add(labeling.getLabel("b"));
		return labeling;
	}

}
