package com.example.android.yetanothermymemory

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.yetanothermymemory.models.BoardSize
import com.example.android.yetanothermymemory.models.MemoryGame
import com.example.android.yetanothermymemory.utils.EXTRA_BOARD_SIZE
import com.example.android.yetanothermymemory.utils.EXTRA_GAME_NAME
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {


    companion object{
        private const val CREATE_REQUEST_CODE = 248
    }


    private lateinit var clRoot:CoordinatorLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var tvTimer: TextView
    private lateinit var memoryGame: MemoryGame
    private  var customGameImages: List<String>? = null
    private lateinit var adapter: MemoryBoardAdapter
    private var gameOver : Boolean = true
    private var gameStarted: Boolean = false

    private val db = Firebase.firestore
    private var gameName: String? = null


    private var boardSize: BoardSize = BoardSize.EASY



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)




        setupBoard()



    }



    override fun onCreateOptionsMenu(menu: Menu?):Boolean{
        menuInflater.inflate(R.menu.menu_main,menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh-> {

                showAlertDialog(getString(R.string.quit_current), null, View.OnClickListener {
                   cancelTimer()
                    setupBoard()
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
            R.id.mi_download->{
                showDownloadDialog()
                return true
            }
        }

        return true

    }

    private fun showDownloadDialog() {
        val boardDownloadView=LayoutInflater.from(this).inflate(R.layout.dialog_download_board,null)
        showAlertDialog(getString(R.string.fetch_game),boardDownloadView,View.OnClickListener {
            val etDownloadGame=boardDownloadView.findViewById<EditText>(R.id.edDownloadGame)
            val gameToDownload=etDownloadGame.text.toString().trim()
            downloadGame(gameToDownload)
        })
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog(getString(R.string.create_board),boardSizeView,View.OnClickListener {

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if(customGameName == null){
                return
            }
            downloadGame(customGameName)

        }



        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener { document->
            val userImageList:UserImageList?=document.toObject(UserImageList::class.java)
            if(userImageList?.images==null){
                Snackbar.make(clRoot,"Sorry, we couldn't find any such game, $customGameName",Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val numCards= userImageList.images!!.size*2
            boardSize= BoardSize.getByValue(numCards)
            customGameImages=userImageList.images
            gameName=customGameName
            for(imageUrl in userImageList.images!!){
                Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot,"You're now playing '$customGameName'!",Snackbar.LENGTH_LONG).show()

            setupBoard()
        }.addOnFailureListener{exception->

        }


    }

    private fun showNewSizeDialog() {

         val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
         val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

            when(boardSize){
                BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
                BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
                BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)

            }
        showAlertDialog(getString(R.string.choose_new_size),boardSizeView,View.OnClickListener {

            boardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName = null
            customGameImages = null

            setupBoard()
        })

    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
       AlertDialog.Builder(this)
           .setTitle(title)
           .setView(view)
           .setNegativeButton(getString(R.string.cancel),null)
           .setPositiveButton(getString(R.string.ok)){_, _ ->

                positiveClickListener.onClick(null)
           }.show()

    }

    private fun setupBoard() {

        supportActionBar?.title = gameName ?: getString(R.string.app_name)

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

        memoryGame = MemoryGame(boardSize, customGameImages)
        tvTimer = findViewById(R.id.tvTimer)



        memoryGame =  MemoryGame(boardSize, customGameImages)
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListner{
            override fun onCardClicked(position: Int) {

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
            Snackbar.make(clRoot, getString(R.string.already_won), Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            Snackbar.make(clRoot, getString(R.string.invalid_move), Snackbar.LENGTH_SHORT).show()
            return
        }

        if(gameOver) {

            if(!gameStarted){
                Snackbar.make(clRoot, getString(R.string.game_not_started), Snackbar.LENGTH_SHORT).show()
                return

            }
            Snackbar.make(clRoot, getString(R.string.game_over), Snackbar.LENGTH_SHORT).show()
            return

        }









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

                Snackbar.make(clRoot,getString(R.string.congratulate),Snackbar.LENGTH_LONG).show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.BLUE, Color.MAGENTA, Color.CYAN)).oneShot()
                cancelTimer()
                tvTimer.setTextColor(getResources().getColor(R.color.color_progress_full))
                tvTimer.text = "You won!"
                gameOver=true
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
        tvTimer.setTextColor(getResources().getColor(R.color.color_red))
        timer?.start()
        gameStarted=false
    }

    private fun startTimer2(){
        gameStarted=true
        when(boardSize){
            BoardSize.EASY -> {
                timer2 = Timer2(10000);

            }
            BoardSize.MEDIUM -> {
                timer2 = Timer2(15000);

            }
            BoardSize.HARD -> {
                timer2 = Timer2(20000);

            }
        }
        timer2 = Timer2(15000);
        tvTimer.setTextColor(getResources().getColor(R.color.black))
        timer2?.start()
    }

    //Call this method to update the timer
    private fun updateTimer(){
        if(timer2!=null) {
            val miliis = timer2?.millisUntilFinished?.plus(TimeUnit.SECONDS.toMillis(5))
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

            tvTimer.text = getString(R.string.game_ended)
            tvTimer.setTextColor(getResources().getColor(R.color.color_red))
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