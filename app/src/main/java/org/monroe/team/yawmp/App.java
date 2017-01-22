package org.monroe.team.yawmp;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.monroe.team.android.jetpack.logger.Logger;
import org.monroe.team.android.jetpack.pebble.MessageProducer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class App extends Application {

    public static Logger APP_LOG = new Logger("APP");
    private static App instance;
    private static MessageProducer mMessageProducer;
    private boolean agentActivated = false;
    private Date activationDate = null;
    private Date lastAlarmDate = null;

    private ServiceConnection mServiceConnection;
    private MainService.MainServiceBinder mService;


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

        synchronized (App.this){
            if (mService != null){
                mService.updateTitle(getAgentStatus());
            }
        }

        pebbleDictionary.addString(Constants.KEY_AGENT_STATUS, getAgentStatus());
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

    public String getAgentStatus() {
        if (agentActivated){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            String statusString =  "Activated since "+simpleDateFormat.format(activationDate);
            if (lastAlarmDate != null){
                statusString += ", last alarm at "+simpleDateFormat.format(lastAlarmDate);
            }
            return statusString;
        } else {
            return "Not active. Hit up for activation.";
        }
    }

    public void enableAgent(boolean enable) {
        agentActivated = enable;
        activationDate = new Date();
        if (!enable){
            lastAlarmDate = null;
            stopAppService();
            publishAgentStatus();
        } else {
            startAppService();
        }
        APP_LOG.i("Set agent status to:"+ enable);

    }

    public void sendAlarm() {
        lastAlarmDate = new Date();
        publishAgentStatus();
        sendAlarmImpl(0);
    }

    private void sendAlarmImpl(final int tryIndex) {
        if (tryIndex > 3){
            sendAlarmFailback();
        }
        PebbleDictionary pebbleDictionary = new PebbleDictionary();
        pebbleDictionary.addInt32(Constants.KEY_EVENT_TYPE, Constants.EVENT_TYPE_AGENT_ALARM);
        pebbleMessageProducer().sendMessage(pebbleDictionary, new MessageProducer.Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PebbleKit.startAppOnPebble(App.get(), UUID.fromString(Constants.WATCHAPP_UUID));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendAlarmImpl(tryIndex + 1);
                    }
                }).start();
            }

            @Override
            public void onTimeout() {
                onFail();
            }
        });
    }

    private void sendAlarmFailback() {

    }


    public synchronized void startAppService() {
        //DO nothing as seems to be connecting ...
        if (mServiceConnection != null) return;

        Intent serviceStartIntent = new Intent(this, MainService.class);
        serviceStartIntent.putExtra("title", getAgentStatus());
        startService(serviceStartIntent);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                synchronized (App.this){
                    mService = (MainService.MainServiceBinder) service;
                }
                mService.startWatching();
                App.get().publishAgentStatus();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                synchronized (App.this){
                    mService = null;
                    mServiceConnection = null;
                }
                App.get().publishAgentStatus();
            }
        };
        bindService(serviceStartIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public synchronized void stopAppService() {
        if (mServiceConnection == null) return;
        unbindService(mServiceConnection);
        mService.stop();
        mService = null;
        mServiceConnection = null;
    }

}
