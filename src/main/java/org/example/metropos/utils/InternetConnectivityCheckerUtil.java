package org.example.metropos.utils;

import java.io.IOException;
import java.net.InetAddress;

public class InternetConnectivityCheckerUtil {
    public static boolean isInternetAvailable() {
        try {
            InetAddress.getByName("8.8.8.8").isReachable(2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
