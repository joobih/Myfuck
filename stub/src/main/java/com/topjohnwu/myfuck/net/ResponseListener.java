package com.topjohnwu.myfuck.net;

public interface ResponseListener<T> {
    void onResponse(T response);
}
