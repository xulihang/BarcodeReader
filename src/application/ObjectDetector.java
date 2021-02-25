package application;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;

import org.opencv.utils.Converters;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class ObjectDetector {

	private Net net;
	private int inpHeight;
	private int inpWidth;
	public ObjectDetector(Net loadedNet,int width,int height) {
		net=loadedNet;
		inpWidth=width;
		inpHeight=height;
	}
	
	public ObjectDetector(String darkNetConfig, String modelWeights, int width,int height) {
		net = Dnn.readNetFromDarknet(darkNetConfig, modelWeights);
		inpWidth=width;
		inpHeight=height;
	}
	
	public List<Rect2d> Detect(String imgPath) throws IOException {
		Mat img = Imgcodecs.imread(imgPath); 
	    return Detect(img);	
	}
	
	public List<Rect2d> Detect(Image img) throws IOException {
		BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);
		return Detect(BufferedImage2Mat(bImage));	
	}
	
	public List<Rect2d> Detect(Mat img) {
	    net.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV);
	    net.setPreferableTarget(Dnn.DNN_TARGET_CPU);
	    //Create a 4D blob from a frame.
	    Mat blob = Dnn.blobFromImage(img, 1/255.0, new Size(inpWidth,inpHeight), new Scalar(0,0,0), false, false);

	    //Sets the input to the network
	    net.setInput(blob);
	    //Runs the forward pass to get output of the output layers
	    List<Mat> outs = new ArrayList<>();
        List<String> outBlobNames = getOutputNames();
	    net.forward(outs,outBlobNames);
	    System.out.println(outs);
	    return postprocess(img,outs,true);
	    		
	}
	
	//Get the names of the output layers
	private List<String> getOutputNames() {
	    //Get the names of all the layers in the network
	    List<String> layersNames = net.getLayerNames();
	    MatOfInt m=net.getUnconnectedOutLayers();
	    List<String> names= new ArrayList<String>();
	    for (int i=0;i<m.rows();i++) {
	    	int index = (int) m.get(i, 0)[0]-1;
	    	names.add(layersNames.get(index));
	    }	
	    //Get the names of the output layers, i.e. the layers with unconnected outputs
	    return names;
	}
	
	private List<Rect2d> postprocess(Mat img, List<Mat> result,Boolean nms) {
		int imgWidth=img.cols();
		int imgHeight=img.rows();
		 float confThreshold = 0.4f; //Insert thresholding beyond which the model will detect objects//
	        List<Integer> clsIds = new ArrayList<>();
	        List<Float> confs = new ArrayList<>();
	        List<Rect2d> rects2d = new ArrayList<>();
	        int num=0;
	        for (int i = 0; i < result.size(); ++i)
	        {
	            // each row is a candidate detection, the 1st 4 numbers are
	            // [center_x, center_y, width, height], followed by (N-4) class probabilities
	            Mat level = result.get(i);
	            for (int j = 0; j < level.rows(); ++j)
	            {
	                Mat row = level.row(j);
	                Mat scores = row.colRange(5, level.cols());
	                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
	                float confidence = (float)mm.maxVal;
	                Point classIdPoint = mm.maxLoc;
	                if (confidence > confThreshold)
	                {
	                	num=num+1;
	                    int centerX = (int)(row.get(0,0)[0] * imgWidth); //scaling for drawing the bounding boxes//
	                    int centerY = (int)(row.get(0,1)[0] * imgHeight);
	                    int width   = (int)(row.get(0,2)[0] * imgWidth);
	                    int height  = (int)(row.get(0,3)[0] * imgHeight);
	                    int left    = centerX - width  / 2;
	                    int top     = centerY - height / 2;

	                    clsIds.add((int)classIdPoint.x);
	                    confs.add((float)confidence);
	                    rects2d.add(new Rect2d(left,top,width,height));
	                }
	            }
	        }
	        if (nms==true) {
		        float nmsThresh = 0.5f;
		        MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
		        Rect2d[] boxesArray = rects2d.toArray(new Rect2d[0]);
		        MatOfRect2d boxes = new MatOfRect2d(boxesArray);
		        //boxes.fromArray(boxesArray);
		        MatOfInt indices = new MatOfInt();
		        Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);
		        int [] ind = indices.toArray();
		        List<Rect2d> rectsAfterNMS = new ArrayList<>();
		        for (int i = 0; i < ind.length; ++i)
		        {
		            int idx = ind[i];
		            Rect2d box = boxesArray[idx];
		            int width   = (int)box.width;
                    int height  = (int)box.height;
                    int left    = (int)box.x;
                    int top     = (int)box.y;
		            rectsAfterNMS.add(new Rect2d(left,top,width,height));
		        }	 
		        return rectsAfterNMS;	
	        }else {
	        	return rects2d;	
	        }
	}
	
	public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ImageIO.write(image, "jpg", byteArrayOutputStream);
	    byteArrayOutputStream.flush();
	    return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
	}
}

