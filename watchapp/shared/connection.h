//
// Created by mrjbee on 8/17/16.
//

#include <pebble.h>

enum {
    KEY_STATUS = 0,
    KEY_VERSION = 1,
    KEY_EVENT_TYPE=3,
    KEY_PLAYLIST_ID=4,
    KEY_PLAYLIST_NAME=5
};

enum {
    EVENT_TYPE_VERSION = 0,
    EVENT_TYPE_PLAYLIST_UPDATED = 1,
    EVENT_TYPE_PLAYLIST_PLAY = 2,
};

typedef DictionaryResult (*SendAction)(DictionaryIterator *iterator);
typedef void (*Subscriber)(int event, DictionaryIterator *iterator);


void initializeAppMessage(Subscriber subscriber);
AppMessageResult publish(const char *const sendDebugTag, SendAction sendAction);