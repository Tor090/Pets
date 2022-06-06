package com.example.pets


import android.content.Context
import android.database.Cursor
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.example.pets.data.PetContract.PetEntry


class PetCursorAdapter(context: Context, cursor: Cursor?) : CursorAdapter(context, cursor, 0) {
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val nameTextView = view!!.findViewById<TextView>(R.id.name)
        val summaryTextView = view!!.findViewById<TextView>(R.id.summary)

        val nameColumnIndex = cursor!!.getColumnIndex(PetEntry.COLUMN_PET_NAME)
        val breedColumnIndex = cursor!!.getColumnIndex(PetEntry.COLUMN_PET_BREED)

        val petName = cursor.getString(nameColumnIndex)
        var petBreed = cursor.getString(breedColumnIndex)

        if (TextUtils.isEmpty(petBreed)) {
            petBreed = context!!.getString(R.string.unknown_breed);
        }

        nameTextView.text =petName
        summaryTextView.text = petBreed
    }

}