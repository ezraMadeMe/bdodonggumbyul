package com.example.bdodonggumbyul.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bdodonggumbyul.databinding.ActivityDatePickerBinding

class DatePickerActivity: AppCompatActivity() {
    val binding: ActivityDatePickerBinding by lazy { ActivityDatePickerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}