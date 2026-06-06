package com.kalindu.pocketfit.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ProfilePhotoRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun savePhoto(uri: Uri): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val destination = profilePhotoFile()
            val temporaryFile = normalizePhoto(uri)

            try {
                try {
                    Files.move(
                        temporaryFile.toPath(),
                        destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(
                        temporaryFile.toPath(),
                        destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            } finally {
                temporaryFile.delete()
            }

            fileUri(destination)
        }
    }

    suspend fun removePhoto(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val photoFile = profilePhotoFile()
            if (photoFile.exists() && !photoFile.delete()) {
                error("The saved profile picture could not be removed.")
            }
        }
    }

    fun currentPhotoUri(): Uri? {
        val photoFile = runCatching { profilePhotoFile() }.getOrNull() ?: return null
        return photoFile.takeIf(File::exists)?.let(::fileUri)
    }

    fun deleteTemporaryPhoto(uri: Uri) {
        runCatching {
            context.contentResolver.delete(uri, null, null)
        }
    }

    private fun profilePhotoFile(): File {
        val userId = auth.currentUser?.uid
            ?: error("You must be signed in to update your profile picture.")
        val photoDirectory = File(context.filesDir, "images").apply {
            if (!exists() && !mkdirs()) {
                error("The profile picture directory could not be created.")
            }
        }
        return File(photoDirectory, "profile_$userId.jpg")
    }

    private fun fileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun normalizePhoto(sourceUri: Uri): File {
        val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: error("The captured image could not be read.")

        val orientation = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        val rotatedBitmap = rotateBitmap(bitmap, orientation)
        val scaledBitmap = scaleBitmap(rotatedBitmap, MAX_IMAGE_DIMENSION)
        val processingDirectory = File(context.cacheDir, "profile_processing").apply {
            if (!exists() && !mkdirs()) {
                error("The profile picture could not be prepared.")
            }
        }
        val outputFile = File.createTempFile("profile_", ".jpg", processingDirectory)

        try {
            FileOutputStream(outputFile).use { output ->
                check(scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    "The captured image could not be saved."
                }
            }
        } catch (error: Exception) {
            outputFile.delete()
            throw error
        } finally {
            if (scaledBitmap !== rotatedBitmap) scaledBitmap.recycle()
            if (rotatedBitmap !== bitmap) rotatedBitmap.recycle()
            bitmap.recycle()
        }

        return outputFile
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    setRotate(180f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    setRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    setRotate(-90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
            }
        }

        if (matrix.isIdentity) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val largestDimension = maxOf(bitmap.width, bitmap.height)
        if (largestDimension <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / largestDimension
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )
    }

    private companion object {
        const val MAX_IMAGE_DIMENSION = 1280
        const val JPEG_QUALITY = 85
    }
}
