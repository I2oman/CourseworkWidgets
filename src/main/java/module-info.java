module com.example.courseworkwidgets {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.management;
    requires jdk.management;
    requires com.google.gson;
    requires java.prefs;


    opens com.example.courseworkwidgets to javafx.fxml;
    exports com.example.courseworkwidgets;
}