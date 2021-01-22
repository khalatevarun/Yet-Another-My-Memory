package com.example.android.mymemory.models

import com.example.android.mymemory.utils.DEFAULT_ICONS

class MemoryGame(
    private val boardSize: BoardSize,
   private val customImages: List<String>?
){


    val cards: List<MemoryCard>
    var numPairsFound = 0
    var TimeOver = false
    var bonus = true

    private var numCardFlips = 0
    private var indexOfSingleSelectedCard: Int? = null
        init {
            if (customImages == null) {
                val choosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
                val randomizedImages = (choosenImages + choosenImages).shuffled()
                cards = randomizedImages.map { MemoryCard(it) }
            }else{
                val randomziedImages = (customImages +customImages).shuffled()
                cards = randomziedImages.map { MemoryCard(it.hashCode(),it) }
            }
        }


    fun flipCard(position: Int)  : Boolean{
       numCardFlips++
        val card = cards[position]

        var foundMatch = false

        if(indexOfSingleSelectedCard == null){
            restoreCards()
            indexOfSingleSelectedCard = position
        }
        else{
           foundMatch =  checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }

        card.isFaceUp = !card.isFaceUp
        return foundMatch

    }

     fun checkForMatch(position1: Int, position2: Int): Boolean {
        if(cards[position1].identifier != cards[position2].identifier){
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
         bonus = true
        return true
    }

    private fun restoreCards() {
        for(card in cards){
            if(!card.isMatched)
            card.isFaceUp = false
        }

    }

    fun haveWonGame(): Boolean {
        return  numPairsFound == boardSize.getNumPairs()

    }

    fun isCardFaceUp(position: Int): Boolean {
        return  cards[position].isFaceUp

    }

    fun getNumMove(): Int {

         return   numCardFlips/2
    }





}