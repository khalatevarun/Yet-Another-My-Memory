package com.example.android.mymemory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class StartScreen : AppCompatActivity() {

    private lateinit var startButton : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        startButton = findViewById<Button>(R.id.start)

        startButton.setOnClickListener(View.OnClickListener {

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()

        })

    }
}