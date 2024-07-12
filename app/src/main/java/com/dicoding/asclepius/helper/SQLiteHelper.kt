package com.dicoding.asclepius.helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class SQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "ImagePredictionDB"
        const val TABLE_NAME = "Predictions"
        const val COLUMN_ID = "_id"
        const val COLUMN_IMAGE = "image"
        const val COLUMN_PREDICTION_RESULT = "prediction_result"
        const val COLUMN_CONFIDENCE_SCORE = "confidence_score"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, "
                + "$COLUMN_IMAGE BLOB, $COLUMN_PREDICTION_RESULT TEXT, "
                + "$COLUMN_CONFIDENCE_SCORE REAL)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertPrediction(image: Bitmap, predictionResult: String, confidenceScore: Float): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageInByte = byteArrayOutputStream.toByteArray()
        contentValues.put(COLUMN_IMAGE, imageInByte)
        contentValues.put(COLUMN_PREDICTION_RESULT, predictionResult)
        contentValues.put(COLUMN_CONFIDENCE_SCORE, confidenceScore)
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getAllPredictions(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun deletePrediction(id: Int): Boolean {
        val db = this.writableDatabase
        return try {
            db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString())) > 0
        } catch (e: SQLException) {
            false
        }
    }

    fun getBitmapFromCursor(cursor: Cursor): Bitmap {
        val imageInByte = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
        return BitmapFactory.decodeByteArray(imageInByte, 0, imageInByte.size)
    }
}
