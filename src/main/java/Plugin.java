import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.silentsoft.actlist.plugin.ActlistPlugin;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class Plugin extends ActlistPlugin {

	@FXML
	private ImageView screenView;
	
	@FXML
	private ColorPicker colorPicker;
	
	@FXML
	private Label redValue, greenValue, blueValue;
	
	private Thread liveCaptureThread;
	
	public static void main(String[] args) throws Exception {
		debug();
	}
	
	public Plugin() throws Exception {
		super("Color Picker");
		
		setPluginVersion("1.0.0");
		setPluginAuthor("silentsoft.org", URI.create("https://github.com/silentsoft/actlist-color-picker-plugin"));
		setPluginUpdateCheckURI(URI.create("http://actlist.silentsoft.org/api/plugin/3df4bbf7/update/check"));
		
		setMinimumCompatibleVersion(1, 2, 6);
	}

	@Override
	protected void initialize() throws Exception {
		
	}

	@Override
	public void pluginActivated() throws Exception {
		Robot robot = new Robot();
		Rectangle captureRectangle = new Rectangle(0, 0, 0, 0);
		BufferedImage scaleImage = new BufferedImage(110, 110, BufferedImage.TYPE_INT_ARGB);
	    AffineTransformOp scaleTransformOp = new AffineTransformOp(AffineTransform.getScaleInstance(10, 10), AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

	    AtomicReference<Point> previousMouseLocation = new AtomicReference<Point>(null);
	    AtomicReference<java.awt.Color> previousColor = new AtomicReference<java.awt.Color>(null);
	    
		liveCaptureThread = new Thread(() -> {
			try {
				while (true) {
					PointerInfo mousePointer = MouseInfo.getPointerInfo();
					if (mousePointer == null) {
						Thread.yield();
					} else {
						Point mouseLocation = mousePointer.getLocation();
						if (mouseLocation.equals(previousMouseLocation.get()) == false) {
							previousMouseLocation.set(mouseLocation);
							
							captureRectangle.setBounds(mouseLocation.x-5, mouseLocation.y-5, 20, 20);
							BufferedImage captureImage = robot.createScreenCapture(captureRectangle);
							
							scaleTransformOp.filter(captureImage, scaleImage);
						    screenView.setImage(SwingFXUtils.toFXImage(scaleImage, null));
							
							java.awt.Color color = robot.getPixelColor(mouseLocation.x, mouseLocation.y);
							if (color.equals(previousColor.get()) == false) {
								previousColor.set(color);
								
								Platform.runLater(() -> {
									redValue.setText(String.valueOf(color.getRed()));
									greenValue.setText(String.valueOf(color.getGreen()));
									blueValue.setText(String.valueOf(color.getBlue()));
									colorPicker.setValue(javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));
								});
							}
						}
						
						Thread.sleep(50);
					}
				}
			} catch (InterruptedException e) {
				
			} catch (Exception e) {
				throwException(e);
			}
		});
		liveCaptureThread.start();
	}

	@Override
	public void pluginDeactivated() throws Exception {
		if (liveCaptureThread != null) {
			liveCaptureThread.interrupt();
			liveCaptureThread = null;
		}
	}

}
