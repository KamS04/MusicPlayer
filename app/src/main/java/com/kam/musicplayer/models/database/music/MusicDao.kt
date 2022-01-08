package com.kam.musicplayer.models.database.music

import android.util.Log
import androidx.room.*
import com.kam.musicplayer.models.entities.*
import com.kam.musicplayer.utils.Utils.moveElement
import kotlinx.coroutines.flow.Flow
import kotlin.math.max
import kotlin.math.min

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdateSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdateSongs(song: List<Song>)

    @Query("SELECT * FROM SONGS_TABLE")
    fun getAllSongs(): Flow<List<Song>>

    @Delete
    suspend fun deleteSong(song: Song)

    @Delete
    suspend fun deleteSongs(song: List<Song>)

    @Transaction
    @Query("SELECT * FROM PlaylistInfo")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM PlaylistInfo WHERE playlistId = :id")
    fun getPlaylist(id: Long): Flow<Playlist>

    @Insert
    suspend fun insertPlaylistInfo(info: PlaylistInfo): Long

    @Insert
    suspend fun insertPlaylistSong(playlistSong: PlaylistSong)

    @Transaction
    suspend fun insertPlaylist(name: String, vararg songs: Song) {
        val playlistId = insertPlaylistInfo(PlaylistInfo(name))
        for (idx in songs.indices) {
            val song = songs[idx]
            insertPlaylistSong(PlaylistSong(playlistId, idx, song))
        }
    }

    suspend fun addSongsToPlaylist(playlist: Playlist, vararg songs: Song) {
        var idx = playlist.songsCount
        for (song in songs) {
            insertPlaylistSong(PlaylistSong(playlist.info.playlistId, idx, song))
            idx++
        }
    }

    @Update
    suspend fun updatePlaylistInfo(info: PlaylistInfo)

    @Update
    suspend fun updatePlaylistSong(playlistSong: PlaylistSong)

    @Transaction
    suspend fun updatePlaylist(playlist: Playlist) {
        updatePlaylistInfo(playlist.info)
        for (playlistSong in playlist.playlistSongs) {
            updatePlaylistSong(playlistSong)
        }
    }

    @Transaction
    suspend fun moveSongInPlaylist(playlist: Playlist, from: Int, to: Int) {
        Log.i("KMUSIC4", "musicDao")
        val newList = playlist.songs.toMutableList()
        newList.moveElement(from, to)

        (min(from, to) .. max(from, to)).forEach { index ->
            updatePlaylistSong(
                    PlaylistSong(playlist.info.playlistId, index, newList[index])
            )
        }
    }

    @Delete
    suspend fun deletePlaylistInfo(info: PlaylistInfo)

    @Delete
    suspend fun deletePlaylistSong(playlistSong: PlaylistSong)

    @Transaction
    suspend fun deletePlaylist(playlist: Playlist) {
        for (playlistSong in playlist.playlistSongs) {
            deletePlaylistSong(playlistSong)
        }
        deletePlaylistInfo(playlist.info)
    }

    @Transaction
    suspend fun removeSongFromPlaylist(playlist: Playlist, index: Int) {
        val playlistSongs = playlist.playlistSongs.toMutableList()
        for (idx in index until playlistSongs.size-1) {
            val newPlaylistSong = PlaylistSong(playlist.info.playlistId, playlistSongs[idx].position, playlistSongs[idx+1].song)
            updatePlaylistSong(newPlaylistSong)
        }
        deletePlaylistSong(playlistSongs.last())
    }
}