package org.monroe.team.pebemusic;

import android.app.Application;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.monroe.team.android.jetpack.pebble.MessageProducer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class App extends Application {

    private static App instance;
    private static MessageProducer mMessageProducer;

    private List<Playlist> availablePlaylistList = Collections.EMPTY_LIST;
    private int currentShowedPlaylistIndex = -1;

    private ServiceConnection mServiceConnection;
    private MainService.MainServiceBinder mService;

    public synchronized List<Playlist> getAvailablePlaylistList() {
        return availablePlaylistList;
    }


    public synchronized void updateAvailablePlaylistList(List<Playlist> updatedPlaylistList) {
        availablePlaylistList = updatedPlaylistList;
        if (availablePlaylistList.isEmpty()){
            currentShowedPlaylistIndex = -1;
        } else {
            currentShowedPlaylistIndex = 0;
        }
        updateTitle();
    }

    private synchronized void updateTitle() {
        if (mService == null) return;
        String title = getTitle();
        mService.updateTitle(title);
    }

    private synchronized String getTitle() {
        String title = null;
        if (currentShowedPlaylistIndex == -1){
            title = "No playlist available";
        } else {
            title = availablePlaylistList.get(currentShowedPlaylistIndex).name;
        }
        return title;
    }

    @Override
    public void onCreate() {
        instance = this;
        mMessageProducer = MessageProducer.create("dc7955e1-310b-4062-b03d-73670048edfa", getApplicationContext());
        readPlaylists();

        super.onCreate();
    }

    private void readPlaylists() {
        String[] proj = {"*"};
        Uri tempPlaylistURI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor playListCursor = getContentResolver().query(tempPlaylistURI, proj, null, null, null);
        if (playListCursor == null || playListCursor.getCount() == 0) {
            updateAvailablePlaylistList(Collections.EMPTY_LIST);
            return;
        }


        List<Playlist> updatedPlaylistList = new ArrayList<Playlist>(playListCursor.getCount());

        String playListName, playListId = null;

        for (int i = 0; i < playListCursor.getCount(); i++) {
            playListCursor.moveToPosition(i);
            playListId = playListCursor.getString(playListCursor.getColumnIndex("_id"));
            playListName = playListCursor.getString(playListCursor.getColumnIndex("name"));
            updatedPlaylistList.add(new Playlist(playListId, playListName));
        }

        updateAvailablePlaylistList(updatedPlaylistList);
    }

    public static App getInstance() {
        return instance;
    }

    public synchronized boolean isAppServiceRunning() {
        return mServiceConnection != null;
    }

    public synchronized void startAppService() {
        //DO nothing as seems to be connecting ...
        if (mServiceConnection != null) return;

        Intent serviceStartIntent = new Intent(this, MainService.class);
        serviceStartIntent.putExtra("title", getTitle());
        startService(serviceStartIntent);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                synchronized (App.this){
                    mService = (MainService.MainServiceBinder) service;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                synchronized (App.this){
                    mService = null;
                    mServiceConnection = null;
                }
            }
        };
        bindService(serviceStartIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public synchronized void stopAppService() {
        unbindService(mServiceConnection);
        mService.stop();
        mService = null;
        mServiceConnection = null;
    }

    public synchronized void playCurrent() {

        if (currentShowedPlaylistIndex == -1) {
            Toast.makeText(this, "Unable to play selected playlist", Toast.LENGTH_SHORT).show();
        }
        App.Playlist playlist = getAvailablePlaylistList().get(currentShowedPlaylistIndex);

        playPlaylist(playlist);

    }

    private void playPlaylist(Playlist playlist) {
        PowerManager.WakeLock wakeLock = null;

        try {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            wakeLock.acquire();

            Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
            intent.putExtra(MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE, "android.intent.extra.playlist" );
            intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/playlist");
            intent.putExtra("android.intent.extra.playlist", playlist.name);
            intent.putExtra(SearchManager.QUERY, "playlist " + playlist.name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);



            if (intent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(this, "Going to play '"+playlist.name+"'", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to play selected playlist", Toast.LENGTH_SHORT).show();
            }
            PebbleKit.startAppOnPebble(this, UUID.fromString("1f03293d-47af-4f28-b960-f2b02a6dd757"));
        }finally {
            if (wakeLock != null){
                wakeLock.release();
            }
        }
    }

    public void switchNext() {

        currentShowedPlaylistIndex++;

        if (currentShowedPlaylistIndex >= availablePlaylistList.size()){
            currentShowedPlaylistIndex = 0;
        }

        updateTitle();
    }

    public MessageProducer pebbleMessageProducer() {
        return mMessageProducer;
    }

    public void watchReUploadPlaylists() {
        final List<Playlist> playlistsToSync = new ArrayList<>(getAvailablePlaylistList());
        uploadPlaylistList(playlistsToSync);
    }

    private void uploadPlaylistList(final List<Playlist> playlistsToSync) {
        if (playlistsToSync.isEmpty()){
            return;
        }

        Playlist playlist = playlistsToSync.remove(0);
        PebbleDictionary pebbleDictionary = new PebbleDictionary();
        pebbleDictionary.addInt32(Constants.KEY_EVENT_TYPE, Constants.EVENT_TYPE_PLAYLIST_UPDATED);
        pebbleDictionary.addString(Constants.KEY_PLAYLIST_ID, playlist.id);
        pebbleDictionary.addString(Constants.KEY_PLAYLIST_NAME, playlist.name);

        int res = pebbleMessageProducer().sendMessage(pebbleDictionary, new MessageProducer.CallbackSupport() {
            @Override
            public void onSuccess() {
                uploadPlaylistList(playlistsToSync);
            }

            @Override
            public void onFail() {
                uploadPlaylistList(playlistsToSync);
            }
        });
    }

    public void play(String playlistId) {
        for (Playlist playlist : availablePlaylistList) {
            if (playlistId.startsWith(playlist.id)){
                playPlaylist(playlist);
                return;
            }
        }

    }

    public static class Playlist {

        public final String id;
        public final String name;
        /*
          3 = "date_added"
                    4 = "date_modified"
        */

        public Playlist(String id, String name) {
            this.id = id;
            this.name = name;
        }

    }


}
