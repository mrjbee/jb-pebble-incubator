package org.monroe.team.pebemusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.getpebble.android.kit.util.PebbleDictionary;

import team.monroe.org.pebemusic.R;

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


    private void sendPlaylistToWatch(App.Playlist playlist) {

        if (playlist == null) return;

        PebbleDictionary pebbleDictionary = new PebbleDictionary();
        pebbleDictionary.addInt32(Constants.KEY_EVENT_TYPE, Constants.EVENT_TYPE_PLAYLIST_UPDATED);
        pebbleDictionary.addString(Constants.KEY_PLAYLIST_ID, playlist.id);
        pebbleDictionary.addString(Constants.KEY_PLAYLIST_NAME, playlist.name);

    }

    private void updateTitle(String title) {
        Intent resultIntent = new Intent(this, Dashboard.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Dashboard.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Intent playIntent = new Intent("org.monroe.team.pebemusic.PLAY");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent("org.monroe.team.pebemusic.NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_queue_music_black_48dp)
                        .setContentInfo("PebeMusic")
                        .setContentTitle(title)
                        .setContentText("Playlist")
                        .setContentIntent(resultPendingIntent)
                        .setWhen(0)
                        .addAction(new NotificationCompat.Action(
                                R.drawable.ic_skip_next_black_18dp,
                                "Next",
                                nextPendingIntent))
                        .addAction(new NotificationCompat.Action(
                                R.drawable.ic_playlist_play_black_18dp,
                                "Play",
                                playPendingIntent))
                        .getNotification();
        startForeground(mNotificationId, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MainServiceBinder(this);
    }

    private void kill() {
        stopForeground(true);
        stopSelf();
    }

    public static class MainServiceBinder extends Binder {

        private final MainService owner;

        public MainServiceBinder(MainService owner) {
            this.owner = owner;
        }

        public void stop() {
            owner.kill();
        }

        public void updateTitle(String title) {
            owner.updateTitle(title);
        }
    }


}
