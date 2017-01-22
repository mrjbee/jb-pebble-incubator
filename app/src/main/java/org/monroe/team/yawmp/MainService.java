package org.monroe.team.yawmp;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.getpebble.android.kit.util.PebbleDictionary;


public class MainService extends Service {


    private int mNotificationId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String title = "No title";
        if (intent != null && intent.hasExtra("title")) {
            title = intent.getStringExtra("title");
        }

        mNotificationId = startId;
        // Creates an explicit intent for an Activity in your app
        updateTitle(title);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateTitle(String title) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        Notification notification =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentInfo(title)
                        .setContentTitle("YAWMP")
                        .setContentIntent(resultPendingIntent)
                        .setWhen(0)
                        .getNotification();
        startForeground(mNotificationId, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MainServiceBinder(this);
    }

    private void startWatching() {

    }

    private void kill() {
        stopWatching();
        stopForeground(false);
        stopSelf();
    }

    private void stopWatching() {

    }

    public static class MainServiceBinder extends Binder {

        private final MainService owner;

        public MainServiceBinder(MainService owner) {
            this.owner = owner;
        }

        public void stop() {
            App.APP_LOG.i("Request service stop");
            owner.kill();
        }

        public void updateTitle(String title) {
            App.APP_LOG.i("Request title update");
            owner.updateTitle(title);
        }

        public void startWatching() {
            App.APP_LOG.i("Request service start");
            owner.startWatching();
        }
    }



}

