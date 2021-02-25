package application;
	
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {
	private static Stage primaryStage; // **Declare static Stage**

    private void setPrimaryStage(Stage stage) {
        Main.primaryStage = stage;
    }

    static public Stage getPrimaryStage() {
        return Main.primaryStage;
    }
    
	@Override
	public void start(Stage primaryStage) {
		setPrimaryStage(primaryStage);
		System.load("C:\\\\Users\\\\admin\\\\Documents\\\\B4J\\\\ImageTrans\\\\Objects\\\\opencv_java450.dll");
		try {
            // Read file fxml and draw interface.
            Parent root = FXMLLoader.load(getClass()
                    .getResource("/resources/Main.fxml"));
            primaryStage.setTitle("My Application");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
