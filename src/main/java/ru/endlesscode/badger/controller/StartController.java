package ru.endlesscode.badger.controller;

import javafx.event.ActionEvent;
import ru.endlesscode.badger.Badger;

public class StartController {
    public void onCreateNewClicked(ActionEvent actionEvent) {
        Badger.gotoCreatingNew();
    }
}
