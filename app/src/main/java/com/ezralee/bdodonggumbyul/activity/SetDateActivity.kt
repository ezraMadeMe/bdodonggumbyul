package com.ezralee.bdodonggumbyul.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ezralee.bdodonggumbyul.databinding.ActivitySetDateBinding

class SetDateActivity: AppCompatActivity() {

    val binding: ActivitySetDateBinding by lazy { ActivitySetDateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}