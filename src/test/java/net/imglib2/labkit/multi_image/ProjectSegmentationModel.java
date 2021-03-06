
package net.imglib2.labkit.multi_image;

import net.imagej.ImgPlus;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ExtensionPoints;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.project.LabeledImage;
import net.imglib2.labkit.project.LabkitProjectModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.segmentation.weka.PixelClassificationPlugin;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

import java.util.AbstractList;
import java.util.List;

/**
 * ProjectSegmentationModel is a {@link SegmentationModel}, that allows to
 * change the {@link LabeledImage} it works with.
 */
public class ProjectSegmentationModel implements SegmentationModel {

	private final Context context;

	private final LabkitProjectModel projectModel;

	private ImageLabelingModel imageLabelingModel;

	private final SegmenterListModel segmenterList;

	private LabeledImage selectedImage;

	public ProjectSegmentationModel(LabkitProjectModel projectModel) {
		this.context = projectModel.context();
		this.projectModel = projectModel;
		this.segmenterList = initSegmenterListModel(projectModel.segmenterFiles());
	}

	@Override
	public Context context() {
		return context;
	}

	@Override
	public ImageLabelingModel imageLabelingModel() {
		return imageLabelingModel;
	}

	@Override
	public SegmenterListModel segmenterList() {
		return segmenterList;
	}

	public LabkitProjectModel projectModel() {
		return projectModel;
	}

	private SegmenterListModel initSegmenterListModel(List<String> segmenters) {
		SegmenterListModel segmenterListModel = new SegmenterListModel(context, new ExtensionPoints());
		segmenterListModel.trainingData().set(new TrainingData());
		for (String filename : segmenters) {
			SegmentationItem segmentationItem = segmenterListModel.addSegmenter(PixelClassificationPlugin
				.create());
			segmentationItem.openModel(filename);
		}
		return segmenterListModel;
	}

	public void setSelectedImage(LabeledImage image) {
		if (image == selectedImage)
			return;
		if (selectedImage != null) {
			selectedImage.close();
		}
		this.selectedImage = image;
		imageLabelingModel = image.open();
	}

	private class TrainingData extends AbstractList<Pair<ImgPlus<?>, Labeling>> {

		@Override
		public Pair<ImgPlus<?>, Labeling> get(int index) {
			LabeledImage imageItem = projectModel.labeledImages().get(index);
			if (imageItem == selectedImage) {
				ImgPlus<?> image = imageLabelingModel.imageForSegmentation().get();
				Labeling labeling = imageLabelingModel.labeling().get();
				return new ValuePair<>(image, labeling);
			}
			ImageLabelingModel ilm = imageItem.snapshot();
			return new ValuePair<>(ilm.imageForSegmentation().get(), ilm.labeling().get());
		}

		@Override
		public int size() {
			return projectModel.labeledImages().size();
		}
	}
}
