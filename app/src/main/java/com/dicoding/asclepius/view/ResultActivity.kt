package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.SQLiteHelper

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var currentImageUri: Uri
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        sqliteHelper = SQLiteHelper(this)

        intent?.getStringExtra(EXTRA_IMAGE_URI)?.let { uriString ->
            currentImageUri = Uri.parse(uriString)
            binding.resultImage.setImageURI(currentImageUri)
        } ?: showErrorAndFinish()

        val resultText = intent.getStringExtra(EXTRA_RESULT)
        resultText?.let {
            binding.resultText.text = it
        } ?: showErrorAndFinish()

        binding.buttonSave.setOnClickListener {
            saveAnalysisToSQLite()
        }

    }

    private fun showErrorAndFinish() {
        finish()
    }

    private fun saveAnalysisToSQLite() {
        val predictionResult = binding.resultText.text.toString()
        val confidenceScore = intent.getFloatExtra(EXTRA_CONFIDENCE_SCORE, 0f)

        @Suppress("DEPRECATION") val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, currentImageUri)

        val insertedRowId = sqliteHelper.insertPrediction(imageBitmap, predictionResult, confidenceScore)

        if (insertedRowId != -1L) {
            showSuccessDialog()
        } else {
            showErrorDialog()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Analysis saved successfully")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("Failed to save analysis")
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_CONFIDENCE_SCORE = "extra_confidence_score"
    }
}