package com.wt.receivesharedemo

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object Utils {
    /**
     * 获取真实路径
     *
     * @param context
     */
    fun getFileFromUri(context: Context, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                //Android7.0之后的uri content:// URI
                return getFilePathFromContentUri(context, uri)
            }

            ContentResolver.SCHEME_FILE -> {
                //Android7.0之前的uri file://
                return File(uri.path).absolutePath
            }

            else -> {
                //Android7.0之前的uri file://
                return File(uri.path).absolutePath
            }
        }
    }


    /**
     * 从uri获取path
     *
     * @param uri content://media/external/file/109009
     *            <p>
     *            FileProvider适配
     *            content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Tencent/QQfile_recv/
     *            content://com.tencent.mm.external.fileprovider/external/tencent/MicroMsg/Download/
     */
    private fun getFilePathFromContentUri(context: Context, uri: Uri?): String? {
        if (null == uri) return null
        var data: String? = null

        val filePathColumn =
            arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor: Cursor? = context.contentResolver.query(
            uri,
            filePathColumn,
            null,
            null,
            null
        )
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                data = if (index > -1) {
                    cursor.getString(index)
                } else {
                    val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(nameIndex)
                    getPathFromInputStreamUri(context, uri, fileName)
                }
            }
            cursor.close()
        }
        return data
    }

    /**
     * 用流拷贝文件一份到自己APP私有目录下
     *
     * @param context
     * @param uri
     * @param fileName
     */
    private fun getPathFromInputStreamUri(context: Context, uri: Uri, fileName: String): String? {
        var inputStream: InputStream? = null
        var filePath: String? = null

        if (uri.authority != null) {
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                val file: File? = createTemporalFileFrom(context, inputStream, fileName)
                filePath = file?.path

            } catch (_: Exception) {
            } finally {
                try {
                    inputStream?.close()
                } catch (_: Exception) {
                }
            }
        }

        return filePath
    }

    private fun createTemporalFileFrom(
        context: Context,
        inputStream: InputStream?,
        fileName: String
    ): File? {
        var targetFile: File? = null

        if (inputStream != null) {
            var read: Int
            val buffer = ByteArray(8 * 1024)
            //自己定义拷贝文件路径
            targetFile = File(context.externalCacheDir, fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            val outputStream: OutputStream = FileOutputStream(targetFile)

            read = inputStream.read(buffer)
            while (read != -1) {
                outputStream.write(buffer, 0, read)
                read = inputStream.read(buffer)
            }
            outputStream.flush()

            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return targetFile
    }
}