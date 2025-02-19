package com.salawubabatunde.seafarerbiometric.controllers;

import com.salawubabatunde.seafarerbiometric.MFXResourcesLoader;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;

public class LoaderController {

    private MFXGenericDialog dialogContent;
    private MFXStageDialog dialog;


    public LoaderController(Stage stage, String indicatorText) {

        AnchorPane rootPane = (AnchorPane) stage.getScene().getRoot();

        MFXProgressSpinner spinner = new MFXProgressSpinner();

        VBox loaderContent = new VBox();
        loaderContent.setAlignment(Pos.CENTER);
        loaderContent.setSpacing(5);
        Label indicator = new Label(indicatorText);

        loaderContent.getChildren().addAll(spinner, indicator);
        StackPane contentWrapper = new StackPane(loaderContent);
        contentWrapper.setPrefSize(100, 80); // Force a small size
        contentWrapper.setMaxSize(100, 80);
        spinner.setPrefSize(25, 25);

        spinner.getStyleClass().add("loadSpinner");
        contentWrapper.getStyleClass().add("loaderContainer");

        this.dialogContent = MFXGenericDialogBuilder.build()
                .setContent(contentWrapper)
                .setShowClose(false)
                .setShowMinimize(false)
                .setShowAlwaysOnTop(false)
                .get();

        this.dialog = MFXGenericDialogBuilder.build(dialogContent)
                .toStageDialogBuilder()
                .initOwner(stage)
                .initModality(Modality.APPLICATION_MODAL)
                .setOwnerNode(rootPane)
                .setScrimPriority(ScrimPriority.WINDOW)
                .setScrimOwner(true)
                .get();


        dialogContent.getStylesheets().add(MFXResourcesLoader.load("css/style.css"));


    }

    @FXML
    private void openInfo(ActionEvent event) {
        MFXFontIcon infoIcon = new MFXFontIcon("fas-circle-info", 18);
        dialogContent.setHeaderIcon(infoIcon);
        dialogContent.setHeaderText("This is a generic info dialog");
        convertDialogTo("mfx-info-dialog");
        dialog.showDialog();
    }

    public void loading(){
        // Ensure dialogContent is not null and set properties safely
        if (dialogContent != null) {
            convertDialogTo("mfx-dialog");
            dialog.showDialog();
        } else {
            System.err.println("Dialog content is not initialized.");
        }
    }


    public void disMissLoader(){
        if(dialog != null){
            dialog.close();
        }
    }

    @FXML
    private void openWarning(ActionEvent event) {
        MFXFontIcon warnIcon = new MFXFontIcon("fas-circle-exclamation", 18);
        dialogContent.setHeaderIcon(warnIcon);
        dialogContent.setHeaderText("This is a warning info dialog");
        convertDialogTo("mfx-warn-dialog");
        dialog.showDialog();
    }

    @FXML
    private void openError(ActionEvent event) {
        MFXFontIcon errorIcon = new MFXFontIcon("fas-circle-xmark", 18);
        dialogContent.setHeaderIcon(errorIcon);
        dialogContent.setHeaderText("This is a error info dialog");
        convertDialogTo("mfx-error-dialog");
        dialog.showDialog();
    }

    @FXML
    private void openGeneric(ActionEvent event) {
        dialogContent.setHeaderIcon(null);
        dialogContent.setHeaderText("This is a generic dialog");
        convertDialogTo(null);
        dialog.showDialog();
    }

    private void convertDialogTo(String styleClass) {
        dialogContent.getStyleClass().removeIf(
                s -> s.equals("mfx-info-dialog") || s.equals("mfx-warn-dialog") || s.equals("mfx-error-dialog")
        );

        if (styleClass != null)
            dialogContent.getStyleClass().add(styleClass);
    }
}
