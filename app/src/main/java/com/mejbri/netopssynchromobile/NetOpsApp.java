package com.mejbri.netopssynchromobile;

import android.app.Application;
import com.mejbri.netopssynchromobile.network.ApiClient;

public class NetOpsApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
    }
}
