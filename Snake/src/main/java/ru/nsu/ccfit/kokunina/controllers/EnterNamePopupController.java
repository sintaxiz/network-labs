package ru.nsu.ccfit.kokunina.controllers;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EnterNamePopupController implements Initializable {
    public TextField nameTextField;
    public Button okayButton;

    private static final String POPUP_FXML = "enter_name_popup.fxml";

    private String name = null;
    private Stage stage = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okayButton.setOnAction(event -> {
            name = nameTextField.getText();
            closeStage();
        });
    }

    private void closeStage() {
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Creates popup window asking name.
     * @return name -- can be null if popup was closed
     * @throws IOException if can not create popup window
     */
    public static String askName() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(EnterNamePopupController.class.getClassLoader().getResource(POPUP_FXML));
        Parent layout = loader.load();

        EnterNamePopupController popupController = loader.getController();

        Scene scene = new Scene(layout);
        Stage popupStage = new Stage();
        popupController.setStage(popupStage);

        popupStage.setOnCloseRequest(event -> {
            popupController.setName(null);
        });
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        return popupController.getName();
    }

    private void setStage(Stage popupStage) {
        stage = popupStage;
    }

    private String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }
}
