package Feature;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import Utils.RGBToHSVHistogram;

public class FeatureHSVColorHistogram extends Feature {
	private File imgFile = null;	
	private BufferedImage imgBuff = null;
	private int[] imgPixel = null;
	
	private double[] histogram = null;
	
	public FeatureHSVColorHistogram(File arg0) {
		imgFile = arg0;
	}
	
	public void run() {
		super.run();
	}
	
	protected void init() {
		super.init();
	}
	
	protected void initialize() {
		try {
			imgBuff = ImageIO.read(imgFile);
			imgPixel = new int[imgBuff.getWidth()*imgBuff.getHeight()];
			
			imgBuff.getRGB(0, 0, 
					imgBuff.getWidth(null), imgBuff.getHeight(null), 
					imgPixel, 0, imgBuff.getWidth(null));
						
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Cannot read image file.");
		}
	}	
	
	protected void extractFeature() {
		histogram = RGBToHSVHistogram.hsvHistogram(imgPixel);
	}
	
	public double[] getFeature() {
		return histogram;
	}
}
