//
// Created by mrjbee on 8/17/16.
//

#include <pebble.h>

enum {
    KEY_SYSTEM_VERSION = 1,
    KEY_SYSTEM_EVENT_TYPE=3,
};

enum {
    EVENT_SYSTEM_TYPE_VERSION = 0,
};

typedef DictionaryResult (*SendAction)(DictionaryIterator *iterator);
typedef void (*Subscriber)(int event, DictionaryIterator *iterator);


void initializeAppMessage(Subscriber subscriber);
AppMessageResult publish(const char *const sendDebugTag, SendAction sendAction);