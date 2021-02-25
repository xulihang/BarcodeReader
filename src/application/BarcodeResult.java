package application;

import java.util.List;

import javafx.geometry.Point2D;

public class BarcodeResult {

	public List<Point2D> points;
	public String text;
	public String format;	
	
	public BarcodeResult(List<Point2D> detectedPoints,String detectedText,String detectedFormat) {
		points=detectedPoints;
		text=detectedText;
		format=detectedFormat;
	}
}
