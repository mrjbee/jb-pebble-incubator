#include <pebble.h>
#include "../shared_c/connection.h"
#include "playlists.h"

enum {
    KEY_PLAYLIST_ID=4,
    KEY_PLAYLIST_NAME=5
};

enum {
    EVENT_TYPE_PLAYLIST_UPDATED = 1,
    EVENT_TYPE_PLAYLIST_PLAY = 2,
    EVENT_TYPE_OPEN_PLAYER = 3,
};

static Window *s_window;
static TextLayer *text_layer_playlist_title;
static TextLayer *text_layer_playlist_caption;
static ActionBarLayer *action_bar;
static GBitmap *next_bitmap;
static GBitmap *play_bitmap;
static GBitmap *prev_bitmap;
static char* playlistCation;

void text_layer_playlist_title_init(Layer *window_layer, GRect *bounds);
void text_layer_playlist_caption_init(Layer *window_layer, GRect *bounds);


static DictionaryResult action_PlayPlaylistId(DictionaryIterator *iterator) {
    dict_write_int8(iterator, KEY_SYSTEM_EVENT_TYPE, EVENT_TYPE_PLAYLIST_PLAY);
    return dict_write_cstring(iterator, KEY_PLAYLIST_ID, currentPlaylist()->id);
}

static DictionaryResult action_OpenPlayApp(DictionaryIterator *iterator) {
    return dict_write_int8(iterator, KEY_SYSTEM_EVENT_TYPE, EVENT_TYPE_OPEN_PLAYER);
}

static void update_playlist_UI(){
    Playlist_t* current = currentPlaylist();
    text_layer_set_text(text_layer_playlist_title, current->title);
    int index = indexOf(current) + 1;
    int count = countPlaylist();
    snprintf(playlistCation, 100, "Playlist %d/%d", index, count);
    text_layer_set_text(text_layer_playlist_caption, playlistCation);
}

static void btn_select(ClickRecognizerRef recognizer, void *context) {
      if (currentPlaylist() != NULL){
        publish("play",action_PlayPlaylistId);
        //window_stack_pop_all(true);
      }
}

static void btn_up(ClickRecognizerRef recognizer, void *context) {
    if (prevPlaylist() != NULL){
        update_playlist_UI();
    }
}

static void btn_up_long(ClickRecognizerRef recognizer, void *context) {
    publish("open_player", action_OpenPlayApp);
}

static void btn_down(ClickRecognizerRef recognizer, void *context) {
    if (nextPlaylist()!= NULL){
        update_playlist_UI();
    }
}

static void btn_config_provider(void *context) {
    window_single_click_subscribe(BUTTON_ID_SELECT, btn_select);
    window_single_click_subscribe(BUTTON_ID_UP, btn_up);
    window_single_click_subscribe(BUTTON_ID_DOWN, btn_down);
    window_long_click_subscribe(BUTTON_ID_UP, 200, btn_up_long, NULL);
}


static void window_main_load(Window *window) {

    window_set_background_color(window,GColorBlack);
    Layer *window_layer = window_get_root_layer(window);
    GRect bounds = layer_get_bounds(window_layer);

    text_layer_playlist_title_init(window_layer, &bounds);
    text_layer_playlist_caption_init(window_layer, &bounds);

    action_bar = action_bar_layer_create();
    action_bar_layer_add_to_window(action_bar, window);
    action_bar_layer_set_background_color(action_bar, GColorWhite);
    action_bar_layer_set_click_config_provider(action_bar, btn_config_provider);

    // Set the icons:
    // The loading of the icons is omitted for brevity... See gbitmap_create_with_resource()
    next_bitmap = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_NEXT);
    prev_bitmap = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_PREV);
    play_bitmap = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_PLAY);
    action_bar_layer_set_icon_animated(action_bar, BUTTON_ID_UP, prev_bitmap, true);
    action_bar_layer_set_icon_animated(action_bar, BUTTON_ID_SELECT, play_bitmap, true);
    action_bar_layer_set_icon_animated(action_bar, BUTTON_ID_DOWN, next_bitmap, true);
}

void text_layer_playlist_title_init(Layer *window_layer, GRect *bounds) {
    text_layer_playlist_title = text_layer_create(GRect(10, 50, (*bounds).size.w - 20 - ACTION_BAR_WIDTH, (*bounds).size.h - 20 - 40));
    text_layer_set_text(text_layer_playlist_title, "Refreshing...");
    text_layer_set_overflow_mode(text_layer_playlist_title, GTextOverflowModeWordWrap);
    text_layer_set_text_alignment(text_layer_playlist_title, GTextAlignmentCenter);
    text_layer_set_font(text_layer_playlist_title, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_color(text_layer_playlist_title, GColorWhite);
    text_layer_set_background_color(text_layer_playlist_title, GColorBlack);
    layer_add_child(window_layer, text_layer_get_layer(text_layer_playlist_title));
}

void text_layer_playlist_caption_init(Layer *window_layer, GRect *bounds) {
    text_layer_playlist_caption = text_layer_create(GRect(10, 10, (*bounds).size.w - 20 - ACTION_BAR_WIDTH, 40));
    text_layer_set_text(text_layer_playlist_caption, "Please Wait...");
    text_layer_set_overflow_mode(text_layer_playlist_caption, GTextOverflowModeWordWrap);
    text_layer_set_text_alignment(text_layer_playlist_caption, GTextAlignmentLeft);
    text_layer_set_font(text_layer_playlist_caption, fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_color(text_layer_playlist_caption, GColorWhite);
    text_layer_set_background_color(text_layer_playlist_caption, GColorBlack);
    layer_add_child(window_layer, text_layer_get_layer(text_layer_playlist_caption));
}


static void window_main_unload(Window *window) {
    text_layer_destroy(text_layer_playlist_title);
    text_layer_destroy(text_layer_playlist_caption);
    action_bar_layer_destroy(action_bar);
    gbitmap_destroy(next_bitmap);
    gbitmap_destroy(play_bitmap);
    gbitmap_destroy(prev_bitmap);
}


static void deinit(void) {
    window_destroy(s_window);
    free(playlistCation);
    freeCollection();
}

static void init(void) {
    playlistCation = malloc(100);
    s_window = window_create();
    window_set_click_config_provider(s_window, btn_config_provider);
    window_set_window_handlers(s_window, (WindowHandlers) {
            .load = window_main_load,
            .unload = window_main_unload,
    });
    const bool animated = true;
    window_stack_push(s_window, animated);
}

static void on_message(int event, DictionaryIterator *iterator){
    char* id = dict_find(iterator, KEY_PLAYLIST_ID)->value->cstring;
    char* name = dict_find(iterator, KEY_PLAYLIST_NAME)->value->cstring;

    char* copyOfId = malloc(strlen(id));
    strncpy(copyOfId, id, strlen(id));

    char* copyOfName = malloc(strlen(name));
    strncpy(copyOfName, name, strlen(name));

    addPlaylist(copyOfName, copyOfId);
    update_playlist_UI();
}


int main(void) {
    init();
    initializeAppMessage(on_message);
    app_event_loop();
    deinit();
}
