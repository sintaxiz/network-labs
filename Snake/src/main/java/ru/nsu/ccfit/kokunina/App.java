package ru.nsu.ccfit.kokunina;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {

    private final String gameTitle = "SNAKES ONLINE";
    private final String mainMenuFXML = "main_menu.fxml";
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource(mainMenuFXML));
        primaryStage.setScene(new Scene(mainMenu));
        primaryStage.setTitle(gameTitle);
        primaryStage.setOnCloseRequest(this::close);
        primaryStage.show();
    }
    public void close(WindowEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
