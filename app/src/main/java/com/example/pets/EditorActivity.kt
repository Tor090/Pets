package com.example.pets

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.pets.data.PetContract.PetEntry


@SuppressLint("ClickableViewAccessibility")
class EditorActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>{


    private val EXISTING_PET_LOADER = 0

    private var mCurrentPetUri: Uri? = null

    private var mPetHasChanged = false

    private var mNameEditText: EditText? = null

    private var mBreedEditText: EditText? = null

    private var mWeightEditText: EditText? = null

    private var mGenderSpinner: Spinner? = null

    private var mGender = PetEntry.GENDER_UNKNOWN

    private val mTouchListener =
        OnTouchListener { view, motionEvent ->
            mPetHasChanged = true
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        val intent = intent
        mCurrentPetUri = intent.data

        if (mCurrentPetUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));

            LoaderManager.getInstance(this).initLoader(EXISTING_PET_LOADER, null, this)

        }

        mNameEditText = findViewById(R.id.edit_pet_name)
        mBreedEditText = findViewById(R.id.edit_pet_breed)
        mWeightEditText = findViewById(R.id.edit_pet_weight)
        mGenderSpinner = findViewById(R.id.spinner_gender)

        mNameEditText!!.setOnTouchListener(mTouchListener)
        mBreedEditText!!.setOnTouchListener(mTouchListener)
        mWeightEditText!!.setOnTouchListener(mTouchListener)
        mGenderSpinner!!.setOnTouchListener(mTouchListener)

        setupSpiner()
    }

    override fun onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed()
            return
        }

        val discardButtonClickListener =
            DialogInterface.OnClickListener { dialogInterface, i ->
                finish()
            }

        showUnsavedChangesDialog(discardButtonClickListener)
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        val projection = arrayOf(
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT
        )

        return CursorLoader(
            this,
            mCurrentPetUri!!,
            projection,
            null,
            null,
            null
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        if (mCurrentPetUri == null) {
            val menuItem = menu.findItem(R.id.action_delete)
            menuItem.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_save -> {
                savePet()
                finish()
                return true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                return true
            }
            android.R.id.home -> {
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this.parent);
                    return true
                }

                val discardButtonClickListener =
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        NavUtils.navigateUpFromSameTask(this@EditorActivity)
                    }
                showUnsavedChangesDialog(discardButtonClickListener)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (cursor == null || cursor.count < 1) {
            return
        }

        if (cursor.moveToFirst()) {
            val nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)
            val breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)
            val genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER)
            val weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)

            val name = cursor.getString(nameColumnIndex)
            val breed = cursor.getString(breedColumnIndex)
            val gender = cursor.getInt(genderColumnIndex)
            val weight = cursor.getInt(weightColumnIndex)

            mNameEditText!!.setText(name)
            mBreedEditText!!.setText(breed)
            mWeightEditText!!.setText(weight.toString())
            when (gender) {
                PetEntry.GENDER_MALE -> mGenderSpinner!!.setSelection(1)
                PetEntry.GENDER_FEMALE -> mGenderSpinner!!.setSelection(2)
                else -> mGenderSpinner!!.setSelection(0)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mNameEditText!!.setText("")
        mBreedEditText!!.setText("")
        mWeightEditText!!.setText("")
        mGenderSpinner!!.setSelection(0)
    }

    private fun setupSpiner(){
        val genderSpinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.array_gender_options, android.R.layout.simple_spinner_item
        )

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        mGenderSpinner?.adapter = genderSpinnerAdapter

        mGenderSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selection = parent?.getItemAtPosition(position)
                if(!TextUtils.isEmpty(selection as CharSequence?)){
                    if (selection!!.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                mGender = PetEntry.GENDER_UNKNOWN
            }

        }
    }

    private fun deletePet() {
        if (mCurrentPetUri != null) {
            val rowsDeleted = contentResolver.delete(mCurrentPetUri!!, null, null)

            if (rowsDeleted == 0) {
                Toast.makeText(
                    this, getString(R.string.editor_delete_pet_failed),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, getString(R.string.editor_delete_pet_successful),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        finish()
    }

    private fun showDeleteConfirmationDialog() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(R.string.delete,
            { dialog, id ->
                deletePet()
            })
        builder.setNegativeButton(R.string.cancel,
            { dialog, id ->
                dialog?.dismiss()
            })

        val alertDialog: android.app.AlertDialog? = builder.create()
        alertDialog!!.show()
    }

    private fun showUnsavedChangesDialog(
        discardButtonClickListener: DialogInterface.OnClickListener
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
        builder.setPositiveButton(R.string.discard, discardButtonClickListener)
        builder.setNegativeButton(R.string.keep_editing, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, id: Int) {
                if (dialog != null) {
                    dialog.dismiss()
                }
            }
        })

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun savePet() {
        val nameString = mNameEditText!!.text.toString().trim { it <= ' ' }
        val breedString = mBreedEditText!!.text.toString().trim { it <= ' ' }
        val weightString = mWeightEditText!!.text.toString().trim { it <= ' ' }

        if (mCurrentPetUri == null &&
            TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
            TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN
        ) {
            return
        }

        val values = ContentValues()
        values.put(PetEntry.COLUMN_PET_NAME, nameString)
        values.put(PetEntry.COLUMN_PET_BREED, breedString)
        values.put(PetEntry.COLUMN_PET_GENDER, mGender)
        var weight = 0
        if (!TextUtils.isEmpty(weightString)) {
            weight = weightString.toInt()
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight)

        if (mCurrentPetUri == null) {
            val newUri = contentResolver.insert(PetEntry.CONTENT_URI, values)

            if (newUri == null) {
                Toast.makeText(
                    this, getString(R.string.editor_insert_pet_failed),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, getString(R.string.editor_insert_pet_successful),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val rowsAffected = contentResolver.update(mCurrentPetUri!!, values, null, null)

            if (rowsAffected == 0) {
                Toast.makeText(
                    this, getString(R.string.editor_update_pet_failed),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, getString(R.string.editor_update_pet_successful),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
