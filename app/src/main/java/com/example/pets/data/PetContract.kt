package com.example.pets.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns


class PetContract {

    companion object{
        const val CONTENT_AUTHORITY = "com.example.pets"
        val BASE_CONTENT_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")
        const val PATH_PETS = "pets"
    }

    class PetEntry : BaseColumns {
        companion object {
            val CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS)
            const val CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS
            const val CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS


            val TABLE_NAME = "pets"

            val _ID = BaseColumns._ID
            val COLUMN_PET_NAME = "name"
            val COLUMN_PET_BREED = "breed"
            val COLUMN_PET_GENDER = "gender"
            val COLUMN_PET_WEIGHT = "weight"

            const val GENDER_UNKNOWN = 0
            const val GENDER_MALE = 1
            const val GENDER_FEMALE = 2

            fun isValidGender(gender: Int): Boolean {
                return gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE
            }
        }
    }
}