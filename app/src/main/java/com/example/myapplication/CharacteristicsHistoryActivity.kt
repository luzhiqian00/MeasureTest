package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityCharacteristicsHistoryBinding

class CharacteristicsHistoryActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCharacteristicsHistoryBinding.inflate(layoutInflater) }
    private val characteristicValueListView by lazy { binding.characteristicValueListView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}