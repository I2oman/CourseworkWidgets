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
        prefs = Preferences.userRoot().node(MainApplication.class.getName());
        properties = new Properties();

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
        return prefs.get(key, null);
    }

    public void showPrefs() {
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
        try {
            prefs.clear();
        } catch (Exception ignored) {
        }
    }

    public void generatePrefsFile() {
        String FILE_NAME = "config.properties";

        String currentDirectory = MainApplication.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        currentDirectory = currentDirectory.substring(1, currentDirectory.length());

        String[] splitPath = currentDirectory.split("/");

        StringBuilder FILE_PATH = new StringBuilder();

        for (int i = 0; i < splitPath.length - 1; i++) {
            FILE_PATH.append(splitPath[i]).append("/");
        }

        FILE_PATH.append(FILE_NAME);

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

        try (OutputStream output = new FileOutputStream(FILE_PATH.toString())) {
            properties.store(output, "Program Properties");
        } catch (Exception e) {
            System.out.println("Can't save properties");
        }
    }

    public void loadPrefsFile(String filePath) {
        try (InputStream input = new FileInputStream(filePath)) {
            properties.clear();
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                prefs.put(key, properties.getProperty(key));
            }
            showPrefs();
        } catch (Exception ignored) {
        }
    }

    public void applyPrefs(MainApplication mainApplication, MainController mainController) {
        applyPrefs(mainApplication, mainController, false);
    }

    public void applyPrefs(MainApplication mainApplication, MainController mainController, boolean onStartup) {
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

        mainController.unitsGroup.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getId().equals(loadPrefs("unitsGroup")))
                .findFirst()
                .ifPresent(toggle -> toggle.setSelected(true));

        mainController.mainMenuDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("mainMenuDarkThemeCheckBox")));
        mainController.singleWidgetStyle(mainController.mainMenuDarkThemeCheckBox.isSelected(), mainApplication.primaryStage);

        mainController.mainMenuOnStartupCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("mainMenuOnStartup")));

        //----------------------------------------------------------------------------------------------------------------------------
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

        mainApplication.showWeather.setState(Boolean.parseBoolean(loadPrefs("weather")));

        mainController.weatherDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("weatherDarkThemeCheckBox")));

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

        if (!loadPrefs("cityTextField").equals("null")) {
            mainController.cityTextField.setText(loadPrefs("cityTextField"));
        }

        Platform.runLater(() -> {
            mainController.weatherCheckBox.setSelected(mainApplication.showWeather.getState());
            try {
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
        //----------------------------------------------------------------------------------------------------------------------------

        //----------------------------------------------------------------------------------------------------------------------------
        try {
            mainController.systemMonitorStage.setX(Double.parseDouble(loadPrefs("systemMonitorX")));
            mainController.systemMonitorStage.setY(Double.parseDouble(loadPrefs("systemMonitorY")));
        } catch (Exception ignored) {
        }
        try {
            mainController.systemMonitorX = Double.parseDouble(loadPrefs("systemMonitorX"));
            mainController.systemMonitorY = Double.parseDouble(loadPrefs("systemMonitorY"));
        } catch (Exception ignored) {
        }

        mainController.systemMonitorStyleGroup.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getId().equals(loadPrefs("systemMonitorStyleGroup")))
                .findFirst()
                .ifPresent(toggle -> toggle.setSelected(true));

        mainApplication.showSystemMonitor.setState(Boolean.parseBoolean(loadPrefs("systemMonitor")));

        mainController.systemMonitorDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("systemMonitorDarkThemeCheckBox")));

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

        Platform.runLater(() -> {
            mainController.systemMonitorCheckBox.setSelected(mainApplication.showSystemMonitor.getState());
            try {
                if (mainController.systemMonitorStage != null) {
                    if (mainController.systemMonitorStage.isShowing() != mainApplication.showSystemMonitor.getState()) {
                        mainController.systemMonitor(mainApplication.showSystemMonitor.getState());
                    }
                } else {
                    mainController.systemMonitor(mainApplication.showSystemMonitor.getState());
                }
            } catch (Exception ignored) {
            }
        });
        //----------------------------------------------------------------------------------------------------------------------------

        //----------------------------------------------------------------------------------------------------------------------------
        try {
            mainController.cpuMonitorStage.setX(Double.parseDouble(loadPrefs("cpuMonitorX")));
            mainController.cpuMonitorStage.setY(Double.parseDouble(loadPrefs("cpuMonitorY")));
        } catch (Exception ignored) {
        }
        try {
            mainController.cpuMonitorX = Double.parseDouble(loadPrefs("cpuMonitorX"));
            mainController.cpuMonitorY = Double.parseDouble(loadPrefs("cpuMonitorY"));
        } catch (Exception ignored) {
        }

        mainController.cpuMonitorStyleGroup.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getId().equals(loadPrefs("cpuMonitorStyleGroup")))
                .findFirst()
                .ifPresent(toggle -> toggle.setSelected(true));

        mainApplication.showCPUMonitor.setState(Boolean.parseBoolean(loadPrefs("cpuMonitor")));


        mainController.cpuMonitorDarkThemeCheckBox.setSelected(Boolean.parseBoolean(loadPrefs("cpuMonitorDarkThemeCheckBox")));

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

        Platform.runLater(() -> {
            mainController.cpuMonitorCheckBox.setSelected(mainApplication.showCPUMonitor.getState());
            try {
                if (mainController.cpuMonitorStage != null) {
                    if (mainController.cpuMonitorStage.isShowing() != mainApplication.showCPUMonitor.getState()) {
                        mainController.cpuMonitor(mainApplication.showCPUMonitor.getState());
                    }
                } else {
                    mainController.cpuMonitor(mainApplication.showCPUMonitor.getState());
                }
            } catch (Exception ignored) {
            }
        });
        //----------------------------------------------------------------------------------------------------------------------------

        //----------------------------------------------------------------------------------------------------------------------------
        try {
            mainController.memoryMonitorStage.setX(Double.parseDouble(loadPrefs("memoryMonitorX")));
            mainController.memoryMonitorStage.setY(Double.parseDouble(loadPrefs("memoryMonitorY")));
        } catch (Exception ignored) {
        }
        try {
            mainController.memoryMonitorX = Double.parseDouble(loadPrefs("memoryMonitorX"));
            mainController.memoryMonitorY = Double.parseDouble(loadPrefs("memoryMonitorY"));
        } catch (Exception ignored) {
        }

        mainController.memoryMonitorStyleGroup.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getId().equals(loadPrefs("memoryMonitorStyleGroup")))
                .findFirst()
                .ifPresent(toggle -> toggle.setSelected(true));


        mainApplication.showMemoryMonitor.setState(Boolean.parseBoolean(loadPrefs("memoryMonitor")));

        if (Boolean.parseBoolean(loadPrefs("memoryMonitorDarkThemeCheckBox"))) {
            mainController.memoryMonitorDarkThemeCheckBox.setSelected(true);
        }

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

        Platform.runLater(() -> {
            mainController.memoryMonitorCheckBox.setSelected(mainApplication.showMemoryMonitor.getState());
            try {
                if (mainController.memoryMonitorStage != null) {
                    if (mainController.memoryMonitorStage.isShowing() != mainApplication.showMemoryMonitor.getState()) {
                        mainController.memoryMonitor(mainApplication.showMemoryMonitor.getState());
                    }
                } else {
                    mainController.memoryMonitor(mainApplication.showMemoryMonitor.getState());
                }
            } catch (Exception ignored) {
            }
        });
        //----------------------------------------------------------------------------------------------------------------------------

        mainApplication.confGlobalDarkScheme();

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
