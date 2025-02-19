module com.salawubabatunde.seafarerbiometric {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
//    requires org.kordamp.ikonli.javafx;

    requires eu.hansolo.tilesfx;
    requires fr.brouillard.oss.cssfx;

    requires MaterialFX;
    requires VirtualizedFX;

    requires jdk.localedata;


    requires javafx.graphics;
    requires javafx.media;
    requires org.scenicview.scenicview;
    requires fontawesomefx;
    requires okhttp3;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.imaging;
    requires dpuareu;


    opens com.salawubabatunde.seafarerbiometric.controllers to javafx.fxml;
    opens com.salawubabatunde.seafarerbiometric to javafx.fxml;
    opens com.salawubabatunde.seafarerbiometric.model to javafx.base;
    exports com.salawubabatunde.seafarerbiometric;
}