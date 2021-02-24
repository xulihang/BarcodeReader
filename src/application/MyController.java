package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.TextResult;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class MyController implements Initializable {
	
    private BarcodeReader br;
	@FXML
    private Button loadBtn;
	@FXML
    private AnchorPane root;
    @FXML
    private TextArea resultTA;
    @FXML
    private ImageView iv;
	 //更多请阅读：https://www.yiibai.com/javafx/javafx-tutorial-for-beginners.html

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
        BufferedImage bImage = SwingFXUtils.fromFXImage(iv.getImage(), null);
		
		TextResult[] result = br.decodeBufferedImage(bImage, "");
        StringBuilder sb = new StringBuilder();
        
        for(int i =0; i<result.length;i++){
        	sb.append(i+1);
        	sb.append("\n");
        	sb.append("Type: ");
        	sb.append(result[i].barcodeFormatString);
        	sb.append("\n");
        	sb.append("Text: ");
        	sb.append(result[i].barcodeText);
        	sb.append("\n\n");
        }
        resultTA.setText(sb.toString());
    }
 
    public void iv_MouseClicked(Event event) {
        System.out.println("iv Clicked!");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File imgFile = fileChooser.showOpenDialog(Main.getPrimaryStage());
            URI url = imgFile.toURI();
            Image img = new Image(url.toString());
            iv.setImage(img);   
        } catch (Exception e) {
        	
        }
    }
    
    private void showBoxes(TextResult[] result) {
    	ImageView box = new ImageView();
    	box.setImage(null);
    }

}
