package com.topjohnwu.myfuck.net;

import java.net.HttpURLConnection;

public interface ErrorHandler {
    void onError(HttpURLConnection conn, Exception e);
}
