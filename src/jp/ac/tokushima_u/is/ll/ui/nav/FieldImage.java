package jp.ac.tokushima_u.is.ll.ui.nav;
/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
import android.graphics.Bitmap;

public class FieldImage {
	Bitmap image;
	private int x;
	private int y;

	public FieldImage(Bitmap image) {
		this.image = image;
	}

	public FieldImage(Bitmap image, int x, int y) {
		this.image = image;
		this.x = x;
		this.y = y;
	}

	public void setStartX(int x) {
		this.x = x;
	}

	public void setStartY(int y) {
		this.y = y;
	}

	public int getStartX() {
		return x;
	}

	public int getStartY() {
		return y;
	}

	public int getEndX() {
		return x + image.getWidth();
	}

	public int getEndY() {
		return y + image.getHeight();
	}

	public void changeAllColor(int color) {
		int[] pixel = new int[image.getWidth() * image.getHeight()];
		for (int i = 0; i < image.getWidth() * image.getHeight(); i++) {
			pixel[i] = color;
		}
		Bitmap imageCopy = image.copy(Bitmap.Config.ARGB_8888, true);
		imageCopy.setPixels(pixel, 0, image.getWidth(), 0, 0, image.getWidth(),
				image.getHeight());
		image = imageCopy;
	}
}

