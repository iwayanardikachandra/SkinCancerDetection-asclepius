package com.dicoding.asclepius.view

import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.adapter.HistoryAdapter
import com.dicoding.asclepius.databinding.ActivityHistoryBinding
import com.dicoding.asclepius.helper.SQLiteHelper

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sqliteHelper = SQLiteHelper(this)

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        val cursor: Cursor? = sqliteHelper.getAllPredictions()
        historyAdapter = HistoryAdapter(sqliteHelper, cursor)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun loadData() {
        val cursor: Cursor? = sqliteHelper.getAllPredictions()
        historyAdapter.swapCursor(cursor)
    }
}