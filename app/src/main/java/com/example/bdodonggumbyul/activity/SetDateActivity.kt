package com.example.bdodonggumbyul.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bdodonggumbyul.adapter.VCalendarAdapter
import com.example.bdodonggumbyul.databinding.ActivitySetDateBinding

class SetDateActivity: AppCompatActivity() {

    val binding: ActivitySetDateBinding by lazy { ActivitySetDateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}