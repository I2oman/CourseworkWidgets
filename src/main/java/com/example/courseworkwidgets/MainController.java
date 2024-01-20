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
    private SystemMonitor systemMonitor;
    public MonitorWidget systemMonitorWidget;
    public MonitorWidget cpuMonitorWidget;
    public MonitorWidget memoryMonitorWidget;
    private String openMenuSVG = "M 16 12 L 0 12 L 0 14 L 16 14 L 16 6 L 0 6 L 0 8 L 16 8 L 16 0 L 0 0 L 0 2 L 16 2";
    private String closeMenuSVG = "M 16 14 L 6 7 L 16 0 L 16 3 L 10 7 L 16 11 Z";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Sets a transparent background to the stage
        anchorPane.setBackground(Background.fill(Color.TRANSPARENT));

        systemMonitorWidget = new MonitorWidget();
        cpuMonitorWidget = new MonitorWidget();
        memoryMonitorWidget = new MonitorWidget();

        //Sets the closed menu image to the menu button
        menuSVG.setContent(openMenuSVG);

        //Thread to obtain user's city
        Thread requestCity = new Thread(() -> {
            JsonObject jsonObject = GetJsonResponse.get("https://ipinfo.io/json");
            if (jsonObject != null) {
                cityTextField.setText(jsonObject.get("city").getAsString());
            } else {
                cityTextField.setText("Not Found");
            }
        });
        requestCity.start();

        //Handler to change weather units
        unitsGroup.selectedToggleProperty().addListener((observableValue, toggle, newToggle) -> {
            mainApplication.programProperties.savePrefs("unitsGroup", ((RadioButton) newToggle).getId());
            try {
                weatherController.setUnints(((RadioButton) newToggle).getId());
            } catch (Exception ignored) {
            }
        });

        //Handler to change all slider value when the global opacity slider is moved
        globalOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            globalOpacitySlider.lookup(".track").setStyle(format("-fx-background-color: linear-gradient(to right, #646464 %s, #64646433 %s)", newValue.doubleValue(), newValue.doubleValue()));
            setSingleWidgetOpacity(weatherOpacitySlider, weatherStage, newValue);
            setSingleWidgetOpacity(systemMonitorOpacitySlider, systemMonitorWidget.getStage(), newValue);
            setSingleWidgetOpacity(cpuMonitorOpacitySlider, cpuMonitorWidget.getStage(), newValue);
            setSingleWidgetOpacity(memoryMonitorOpacitySlider, memoryMonitorWidget.getStage(), newValue);
        });

        //Handler to change the weather widget opacity
        weatherOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            singleWidgetOpacity(weatherOpacitySlider, weatherStage, newValue);
        });
        //Handler to the system monitor widget opacity
        systemMonitorOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            singleWidgetOpacity(systemMonitorOpacitySlider, systemMonitorWidget.getStage(), newValue);
        });
        //Handler to change the cpu monitor widget opacity
        cpuMonitorOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            singleWidgetOpacity(cpuMonitorOpacitySlider, cpuMonitorWidget.getStage(), newValue);
        });
        //Handler to change the memory monitor widget opacity
        memoryMonitorOpacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            singleWidgetOpacity(memoryMonitorOpacitySlider, memoryMonitorWidget.getStage(), newValue);
        });

        //Handler to change the style of the system monitor widget style
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
        //Handler to change the style of the cpu monitor widget style
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
        //Handler to change the style of the memory monitor widget style
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

        //Creates a system monitor object
        systemMonitor = new SystemMonitor();
    }

    public void init(MainApplication mainApplication) {
        //Obtain the mainApplication object after loading the main menu
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
        //Changes the menu button image when the button is clicked
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
        //Handles a click on the weather checkbox in the main menu
        mainApplication.showWeather.setState(weatherCheckBox.isSelected());
        weather(weatherCheckBox.isSelected());
    }

    public void weatherDarkThemeHandler(ActionEvent actionEvent) {
        //Handles a click on the weather dark theme checkbox in the main menu
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(weatherDarkThemeCheckBox.isSelected(), weatherStage);
        mainApplication.programProperties.savePrefs("weatherDarkThemeCheckBox", weatherDarkThemeCheckBox.isSelected());
    }

    private Timer timer = new Timer();

    public void onCityChange(KeyEvent keyEvent) {
        //Handles input in the city text field in the main menu
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
        //Saves the state of the weather widget to the program properties (on or off)
        mainApplication.programProperties.savePrefs("weather", show);
        if (show) {
            //Creates the weather widget stage with a transparent background,
            // loads it as a utility to run without an icon in the taskbar,
            // and removes window borders
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


            //Loads a stage
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("weather-view.fxml"));
            Parent root = fxmlLoader.load();
            //Sets a handler for mouse movement on the window
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
            //Creates a scene
            Scene scene = new Scene(root, WeatherController.windowCenterX * 2, WeatherController.windowCenterY * 2);
            //Applies a style to a scene
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

            //Passes units and city to the weather controller
            String units = ((RadioButton) unitsGroup.getSelectedToggle()).getId();
            weatherController.setCity(cityTextField.getText(), units);
            mainApplication.programProperties.savePrefs("cityTextField", cityTextField.getText());

            weatherStage.show();
        } else {
            //Exits the widget
            weatherController.exitWidget();
            weatherPrimaryStage.close();
            weatherStage.close();
        }
    }


    public void systemMonitorHandler(ActionEvent actionEvent) throws IOException {
        //Handles a click on the system monitor checkbox in the main menu
        mainApplication.showSystemMonitor.setState(systemMonitorCheckBox.isSelected());
        systemMonitor(systemMonitorCheckBox.isSelected());
    }

    public void systemMonitorDarkThemeHandler(ActionEvent actionEvent) {
        //Handles a click on the system monitor dark theme checkbox in the main menu
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(systemMonitorDarkThemeCheckBox.isSelected(), systemMonitorWidget.getStage());
        mainApplication.programProperties.savePrefs("systemMonitorDarkThemeCheckBox", systemMonitorDarkThemeCheckBox.isSelected());
    }

    public void systemMonitor(boolean show) throws IOException {
        //Starts the system monitor widget
        mainApplication.programProperties.savePrefs("systemMonitor", show);
        if (show) {
            systemMonitorWidget.start(mainApplication, systemMonitor,
                    "system", systemMonitorOpacitySlider.getValue(),
                    ((RadioButton) systemMonitorStyleGroup.getSelectedToggle()).getId(),
                    systemMonitorDarkThemeCheckBox.isSelected());
        } else {
            systemMonitorWidget.exit();
        }
    }


    public void cpuMonitorHandler(ActionEvent actionEvent) throws IOException {
        //Handles a click on the CPU monitor checkbox in the main menu
        mainApplication.showCPUMonitor.setState(cpuMonitorCheckBox.isSelected());
        cpuMonitor(cpuMonitorCheckBox.isSelected());
    }

    public void cpuMonitorDarkThemeHandler(ActionEvent actionEvent) {
        //Handles a click on the CPU monitor dark theme checkbox in the main menu
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(cpuMonitorDarkThemeCheckBox.isSelected(), cpuMonitorWidget.getStage());
        mainApplication.programProperties.savePrefs("cpuMonitorDarkThemeCheckBox", cpuMonitorDarkThemeCheckBox.isSelected());
    }

    public void cpuMonitor(boolean show) throws IOException {
        //Starts the CPU monitor widget
        mainApplication.programProperties.savePrefs("cpuMonitor", show);
        if (show) {
            cpuMonitorWidget.start(mainApplication, systemMonitor,
                    "cpu", cpuMonitorOpacitySlider.getValue(),
                    ((RadioButton) cpuMonitorStyleGroup.getSelectedToggle()).getId(),
                    cpuMonitorDarkThemeCheckBox.isSelected());
        } else {
            cpuMonitorWidget.exit();
        }
    }


    public void memoryMonitorHandler(ActionEvent actionEvent) throws IOException {
        //Handles a click on the memory monitor checkbox in the main menu
        mainApplication.showMemoryMonitor.setState(memoryMonitorCheckBox.isSelected());
        memoryMonitor(memoryMonitorCheckBox.isSelected());
    }

    public void memoryMonitorDarkThemeHandler(ActionEvent actionEvent) {
        //Handles a click on the memory monitor dark theme checkbox in the main menu
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(memoryMonitorDarkThemeCheckBox.isSelected(), memoryMonitorWidget.getStage());
        mainApplication.programProperties.savePrefs("memoryMonitorDarkThemeCheckBox", memoryMonitorDarkThemeCheckBox.isSelected());
    }

    public void memoryMonitor(boolean show) throws IOException {
        //Starts the memory monitor widget
        mainApplication.programProperties.savePrefs("memoryMonitor", show);
        if (show) {
            memoryMonitorWidget.start(mainApplication, systemMonitor,
                    "memory", memoryMonitorOpacitySlider.getValue(),
                    ((RadioButton) memoryMonitorStyleGroup.getSelectedToggle()).getId(),
                    memoryMonitorDarkThemeCheckBox.isSelected());
        } else {
            memoryMonitorWidget.exit();
        }
    }


    public void globalDarkThemeHandler(ActionEvent actionEvent) {
        //Handles a click on the global dark theme checkbox in the main menu
        mainApplication.globalDarkTheme.setState(globalDarkThemeCheckBox.isSelected());
        globalDarkTheme(globalDarkThemeCheckBox.isSelected());
        mainApplication.confGlobalDarkScheme();
    }

    public void globalDarkTheme(boolean dark) {
        //Changes all dark theme checkboxes to the provided value
        mainMenuDarkThemeCheckBox.setSelected(dark);
        weatherDarkThemeCheckBox.setSelected(dark);
        systemMonitorDarkThemeCheckBox.setSelected(dark);
        cpuMonitorDarkThemeCheckBox.setSelected(dark);
        memoryMonitorDarkThemeCheckBox.setSelected(dark);

        //Changes the style of all widgets
        singleWidgetStyle(dark, mainApplication.primaryStage);
        singleWidgetStyle(dark, weatherStage);
        singleWidgetStyle(dark, systemMonitorWidget.getStage());
        singleWidgetStyle(dark, cpuMonitorWidget.getStage());
        singleWidgetStyle(dark, memoryMonitorWidget.getStage());
    }

    public void mainMenuDarkThemeHandler(ActionEvent actionEvent) {
        //Handles a click on the main menu dark theme checkbox in the main menu
        mainApplication.confGlobalDarkScheme();
        singleWidgetStyle(mainMenuDarkThemeCheckBox.isSelected(), mainApplication.primaryStage);
        mainApplication.programProperties.savePrefs("mainMenuDarkThemeCheckBox", mainMenuDarkThemeCheckBox.isSelected());
    }

    public void mainMenuOnStartupHandler(ActionEvent actionEvent) {
        //Handles a click on the main menu on startup checkbox in the main menu
        mainApplication.programProperties.savePrefs("mainMenuOnStartup", mainMenuOnStartupCheckBox.isSelected());
    }

    public void generateSettingsHandler(ActionEvent actionEvent) {
        //Handles a click on the generate settings button in the main menu
        mainApplication.programProperties.generatePrefsFile();
    }

    public void loadSettingsHandler(ActionEvent actionEvent) {
        //Handles a click on the load settings button in the main menu
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
        //Changes the style of the passed widget
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
        //Changes the value of the passed slider and calls a function to change the widget opacity
        slider.setValue((Double) newValue);
        singleWidgetOpacity(slider, stage, newValue);
    }

    public void singleWidgetOpacity(Slider slider, Stage stage, Number newValue) {
        //Changes the opacity of the passed widget
        mainApplication.programProperties.savePrefs(slider.getId(), (Double) newValue);
        try {
            String s = format("-fx-background-color: linear-gradient(to right, #646464 %s, #64646433 %s)", newValue.doubleValue(), newValue.doubleValue());
            slider.lookup(".track").setStyle(s);
            stage.setOpacity((Double) newValue);
        } catch (Exception ignored) {
        }
    }

    public void exit() {
        //Closes all widget and stops the system monitor object
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
