package com.ezralee.bdodonggumbyul.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ezralee.bdodonggumbyul.databinding.ActivityDatePickerBinding

class DatePickerActivity: AppCompatActivity() {
    val binding: ActivityDatePickerBinding by lazy { ActivityDatePickerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}