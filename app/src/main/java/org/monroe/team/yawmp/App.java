package org.monroe.team.yawmp;

import android.app.Application;
import android.widget.Toast;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.monroe.team.android.jetpack.logger.Logger;
import org.monroe.team.android.jetpack.pebble.MessageProducer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class App extends Application {
    public static Logger APP_LOG = new Logger("APP");
    private static App instance;
    private static MessageProducer mMessageProducer;
    private boolean agentActivated = false;
    private Date activationDate = null;

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
        Toast.makeText(this, "YAWMP Version:"+watchAppVersion,Toast.LENGTH_LONG)
                .show();
        publishAgentStatus();
    }

    public void publishAgentStatus() {
        PebbleDictionary pebbleDictionary = new PebbleDictionary();
        pebbleDictionary.addInt32(Constants.KEY_EVENT_TYPE, Constants.EVENT_TYPE_AGENT_STATUS_UPDATE);

        pebbleDictionary.addInt32(
                Constants.KEY_AGENT_ACTIVE, agentActivated ?
                    Constants.VALUE_AGENT_ACTIVATE_ON:
                    Constants.VALUE_AGENT_ACTIVATE_OFF);

        if (agentActivated) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            pebbleDictionary.addString(Constants.KEY_AGENT_STATUS, "Activated since "+simpleDateFormat.format(activationDate));
        } else {
            pebbleDictionary.addString(Constants.KEY_AGENT_STATUS, "Currently deactivated");
        }

        pebbleMessageProducer().sendMessage(pebbleDictionary, new MessageProducer.Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFail() {

            }

            @Override
            public void onTimeout() {

            }
        });
    }

    public void enableAgent(boolean enable) {
        agentActivated = enable;
        activationDate = new Date();
        APP_LOG.i("Set agent status to:"+ enable);
        App.get().publishAgentStatus();
    }
}
