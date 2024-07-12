package com.dicoding.asclepius.adapter

import android.annotation.SuppressLint
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.R
import com.dicoding.asclepius.helper.SQLiteHelper

class HistoryAdapter(private val sqliteHelper: SQLiteHelper, cursor: Cursor?) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var cursor: Cursor? = null

    init {
        this.cursor = cursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewholder: ViewHolder, position: Int) {
        cursor?.moveToPosition(position)
        val predictionResult =
            cursor?.getColumnIndexOrThrow(SQLiteHelper.COLUMN_PREDICTION_RESULT)
                ?.let { cursor?.getString(it) }
        viewholder.textView.text = predictionResult

        viewholder.deleteButton.setOnClickListener {
            deleteItem(viewholder.itemView, position)
        }

        val imageBitmap = cursor?.let { sqliteHelper.getBitmapFromCursor(it) }
        viewholder.imageView.setImageBitmap(imageBitmap)

        viewholder.deleteButton.setOnClickListener {
            deleteItem(viewholder.itemView, position)
        }

    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    private fun deleteItem(itemView: View, position: Int) {
        cursor?.moveToPosition(position)
        val id = cursor?.getInt(cursor!!.getColumnIndexOrThrow(SQLiteHelper.COLUMN_ID))
        id?.let { itemId ->
            val alertDialogBuilder = AlertDialog.Builder(itemView.context)
            alertDialogBuilder.apply {
                setTitle("Delete Item")
                setMessage("Are you sure you want to delete this item?")
                setPositiveButton("Yes") { _, _ ->
                    sqliteHelper.deletePrediction(itemId)
                    swapCursor(sqliteHelper.getAllPredictions())
                }
                setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                create().show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapCursor(newCursor: Cursor?) {
        cursor?.close()
        cursor = newCursor
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}