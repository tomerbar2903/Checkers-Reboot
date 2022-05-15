package com.example.checkers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClosingController {

    @FXML
    private Button closeButton;

    @FXML
    private Text message;

    @FXML
    public void setMessage(String message) {
        this.message.setText(message);
    }

    @FXML
    public void onClose() {
        Stage mainCheckers = (Stage) this.closeButton.getScene().getUserData();
        mainCheckers.getOnCloseRequest().handle(new WindowEvent(this.closeButton.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
        ((Stage) this.closeButton.getScene().getWindow()).close();
        mainCheckers.close();
    }

}
