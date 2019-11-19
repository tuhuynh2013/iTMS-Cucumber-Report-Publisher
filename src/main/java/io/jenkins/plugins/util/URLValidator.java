package io.jenkins.plugins.util;

import java.net.URL;
import java.net.URLConnection;


public class URLValidator {

    public static boolean isValidUrl(String string) {

        try {
			URL url = new URL(string);
			URLConnection conn = url.openConnection();
            conn.connect();

        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
