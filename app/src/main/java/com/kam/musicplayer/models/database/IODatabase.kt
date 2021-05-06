package com.kam.musicplayer.models.database

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import com.kam.musicplayer.models.database.music.MusicDao
import com.kam.musicplayer.models.entities.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IODatabase(private val context: Context, private val musicDao: MusicDao) {

    @SuppressLint("Recycle")
    suspend fun refreshAllSongs() : MutableList<Song> = withContext(Dispatchers.IO) {
        val columns: Array<String> = arrayOf(
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ALBUM_ID,
            MediaStore.Audio.ArtistColumns.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media._ID
        )

        val where = "${MediaStore.Audio.Media.IS_MUSIC}=1"

        val audioCursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            columns,
            where,
            null, null
        )

        return@withContext if (audioCursor != null) retrieveSongs(audioCursor) else mutableListOf()
    }

    private  suspend fun retrieveSongs(cursor: Cursor): MutableList<Song> = withContext(Dispatchers.IO) {
        val output = mutableListOf<Song>()
        if (cursor.moveToFirst()) {
            val getColIdx = { column: String -> cursor.getColumnIndex(column) }

            val colTitle = getColIdx(MediaStore.Audio.Media.TITLE)
            val colArtist = getColIdx(MediaStore.Audio.Artists.ARTIST)
            val colAlbum = getColIdx(MediaStore.Audio.AlbumColumns.ALBUM)
            val colDisplayName = getColIdx(MediaStore.Audio.Media.DISPLAY_NAME)

            val colAlbumId = getColIdx(MediaStore.Audio.AlbumColumns.ALBUM_ID)
            val colId = getColIdx(MediaStore.Audio.Media._ID)

            do {
                val title = cursor.getString(colTitle)
                val artist = cursor.getString(colArtist)
                val album = cursor.getString(colAlbum)
                val displayName = cursor.getStringOrNull(colDisplayName) ?: ""

                val albumId = cursor.getLong(colAlbumId)
                val id = cursor.getLong(colId)

                val fileUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "$id")
                // val coverUri = Uri.withAppendedPath(Constants.artworksUri, "$albumId")

                val albumColumns = arrayOf(MediaStore.Audio.AlbumColumns.ALBUM)

                val albumArtCursor = context.contentResolver.query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    albumColumns,
                    MediaStore.Audio.AlbumColumns.ALBUM_ART + "=$albumId",
                    null, null
                )

                val albumUri = if (albumArtCursor != null && albumArtCursor.moveToFirst()) {
                    Uri.parse(albumArtCursor.getString(albumArtCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)))
                } else null

                albumArtCursor?.close()

                output.add(
                    Song(id,
                        title ?: displayName,
                        fileUri,
                        artist ?: "",
                        album ?: "",
                        albumUri
                    )
                )

            } while (cursor.moveToNext())
        }

        return@withContext output
    }

}