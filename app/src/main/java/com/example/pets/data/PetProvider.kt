package com.example.pets.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.pets.data.PetContract.PetEntry


class PetProvider : ContentProvider() {

    val LOG_TAG = PetProvider::class.java.simpleName

    private var mDbHelper: PetDbHelper? = null

    companion object{
        val PETS = 100
        val PET_ID = 101
        var sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    }

    init{
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS)
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }


    override fun onCreate(): Boolean {
        mDbHelper = PetDbHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val database = mDbHelper!!.readableDatabase
        val cursor: Cursor
        val match = sUriMatcher.match(uri)

        when(match){
            PETS -> cursor = database.query(
                PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                null, null, sortOrder)
            PET_ID -> {
                cursor = database.query(
                    PetContract.PetEntry.TABLE_NAME, projection, PetContract.PetEntry._ID + "=?",
                    arrayOf(ContentUris.parseId(uri).toString()), null,
                    null, sortOrder
                )
            }
            else -> throw IllegalArgumentException("Cannot query unknown URI " + uri)
            }

        cursor.setNotificationUri(context?.contentResolver, uri);

        return cursor
        }

    override fun getType(uri: Uri): String {
        val match = sUriMatcher.match(uri)
        return when (match) {
            PETS -> PetEntry.CONTENT_LIST_TYPE
            PET_ID -> PetEntry.CONTENT_ITEM_TYPE
            else -> throw IllegalStateException("Unknown URI $uri with match $match")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val match: Int = sUriMatcher.match(uri)
        return when (match) {
            PETS -> insertPet(uri, values!!)
            else -> throw java.lang.IllegalArgumentException("Insertion is not supported for $uri")
        }
    }

    private fun insertPet(uri: Uri, values: ContentValues): Uri? {
        val name = values.getAsString(PetEntry.COLUMN_PET_NAME)
            ?: throw java.lang.IllegalArgumentException("Pet requires a name")

        val gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER)
        require(!(gender == null || !PetEntry.isValidGender(gender))) { "Pet requires valid gender" }

        val weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT)
        require(!(weight != null && weight < 0)) { "Pet requires valid weight" }

        val database = mDbHelper!!.writableDatabase

        val id = database.insert(PetEntry.TABLE_NAME, null, values)
        if (id == -1L) {
            Log.e(LOG_TAG, "Failed to insert row for $uri")
            return null
        }

        context?.contentResolver?.notifyChange(uri, null)

        return ContentUris.withAppendedId(uri, id)
    }


    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val database = mDbHelper!!.writableDatabase

        var rowsDeleted: Int = 0

        val match = sUriMatcher.match(uri)
        when (match) {
            PETS ->
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            PET_ID -> {
                database.delete(PetEntry.TABLE_NAME, PetEntry._ID + "=?", arrayOf(ContentUris.parseId(uri).toString()))
            }
            else -> throw java.lang.IllegalArgumentException("Deletion is not supported for $uri")
        }
        if (rowsDeleted != 0) {
            context?.contentResolver?.notifyChange(uri, null);
        }

        return rowsDeleted
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val match = sUriMatcher.match(uri)
        return when (match) {
            PETS -> updatePet(uri, values!!, selection, selectionArgs)
            PET_ID -> {
                updatePet(uri, values!!, PetEntry._ID + "=?", arrayOf(ContentUris.parseId(uri).toString()))
            }
            else -> throw java.lang.IllegalArgumentException("Update is not supported for $uri")
        }
    }

    private fun updatePet(uri: Uri, values : ContentValues, selection: String?, selectionArgs: Array<out String>?) : Int{
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            val name = values.getAsString(PetEntry.COLUMN_PET_NAME)
                ?: throw java.lang.IllegalArgumentException("Pet requires a name")
        }

        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            val gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER)
            require(!(gender == null || !PetEntry.isValidGender(gender))) { "Pet requires valid gender" }
        }

        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            val weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT)
            require(!(weight != null && weight < 0)) { "Pet requires valid weight" }
        }

        if (values.size() == 0) {
            return 0;
        }
        val database = mDbHelper!!.writableDatabase

        val rowsUpdated = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs)

        if (rowsUpdated != 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }

        return rowsUpdated
    }
}