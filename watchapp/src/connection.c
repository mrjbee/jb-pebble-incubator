//
// Created by mrjbee on 8/17/16.
//
#include <pebble.h>
#include "connection.h"

Subscriber SUBSCRIBER = NULL;

static bool isDictionaryResultOk(DictionaryResult *result) { return (*result) != DICT_OK; }
static bool isMessageResultNotOk(AppMessageResult *messageResult) { return (*messageResult) != APP_MSG_OK; }

static DictionaryResult sendVersionAction(DictionaryIterator *iterator) {
    dict_write_int8(iterator, KEY_EVENT_TYPE, EVENT_TYPE_VERSION);
    return dict_write_cstring(iterator, KEY_VERSION, "1.0");
}



static void outbox_sent_handler(DictionaryIterator *iter, void *context) {
}

static void outbox_failed_handler(DictionaryIterator *iter, AppMessageResult reason, void *context) {
}


static void inbox_received_handler(DictionaryIterator *iterator, void *context){
    Tuple *statusTuple = dict_find(iterator, KEY_STATUS);
    if(statusTuple) {
        // This value was stored as JS Number, which is stored here as int32_t
        int32_t status = statusTuple->value->int32;
        APP_LOG(APP_LOG_LEVEL_INFO, "Application intialized with status: %d", (int)status);
        publish("versionMessage", sendVersionAction);
    } else {
        Tuple *tuple = dict_find(iterator, KEY_EVENT_TYPE);
        if (tuple){
            int32_t event = tuple->value->int32;
            SUBSCRIBER(event, iterator);
        } else {
            APP_LOG(APP_LOG_LEVEL_WARNING, "Event type not found");
        }
    }

}

static void inbox_dropped_handler(AppMessageResult reason, void *context){
}

void initializeAppMessage(Subscriber subscriber) {
    SUBSCRIBER = subscriber;
    // Open AppMessage
    app_message_register_inbox_received(inbox_received_handler);
    app_message_register_inbox_dropped(inbox_dropped_handler);
    app_message_register_outbox_sent(outbox_sent_handler);
    app_message_register_outbox_failed(outbox_failed_handler);

    // Largest expected inbox and outbox message sizes
    const uint32_t inbox_size = 128;
    const uint32_t outbox_size = 128;
    app_message_open(inbox_size, outbox_size);
}

AppMessageResult publish(const char *const sendDebugTag, SendAction sendAction) {
    // app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
    DictionaryIterator *iter;

    AppMessageResult messageResult = app_message_outbox_begin(&iter);
    if (isMessageResultNotOk(&messageResult)) {
        APP_LOG(APP_LOG_LEVEL_DEBUG, "[%s] outbox begin: %d", sendDebugTag, messageResult);
        return messageResult;
    }

    DictionaryResult result = sendAction(iter);
    if (isDictionaryResultOk(&result)){
        //TODO: should result DictionaryResult, miss with that at the beginning
        APP_LOG(APP_LOG_LEVEL_DEBUG, "[%s] outbox publish action result with: %d", sendDebugTag, result);
        return messageResult;
    }

    messageResult = app_message_outbox_send();
    APP_LOG(APP_LOG_LEVEL_DEBUG, "[%s] outbox publish: %d", sendDebugTag, messageResult);
    return messageResult;
}
