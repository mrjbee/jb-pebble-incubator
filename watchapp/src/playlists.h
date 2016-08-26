
#include <pebble.h>

typedef struct Playlist {
    char *title;
    char *id;
    struct Playlist * next;
    struct Playlist * prev;
} Playlist_t;

Playlist_t* headPlaylist();
Playlist_t* currentPlaylist();
Playlist_t* nextPlaylist();
Playlist_t* prevPlaylist();
void addPlaylist(char *title, char *id);
int countPlaylist();
int indexOf(Playlist_t* playlist);
void freeCollection();
