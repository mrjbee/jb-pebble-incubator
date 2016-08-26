package org.monroe.team.pebemusic;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import team.monroe.org.pebemusic.R;

public class Dashboard extends AppCompatActivity {

    private Switch mServiceSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_dashboard);
        mServiceSwitch = (Switch) findViewById(R.id.switch_service);
        mServiceSwitch.setChecked(getApp().isAppServiceRunning());
        mServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getApp().startAppService();
                } else {
                    getApp().stopAppService();
                }
            }
        });
    }


    private App getApp() {
        return (App) getApplication();
    }

    private void refreshAvailablePlaylistList_() {
        String[] proj = {"*"};
        Uri tempPlaylistURI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

        // In the next line 'this' points to current Activity.
        // If you want to use the same code in other java file then activity,
        // then use an instance of any activity in place of 'this'.

        Cursor playListCursor = Dashboard.this.managedQuery(tempPlaylistURI, proj, null, null, null);

        if (playListCursor == null || playListCursor.getCount() == 0) {
            getApp().updateAvailablePlaylistList(Collections.EMPTY_LIST);
            return;
        }


        List<App.Playlist> updatedPlaylistList = new ArrayList<App.Playlist>(playListCursor.getCount());

        String playListName, playListId = null;

        for (int i = 0; i < playListCursor.getCount(); i++) {
            playListCursor.moveToPosition(i);
            playListId = playListCursor.getString(playListCursor.getColumnIndex("_id"));
            playListName = playListCursor.getString(playListCursor.getColumnIndex("name"));
            updatedPlaylistList.add(new App.Playlist(playListId, playListName));
        }

        getApp().updateAvailablePlaylistList(updatedPlaylistList);
    }

}
