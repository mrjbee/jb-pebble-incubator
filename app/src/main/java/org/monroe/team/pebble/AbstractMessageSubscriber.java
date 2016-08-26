package org.monroe.team.pebble;

import android.content.Context;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public abstract class AbstractMessageSubscriber extends PebbleKit.PebbleDataReceiver {

    private final MessageHandler[] messageHandlers;
    private final int messageTypeKey;

    protected AbstractMessageSubscriber(String subscribedUuid, int messageTypeKey, MessageHandler... messageHandlers) {
        super(UUID.fromString(subscribedUuid));
        this.messageTypeKey = messageTypeKey;
        this.messageHandlers = messageHandlers;
    }

    @Override
    public final void receiveData(Context context, int transactionId, PebbleDictionary dict) {
        // A new AppMessage was received, tell Pebble
        Long messageType = dict.getInteger(messageTypeKey);
        if (messageType == null){
            onNoMessageType(context, transactionId);
        }else {

            MessageHandler handler = null;
            int actualType = messageType.intValue();

            try {
                handler = selectMessageHandler(actualType);
            } catch (RuntimeException e){
                PebbleKit.sendNackToPebble(context, transactionId);
                throw e;
            }

            if (handler != null) {
                PebbleKit.sendAckToPebble(context, transactionId);
                handler.onMessage(actualType, dict);
            } else {
                onNoMessageHandler(context, transactionId);
            }
        }
    }

    private MessageHandler selectMessageHandler(int actualType) {
        for (MessageHandler messageHandler : messageHandlers) {
            for (int type : messageHandler.messageTypes()) {
                if (type == actualType){
                    return messageHandler;
                }
            }
        }
        return null;
    }

    private void onNoMessageHandler(Context context, int transactionId) {
        PebbleKit.sendNackToPebble(context, transactionId);
    }

    protected void onNoMessageType(Context context, int transactionId) {
        PebbleKit.sendNackToPebble(context, transactionId);
    }

    public interface MessageHandler {
        int[] messageTypes();
        void onMessage(int messageType, PebbleDictionary dictionary);
    }

    public static abstract class AbstractMessageHandler implements MessageHandler {

        private final int[] messageTypes;

        public AbstractMessageHandler(int messageType) {
            this(new int[]{messageType});
        }

        public AbstractMessageHandler(int... messageTypes) {
            this.messageTypes = messageTypes;
        }

        @Override
        public final int[] messageTypes() {
            return messageTypes;
        }
    }

}
