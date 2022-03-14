package com.example.checkers;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class OpeningController implements Initializable {

    public static boolean startClicked = false;

    public boolean dark;

    @FXML
    private Button start;

    @FXML
    private CheckBox firstTurn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.dark = true;
        this.firstTurn.setSelected(true);
    }

    @FXML
    public void onLabel() {
        if (this.firstTurn.isSelected()) {
            this.firstTurn.setSelected(false);
        }
        else {
            this.firstTurn.setSelected(true);
        }
    }

    @FXML
    public void onStart() {
        OpeningController.startClicked = true;
        OpeningMetadata transfer = new OpeningMetadata(this.firstTurn.isSelected());
        this.start.getScene().getWindow().setUserData(transfer);
        this.start.getScene().getWindow().getOnCloseRequest().handle(new WindowEvent(this.start.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
