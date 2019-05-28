package com.wlt.HJsocket;



public interface Callback {
    void onConnected();
    void onDisconnected();
    void onReconnected();
    void onSend();
    void onReceived(String msg);
    void onError(String msg);
}
