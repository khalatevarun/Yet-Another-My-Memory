package com.example.android.mymemory

import android.animation.ArgbEvaluator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mymemory.models.BoardSize
import com.example.android.mymemory.models.MemoryGame
import com.example.android.mymemory.utils.EXTRA_BOARD_SIZE
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {


    companion object{
        private const val CREATE_REQUEST_CODE = 248
    }


    private lateinit var clRoot:ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var tvTimer: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var gameOver : Boolean = true


    private var boardSize: BoardSize = BoardSize.EASY



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)




        setupBorad()



    }



    override fun onCreateOptionsMenu(menu: Menu?):Boolean{
        menuInflater.inflate(R.menu.menu_main,menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh-> {

                showAlertDialog("Quit your current game?", null, View.OnClickListener {
                   cancelTimer()
                    setupBorad()
                })


            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom ->{
                showCreationDialog()
                return true
            }
        }

        return true

    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Create your own memory board",boardSizeView,View.OnClickListener {

          val   desiredBoardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }

                val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
                startActivityForResult(intent, CREATE_REQUEST_CODE)

        })

    }

    private fun showNewSizeDialog() {

         val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
         val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

            when(boardSize){
                BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
                BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
                BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)

            }
        showAlertDialog("Choose new size",boardSizeView,View.OnClickListener {

            boardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }

            setupBorad()
        })

    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
       AlertDialog.Builder(this)
           .setTitle(title)
           .setView(view)
           .setNegativeButton("Cancel",null)
           .setPositiveButton("OK"){_, _ ->

                positiveClickListener.onClick(null)
           }.show()

    }

    private fun setupBorad() {

        gameOver = true

          cancelTimer()



        when(boardSize){
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy: 4 x 2"
                tvNumPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Medium: 6 x 3"
                tvNumPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Hard: 6 x 4"
                tvNumPairs.text = "Pairs: 0 / 12"
            }
        }

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
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.BLUE, Color.YELLOW, Color.MAGENTA)).oneShot()
                cancelTimer()
                tvTimer.text = "Game ended! You won!!"
            }
        }

        tvNumMoves.text = "Moves: ${memoryGame.getNumMove()}"

        adapter.notifyDataSetChanged()


    }


    var timer: Timer?=null
    var timer2: Timer2?=null

    //Call this method to start timer on activity start
    private fun startTimer(){
        timer = Timer(5000);
        timer?.start()
    }

    private fun startTimer2(){
        timer2 = Timer2(15000);
        timer2?.start()
    }

    //Call this method to update the timer
    private fun updateTimer(){
        if(timer2!=null) {
            val miliis = timer2?.millisUntilFinished?.plus(TimeUnit.SECONDS.toMillis(4))
            //Here you need to maintain single instance for previous
            timer2?.cancel()
            timer2 = miliis?.let { Timer2(it) };
            timer2?.start()
        }else{
            startTimer2()
        }
    }

    inner class Timer(miliis:Long) : CountDownTimer(miliis,1000){
        var millisUntilFinished:Long = 0
        override fun onFinish() {
            gameOver = false
           startTimer2()


        }

        override fun onTick(millisUntilFinished: Long) {
            this.millisUntilFinished = millisUntilFinished
            tvTimer.text = "Game starts in: "+millisUntilFinished/1000
        }
    }

    inner class Timer2(miliis:Long) : CountDownTimer(miliis,1000){
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

    fun cancelTimer(){
        timer?.cancel()
        timer2?.cancel()
    }




}