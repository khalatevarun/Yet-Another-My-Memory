package com.example.android.mymemory

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mymemory.models.BoardSize
import com.example.android.mymemory.models.MemoryGame
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {


    private lateinit var clRoot:ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var tvTimer: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var gameOver : Boolean = false


    private val boardSize: BoardSize = BoardSize.EASY



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        tvNumPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
        tvTimer = findViewById(R.id.tvTimer)



         memoryGame =  MemoryGame(boardSize)


     adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListner{
            override fun onCardClicked(position: Int) {
                if(!gameOver)
                updateGameWithFlip(position)


            }

        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())



         startTimer()



    }

    private fun updateGameWithFlip(position: Int) {


        if(memoryGame.haveWonGame()){
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show()
            return
        }
      /**  if(memoryGame.isTimeOver()){
            return
        }
      **/


        if(memoryGame.flipCard(position)){

            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat()/boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full)
            )as Int

            tvNumPairs.setTextColor(color)

            if(memoryGame.bonus){
                updateTimer()
                memoryGame.bonus=false
            }

            tvNumPairs.text="Pairs: ${memoryGame.numPairsFound}/${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()){
                Snackbar.make(clRoot,"You've won! Congratulations.",Snackbar.LENGTH_LONG).show()
                timer?.cancel()
                tvTimer.text = "Game ended! You won!!"
            }
        }

        tvNumMoves.text = "Moves: ${memoryGame.getNumMove()}"

        adapter.notifyDataSetChanged()


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
            gameOver =true


        }

        override fun onTick(millisUntilFinished: Long) {
            this.millisUntilFinished = millisUntilFinished
            tvTimer.text = "Game ends in: "+millisUntilFinished/1000
        }
    }




}