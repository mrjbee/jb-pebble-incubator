package org.monroe.team.pebble;

import android.content.Context;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.Random;
import java.util.UUID;

public class MessageProducer {

    private static MessageProducer defaultInstance = null;

    private final PebbleKit.PebbleAckReceiver mPebbleAckReceiver;
    private final PebbleKit.PebbleNackReceiver mPebbleNackReceiver;
    private final UUID appUuid;
    private final Context mContext;
    private Integer mAwaitingTransactionId;
    private Callback mAwaitingCallback;

    private MessageProducer(UUID appUuid, Context context) {
        this.appUuid = appUuid;

        mPebbleAckReceiver = new PebbleKit.PebbleAckReceiver(appUuid) {
            @Override
            public void receiveAck(Context context, int transactionId) {
                onAck(transactionId);
            }
        };
        mPebbleNackReceiver = new PebbleKit.PebbleNackReceiver(appUuid) {
            @Override
            public void receiveNack(Context context, int transactionId) {
                onNack(transactionId);
            }
        };
        mContext = context;
    }

    public static synchronized MessageProducer create(String uuid, Context context) {

        if (defaultInstance != null) throw new IllegalStateException("Single MessageProducer per app allowed");

        UUID appUuid = UUID.fromString(uuid);
        defaultInstance = new MessageProducer(appUuid, context);
        PebbleKit.registerReceivedAckHandler(context, defaultInstance.mPebbleAckReceiver);
        PebbleKit.registerReceivedNackHandler(context, defaultInstance.mPebbleNackReceiver);
        return defaultInstance;
    }


    private synchronized void onAck(int transactionId) {
        if (isSameAsAwaitingTransaction(transactionId)){
            Callback curCallback = mAwaitingCallback;
            cleanAwaitingState();
            curCallback.onSuccess();
        }
    }

    private synchronized void onNack(int transactionId) {
        if (isSameAsAwaitingTransaction(transactionId)){
            Callback curCallback = mAwaitingCallback;
            cleanAwaitingState();
            curCallback.onFail();
        }
    }

    private void cleanAwaitingState() {
        mAwaitingTransactionId = null;
        mAwaitingCallback = null;
    }

    private boolean isSameAsAwaitingTransaction(int transactionId) {
        return mAwaitingTransactionId != null && mAwaitingTransactionId.intValue() == transactionId;
    }

    public synchronized int sendMessage(PebbleDictionary dictionary, Callback callback) {

    //    if (mAwaitingTransactionId != null) return 1;

        mAwaitingTransactionId = new Random().nextInt(250);
        mAwaitingCallback = callback;
        PebbleKit.sendDataToPebbleWithTransactionId(mContext, appUuid, dictionary, mAwaitingTransactionId);

        return 0;
    }


    public interface Callback {
        void onSuccess();
        void onFail();
        void onTimeout();
    }

    public abstract static class CallbackSupport implements Callback{
        @Override
        public void onSuccess() {/*do nothing by default*/}

        @Override
        public void onFail() {/*do nothing by default*/}

        @Override
        public void onTimeout() {/*do nothing by default*/}
    }
}
