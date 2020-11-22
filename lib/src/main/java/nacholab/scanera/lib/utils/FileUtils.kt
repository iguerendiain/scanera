package nacholab.scanera.lib.utils

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

fun downloadFile(sourceURL: String, destinationPath: String){
    var output: FileOutputStream? = null
    var input: InputStream? = null

    try{
        val url = URL(sourceURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK){
            throw IOException("Server returned ${connection.responseCode}")
        }

        output = FileOutputStream(destinationPath)
        input = connection.inputStream
        val buffer = ByteArray(4096)
        var total = 0L
        var count = input.read(buffer)

        while(count!=-1){
            total+=count
            output.write(buffer, 0, count)
            count = input.read(buffer)
        }
    }catch(e: Exception) {
        throw e
    }finally {
        output?.close()
        input?.close()
    }
}


fun downloadFileCached(sourceURL: String, destinationDirPath: String?): String {
    val TAG = "FILE-DOWNLOADER"

    val destinationFilename = "$destinationDirPath/${sourceURL
        .replace("https://","")
        .replace("http://","")
        .replace("/","_")}"

    return if (File(destinationFilename).exists()){
        Log.i(TAG, "File for [$sourceURL] exists as [$destinationFilename]")
        destinationFilename
    }else {
        Log.i(TAG, "No file for [$sourceURL]. Downloading to [$destinationFilename]")
        downloadFile(sourceURL, destinationFilename)
        destinationFilename
    }
}

fun Application.copyExternalFileMediaStoreUriToFile(id: Long, internalTarget: File) {
    copyMediaStoreUriToFile(
        ContentUris.withAppendedId(
            MediaStore.Files.getContentUri(
                "external"
            ),
            id
        ),
        internalTarget
    )
}

fun Application.copyMediaStoreUriToFile(uri: Uri, internalTarget: File){
    contentResolver.openInputStream(uri).use { input ->
        internalTarget.outputStream().use { output ->
            input?.copyTo(output)
        }
    }
}

enum class InternalFileType {
    BACKGROUND_MUSIC,
    VIDEO_OBJECT,
    IMAGE_OBJECT,
    GIF_OBJECT,
    STICKER
}

fun Application.buildInternalFile(cardId: Int, type: InternalFileType, extension: String): File{
    val basePath = "card_$cardId"

    val relativePath = when (type){
        InternalFileType.BACKGROUND_MUSIC -> ""
        else -> "pagesObjects"
    }

    val now = System.currentTimeMillis()
    val fileName = when (type){
        InternalFileType.BACKGROUND_MUSIC -> "backgroundMusic.$extension"
        InternalFileType.VIDEO_OBJECT -> "video_${now}.$extension"
        InternalFileType.IMAGE_OBJECT -> "image_${now}.$extension"
        InternalFileType.GIF_OBJECT -> "gif_${now}.$extension"
        InternalFileType.STICKER -> "sticker_${now}.$extension"
    }

    val dir = File(filesDir, "${basePath}/${relativePath}")
    dir.mkdirs()

    return File(dir, fileName)
}

fun Context.getFileDuration(path: String): Long{
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, Uri.fromFile(File(path)))
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        duration?.toLong()?:0L
    }catch (e: java.lang.Exception){
        0
    }
}

fun Application.getMimeType(uri: Uri): String?{
    return if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
        contentResolver.getType(uri)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
    }
}

fun getExtensionFromMimeType(mimeType: String) =
    MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

fun Context.copyAssetFile(assetSourcePath: String, targetFile: File): Boolean{
    return try {
        val input = assets.open(assetSourcePath)
        val output = FileOutputStream(targetFile)
        input.copyTo(output)
        true
    }catch (e: Exception){
        false
    }
}