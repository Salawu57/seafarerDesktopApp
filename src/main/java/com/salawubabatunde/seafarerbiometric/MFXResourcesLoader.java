package com.salawubabatunde.seafarerbiometric;

import java.io.InputStream;
import java.net.URL;

public class MFXResourcesLoader {

    private MFXResourcesLoader() {
    }

    public static URL loadURL(String path) {
        return MFXResourcesLoader.class.getResource(path);
    }

    public static String load(String path) {
        return loadURL(path).toString();
    }

    public static InputStream loadStream(String name) {
        return MFXResourcesLoader.class.getResourceAsStream(name);
    }

}
