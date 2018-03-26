package hu.sztomek.wheresmybuddy.domain

import io.reactivex.Single

interface ImageUploader {

    fun uploadImageFromFile(userId: String, filePath: String) : Single<String>

}