package com.example.courseworkwidgets;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static java.lang.String.format;

public class MainController implements Initializable {
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public SVGPath menuSVG;
    @FXML
    public AnchorPane widgetsPane;
    @FXML
    public AnchorPane menuPane;
    @FXML
    public CheckBox weatherCheckBox;
    @FXML
    public CheckBox weatherDarkThemeCheckBox;
    @FXML
    public Slider weatherOpacitySlider;
    @FXML
    public TextField cityTextField;
    @FXML
    public ToggleGroup systemMonitorStyleGroup;
    @FXML
    public CheckBox systemMonitorCheckBox;
    @FXML
    public CheckBox systemMonitorDarkThemeCheckBox;
    @FXML
    public Slider systemMonitorOpacitySlider;
    @FXML
    public ToggleGroup cpuMonitorStyleGroup;
    @FXML
    public CheckBox cpuMonitorCheckBox;
    @FXML
    public CheckBox cpuMonitorDarkThemeCheckBox;
    @FXML
    public Slider cpuMonitorOpacitySlider;
    @FXML
    public ToggleGroup memoryMonitorStyleGroup;
    @FXML
    public CheckBox memoryMonitorCheckBox;
    @FXML
    public CheckBox memoryMonitorDarkThemeCheckBox;
    @FXML
    public Slider memoryMonitorOpacitySlider;
    @FXML
    public ToggleGroup unitsGroup;
    @FXML
    public CheckBox globalDarkThemeCheckBox;
    @FXML
    public Slider globalOpacitySlider;
    @FXML
    public CheckBox mainMenuDarkThemeCheckBox;
    @FXML
    public CheckBox mainMenuOnStartupCheckBox;
    private MainApplication mainApplication;
    private double weatherOffsetX;
    private double weatherOffsetY;
    public double weatherX;
    public double weatherY;
    private Stage weatherPrimaryStage;
    public Stage weatherStage;
    private WeatherController weatherController;
    private double systemMonitorOffsetX;
    private double systemMonitorOffsetY;
    public double systemMonitorX;
    public double systemMonitorY;
    private Stage systemMonitorPrimaryStage;
    public Stage systemMonitorStage;
    private SystemMonitorController systemMonitorController;
    private SystemMonitor systemMonitor;
    private double cpuMonitorOffsetX;
    private double cpuMonitorOffsetY;
    public double cpuMonitorX;
    public double cpuMonitorY;
    private Stage cpuMonitorPrimaryStage;
    public Stage cpuMonitorStage;
    private CPUMonitorController CPUMonitorController;
    private double memoryMonitorOffsetX;
    private double memoryMonitorOffsetY;
    public double memoryMonitorX;
    public double memoryMonitorY;
    private Stage memoryMonitorPrimaryStage;
    public Stage memoryMonitorStage;
    private MemoryMonitorController memoryMonitorController;
    private String openMenuSVG = "M 16 12 L 0 12 L 0 14 L 16 14 L 16 6 L 0 6 L 0 8 L 16 8 L 16 0 L 0 0 L 0 2 L 16 2";
    private String closeMenuSVG = "M 16 14 L 6 7 L 16 0 L 16 3 L 10 7 L 16 11 Z";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        anchorPane.setBackground(Background.fill(Color.TRANSPARENT));

        menuSVG.setContent(openMenuSVG);

        Thread requestCity = new Thread(() -> {
            JsonObject jsonObject = GetJsonResponse.get("https://ipinfo.io/json");
            if (jsonObject != null) {
                cityTextField.setText(jsonObject.get("city").getAsString());
            } else {
                cityTextField.setText("Not Found");
            }
        });
        requestCity.start();

        unitsGroup.selectedToggleProperty().addListener((observableValue, toggle, newToggle) -> {
            mainApplication.programProperties.savePrefs("unitsGroup", ((RadioButton) newToggle).getId());
            try {
                weatherController.setUnints(((RadioButton) newToggle).getId());
            } catch (Exception ignored) {
            }
        });

        globalOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            globalOpacitySlider.lookup(".track").setStyle(format("-fx-background-color: linear-gradient(to right, #646464 %s, #64646433 %s)", newValue.doubleValue(), newValue.doubleValue()));
            setSingleWidgetOpacity(weatherOpacitySlider, weatherStage, newValue);
            setSingleWidgetOpacity(systemMonitorOpacitySlider, systemMonitorStage, newValue);
            setSingleWidgetOpacity(cpuMonitorOpacitySlider, cpuMonitorStage, newValue);
            setSingleWidgetOpacity(memoryMonitorOpacitySlider, memoryMonitorStage, newValue);
        });

        weatherOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            singleWidgetOpacity(weatherOpacitySlider, weatherStage, newValue);
        });
        systemMonitorOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            singleWidgetOpacity(systemMonitorOpacitySlider, systemMonitorStage, newValue);
        });
        cpuMonitorOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            singleWidgetOpacity(cpuMonitorOpacitySlider, cpuMonitorStage, newValue);
        });
        memoryMonitorOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            singleWidgetOpacity(memoryMonitorOpacitySlider, memoryMonitorStage, newValue);
        });

        systemMonitorStyleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            mainApplication.programProperties.savePrefs("systemMonitorStyleGroup", ((RadioButton) newToggle).getId());
            if (systemMonitorCheckBox.isSelected()) {
                try {
                    systemMonitor(false);
                    systemMonitor(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        cpuMonitorStyleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            mainApplication.programProperties.savePrefs("cpuMonitorStyleGroup", ((RadioButton) newToggle).getId());
            if (cpuMonitorCheckBox.isSelected()) {
                try {
                    cpuMonitor(false);
                    cpuMonitor(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        memoryMonitorStyleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            mainApplication.programProperties.savePrefs("memoryMonitorStyleGroup", ((RadioButton) newToggle).getId());
            if (memoryMonitorCheckBox.isSelected()) {
                try {
                    memoryMonitor(false);
                    memoryMonitor(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        systemMonitor = new SystemMonitor();
    }

    public void init(MainApplication mainApplication) {
        this.mainApplication = mainApplication;
    }

    public void minimizeMainMenuHandler(ActionEvent actionEvent) {
        mainApplication.primaryStage.setIconified(true);
    }

    public void closeMainMenuHandler(ActionEvent actionEvent) {
        mainApplication.primaryStage.close();
        mainApplication.programProperties.savePrefs("mainMenu", false);
    }

    public void menuBtn(ActionEvent actionEvent) {
        if (menuSVG.getContent().equals(openMenuSVG)) {
            menuSVG.setContent(closeMenuSVG);
            widgetsPane.setVisible(false);
            menuPane.setVisible(true);
        } else {
            menuSVG.setContent(openMenuSVG);
            menuPane.setVisible(false);
            widgetsPane.setVisible(true);
        }
    }


    public void weatherHandler(ActionEvent actionEvent) throws IOException {
        mainApplication.showWeather.setState(weatherCheckBox.isSelected());
        weather(weatherCheckBox.isSelected());
    }

    public void weatherDarkThemeHandler(ActionEvent actionEvent) {
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(weatherDarkThemeCheckBox.isSelected(), weatherStage);
        mainApplication.programProperties.savePrefs("weatherDarkThemeCheckBox", weatherDarkThemeCheckBox.isSelected());
    }

    private Timer timer = new Timer();

    public void onCityChange(KeyEvent keyEvent) {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!Objects.equals(weatherController.city, cityTextField.getText())) {
                        weatherController.setCity(cityTextField.getText());
                        mainApplication.programProperties.savePrefs("cityTextField", cityTextField.getText());
                    }
                } catch (Exception ignored) {
                }
            }
        }, 3000);
    }

    public void weather(boolean show) throws IOException {
        mainApplication.programProperties.savePrefs("weather", show);
        if (show) {
            weatherPrimaryStage = new Stage();
            weatherPrimaryStage.initStyle(StageStyle.UTILITY);
            weatherPrimaryStage.setOpacity(0);
            weatherPrimaryStage.show();

            weatherStage = new Stage();
            weatherStage.initStyle(StageStyle.TRANSPARENT);
            weatherStage.setOpacity(weatherOpacitySlider.getValue());
            weatherStage.initOwner(weatherPrimaryStage);
            weatherStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    weatherStage.toBack();
//                    weatherStage.toFront();
                }
            });


            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("weather-view.fxml"));
            Parent root = fxmlLoader.load();
            root.setOnMousePressed(event -> {
                weatherOffsetX = event.getSceneX();
                weatherOffsetY = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                weatherStage.setX(event.getScreenX() - weatherOffsetX);
                weatherStage.setY(event.getScreenY() - weatherOffsetY);
            });
            root.setOnMouseReleased(event -> {
                weatherX = weatherStage.getX();
                weatherY = weatherStage.getY();
                mainApplication.programProperties.savePrefs("weatherX", weatherStage.getX());
                mainApplication.programProperties.savePrefs("weatherY", weatherStage.getY());
            });
            Scene scene = new Scene(root, WeatherController.windowCenterX * 2, WeatherController.windowCenterY * 2);
            String stylePath = "/css/weather/";
            if (weatherDarkThemeCheckBox.isSelected()) {
                stylePath += "dark-theme.css";
            } else {
                stylePath += "light-theme.css";
            }
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(stylePath)).toExternalForm());
            scene.setFill(Color.TRANSPARENT);
            weatherStage.setTitle("weather");
            weatherStage.setScene(scene);

            weatherStage.setX(weatherX);
            weatherStage.setY(weatherY);
            weatherStage.setResizable(false);

            weatherController = fxmlLoader.getController();

            String units = ((RadioButton) unitsGroup.getSelectedToggle()).getId();
            weatherController.setCity(cityTextField.getText(), units);
            mainApplication.programProperties.savePrefs("cityTextField", cityTextField.getText());

            weatherStage.show();
        } else {
            weatherController.exitWidget();
            weatherPrimaryStage.close();
            weatherStage.close();
        }
    }


    public void systemMonitorHandler(ActionEvent actionEvent) throws IOException {
        mainApplication.showSystemMonitor.setState(systemMonitorCheckBox.isSelected());
        systemMonitor(systemMonitorCheckBox.isSelected());
    }

    public void systemMonitorDarkThemeHandler(ActionEvent actionEvent) {
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(systemMonitorDarkThemeCheckBox.isSelected(), systemMonitorStage);
        mainApplication.programProperties.savePrefs("systemMonitorDarkThemeCheckBox", systemMonitorDarkThemeCheckBox.isSelected());
    }

    public void systemMonitor(boolean show) throws IOException {
        mainApplication.programProperties.savePrefs("systemMonitor", show);
        if (show) {
            systemMonitorPrimaryStage = new Stage();
            systemMonitorPrimaryStage.initStyle(StageStyle.UTILITY);
            systemMonitorPrimaryStage.setOpacity(0);
            systemMonitorPrimaryStage.show();

            systemMonitorStage = new Stage();
            systemMonitorStage.initStyle(StageStyle.TRANSPARENT);
            systemMonitorStage.setOpacity(systemMonitorOpacitySlider.getValue());
            systemMonitorStage.initOwner(systemMonitorPrimaryStage);
            systemMonitorStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    systemMonitorStage.toBack();
//                    systemMonitorStage.toFront();
                }
            });

            String view = ((RadioButton) systemMonitorStyleGroup.getSelectedToggle()).getId();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(String.format("systemMonitor/%s.fxml", view)));
            Parent root = fxmlLoader.load();
            root.setOnMousePressed(event -> {
                systemMonitorOffsetX = event.getSceneX();
                systemMonitorOffsetY = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                systemMonitorStage.setX(event.getScreenX() - systemMonitorOffsetX);
                systemMonitorStage.setY(event.getScreenY() - systemMonitorOffsetY);
            });
            root.setOnMouseReleased(event -> {
                systemMonitorX = systemMonitorStage.getX();
                systemMonitorY = systemMonitorStage.getY();
                mainApplication.programProperties.savePrefs("systemMonitorX", systemMonitorStage.getX());
                mainApplication.programProperties.savePrefs("systemMonitorY", systemMonitorStage.getY());
            });
            Scene scene = new Scene(root);
            String stylePath = "/css/monitors/";
            if (systemMonitorDarkThemeCheckBox.isSelected()) {
                stylePath += "dark-theme.css";
            } else {
                stylePath += "light-theme.css";
            }
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(stylePath)).toExternalForm());
            scene.setFill(Color.TRANSPARENT);
            systemMonitorStage.setTitle("monitors");
            systemMonitorStage.setScene(scene);

            systemMonitorStage.setX(systemMonitorX);
            systemMonitorStage.setY(systemMonitorY);
            systemMonitorStage.setResizable(false);

            systemMonitorController = fxmlLoader.getController();
            systemMonitorController.start(systemMonitor);
            systemMonitorStage.show();
        } else {
            systemMonitorController.exitWidget();
            systemMonitorPrimaryStage.close();
            systemMonitorStage.close();
        }
    }


    public void cpuMonitorHandler(ActionEvent actionEvent) throws IOException {
        mainApplication.showCPUMonitor.setState(cpuMonitorCheckBox.isSelected());
        cpuMonitor(cpuMonitorCheckBox.isSelected());
    }

    public void cpuMonitorDarkThemeHandler(ActionEvent actionEvent) {
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(cpuMonitorDarkThemeCheckBox.isSelected(), cpuMonitorStage);
        mainApplication.programProperties.savePrefs("cpuMonitorDarkThemeCheckBox", cpuMonitorDarkThemeCheckBox.isSelected());
    }

    public void cpuMonitor(boolean show) throws IOException {
        mainApplication.programProperties.savePrefs("cpuMonitor", show);
        if (show) {
            cpuMonitorPrimaryStage = new Stage();
            cpuMonitorPrimaryStage.initStyle(StageStyle.UTILITY);
            cpuMonitorPrimaryStage.setOpacity(0);
            cpuMonitorPrimaryStage.show();

            cpuMonitorStage = new Stage();
            cpuMonitorStage.initStyle(StageStyle.TRANSPARENT);
            cpuMonitorStage.setOpacity(cpuMonitorOpacitySlider.getValue());
            cpuMonitorStage.initOwner(cpuMonitorPrimaryStage);
            cpuMonitorStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    cpuMonitorStage.toBack();
//                    cpuMonitorStage.toFront();
                }
            });

            String view = ((RadioButton) cpuMonitorStyleGroup.getSelectedToggle()).getId();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(String.format("cpuMonitor/%s.fxml", view)));
            Parent root = fxmlLoader.load();
            root.setOnMousePressed(event -> {
                cpuMonitorOffsetX = event.getSceneX();
                cpuMonitorOffsetY = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                cpuMonitorStage.setX(event.getScreenX() - cpuMonitorOffsetX);
                cpuMonitorStage.setY(event.getScreenY() - cpuMonitorOffsetY);
            });
            root.setOnMouseReleased(event -> {
                cpuMonitorX = cpuMonitorStage.getX();
                cpuMonitorY = cpuMonitorStage.getY();
                mainApplication.programProperties.savePrefs("cpuMonitorX", cpuMonitorStage.getX());
                mainApplication.programProperties.savePrefs("cpuMonitorY", cpuMonitorStage.getY());
            });
            Scene scene = new Scene(root);
            String stylePath = "/css/monitors/";
            if (cpuMonitorDarkThemeCheckBox.isSelected()) {
                stylePath += "dark-theme.css";
            } else {
                stylePath += "light-theme.css";
            }
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(stylePath)).toExternalForm());
            scene.setFill(Color.TRANSPARENT);
            cpuMonitorStage.setTitle("monitors");
            cpuMonitorStage.setScene(scene);

            cpuMonitorStage.setX(cpuMonitorX);
            cpuMonitorStage.setY(cpuMonitorY);
            cpuMonitorStage.setResizable(false);

            CPUMonitorController = fxmlLoader.getController();
            CPUMonitorController.start(systemMonitor);
            cpuMonitorStage.show();
        } else {
            CPUMonitorController.exitWidget();
            cpuMonitorPrimaryStage.close();
            cpuMonitorStage.close();
        }
    }


    public void memoryMonitorHandler(ActionEvent actionEvent) throws IOException {
        mainApplication.showMemoryMonitor.setState(memoryMonitorCheckBox.isSelected());
        memoryMonitor(memoryMonitorCheckBox.isSelected());
    }

    public void memoryMonitorDarkThemeHandler(ActionEvent actionEvent) {
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(memoryMonitorDarkThemeCheckBox.isSelected(), memoryMonitorStage);
        mainApplication.programProperties.savePrefs("memoryMonitorDarkThemeCheckBox", memoryMonitorDarkThemeCheckBox.isSelected());
    }

    public void memoryMonitor(boolean show) throws IOException {
        mainApplication.programProperties.savePrefs("memoryMonitor", show);
        if (show) {
            memoryMonitorPrimaryStage = new Stage();
            memoryMonitorPrimaryStage.initStyle(StageStyle.UTILITY);
            memoryMonitorPrimaryStage.setOpacity(0);
            memoryMonitorPrimaryStage.show();

            memoryMonitorStage = new Stage();
            memoryMonitorStage.initStyle(StageStyle.TRANSPARENT);
            memoryMonitorStage.setOpacity(memoryMonitorOpacitySlider.getValue());
            memoryMonitorStage.initOwner(memoryMonitorPrimaryStage);
            memoryMonitorStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    memoryMonitorStage.toBack();
//                    memoryMonitorStage.toFront();
                }
            });

            String view = ((RadioButton) memoryMonitorStyleGroup.getSelectedToggle()).getId();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(String.format("memoryMonitor/%s.fxml", view)));
            Parent root = fxmlLoader.load();
            root.setOnMousePressed(event -> {
                memoryMonitorOffsetX = event.getSceneX();
                memoryMonitorOffsetY = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                memoryMonitorStage.setX(event.getScreenX() - memoryMonitorOffsetX);
                memoryMonitorStage.setY(event.getScreenY() - memoryMonitorOffsetY);
            });
            root.setOnMouseReleased(event -> {
                memoryMonitorX = memoryMonitorStage.getX();
                memoryMonitorY = memoryMonitorStage.getY();
                mainApplication.programProperties.savePrefs("memoryMonitorX", memoryMonitorStage.getX());
                mainApplication.programProperties.savePrefs("memoryMonitorY", memoryMonitorStage.getY());
            });
            Scene scene = new Scene(root);
            String stylePath = "/css/monitors/";
            if (memoryMonitorDarkThemeCheckBox.isSelected()) {
                stylePath += "dark-theme.css";
            } else {
                stylePath += "light-theme.css";
            }
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(stylePath)).toExternalForm());
            scene.setFill(Color.TRANSPARENT);
            memoryMonitorStage.setTitle("monitors");
            memoryMonitorStage.setScene(scene);

            memoryMonitorStage.setX(memoryMonitorX);
            memoryMonitorStage.setY(memoryMonitorY);
            memoryMonitorStage.setResizable(false);

            memoryMonitorController = fxmlLoader.getController();
            memoryMonitorController.start(systemMonitor);
            memoryMonitorStage.show();
        } else {
            memoryMonitorController.exitWidget();
            memoryMonitorPrimaryStage.close();
            memoryMonitorStage.close();
        }
    }


    public void globalDarkThemeHandler(ActionEvent actionEvent) {
        mainApplication.globalDarkTheme.setState(globalDarkThemeCheckBox.isSelected());
        globalDarkTheme(globalDarkThemeCheckBox.isSelected());
        mainApplication.confGlobalDarkScheme();
    }

    public void globalDarkTheme(boolean dark) {
        mainMenuDarkThemeCheckBox.setSelected(dark);
        weatherDarkThemeCheckBox.setSelected(dark);
        systemMonitorDarkThemeCheckBox.setSelected(dark);
        cpuMonitorDarkThemeCheckBox.setSelected(dark);
        memoryMonitorDarkThemeCheckBox.setSelected(dark);

        mainApplication.programProperties.savePrefs("mainMenuDarkThemeCheckBox", dark);
        mainApplication.programProperties.savePrefs("weatherDarkThemeCheckBox", dark);
        mainApplication.programProperties.savePrefs("systemMonitorDarkThemeCheckBox", dark);
        mainApplication.programProperties.savePrefs("cpuMonitorDarkThemeCheckBox", dark);
        mainApplication.programProperties.savePrefs("memoryMonitorDarkThemeCheckBox", dark);

        String stylePath = "";
        if (dark) {
            stylePath += "dark-theme.css";
        } else {
            stylePath += "light-theme.css";
        }
        try {
            mainApplication.primaryStage.getScene().getStylesheets().clear();
            mainApplication.primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/mainMenu/" + stylePath)).toExternalForm());
        } catch (Exception ignored) {
        }
        try {
            weatherStage.getScene().getStylesheets().clear();
            weatherStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/weather/" + stylePath)).toExternalForm());
        } catch (Exception ignored) {
        }
        try {
            systemMonitorStage.getScene().getStylesheets().clear();
            systemMonitorStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/monitors/" + stylePath)).toExternalForm());
        } catch (Exception ignored) {
        }
        try {
            cpuMonitorStage.getScene().getStylesheets().clear();
            cpuMonitorStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/monitors/" + stylePath)).toExternalForm());
        } catch (Exception ignored) {
        }
        try {
            memoryMonitorStage.getScene().getStylesheets().clear();
            memoryMonitorStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/monitors/" + stylePath)).toExternalForm());
        } catch (Exception ignored) {
        }
    }

    public void mainMenuDarkThemeHandler(ActionEvent actionEvent) {
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(mainMenuDarkThemeCheckBox.isSelected(), mainApplication.primaryStage);
        mainApplication.programProperties.savePrefs("mainMenuDarkThemeCheckBox", mainMenuDarkThemeCheckBox.isSelected());
    }

    public void mainMenuOnStartupHandler(ActionEvent actionEvent) {
        mainApplication.programProperties.savePrefs("mainMenuOnStartup", mainMenuOnStartupCheckBox.isSelected());
    }

    public void generateSettingsHandler(ActionEvent actionEvent) {
        mainApplication.programProperties.generatePrefsFile();
    }

    public void loadSettingsHandler(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("properties", "*.properties"));
        File selectedFile = fc.showOpenDialog(null);

        if (selectedFile != null) {
            mainApplication.programProperties.loadPrefsFile(String.valueOf(selectedFile.getAbsoluteFile()));
//            mainApplication.programProperties.showPrefs();
            mainApplication.programProperties.applyPrefs(mainApplication, this);
        }
    }


    public void singleWidgetStyle(boolean dark, Stage stage) {
        try {
            String stylePath = "/css/" + stage.getTitle();
            if (dark) {
                stylePath += "/dark-theme.css";
            } else {
                stylePath += "/light-theme.css";
            }
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource(stylePath)).toExternalForm());
        } catch (Exception ignored) {
        }
    }

    public void setSingleWidgetOpacity(Slider slider, Stage stage, Number newValue) {
        slider.setValue((Double) newValue);
        singleWidgetOpacity(slider, stage, newValue);
    }

    public void singleWidgetOpacity(Slider slider, Stage stage, Number newValue) {
        mainApplication.programProperties.savePrefs(slider.getId(), (Double) newValue);
        try {
            String s = format("-fx-background-color: linear-gradient(to right, #646464 %s, #64646433 %s)", newValue.doubleValue(), newValue.doubleValue());
            slider.lookup(".track").setStyle(s);
            stage.setOpacity((Double) newValue);
        } catch (Exception ignored) {
        }
    }

    public void exit() {
        Platform.runLater(() -> {
            try {
                weather(false);
            } catch (IOException ignored) {
            }
            try {
                systemMonitor(false);
            } catch (IOException ignored) {
            }
            try {
                cpuMonitor(false);
            } catch (IOException ignored) {
            }
            try {
                memoryMonitor(false);
            } catch (IOException ignored) {
            }
            try {
                systemMonitor.stopMonitoring();
            } catch (Exception ignored) {
            }
        });
    }
}
