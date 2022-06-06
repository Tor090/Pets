package com.example.pets

import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.pets.data.PetContract.PetEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CatalogActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val PET_LOADER = 0

    var mCursorAdapter: PetCursorAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            val intent = Intent(this@CatalogActivity, EditorActivity::class.java)
            startActivity(intent)
        }

        val petListView: ListView = findViewById(R.id.list)
        val emptyView = findViewById<View>(R.id.empty_view)
        petListView.emptyView = emptyView

        mCursorAdapter = PetCursorAdapter(this, null)
        petListView.adapter = mCursorAdapter

        petListView.onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                val intent = Intent(this@CatalogActivity, EditorActivity::class.java)

                val currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id)

                intent.data = currentPetUri

                startActivity(intent)
            }

        LoaderManager.getInstance(this).initLoader(PET_LOADER, null, this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_catalog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_insert_dummy_data ->  {
                insertPet()
                return true
            }
            R.id.action_delete_all_entries -> {
                deleteAllPets()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED
        )

        return CursorLoader(
            this,
            PetEntry.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        mCursorAdapter!!.swapCursor(data);
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mCursorAdapter!!.swapCursor(null);
    }

    private fun insertPet() {

        val values = ContentValues()
        values.put(PetEntry.COLUMN_PET_NAME, "Toto")
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier")
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE)
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7)

        val newUri: Uri? = contentResolver.insert(PetEntry.CONTENT_URI, values)

    }

    private fun deleteAllPets() {
        val rowsDeleted = contentResolver.delete(PetEntry.CONTENT_URI, null, null)
        Log.v("CatalogActivity", "$rowsDeleted rows deleted from pet database")
    }
}