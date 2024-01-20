package com.example.courseworkwidgets;

import javafx.application.Platform;
import javafx.scene.control.RadioButton;

import java.io.*;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.lang.String.format;

public class ProgramProperties {
    Preferences prefs;
    Properties properties;

    public ProgramProperties() {
        //Obtains saved program properties
        prefs = Preferences.userRoot().node(MainApplication.class.getName());
        properties = new Properties();

        //If properties are empty, generates default
        try {
            if (prefs.keys().length > 0) {
                return;
            }
        } catch (BackingStoreException e) {
            System.out.println("Preferences are not available");
        }

        prefs.put("rootX", "null");
        prefs.put("rootY", "null");
        prefs.put("mainMenu", "true");
        prefs.put("mainMenuDarkThemeCheckBox", "true");
        prefs.put("mainMenuOnStartup", "true");

        prefs.put("unitsGroup", "metric");

        prefs.put("weatherX", "0");
        prefs.put("weatherY", "0");
        prefs.put("weather", "false");
        prefs.put("weatherDarkThemeCheckBox", "true");
        prefs.put("weatherOpacitySlider", "1");
        prefs.put("cityTextField", "null");

        prefs.put("systemMonitorX", "0");
        prefs.put("systemMonitorY", "0");
        prefs.put("systemMonitor", "false");
        prefs.put("systemMonitorStyleGroup", "vertical");
        prefs.put("systemMonitorDarkThemeCheckBox", "true");
        prefs.put("systemMonitorOpacitySlider", "1");

        prefs.put("cpuMonitorX", "0");
        prefs.put("cpuMonitorY", "0");
        prefs.put("cpuMonitor", "false");
        prefs.put("cpuMonitorStyleGroup", "cpuBig");
        prefs.put("cpuMonitorDarkThemeCheckBox", "true");
        prefs.put("cpuMonitorOpacitySlider", "1");

        prefs.put("memoryMonitorX", "0");
        prefs.put("memoryMonitorY", "0");
        prefs.put("memoryMonitor", "false");
        prefs.put("memoryMonitorStyleGroup", "memSmall");
        prefs.put("memoryMonitorDarkThemeCheckBox", "true");
        prefs.put("memoryMonitorOpacitySlider", "1");
    }

    //The following three functions save keys and their corresponding values of different types to preferences
    public void savePrefs(String key, boolean value) {
        savePrefs(key, String.valueOf(value));
    }

    public void savePrefs(String key, double value) {
        savePrefs(key, String.valueOf(value));
    }

    public void savePrefs(String key, String value) {
        prefs.put(key, value);
    }

    public String loadPrefs(String key) {
        //Loads preferences by the provided key
        return prefs.get(key, null);
    }

    public void showPrefs() {
        //Displays preferences with formatted output
        try {
            String[] keys = prefs.keys();
            if (keys != null) {
                System.out.printf("%1$50s | %2$s%n", "Key", "Value");
                System.out.println("----------------------------------------------------------------------------------------------------");
                for (String ke : keys) {
                    String value = prefs.get(ke, null);
                    System.out.printf("%1$50s | %2$s%n", ke, value);
                }
                System.out.println("----------------------------------------------------------------------------------------------------");
                System.out.println();
            } else {
                System.out.println("No preferences found.");
            }
        } catch (BackingStoreException e) {
            System.out.println("Preferences are not available");
        }
    }

    public void clearPrefs() {
        //Clears preferences
        try {
            prefs.clear();
        } catch (Exception ignored) {
        }
    }

    public void generatePrefsFile() {
        String FILE_NAME = "config.properties";

        //Fetches self-path
        String currentDirectory = MainApplication.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        currentDirectory = currentDirectory.substring(1, currentDirectory.length());

        String[] splitPath = currentDirectory.split("/");

        StringBuilder FILE_PATH = new StringBuilder();

        for (int i = 0; i < splitPath.length - 1; i++) {
            FILE_PATH.append(splitPath[i]).append("/");
        }

        FILE_PATH.append(FILE_NAME);

        //Loads the preferences into the properties
        try {
            String[] keys = prefs.keys();
            if (keys != null) {
                for (String key : keys) {
                    String value = prefs.get(key, null);
                    properties.setProperty(key, value);
                }
            }
        } catch (Exception e) {
            System.out.println("Properties is empty");
        }

        //Writes properties to the file
        try (OutputStream output = new FileOutputStream(FILE_PATH.toString())) {
            properties.store(output, "Program Properties");
        } catch (Exception e) {
            System.out.println("Can't save properties");
        }
    }

