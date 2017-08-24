package net.imglib2.atlas;

import ij.ImagePlus;
import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.classification.Classifier;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class BatchSegmenter {
	public static void classifyLung() throws IOException, IncompatibleTypeException, ImgIOException, InterruptedException {
		final Img<ARGBType> rawImg = loadImage();
		Classifier classifier = loadClassifier();
		final int[] cellDimensions = new int[] { 256, 256 };
		Img<UnsignedByteType> segmentation = segment(rawImg, classifier, cellDimensions);
		new ImgSaver().saveImg("/home/arzt/test.tif", segmentation);
	}

	private static Img<ARGBType> loadImage() {
		final String imgPath = "/home/arzt/Documents/20170804_LungImages/2017_08_03__0006.jpg";
		return ImageJFunctions.wrap( new ImagePlus( imgPath ) );
	}

	private static Classifier loadClassifier() throws IOException {
		final String classifierPath = "/home/arzt/Documents/20170804_LungImages/0006.classifier";
		return Classifier.load(classifierPath);
	}

	private static Img<UnsignedByteType> segment(Img<ARGBType> rawImg, Classifier classifier, int[] cellDimensions) throws InterruptedException {
		RandomAccessible<ARGBType> image = Views.extendBorder(rawImg);
		Consumer<RandomAccessibleInterval<UnsignedByteType>> loader = target -> classifier.segment(target, image);
		Img<UnsignedByteType> result = ArrayImgs.unsignedBytes(Intervals.dimensionsAsLongArray(rawImg));
		List<Callable<Void>> chunks = ParallelUtils.chunkOperation(result, cellDimensions, loader);
		ParallelUtils.executeInParallel(
				Executors.newFixedThreadPool(10),
				ParallelUtils.addShowProgress(chunks)
		);
		return result;
	}
}
