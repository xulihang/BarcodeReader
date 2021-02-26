package application;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.opencv.core.Rect2d;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.EnumResultCoordinateType;
import com.dynamsoft.dbr.Point;
import com.dynamsoft.dbr.PublicRuntimeSettings;
import com.dynamsoft.dbr.TextResult;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class MyController implements Initializable {
	private File currentImgFile;
    private BarcodeReader br;
    private ObjectDetector qrcodeDetector;
    @FXML
    private Label timeLbl;
    @FXML
    private CheckBox useObjectDetectionChk;
    @FXML
    private CheckBox useZXingChk;
	@FXML
    private Button loadBtn;
	@FXML
    private AnchorPane root;
    @FXML
    private TextArea resultTA;
    @FXML
    private TextArea templateTA;
    @FXML
    private Canvas cv;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
		try {
			qrcodeDetector = new ObjectDetector("C:\\\\Users\\\\admin\\\\Desktop\\\\Yolo-Fastest-opencv-dnn\\\\qrcode-yolov3-tiny.cfg","C:\\\\Users\\\\admin\\\\Desktop\\\\Yolo-Fastest-opencv-dnn\\\\qrcode-yolov3-tiny_last.weights",416,416);
			br = new BarcodeReader("t0068NQAAAJYBYfmF8T9A4FyRD4gw30Kx9VtWdhk4M7K8OgvmtsAySfNNO0Fi3uIBlvoHUBWLJB4MQ1bUt9k8v+TrrG1cXio=");
		} catch (BarcodeReaderException e) {
			e.printStackTrace();
		}		
    }
    
    public void loadBtn_MouseClicked(Event event) throws IOException, BarcodeReaderException {

    	System.out.println("Button Clicked!");    
    	if (currentImgFile==null) {
    		System.out.println("no img!");   
    		return;
    	}
    	Image img = new Image(currentImgFile.toURI().toString());
    	redrawImage(img);
    	String imgPath=currentImgFile.getAbsolutePath();
    	System.out.println(imgPath);
    	String template = templateTA.getText();
    	try {
        	br.initRuntimeSettingsWithString(template,EnumConflictMode.CM_OVERWRITE);   
    	}  catch (Exception e) {
    		br.resetRuntimeSettings();
    	}
    	unifyCoordinateReturnType();
    	
    	List<BarcodeResult> allResults = new ArrayList<BarcodeResult>();
    	
    	StringBuilder timeSb = new StringBuilder();
    	Date startDate = new Date();
    	Long startTime = startDate.getTime();
    	Long detectedTime = null;
    	Long endTime = null;
    	if (useObjectDetectionChk.isSelected()) {        	
        	List<Rect2d> rects = qrcodeDetector.Detect(imgPath);
        	Date detectedDate = new Date();
        	detectedTime = detectedDate.getTime();
        	int rectIndex=0;
        	for (Rect2d rect:rects) {
        		rectIndex=rectIndex+1; 	
            	BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);
            	int x,y,maxX,maxY,width,height;
            	x=(int) Math.max(rect.x,0);
            	y=(int) Math.max(rect.y,0);
            	width=(int) rect.width;
            	height=(int) rect.height;   
            	maxX=(int) rect.width+x;
            	maxY=(int) rect.height+y;            	
            	if (width!=height) {
            		if (width>height) {
            			y=Math.max(y-(width-height)/2,1);            			
            			maxY=(int) maxY+(width-height)/2;
            		}else {
            			x=Math.max(x-(height-width)/2,1);               			
            			maxX=(int) maxX+(height-width)/2;
            		}
            	}
    			maxY=(int) Math.min(maxY,img.getHeight());
    			height=maxY-y;
    			maxX=(int) Math.min(maxX,img.getWidth());
    			width=maxX-x;
            	rect.x=x;
            	rect.y=y;
            	rect.width=width;
            	rect.height=height;
            	overlayBox(rect,rectIndex);
            	
            	BufferedImage cropped = bImage.getSubimage(x,y,width,height);
            	if (useZXingChk.isSelected()==true) {
            		allResults.add(readQRCode(cropped));
            	}else {
            		TextResult[] results = br.decodeBufferedImage(cropped, "");
            		for(int i =0; i<results.length;i++){
            			allResults.add(TextResult2BarcodeResult(results[i]));
            		}
            	}
            	
            	Date endDate = new Date();
            	endTime = endDate.getTime();
        		
        	}

    	}else {
    		if (useZXingChk.isSelected()==true) {
    			BarcodeResult r = readQRCode(imgPath);
    			allResults.add(r);
    			overlayCode(r);
    		} else {
    			for (TextResult tr:br.decodeFile(imgPath, "")) {
    				BarcodeResult r = TextResult2BarcodeResult(tr);
    				allResults.add(r);
    				overlayCode(r);
    			}
    		}
        	Date endDate = new Date();
        	endTime = endDate.getTime();
    	}
    	StringBuilder sb = new StringBuilder(); 
    	int index=0;
    	for (BarcodeResult result:allResults) {
    		index=index+1;
    		sb.append(index);
        	sb.append("\n");
        	sb.append("Type: ");
        	sb.append(result.format);
        	sb.append("\n");
        	sb.append("Text: ");
        	sb.append(result.text);
        	sb.append("\n\n");        	
    	}
    	resultTA.setText(sb.toString());

    	timeSb.append("Total: ");
    	timeSb.append(endTime-startTime);
    	timeSb.append("ms");
    	if (useObjectDetectionChk.isSelected()) { 
    		timeSb.append(" Object detection: ");
        	timeSb.append(detectedTime-startTime);
        	timeSb.append("ms");
    		timeSb.append(" DBR: ");
        	timeSb.append(endTime-detectedTime);
        	timeSb.append("ms");
    	}
    	timeLbl.setText(timeSb.toString());
    }
 
    public void cv_MouseClicked(Event event) {
        System.out.println("cv Clicked!");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            currentImgFile = fileChooser.showOpenDialog(Main.getPrimaryStage());
            Image img = new Image(currentImgFile.toURI().toString());
            System.out.println(img.getWidth());
            redrawImage(img);
        } catch (Exception e) {
        	
        }
    }
    
    private void redrawImage(Image img) {
        cv.setWidth(img.getWidth());
        cv.setHeight(img.getHeight());
        GraphicsContext gc = cv.getGraphicsContext2D();
        gc.drawImage(img, 0, 0, cv.getWidth(), cv.getHeight());
    }

    private void overlayCode(BarcodeResult result) {
    	GraphicsContext gc=cv.getGraphicsContext2D();

		List<Point2D> points=result.points;
		
		gc.setStroke(Color.RED);
		gc.setLineWidth(5);
		gc.beginPath();
		for (int i = 0;i<points.size()-1;i++) {
			Point2D point=points.get(i);
			Point2D nextPoint=points.get(i+1);
			gc.moveTo(point.getX(), point.getY());
			gc.lineTo(nextPoint.getX(), nextPoint.getY());			
		}
		Point2D firstPoint = points.get(0); 
		Point2D lastPoint = points.get(points.size()-1); 
		gc.moveTo(lastPoint.getX(), lastPoint.getY());
		gc.lineTo(firstPoint.getX(), firstPoint.getY());
		gc.closePath();
		gc.stroke();
    }
    
    
    private void overlayCode2(TextResult result) {
    	GraphicsContext gc=cv.getGraphicsContext2D();

		Point[] points=result.localizationResult.resultPoints;
		
		double minX, minY, maxX, maxY;
		minX=points[0].x;
		minY=points[0].y;
		maxX=0;
		maxY=0;
		for (Point point : points) {
			minX=Math.min(minX,point.x);
			minY=Math.min(minY,point.y);
			maxX=Math.max(point.x,maxX);
			maxY=Math.max(point.y,maxY);
		}
		gc.setStroke(Color.RED);
		gc.setLineWidth(5);
		gc.strokeRect(minX, minY, maxX-minX, maxY-minY);
    }
    
    private void overlayBox(Rect2d rect,int index) {
    	GraphicsContext gc=cv.getGraphicsContext2D();
		gc.setStroke(Color.RED);
		gc.setLineWidth(5);
		gc.strokeRect(rect.x, rect.y, rect.width, rect.height);
    }
    
    private void unifyCoordinateReturnType() {
		PublicRuntimeSettings settings;
		try {
			settings = br.getRuntimeSettings();
			settings.resultCoordinateType=EnumResultCoordinateType.RCT_PIXEL;
			br.updateRuntimeSettings(settings);
		} catch (BarcodeReaderException e) {
			e.printStackTrace();
		}
    }
    
    public BarcodeResult readQRCode(String fileName) {
		File file = new File(fileName);
		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return readQRCode(image);
	}
    
    public BarcodeResult readQRCode(BufferedImage image) {
    	System.out.println("using zxing");
		BinaryBitmap bitmap = null;
		Result result = null;
		int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
		bitmap = new BinaryBitmap(new HybridBinarizer(source));

		QRCodeReader reader = new QRCodeReader();	
		try {
			result = reader.decode(bitmap);
			return ZxingResult2BarcodeResult(result);
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ChecksumException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
    
    private BarcodeResult TextResult2BarcodeResult(TextResult result) {
    	Point[] points=result.localizationResult.resultPoints;
    	List<Point2D> newPoints = new ArrayList<Point2D>();
    	for (Point point:points) {
    		Point2D point2d = new Point2D(point.x,point.y);
    		newPoints.add(point2d);
    	}
    	return new BarcodeResult(newPoints,result.barcodeText,result.barcodeFormatString);
    }
    
    private BarcodeResult ZxingResult2BarcodeResult(Result result) {
    	ResultPoint[] points=result.getResultPoints();
    	List<Point2D> newPoints = new ArrayList<Point2D>();
    	for (ResultPoint point:points) {
    		Point2D point2d = new Point2D(point.getX(),point.getY());
    		newPoints.add(point2d);
    	}
    	return new BarcodeResult(newPoints,result.getText(),result.getBarcodeFormat().name());
    }
}
