package com.example.courseworkwidgets;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class WeatherController implements Initializable {
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public Label cityNameLable;
    @FXML
    public ImageView weatherImage;
    private static final int bigRadius = 85;
    private static final int smallRadius = 70;
    private final int centerRadius = 60;
    private final int arcLength = 30;
    public static int windowCenterY = bigRadius + 1;
    public static int windowCenterX = bigRadius + 1;
    private int sunRise;
    private int sunSet;
    private Rotate rotateSecond;
    private Rotate rotateMinute;
    private Rotate rotateHour;
    private Rotate rotateWind;
    private boolean afternoon;
    private Thread clockThread;
    private int timeSecond;
    private int timeMinute;
    private int timeHour;
    public String city;
    private double windDeg;
    private String unints;
    private String tempUnints;
    private String speedUnints;
    private String appid = "e55ee5b275465e12ec2e869fe7e8bd91";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Makes the stage background transparent for the window
        anchorPane.setBackground(Background.fill(Color.TRANSPARENT));

        //Collects local time
        LocalDateTime now = LocalDateTime.now();
        timeHour = now.getHour();
        timeMinute = now.getMinute();
        timeSecond = now.getSecond();

        afternoon = timeHour > 12;

        createClock();

        startClock();
    }

    public void setCity(String city) {
        setCity(city, null);
    }

    public void setCity(String city, String unints) {
        //Collects and sets city name
        if (unints != null) {
            setUnints(unints);
        }
        boolean accc = getWeather(city);
        if (accc) {
            this.city = city;
        }

        Platform.runLater(this::createClock);
    }

    public void setUnints(String unints) {
        //Collects and sets temperature and distance units
        this.unints = unints;
        switch (unints) {
            case "metric" -> {
                tempUnints = "°C";
                speedUnints = "m/s";
            }
            case "imperial" -> {
                tempUnints = "°F";
                speedUnints = "mi/h";
            }
            default -> {
                tempUnints = "°K";
                speedUnints = "m/s";
            }
        }
        getWeather(city);
    }

    private void getWeather() {
        getWeather(city);
    }

    private boolean getWeather(String city) {
        try {
            //Creates a link to obtain weather information
            String link = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s", city, unints, appid);

            //Obtains weather information as a Gson object
            JsonObject jsonObject = GetJsonResponse.get(link);

            //Separates and sets weather information into labels
            String cityName = jsonObject.get("name").getAsString();

            if (!cityName.equalsIgnoreCase(city)) {
                Platform.runLater(() -> cityNameLable.setText("Not Found"));
                return false;
            }

            String cityCountry = jsonObject.get("sys").getAsJsonObject().get("country").getAsString();
            String cityFullName = cityName + ", " + cityCountry;

            double temp = jsonObject.get("main").getAsJsonObject().get("temp").getAsDouble();
            temp = (Math.round(temp * 2)) / 2.0;

            final String iconUrl = String.format("https://openweathermap.org/img/wn/%s@2x.png",
                    jsonObject.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString());

            DateTimeFormatter formatterH = DateTimeFormatter.ofPattern("HH");
            DateTimeFormatter formatterM = DateTimeFormatter.ofPattern("mm");

            long sunriseLong = jsonObject.get("sys").getAsJsonObject().get("sunrise").getAsLong();
            Instant instant = Instant.ofEpochSecond(sunriseLong);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            String sunriseTimeH = localDateTime.format(formatterH);
            String sunriseTimeM = localDateTime.format(formatterM);

            long sunsetLong = jsonObject.get("sys").getAsJsonObject().get("sunset").getAsLong();
            Instant instant1 = Instant.ofEpochSecond(sunsetLong);
            LocalDateTime localDateTime1 = LocalDateTime.ofInstant(instant1, ZoneOffset.UTC);
            String sunsetTimeH = localDateTime1.format(formatterH);
            String sunsetTimeM = localDateTime1.format(formatterM);

            int timezone = jsonObject.get("timezone").getAsInt() / (3600);

            sunRise = Integer.parseInt(sunriseTimeH) + timezone;
            if (Integer.parseInt(sunriseTimeM) > 40) {
                sunRise++;
            }
            sunSet = Integer.parseInt(sunsetTimeH) + timezone;
            if (Integer.parseInt(sunsetTimeM) > 40) {
                sunSet++;
            }

            JsonObject wind = jsonObject.get("wind").getAsJsonObject();
            double windSpeed = wind.get("speed").getAsDouble();
            windDeg = wind.get("deg").getAsDouble();

            double finalTemp = temp;
            Platform.runLater(() -> {
                weatherImage.setImage(new Image(iconUrl));
                cityNameLable.setText(cityFullName + "\n\t" + windSpeed + speedUnints + "\n" + finalTemp + tempUnints);
                rotateWind.setAngle(windDeg);
            });
        } catch (Exception e) {
            Platform.runLater(() -> cityNameLable.setText("Not Found\n\n\n"));
        }
        return true;
    }

    private void createClock() {
        //Clears the window and recreates the clock
        anchorPane.getChildren().clear();
        int startEngel = 75;
        Circle currentHalfCircle = new Circle(windowCenterX, windowCenterY, smallRadius);
        currentHalfCircle.setFill(Color.WHITE);
        currentHalfCircle.setStroke(Color.BLACK);
        currentHalfCircle.setStrokeWidth(0);

        Circle otherHalfcircle = new Circle(windowCenterX, windowCenterY, centerRadius);
        otherHalfcircle.setFill(Color.WHITE);
        otherHalfcircle.setStroke(Color.BLACK);
        otherHalfcircle.setStrokeWidth(0);

        Arc hourArc = new Arc(windowCenterX, windowCenterY, 0, 0, startEngel, arcLength);
        hourArc.setType(ArcType.ROUND);
        hourArc.setStrokeWidth(0);

        Circle[] circles = {currentHalfCircle, otherHalfcircle};
        int[] radiuses = {bigRadius, smallRadius};

        for (int p = 0; p < 2; p++) {
            for (int h = 0; h < 12; h++) {
                hourArc.setRadiusX(radiuses[p]);
                hourArc.setRadiusY(radiuses[p]);
                hourArc.setStartAngle(startEngel);
                Shape subtractSector = Shape.subtract(hourArc, circles[p]);
                subtractSector.setFill(Color.GRAY);
                if (!afternoon) {
                    if ((p == 0 && h >= sunRise) || (p == 1 && h + 12 <= sunSet)) {
                        subtractSector.setFill(Color.rgb(241, 156, 70));
                    }
                } else {
                    if ((p == 1 && h >= sunRise) || (p == 0 && h + 12 <= sunSet)) {
                        subtractSector.setFill(Color.rgb(241, 156, 70));
                    }
                }
                subtractSector.setStroke(subtractSector.getFill());
                subtractSector.setStrokeWidth(1);
                anchorPane.getChildren().add(subtractSector);
                startEngel -= 30;
                if (startEngel < 0) {
                    startEngel += 360;
                }
            }
            startEngel = 75;
        }

        otherHalfcircle.getStyleClass().add("centerCircle");

        anchorPane.getChildren().add(otherHalfcircle);

        int polylineCX = windowCenterX - 25;
        int polylineCY = windowCenterY + 22;
        Polyline polyline = new Polyline(
                polylineCX + 1, polylineCY + 12,
                polylineCX + 6, polylineCY,
                polylineCX + 11, polylineCY + 12,
                polylineCX + 6, polylineCY + 9
        );
        polyline.setFill(Color.WHITE);
        polyline.setStrokeWidth(0);
        polyline.getStyleClass().add("text");
        rotateWind = new Rotate();
        rotateWind.setPivotX(polylineCX + 6);
        rotateWind.setPivotY(polylineCY + 6);
        polyline.getTransforms().add(rotateWind);
        rotateWind.setAngle(windDeg);
        anchorPane.getChildren().add(polyline);

        anchorPane.getChildren().add(weatherImage);
        anchorPane.getChildren().add(cityNameLable);

        createClockMarking();
        createArrows();
    }

    private void createClockMarking() {
        //Creates clock markings
        int startEngel = 90;
        Circle hourCircle = new Circle(windowCenterX, windowCenterY, bigRadius - 10);
        hourCircle.setFill(Color.WHITE);
        hourCircle.setStrokeWidth(0);
        Circle minuteCircle = new Circle(windowCenterX, windowCenterY, bigRadius - 5);
        minuteCircle.setFill(Color.WHITE);
        minuteCircle.setStrokeWidth(0);
        Arc line = new Arc(windowCenterX, windowCenterY, bigRadius, bigRadius, 0, 1);
        line.setType(ArcType.ROUND);
        line.setFill(Color.BLACK);
        line.setStrokeWidth(0);
        for (int i = 0; i < 60; i++) {
            line.setStartAngle(startEngel - 0.5);
            Shape mark;
            if (startEngel % 30 == 0) {
                mark = Shape.subtract(line, hourCircle);
            } else {
                mark = Shape.subtract(line, minuteCircle);
            }
            mark.setFill(Color.BLACK);
            mark.setStrokeWidth(0);
            anchorPane.getChildren().add(mark);
            startEngel -= 6;
            if (startEngel < 0) {
                startEngel += 360;
            }
        }
    }

    public void createArrows() {
        //Creates clock arrows
        Circle clockStroke = new Circle(windowCenterX, windowCenterY, bigRadius);
        clockStroke.setFill(Color.TRANSPARENT);
        clockStroke.setStroke(Color.BLACK);
        clockStroke.setStrokeWidth(1);

        Circle hourCircle = new Circle(windowCenterX, windowCenterY, smallRadius);
        hourCircle.setFill(Color.WHITE);
        hourCircle.setStrokeWidth(0);

        Arc minuteArc = new Arc(windowCenterX, windowCenterY, bigRadius, bigRadius, -0.5, 1);
        minuteArc.setType(ArcType.ROUND);
        minuteArc.setFill(Color.BLACK);
        minuteArc.setStrokeWidth(0);
        Shape subtractMinute = Shape.subtract(minuteArc, hourCircle);
        subtractMinute.setFill(Color.WHITE);
        subtractMinute.setStrokeWidth(0);
        Group minuteArrow = new Group(subtractMinute, clockStroke);
        rotateMinute = new Rotate();
        rotateMinute.setPivotX(windowCenterX);
        rotateMinute.setPivotY(windowCenterY);
        minuteArrow.getTransforms().add(rotateMinute);
        anchorPane.getChildren().add(minuteArrow);

        Arc hourArc = new Arc(windowCenterX, windowCenterY, bigRadius, bigRadius, -1.5, 3);
        hourArc.setType(ArcType.ROUND);
        hourArc.setFill(Color.BLACK);
        hourArc.setStrokeWidth(0);
        Shape subtractHour = Shape.subtract(hourArc, hourCircle);
        subtractHour.setFill(Color.WHITE);
        subtractHour.setStrokeWidth(0);
        Group hourArrow = new Group(subtractHour, clockStroke);
        rotateHour = new Rotate();
        rotateHour.setPivotX(windowCenterX);
        rotateHour.setPivotY(windowCenterY);
        hourArrow.getTransforms().add(rotateHour);
        anchorPane.getChildren().add(hourArrow);

        Arc secondArc = new Arc(windowCenterX, windowCenterY, bigRadius, bigRadius, -0.5, 1);
        secondArc.setType(ArcType.ROUND);
        secondArc.setFill(Color.BLACK);
        secondArc.setStrokeWidth(0);
        Shape subtractSecond = Shape.subtract(secondArc, hourCircle);
        subtractSecond.setFill(Color.RED);
        subtractSecond.setStrokeWidth(0);
        Group secondArrow = new Group(subtractSecond, clockStroke);
        rotateSecond = new Rotate();
        rotateSecond.setPivotX(windowCenterX);
        rotateSecond.setPivotY(windowCenterY);
        secondArrow.getTransforms().add(rotateSecond);
        anchorPane.getChildren().add(secondArrow);
    }

    public void startClock() {
        //Starts the time thread and counts time
        clockThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    setTime(timeHour, timeMinute, timeSecond);
                    Thread.sleep(1000);
                    timeSecond++;
                    if (timeSecond == 60) {
                        timeMinute++;
                        timeSecond = 0;
                    }
                    if ((timeMinute + 1) % 30 == 0 && timeSecond == 55) {
                        Thread getW = new Thread(this::getWeather);
                        getW.start();
                    }
                    if (timeMinute == 60) {
                        LocalDateTime now = LocalDateTime.now();
                        timeHour = now.getHour();
                        timeMinute = now.getMinute();
                        timeSecond = now.getSecond();
                    }
                    if ((afternoon && timeHour == 0) || (!afternoon && timeHour == 12)) {
                        if (afternoon && timeHour == 0) {
                            afternoon = false;
                        }
                        if (!afternoon && timeHour == 12) {
                            afternoon = true;
                        }
                        Platform.runLater(() -> {
                            createClock();
                            setTime(timeHour, timeMinute, timeSecond);
                        });
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        clockThread.start();
    }

    public void setTime(int h, int m, int s) {
        //Calculates the correct angle to move arrows according to the provided time
        double hourEn = 30 * ((h + 9) % 12);
        double minuteEn = 6 * ((m + 45) % 60);
        double secondEn = 6 * ((s + 45) % 60);

        rotateHour.setAngle(hourEn);
        rotateMinute.setAngle(minuteEn);
        rotateSecond.setAngle(secondEn);
    }

    public void exitWidget() {
        clockThread.interrupt();
    }
}
