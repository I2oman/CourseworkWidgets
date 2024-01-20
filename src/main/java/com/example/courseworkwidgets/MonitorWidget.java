package com.example.courseworkwidgets;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class MonitorWidget {
    private double offsetX;
    private double offsetY;
    public double windowX;
    public double windowY;
    private Stage primaryStage;
    public Stage stage;
    private SystemMonitorController systemMonitorController;
    private CPUMonitorController cpuMonitorController;
    private MemoryMonitorController memoryMonitorController;

    public MonitorWidget() {
        this.offsetX = 0;
        this.offsetY = 0;
        this.windowX = 0;
        this.windowY = 0;
        this.primaryStage = new Stage();
        this.stage = new Stage();
        this.systemMonitorController = null;
        this.cpuMonitorController = null;
        this.memoryMonitorController = null;
    }

    public void start(MainApplication mainApplication, SystemMonitor systemMonitor, String monitorType,
                      double sliderValue, String styleId, boolean darkTheme) throws IOException {
        //Creates the weather widget stage with a transparent background,
        // loads it as a utility to run without an icon in the taskbar,
        // and removes window borders
        primaryStage = new Stage();
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.show();

        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setOpacity(sliderValue);
        stage.initOwner(primaryStage);
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                stage.toBack();
//                stage.toFront();
            }
        });

        //Loads a stage
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(String.format("%sMonitor/%s.fxml", monitorType, styleId)));
        Parent root = fxmlLoader.load();
        //Sets a handler for mouse movement on the window
        root.setOnMousePressed(event -> {
            offsetX = event.getSceneX();
            offsetY = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - offsetX);
            stage.setY(event.getScreenY() - offsetY);
        });
        root.setOnMouseReleased(event -> {
            windowX = stage.getX();
            windowY = stage.getY();

            mainApplication.programProperties.savePrefs(monitorType + "MonitorX", stage.getX());
            mainApplication.programProperties.savePrefs(monitorType + "MonitorY", stage.getY());
        });
        //Creates a scene
        Scene scene = new Scene(root);
        //Applies a style to a scene
        String stylePath = "/css/monitors/";
        if (darkTheme) {
            stylePath += "dark-theme.css";
        } else {
            stylePath += "light-theme.css";
        }
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(stylePath)).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("monitors");
        stage.setScene(scene);

        stage.setX(windowX);
        stage.setY(windowY);
        stage.setResizable(false);

        //Passes the 'systemMonitor' to the widget's controller to display it in chart(s)
        try {
            systemMonitorController = fxmlLoader.getController();
            systemMonitorController.start(systemMonitor);
        } catch (Exception ignored) {
        }
        try {
            cpuMonitorController = fxmlLoader.getController();
            cpuMonitorController.start(systemMonitor);
        } catch (Exception ignored) {
        }
        try {
            memoryMonitorController = fxmlLoader.getController();
            memoryMonitorController.start(systemMonitor);
        } catch (Exception ignored) {
        }

        stage.show();
    }

    public Stage getStage() {
        return stage;
    }

    public void setWindowX(double windowX) {
        this.windowX = windowX;
    }

    public void setWindowY(double windowY) {
        this.windowY = windowY;
    }

    public void exit() {
        //Stops monitoring and closes stages
        if (systemMonitorController != null) {
            systemMonitorController.exitWidget();
        }
        if (cpuMonitorController != null) {
            cpuMonitorController.exitWidget();
        }
        if (memoryMonitorController != null) {
            memoryMonitorController.exitWidget();
        }
        primaryStage.close();
        stage.close();
    }
}
