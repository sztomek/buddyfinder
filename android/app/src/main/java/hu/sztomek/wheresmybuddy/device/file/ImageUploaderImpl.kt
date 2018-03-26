package hu.sztomek.wheresmybuddy.device.file

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import hu.sztomek.wheresmybuddy.domain.ImageUploader
import io.reactivex.Single
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class ImageUploaderImpl(private val imageResizer: ImageResizer, private val storage: FirebaseStorage) : ImageUploader {

    override fun uploadImageFromFile(userId: String, filePath: String): Single<String> {
        return Single.create { emitter ->
            val file = File(filePath)
            if (!file.isFile) {
                emitter.onError(ImageUploaderException("File doesn't exist [$filePath]"))
            } else {
                val uri = Uri.fromFile(file)
                val resized = imageResizer.resize(filePath)
                val stream = ByteArrayOutputStream()
                resized.compress(Bitmap.CompressFormat.PNG, 0, stream)
                storage.reference.child("$userId/${uri.lastPathSegment}")
                        .putStream(ByteArrayInputStream(stream.toByteArray()))
                        .addOnCompleteListener({
                            if (it.isSuccessful) {
                                if (it.result.downloadUrl != null) {
                                    emitter.onSuccess(it.result.downloadUrl!!.toString())
                                } else {
                                    Timber.d("Upload successful, but downloadUrl is null :| [$filePath]")
                                    emitter.onError(ImageUploaderException("Something went wrong"))
                                }
                            } else {
                                emitter.onError(ImageUploaderException(it.exception?.toString()
                                        ?: "Failed to upload image [$filePath]"))
                            }
                        })
            }
        }
    }

}