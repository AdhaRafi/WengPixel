package com.wengpixel.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

object ImageUtils {

    /**
     * Membaca Bitmap dari Uri dengan memperhitungkan orientasi EXIF
     * dan batas ukuran maksimum memori guna mencegah OutOfMemoryError.
     */
    fun loadRotatedBitmapFromUri(
        context: Context,
        uri: Uri,
        maxDimension: Int = 2048
    ): Bitmap? {
        val contentResolver = context.contentResolver

        // 1. Dapatkan dimensi asli tanpa decode utuh ke memori
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: return null

        val origWidth = options.outWidth
        val origHeight = options.outHeight
        if (origWidth <= 0 || origHeight <= 0) return null

        // 2. Hitung sub-sampling jika gambar sangat besar (misal 48MP/108MP)
        var sampleSize = 1
        var halfWidth = origWidth
        var halfHeight = origHeight
        while (halfWidth > maxDimension || halfHeight > maxDimension) {
            sampleSize *= 2
            halfWidth /= 2
            halfHeight /= 2
        }

        // 3. Decode bitmap sesungguhnya
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decodedBitmap = contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        // 4. Periksa orientasi EXIF
        val orientation = contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        return rotateBitmapByExif(decodedBitmap, orientation)
    }

    /**
     * Membaca Bitmap dari file lokal dengan orientasi EXIF.
     */
    fun loadRotatedBitmapFromFile(file: File, maxDimension: Int = 2048): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        val origWidth = options.outWidth
        val origHeight = options.outHeight
        if (origWidth <= 0 || origHeight <= 0) return null

        var sampleSize = 1
        var halfWidth = origWidth
        var halfHeight = origHeight
        while (halfWidth > maxDimension || halfHeight > maxDimension) {
            sampleSize *= 2
            halfWidth /= 2
            halfHeight /= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return rotateBitmapByExif(bitmap, orientation)
    }

    private fun rotateBitmapByExif(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    /**
     * Mengganti latar belakang transparan gambar dengan warna solid tertentu.
     * Jika targetColor adalah 0 (Color.TRANSPARENT), bitmap asli transparan dipertahankan.
     */
    fun applyBackgroundColor(sourceBitmap: Bitmap, targetColor: Int): Bitmap {
        if (targetColor == 0 || android.graphics.Color.alpha(targetColor) == 0) {
            return sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val resultBitmap = Bitmap.createBitmap(
            sourceBitmap.width,
            sourceBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(targetColor)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        return resultBitmap
    }

    /**
     * Memotong bitmap ke rasio tertentu (1:1, 4:3, 16:9, 9:16, dsb.) di bagian tengah.
     */
    fun cropCenterWithRatio(source: Bitmap, targetRatio: Float): Bitmap {
        val currentRatio = source.width.toFloat() / source.height.toFloat()
        val cropX: Int
        val cropY: Int
        val cropWidth: Int
        val cropHeight: Int

        if (currentRatio > targetRatio) {
            // Gambar lebih lebar dari rasio yang diinginkan -> potong sisi samping
            cropHeight = source.height
            cropWidth = (cropHeight * targetRatio).roundToInt().coerceAtMost(source.width)
            cropX = (source.width - cropWidth) / 2
            cropY = 0
        } else {
            // Gambar lebih tinggi dari rasio yang diinginkan -> potong sisi atas & bawah
            cropWidth = source.width
            cropHeight = (cropWidth / targetRatio).roundToInt().coerceAtMost(source.height)
            cropX = 0
            cropY = (source.height - cropHeight) / 2
        }

        return Bitmap.createBitmap(source, cropX, cropY, cropWidth, cropHeight)
    }

    /**
     * Mengubah ukuran bitmap ke dimensi tertentu dengan mempertahankan kejernihan.
     */
    fun resizeBitmap(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        if (targetWidth <= 0 || targetHeight <= 0) return source
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    /**
     * Menyimpan bitmap ke berkas dengan format PNG (mempertahankan alpha channel)
     * atau JPEG (jika tidak membutuhkan transparansi).
     */
    fun saveBitmapToFile(
        bitmap: Bitmap,
        targetFile: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Boolean {
        return try {
            targetFile.parentFile?.mkdirs()
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(format, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Mengonversi ukuran byte ke format teks (KB / MB).
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format("%.2f MB", mb)
        } else {
            String.format("%.1f KB", kb)
        }
    }
}
