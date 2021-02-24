package application;


import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.EnumResultCoordinateType;
import com.dynamsoft.dbr.Point;
import com.dynamsoft.dbr.PublicRuntimeSettings;
import com.dynamsoft.dbr.TextResult;


import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class MyController implements Initializable {
	private File currentImgFile;
    private BarcodeReader br;
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
			br = new BarcodeReader("t0068NQAAAJYBYfmF8T9A4FyRD4gw30Kx9VtWdhk4M7K8OgvmtsAySfNNO0Fi3uIBlvoHUBWLJB4MQ1bUt9k8v+TrrG1cXio=");			
		} catch (BarcodeReaderException e) {
			e.printStackTrace();
		}		
    }
    
    public void loadBtn_MouseClicked(Event event) throws IOException, BarcodeReaderException {
    	System.out.println("Button Clicked!");     
    	String template = templateTA.getText();
    	try {
        	System.out.println(template);
        	br.initRuntimeSettingsWithString(template,EnumConflictMode.CM_OVERWRITE);   
    	}  catch (Exception e) {
    		br.resetRuntimeSettings();
    	}
    	unifyCoordinateReturnType();
        String imgPath=currentImgFile.getAbsolutePath();
        System.out.println(imgPath);
		TextResult[] results = br.decodeFile(imgPath, "");
        StringBuilder sb = new StringBuilder();
        
        for(int i =0; i<results.length;i++){
        	sb.append((i+1));
        	sb.append("\n");
        	sb.append("Type: ");
        	sb.append(results[i].barcodeFormatString);
        	sb.append("\n");
        	sb.append("Text: ");
        	sb.append(results[i].barcodeText);
        	sb.append("\n\n");
        	showOneBox(results[i]);
        }
        
        resultTA.setText(sb.toString());
    }
 
    public void cv_MouseClicked(Event event) {
        System.out.println("cv Clicked!");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            currentImgFile = fileChooser.showOpenDialog(Main.getPrimaryStage());
            Image img = new Image(currentImgFile.toURI().toString());
            System.out.println(img.getWidth());
            cv.setWidth(img.getWidth());
            cv.setHeight(img.getHeight());
            GraphicsContext gc = cv.getGraphicsContext2D();
            gc.drawImage(img, 0, 0, cv.getWidth(), cv.getHeight());
            //doDrawing(gc);
        } catch (Exception e) {
        	
        }
    }

    private void showOneBox(TextResult result) {
    	GraphicsContext gc=cv.getGraphicsContext2D();

		Point[] points=result.localizationResult.resultPoints;
		
		double minX, minY, maxX, maxY;
		minX=points[0].x;
		minY=points[0].y;
		maxX=0;
		maxY=0;
		for (Point point : points) {
			System.out.println(point.x);
			System.out.println(point.y);
			minX=Math.min(minX,point.x);
			minY=Math.min(minY,point.y);
			maxX=Math.max(point.x,maxX);
			maxY=Math.max(point.y,maxY);
		}
		gc.setStroke(Color.RED);
		gc.setLineWidth(5);
		gc.strokeRect(minX, minY, maxX-minX, maxY-minY);
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

}
