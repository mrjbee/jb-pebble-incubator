package org.monroe.team.yawmp;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.monroe.team.android.jetpack.pebble.AbstractMessageSubscriber;


public class WatchAppMessageSubscriber extends AbstractMessageSubscriber {

    public WatchAppMessageSubscriber() {
        super(
                Constants.WATCHAPP_UUID,
                Constants.KEY_EVENT_TYPE,
                new AbstractMessageHandler(Constants.EVENT_SYSTEM_TYPE_VERSION) {
                    @Override
                    public void onMessage(int messageType, PebbleDictionary dictionary) {
                        String watchAppVersion = dictionary.getString(Constants.KEY_VERSION);
                        App.get().showVersionToast(watchAppVersion);
                    }
                });
    }

}
