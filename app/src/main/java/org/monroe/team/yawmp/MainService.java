package org.monroe.team.yawmp;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainService extends Service {


    private int mNotificationId;
    private SensorManager mSensorManager;
    private List<SensorEventListener> sensorEventListeners = new ArrayList<>();

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

    private SensorAlarmRequest lastSensorAlarmRequest;

    public synchronized void publishAlarmRequest(SensorAlarmRequest sensorAlarmRequest){
        if (lastSensorAlarmRequest != null){
            if (sensorAlarmRequest.createdAtMs - lastSensorAlarmRequest.createdAtMs > 600){
                App.get().sendAlarm();
                lastSensorAlarmRequest = sensorAlarmRequest;
            }
        } else {
            App.get().sendAlarm();
            lastSensorAlarmRequest = sensorAlarmRequest;
        }
    }

    private synchronized void startWatching() {

        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

        try {
            SensorEventListener eventListener = new SensorEventListener() {

                private SensorValue sensorValue1 = new SensorValue();
                private SensorValue sensorValue2 = new SensorValue();
                private SensorValue sensorValue3 = new SensorValue();

                @Override
                public void onSensorChanged(SensorEvent event) {

                    if (sensorValue1.update(event.values[0]) ||
                            sensorValue2.update(event.values[1]) ||
                            sensorValue3.update(event.values[2])) {
                        //App.APP_LOG.w("Sensor data:" + Arrays.asList(event.values[0], event.values[1], event.values[2]));
                        publishAlarmRequest(new SensorAlarmRequest());
                    }

                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorEventListeners.add(eventListener);
            mSensorManager.registerListener(
                    eventListener,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                    SensorManager.SENSOR_DELAY_NORMAL * 3);
            ;
        } catch (Exception e) {
            App.APP_LOG.w("Exception during regitering sensor", e);
            App.get().stopAppService();
        }
    }

    private void stopWatching() {
        for (SensorEventListener eventListener : sensorEventListeners) {
            try {
                mSensorManager.unregisterListener(eventListener);
            } catch (Exception e) {
                App.APP_LOG.w("Exception during unregitering sensor event listener = " + eventListener, e);
            }
        }
        sensorEventListeners.clear();
    }

    private void kill() {
        stopWatching();
        stopForeground(false);
        stopSelf();
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

    private static class SensorValue {

        private Float value = null;

        private synchronized boolean update(float newValue) {
            if (value == null) {
                value = newValue;
                return false;
            } else {
                boolean answer = (Math.abs(value - newValue)) > 0.8f;
                if (answer) {
                    value = newValue;
                }
                return answer;
            }
        }
    }

    private static class SensorAlarmRequest{
        private final long createdAtMs = System.currentTimeMillis();
    }

}

