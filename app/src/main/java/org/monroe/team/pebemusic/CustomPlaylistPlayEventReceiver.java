package org.monroe.team.pebemusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CustomPlaylistPlayEventReceiver extends BroadcastReceiver {
    public CustomPlaylistPlayEventReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent originalIntent) {
        if ("org.monroe.team.pebemusic.NEXT".equals(originalIntent.getAction())){
            getApp().switchNext();
        } else if ("org.monroe.team.pebemusic.PLAY".equals(originalIntent.getAction())){
            getApp().playCurrent();
        } else {
            Toast.makeText(getApp(),
                    "Unknown action = "+originalIntent.getAction(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private App getApp() {
        return App.getInstance();
    }

}
