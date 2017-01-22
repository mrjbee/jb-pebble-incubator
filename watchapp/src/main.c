#include <pebble.h>
#include "../shared_c/connection.h"

static Window *s_window;
static TextLayer *text_layer_playlist_title;


enum {
    KEY_TEST_TEXT=4,
};

enum {
    EVENT_TYPE_TEST = 1,
};


//static DictionaryResult sendCurrentPlaylistId(DictionaryIterator *iterator) {
//    dict_write_int8(iterator, KEY_SYSTEM_EVENT_TYPE, EVENT_TYPE_PLAYLIST_PLAY);
//    return dict_write_cstring(iterator, KEY_PLAYLIST_ID, currentPlaylist()->id);
//}

//static void btn_select(ClickRecognizerRef recognizer, void *context) {
//publish("play",sendCurrentPlaylistId);
//}


static DictionaryResult message_Test(DictionaryIterator *iterator) {
    dict_write_int8(iterator, KEY_SYSTEM_EVENT_TYPE, EVENT_TYPE_TEST);
    return dict_write_cstring(iterator, KEY_TEST_TEXT, "Hello World");
}


static void btn_select(ClickRecognizerRef recognizer, void *context) {
    publish("test", message_Test);
}

static void btn_up(ClickRecognizerRef recognizer, void *context) {
}

static void btn_down(ClickRecognizerRef recognizer, void *context) {
}

static void btn_config_provider(void *context) {
    window_single_click_subscribe(BUTTON_ID_SELECT, btn_select);
    window_single_click_subscribe(BUTTON_ID_UP, btn_up);
    window_single_click_subscribe(BUTTON_ID_DOWN, btn_down);
}

void text_layer_playlist_title_init(Layer *window_layer, GRect *bounds) {
    text_layer_playlist_title = text_layer_create(GRect(10, 50, (*bounds).size.w - 20 - ACTION_BAR_WIDTH, (*bounds).size.h - 20 - 40));
    text_layer_set_text(text_layer_playlist_title, "Yeat another watch my phone");
    text_layer_set_overflow_mode(text_layer_playlist_title, GTextOverflowModeWordWrap);
    text_layer_set_text_alignment(text_layer_playlist_title, GTextAlignmentCenter);
    text_layer_set_font(text_layer_playlist_title, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_color(text_layer_playlist_title, GColorWhite);
    text_layer_set_background_color(text_layer_playlist_title, GColorBlack);
    layer_add_child(window_layer, text_layer_get_layer(text_layer_playlist_title));
}


static void window_main_load(Window *window) {

    window_set_background_color(window,GColorBlack);
    Layer *window_layer = window_get_root_layer(window);
    GRect bounds = layer_get_bounds(window_layer);

    text_layer_playlist_title_init(window_layer, &bounds);

    // Set the icons:
    // The loading of the icons is omitted for brevity... See gbitmap_create_with_resource()
    // next_bitmap = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_NEXT);
    // prev_bitmap = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_PREV);
    // play_bitmap = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_PLAY);
    // action_bar_layer_set_icon_animated(action_bar, BUTTON_ID_UP, prev_bitmap, true);
    // action_bar_layer_set_icon_animated(action_bar, BUTTON_ID_SELECT, play_bitmap, true);
    // action_bar_layer_set_icon_animated(action_bar, BUTTON_ID_DOWN, next_bitmap, true);
}



static void window_main_unload(Window *window) {
    text_layer_destroy(text_layer_playlist_title);
}


static void deinit(void) {
    window_destroy(s_window);
}

static void on_message(int event, DictionaryIterator *iterator){
//    char* id = dict_find(iterator, KEY_PLAYLIST_ID)->value->cstring;
//    char* name = dict_find(iterator, KEY_PLAYLIST_NAME)->value->cstring;
//
//    char* copyOfId = malloc(strlen(id));
//    strncpy(copyOfId, id, strlen(id));

//    char* copyOfName = malloc(strlen(name));
//    strncpy(copyOfName, name, strlen(name));
}


static void init(void) {
    initializeAppMessage(on_message);
    s_window = window_create();
    window_set_click_config_provider(s_window, btn_config_provider);
    window_set_window_handlers(s_window, (WindowHandlers) {
            .load = window_main_load,
            .unload = window_main_unload,
    });
    const bool animated = true;
    window_stack_push(s_window, animated);
}



int main(void) {
    init();
    app_event_loop();
    deinit();
}
