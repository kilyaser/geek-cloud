module com.geekbrains.sep22.geekcloudclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons;


    opens com.geekbrains.sep22.geekcloudclient to javafx.fxml;
    exports com.geekbrains.sep22.geekcloudclient;
    exports com.geekbrains.sep22.geekcloudclient.controller;
    opens com.geekbrains.sep22.geekcloudclient.controller to javafx.fxml;
}