    public void loadPrefsFile(String filePath) {
        //Loads properties from the file at the provided path
        try (InputStream input = new FileInputStream(filePath)) {
            properties.clear();
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                prefs.put(key, properties.getProperty(key));
            }
//            showPrefs();
        } catch (Exception ignored) {
        }
    }

    public void applyPrefs(MainApplication mainApplication, MainController mainController) {
        //Redirects to the applyPrefs() function if only 2 parameters are provided
        applyPrefs(mainApplication, mainController, false);
    }

    public void applyPrefs(MainApplication mainApplication, MainController mainController, boolean onStartup) {
        //------------------------------------------------- Main Menu --------------------------------------------------
        //Changes the position of the main menu window
        try {
            mainApplication.primaryStage.setX(Double.parseDouble(loadPrefs("rootX")));
            mainApplication.primaryStage.setY(Double.parseDouble(loadPrefs("rootY")));
        } catch (Exception ignored) {
        }
        try {
            mainApplication.rootX = Double.parseDouble(loadPrefs("rootX"));
            mainApplication.rootY = Double.parseDouble(loadPrefs("rootY"));
        } catch (Exception ignored) {
        }

        //Selects units according to the saved settings
        mainController.unitsGroup.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getId().equals(loadPrefs("unitsGroup")))
                .findFirst()
                .ifPresent(toggle -> toggle.setSelected(true));

        //Applies style according to the saved settings
        mainController.mainMenuDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("mainMenuDarkThemeCheckBox")));
        mainController.singleWidgetStyle(mainController.mainMenuDarkThemeCheckBox.isSelected(), mainApplication.primaryStage);

        //Changes the main menu checkbox according to the saved settings
        mainController.mainMenuOnStartupCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("mainMenuOnStartup")));
        //--------------------------------------------------------------------------------------------------------------

        //-------------------------------------------------- Weather ---------------------------------------------------
        //Changes the position of the weather window
        try {
            mainController.weatherStage.setX(Double.parseDouble(loadPrefs("weatherX")));
            mainController.weatherStage.setY(Double.parseDouble(loadPrefs("weatherY")));
        } catch (Exception ignored) {
        }
        try {
            mainController.weatherX = Double.parseDouble(loadPrefs("weatherX"));
            mainController.weatherY = Double.parseDouble(loadPrefs("weatherY"));
        } catch (Exception ignored) {
        }

        //Changes the weather checkbox in the system tray according to the saved settings
        mainApplication.showWeather.setState(Boolean.parseBoolean(loadPrefs("weather")));

        //Changes the weather dark theme checkbox in the system tray according to the saved settings
        mainController.weatherDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("weatherDarkThemeCheckBox")));

        //Obtains the weather slider value, sets it, and applies style to the slider
        double weatherSliderValue = Double.parseDouble(loadPrefs("weatherOpacitySlider"));
        mainController.weatherOpacitySlider.setValue(weatherSliderValue);
        Platform.runLater(() -> {
            try {
                mainController.weatherOpacitySlider.lookup(".track")
                        .setStyle(format("-fx-background-color: linear-gradient(to right, #646464 %s, #64646433 %s)",
                                weatherSliderValue, weatherSliderValue));
            } catch (Exception ignored) {
            }
        });

        //Sets the text field value to the loaded value if it is not null
        if (!loadPrefs("cityTextField").equals("null")) {
            mainController.cityTextField.setText(loadPrefs("cityTextField"));
        }

        //Applies all obtained settings in this section
        Platform.runLater(() -> {
            mainController.weatherCheckBox.setSelected(mainApplication.showWeather.getState());
            try {
                //If the weather widget state is different from the value that represents whether the widget needs to be shown,
                // then the state changes to the loaded settings
                if (mainController.weatherStage != null) {
                    if (mainController.weatherStage.isShowing() != mainApplication.showWeather.getState()) {
                        mainController.weather(mainApplication.showWeather.getState());
                    }
                } else {
                    mainController.weather(mainApplication.showWeather.getState());
                }
            } catch (Exception ignored) {
            }
        });
        //--------------------------------------------------------------------------------------------------------------

        //----------------------------------------------- System Monitor -----------------------------------------------
        //Changes the position of the system monitor window
        try {
            mainController.systemMonitorWidget.getStage().setX(Double.parseDouble(loadPrefs("systemMonitorX")));
            mainController.systemMonitorWidget.getStage().setY(Double.parseDouble(loadPrefs("systemMonitorY")));
        } catch (Exception ignored) {
        }
        try {
            mainController.systemMonitorWidget.setWindowX(Double.parseDouble(loadPrefs("systemMonitorX")));
            mainController.systemMonitorWidget.setWindowY(Double.parseDouble(loadPrefs("systemMonitorY")));
        } catch (Exception ignored) {
        }

        //Applies widget style according to the saved settings
        mainController.systemMonitorStyleGroup.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getId().equals(loadPrefs("systemMonitorStyleGroup")))
                .findFirst()
                .ifPresent(toggle -> toggle.setSelected(true));

        //Changes the system monitor checkbox in the system tray according to the saved settings
        mainApplication.showSystemMonitor.setState(Boolean.parseBoolean(loadPrefs("systemMonitor")));

        //Changes the system monitor dark theme in the system tray according to the saved settings
        mainController.systemMonitorDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("systemMonitorDarkThemeCheckBox")));

        //Obtains the system monito slider value, sets it, and applies style to the slider
        double systemMonitorSliderValue = Double.parseDouble(loadPrefs("systemMonitorOpacitySlider"));
        mainController.systemMonitorOpacitySlider.setValue(systemMonitorSliderValue);
        Platform.runLater(() -> {
            try {
                mainController.systemMonitorOpacitySlider.lookup(".track")
                        .setStyle(format("-fx-background-color: linear-gradient(to right, #646464 %s, #64646433 %s)",
                                systemMonitorSliderValue, systemMonitorSliderValue));
            } catch (Exception ignored) {
            }
        });

        //Applies all obtained settings in this section
        Platform.runLater(() -> {
            mainController.systemMonitorCheckBox.setSelected(mainApplication.showSystemMonitor.getState());
            try {
                //If the system monitor widget state is different from the value that represents whether the widget needs to be shown,
                // then the state changes to the loaded settings
                if (mainController.systemMonitorWidget.getStage() != null) {
                    if (mainController.systemMonitorWidget.getStage().isShowing() != mainApplication.showSystemMonitor.getState()) {
                        mainController.systemMonitor(mainApplication.showSystemMonitor.getState());
                    }
                } else {
                    mainController.systemMonitor(mainApplication.showSystemMonitor.getState());
                }
            } catch (Exception ignored) {
            }
        });
        //--------------------------------------------------------------------------------------------------------------

        //------------------------------------------------ CPU Monitor -------------------------------------------------
        //Changes the position of the cpu monitor window
        try {
            mainController.cpuMonitorWidget.getStage().setX(Double.parseDouble(loadPrefs("cpuMonitorX")));
            mainController.cpuMonitorWidget.getStage().setY(Double.parseDouble(loadPrefs("cpuMonitorY")));
        } catch (Exception ignored) {
        }
        try {
            mainController.cpuMonitorWidget.setWindowX(Double.parseDouble(loadPrefs("cpuMonitorX")));
            mainController.cpuMonitorWidget.setWindowY(Double.parseDouble(loadPrefs("cpuMonitorY")));
        } catch (Exception ignored) {
        }

        //Applies widget style according to the saved settings
        mainController.cpuMonitorStyleGroup.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getId().equals(loadPrefs("cpuMonitorStyleGroup")))
                .findFirst()
                .ifPresent(toggle -> toggle.setSelected(true));

        //Changes the cpu monitor checkbox in the system tray according to the saved settings
        mainApplication.showCPUMonitor.setState(Boolean.parseBoolean(loadPrefs("cpuMonitor")));

        //Changes the system cpu dark theme in the system tray according to the saved settings
        mainController.cpuMonitorDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("cpuMonitorDarkThemeCheckBox")));

        //Obtains the cpu monito slider value, sets it, and applies style to the slider
        double cpuMonitorSliderValue = Double.parseDouble(loadPrefs("cpuMonitorOpacitySlider"));
        mainController.cpuMonitorOpacitySlider.setValue(cpuMonitorSliderValue);
        Platform.runLater(() -> {
            try {
                mainController.cpuMonitorOpacitySlider.lookup(".track")
                        .setStyle(format("-fx-background-color: linear-gradient(to right, #646464 %s, #64646433 %s)",
                                cpuMonitorSliderValue, cpuMonitorSliderValue));
            } catch (Exception ignored) {
            }
        });

        //Applies all obtained settings in this section
        Platform.runLater(() -> {
            mainController.cpuMonitorCheckBox.setSelected(mainApplication.showCPUMonitor.getState());
            try {
                //If the cpu monitor widget state is different from the value that represents whether the widget needs to be shown,
                // then the state changes to the loaded settings
                if (mainController.cpuMonitorWidget.getStage() != null) {
                    if (mainController.cpuMonitorWidget.getStage().isShowing() != mainApplication.showCPUMonitor.getState()) {
                        mainController.cpuMonitor(mainApplication.showCPUMonitor.getState());
                    }
                } else {
                    mainController.cpuMonitor(mainApplication.showCPUMonitor.getState());
                }
            } catch (Exception ignored) {
            }
        });
        //--------------------------------------------------------------------------------------------------------------

        //----------------------------------------------- Memory Monitor -----------------------------------------------
        //Changes the position of the memory monitor window
        try {
            mainController.memoryMonitorWidget.getStage().setX(Double.parseDouble(loadPrefs("memoryMonitorX")));
            mainController.memoryMonitorWidget.getStage().setY(Double.parseDouble(loadPrefs("memoryMonitorY")));
        } catch (Exception ignored) {
        }
        try {
            mainController.memoryMonitorWidget.setWindowX(Double.parseDouble(loadPrefs("memoryMonitorX")));
            mainController.memoryMonitorWidget.setWindowY(Double.parseDouble(loadPrefs("memoryMonitorY")));
        } catch (Exception ignored) {
        }

        //Applies widget style according to the saved settings
        mainController.memoryMonitorStyleGroup.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getId().equals(loadPrefs("memoryMonitorStyleGroup")))
                .findFirst()
                .ifPresent(toggle -> toggle.setSelected(true));

        //Changes the memory monitor checkbox in the system tray according to the saved settings
        mainApplication.showMemoryMonitor.setState(Boolean.parseBoolean(loadPrefs("memoryMonitor")));

        //Changes the system memory dark theme in the system tray according to the saved settings
        mainController.memoryMonitorDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("memoryMonitorDarkThemeCheckBox")));

        //Obtains the memory monito slider value, sets it, and applies style to the slider
        double memoryMonitorSliderValue = Double.parseDouble(loadPrefs("memoryMonitorOpacitySlider"));
        mainController.memoryMonitorOpacitySlider.setValue(memoryMonitorSliderValue);
        Platform.runLater(() -> {
            try {
                mainController.memoryMonitorOpacitySlider.lookup(".track")
                        .setStyle(format("-fx-background-color: linear-gradient(to right, #646464 %s, #64646433 %s)",
                                memoryMonitorSliderValue, memoryMonitorSliderValue));
            } catch (Exception ignored) {
            }
        });

        //Applies all obtained settings in this section
        Platform.runLater(() -> {
            mainController.memoryMonitorCheckBox.setSelected(mainApplication.showMemoryMonitor.getState());
            try {
                //If the memory monitor widget state is different from the value that represents whether the widget needs to be shown,
                // then the state changes to the loaded settings
                if (mainController.memoryMonitorWidget.getStage() != null) {
                    if (mainController.memoryMonitorWidget.getStage().isShowing() != mainApplication.showMemoryMonitor.getState()) {
                        mainController.memoryMonitor(mainApplication.showMemoryMonitor.getState());
                    }
                } else {
                    mainController.memoryMonitor(mainApplication.showMemoryMonitor.getState());
                }
            } catch (Exception ignored) {
            }
        });
        //--------------------------------------------------------------------------------------------------------------

        //Configures the state of the global dark theme checkbox based on all other dark theme checkboxes
        mainApplication.confGlobalDarkScheme();

        //If the program is booting - use the "mainMenuOnStartup" variable to determine whether to load the main menu or not.
        //If the "Load Settings" button is pressed - use the "mainMenu" variable to determine whether to hide or leave the main menu opened
        String loadValue = "mainMenu";
        if (onStartup) {
            loadValue = "mainMenuOnStartup";
        }

        if (Boolean.parseBoolean(loadPrefs(loadValue))) {
            mainApplication.primaryStage.show();
        } else {
            mainApplication.primaryStage.close();
        }
    }
}
