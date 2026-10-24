package com.d1vivek.wallet.app;

import android.app.Application;
import android.content.Context;

import java.time.Clock;

public class WalletApplication extends Application {

    private AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(Clock.systemUTC());
    }

    public static AppContainer container(Context context) {
        return ((WalletApplication) context.getApplicationContext()).container;
    }
}
