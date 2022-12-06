package com.adafruit.pyleap.utils

import android.util.Log
import java.io.*
import java.util.zip.ZipFile

// from: https://gist.github.com/NitinPraksash9911/dea21ec4b8ae7df068f8f891187b6d1e

/**
 * UnzipUtils class extracts files and sub-directories of a standard zip file to
 * a destination directory.
 */

object UnzipUtils {
    /**
     * Size of the buffer to read/write data
     */
    private const val BUFFER_SIZE = 4096

    /**
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unzip(zipFilePath: File, destDirectory: String) {

        File(destDirectory).run {
            val success: Boolean
            if (exists()) {
                deleteRecursively()
                success = mkdirs()
            } else {
                success = mkdirs()
            }

            if (!success) {
                Log.w("Test", "Error creating directory: " + path)
            }
        }

        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = destDirectory + File.separator + entry.name

                    if (entry.isDirectory) {
                        // if the entry is a directory, make the directory
                        val dir = File(filePath)
                        val success = dir.mkdirs()
                        if (!success) {
                            Log.w("Test", "Error creating directory: " + filePath)
                        }
                    } else {
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    }
                }
            }
        }
    }

    /**
     * Extracts a zip entry (file entry)
     * @param inputStream
     * @param destFilePath
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }


}