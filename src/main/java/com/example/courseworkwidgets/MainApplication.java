package com.example.courseworkwidgets;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.Menu;
import java.awt.MenuItem;
import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    public Stage primaryStage;
    public double rootX;
    public double rootY;
    public ProgramProperties programProperties;
    public CheckboxMenuItem showWeather;
    public CheckboxMenuItem showSystemMonitor;
    public CheckboxMenuItem showCPUMonitor;
    public CheckboxMenuItem showMemoryMonitor;
    public CheckboxMenuItem globalDarkTheme;
    private MainController mainController;
    private TrayIcon trayIcon;
    private double xOffset = 0;
    private double yOffset = 0;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.TRANSPARENT);

        Platform.setImplicitExit(false);

        programProperties = new ProgramProperties();
//        programProperties.clearPrefs();
//        System.exit(666);
//        programProperties.showPrefs();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        root.setOnMouseReleased(event -> {
            programProperties.savePrefs("rootX", stage.getX());
            programProperties.savePrefs("rootY", stage.getY());
        });
        Scene scene = new Scene(root, 220, 350, Color.TRANSPARENT);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/mainMenu/dark-theme.css")).toExternalForm());

        stage.setTitle("mainMenu");
        stage.setScene(scene);

        Rectangle2D sc = Screen.getPrimary().getBounds();
        rootX = sc.getWidth() / 2 - scene.getWidth() / 2;
        rootY = sc.getHeight() / 2 - scene.getHeight() / 2;

        try {
            rootX = Double.parseDouble(programProperties.loadPrefs("rootX"));
            rootY = Double.parseDouble(programProperties.loadPrefs("rootY"));
        } catch (Exception ignored) {
        }

        stage.setX(rootX);
        stage.setY(rootY);

        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResource("/images/Logo_16x16.png").openStream()));

        stage.setOnCloseRequest(e -> Platform.runLater(stage::hide));

        mainController = fxmlLoader.getController();

        mainController.init(this);

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popupMenu = new PopupMenu();

            Menu widgetsMenu = new Menu("Widgets");

            showWeather = new CheckboxMenuItem("Weather");
            showWeather.addItemListener(event -> {
                Platform.runLater(() -> {
                    mainController.weatherCheckBox.setSelected(showWeather.getState());
                    try {
                        mainController.weather(showWeather.getState());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            });

            showCPUMonitor = new CheckboxMenuItem("CPU Monitor");
            showCPUMonitor.addItemListener(event -> {
                Platform.runLater(() -> {
                    mainController.cpuMonitorCheckBox.setSelected(showCPUMonitor.getState());
                    try {
                        mainController.cpuMonitor(showCPUMonitor.getState());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            });

            showMemoryMonitor = new CheckboxMenuItem("Memory Monitor");
            showMemoryMonitor.addItemListener(event -> {
                Platform.runLater(() -> {
                    mainController.memoryMonitorCheckBox.setSelected(showMemoryMonitor.getState());
                    try {
                        mainController.memoryMonitor(showMemoryMonitor.getState());
                    } catch (Exception ignored) {
                    }
                });
            });

            showSystemMonitor = new CheckboxMenuItem("System Monitor");
            showSystemMonitor.addItemListener(e -> {
                Platform.runLater(() -> {
                    mainController.systemMonitorCheckBox.setSelected(showSystemMonitor.getState());
                    try {
                        mainController.systemMonitor(showSystemMonitor.getState());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            });

            widgetsMenu.add(showWeather);
            widgetsMenu.addSeparator();
            widgetsMenu.add(showSystemMonitor);
            widgetsMenu.add(showCPUMonitor);
            widgetsMenu.add(showMemoryMonitor);

            globalDarkTheme = new CheckboxMenuItem("Global Dark Theme");
            globalDarkTheme.setState(true);
            globalDarkTheme.addItemListener(e -> Platform.runLater(() -> {
                mainController.globalDarkThemeCheckBox.setSelected(globalDarkTheme.getState());
                mainController.globalDarkTheme(globalDarkTheme.getState());
            }));

            MenuItem closeProgram = new MenuItem("Exit");
            closeProgram.addActionListener(e -> {
                Platform.runLater(() -> {
                    mainController.exit();
                    tray.remove(trayIcon);
                    Platform.exit();
                });
                System.exit(0);
            });


            popupMenu.add(widgetsMenu);
            popupMenu.add(globalDarkTheme);
            popupMenu.addSeparator();
            popupMenu.add(closeProgram);

            trayIcon = new TrayIcon(
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Logo_16x16.png")),
                    "Coursework Widgets",
                    popupMenu
            );

            trayIcon.setImageAutoSize(true);

            // Hide/show on double click
            trayIcon.addActionListener(e -> {
                if (stage.isShowing()) {
                    Platform.runLater(stage::hide);
                    programProperties.savePrefs("mainMenu", false);
                } else {
                    Platform.runLater(stage::show);
                    stage.setX(rootX);
                    stage.setY(rootY);
                    programProperties.savePrefs("mainMenu", true);
                }
            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
            }
        } else {
            // System tray is not supported
            Alert alert = new Alert(Alert.AlertType.ERROR, "System tray not supported", ButtonType.OK);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.OK) {
                Platform.exit();
            }
        }

        primaryStage = stage;

        programProperties.applyPrefs(this, mainController, true);
    }

    public void confGlobalDarkScheme() {
        CheckBox[] allWidgetsDarkSchemeCheckBoxes = {
                mainController.mainMenuDarkThemeCheckBox,
                mainController.weatherDarkThemeCheckBox,
                mainController.systemMonitorDarkThemeCheckBox,
                mainController.cpuMonitorDarkThemeCheckBox,
                mainController.memoryMonitorDarkThemeCheckBox
        };
        int totalDark = 0;
        for (CheckBox widgetsBox : allWidgetsDarkSchemeCheckBoxes) {
            if (widgetsBox.isSelected()) {
                totalDark++;
            }
        }
        if (totalDark == 0) {
            mainController.globalDarkThemeCheckBox.setIndeterminate(false);
            mainController.globalDarkThemeCheckBox.setSelected(false);
            globalDarkTheme.setState(false);
        } else if (totalDark == allWidgetsDarkSchemeCheckBoxes.length) {
            mainController.globalDarkThemeCheckBox.setIndeterminate(false);
            mainController.globalDarkThemeCheckBox.setSelected(true);
            globalDarkTheme.setState(true);
        } else {
            mainController.globalDarkThemeCheckBox.setIndeterminate(true);
            globalDarkTheme.setState(false);
        }
    }
}