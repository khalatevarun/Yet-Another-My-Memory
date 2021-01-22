package com.example.android.mymemory

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mymemory.models.BoardSize
import com.example.android.mymemory.utils.DEFAULT_ICONS
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var tvTimer: TextView


    private val boardSize: BoardSize = BoardSize.HARD



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)
        tvTimer = findViewById(R.id.tvTimer)

        val choosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomizedImages = (choosenImages + choosenImages).shuffled()

        rvBoard.adapter = MemoryBoardAdapter(this, boardSize, randomizedImages)
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())



        // startTimer()



    }


    var timer: Timer?=null

    //Call this method to start timer on activity start
    private fun startTimer(){
        timer = Timer(10000);
        timer?.start()
    }

    //Call this method to update the timer
    private fun updateTimer(){
        if(timer!=null) {
            val miliis = timer?.millisUntilFinished?.plus(TimeUnit.SECONDS.toMillis(5))
            //Here you need to maintain single instance for previous
            timer?.cancel()
            timer = miliis?.let { Timer(it) };
            timer?.start()
        }else{
            startTimer()
        }
    }

    inner class Timer(miliis:Long) : CountDownTimer(miliis,1000){
        var millisUntilFinished:Long = 0
        override fun onFinish() {
            tvTimer.text = "Game ended!"
          //  handler.removeCallbacks(runnable)

        }

        override fun onTick(millisUntilFinished: Long) {
            this.millisUntilFinished = millisUntilFinished
            tvTimer.text = "Game ends in: "+millisUntilFinished/1000
        }
    }




}