package org.monroe.team.new_project;

import android.app.Application;
import android.widget.Toast;

import org.monroe.team.android.jetpack.pebble.MessageProducer;

public class App extends Application {

    private static App instance;
    private static MessageProducer mMessageProducer;

    @Override
    public void onCreate() {
        instance = this;
        mMessageProducer = MessageProducer.create(Constants.WATCHAPP_UUID, getApplicationContext());
        super.onCreate();
    }

    public static App get() {
        return instance;
    }

    public MessageProducer pebbleMessageProducer() {
        return mMessageProducer;
    }

    public void showVersionToast(String watchAppVersion) {
        Toast.makeText(this, "Test Watch App Version:"+watchAppVersion,Toast.LENGTH_LONG)
                .show();
    }
}
