package org.monroe.team.pebemusic;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.monroe.team.pebble.AbstractMessageSubscriber;

public class PebeMessageSubscriber extends AbstractMessageSubscriber {

    public PebeMessageSubscriber() {
        super(
                "dc7955e1-310b-4062-b03d-73670048edfa",
                Constants.KEY_EVENT_TYPE,
                new AbstractMessageHandler(Constants.EVENT_TYPE_VERSION){
                    @Override
                    public void onMessage(int messageType, PebbleDictionary dictionary) {
                        String watchAppVersion = dictionary.getString(Constants.KEY_VERSION);
                        App.getInstance().watchReUploadPlaylists();
                    }
                },
                new AbstractMessageHandler(Constants.EVENT_TYPE_PLAYLIST_PLAY){
                    @Override
                    public void onMessage(int messageType, PebbleDictionary dictionary) {
                        String playlistId = dictionary.getString(Constants.KEY_PLAYLIST_ID);
                        App.getInstance().play(playlistId);
                    }
                });
    }

}
