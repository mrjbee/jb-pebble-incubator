#include "playlists.h"
#include <stdbool.h>
#include <stddef.h>
#include <malloc.h>

Playlist_t * HEAD = NULL;
Playlist_t * CURRENT = NULL;

Playlist_t* headPlaylist() {
    return HEAD;
}

Playlist_t* currentPlaylist() {
    if (CURRENT == NULL){
        CURRENT = headPlaylist();
    }
    return CURRENT;
}

void addPlaylist(char *title, char *id) {

    Playlist_t * playlist = malloc(sizeof(Playlist_t));
    playlist->id = id;
    playlist->title = title;
    playlist->next = NULL;
    playlist->prev = NULL;

    if (HEAD == NULL){
        HEAD = playlist;
        CURRENT = playlist;
    } else {
        Playlist_t * it = HEAD;
        while (it->next != NULL){
            it = it->next;
        }
        it->next = playlist;
        playlist->prev = it;
    }
}



int countPlaylist() {
    if (HEAD == NULL){
        return 0;
    } else {
        int size = 0;
        Playlist_t * it = HEAD;
        while (it != NULL){
            it = it->next;
            size++;
        }
        return size;
    }
}

Playlist_t *nextPlaylist() {
    if (CURRENT == NULL){
        CURRENT = HEAD;
    } else {
        if (CURRENT->next == NULL){
            CURRENT = HEAD;
        } else {
            CURRENT = CURRENT->next;
        }
    }
    return CURRENT;
}

Playlist_t *prevPlaylist() {
    if (CURRENT == NULL){
        CURRENT = HEAD;
    } else {
        if (CURRENT->prev == NULL){
            Playlist_t * it = HEAD;
            while (it->next != NULL){
                it = it->next;
            }
            CURRENT = it;
        } else {
            CURRENT = CURRENT -> prev;
        }
    }
    return CURRENT;
}

int indexOf(Playlist_t *playlist) {
    int result = 0;
    Playlist_t* it = HEAD;
    while (it != NULL){

        if (it  == playlist){
            return result;
        }

        it = it->next;
        result = result + 1;
    }
    return -1;
}

void freeCollection() {
    Playlist_t* it = HEAD;
    while (it != NULL){
        Playlist_t* remove = it;
        it = it->next;
        free(remove->title);
        free(remove->id);
        free(remove);
    }
}
