package io.openroad.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipUtils {

    // based on: https://stackoverflow.com/questions/3382996/how-to-unzip-files-programmatically-in-android
    @Throws(IOException::class)
    fun unzip(zipFile: File?, targetDirectory: File?) {
        val zipInputStream = ZipInputStream(
            BufferedInputStream(FileInputStream(zipFile))
        )
        zipInputStream.use { zis ->
            var ze: ZipEntry
            var count: Int
            val buffer = ByteArray(8192)
            while (zis.nextEntry.also { ze = it } != null) {
                val file = File(targetDirectory, ze.name)
                val dir: File = if (ze.isDirectory) file else file.parentFile as File
                if (!dir.isDirectory() && !dir.mkdirs()) throw FileNotFoundException(
                    "Failed to ensure directory: " +
                            dir.getAbsolutePath()
                )
                if (ze.isDirectory) continue

                val fileOutputStream = FileOutputStream(file)
                fileOutputStream.use { fout ->
                    while (zis.read(buffer).also { count = it } != -1) fout.write(buffer, 0, count)
                }

                /* if time should be restored as well
                    long time = ze.getTime();
                    if (time > 0)
                        file.setLastModified(time);
                    */
            }
        }
    }

}