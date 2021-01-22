package com.example.android.mymemory.models

import com.example.android.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private  val boardSize: BoardSize){
    val cards: List<MemoryCard>
    val numPairsFound = 0
        init {
            val choosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
            val randomizedImages = (choosenImages + choosenImages).shuffled()
             cards = randomizedImages.map{MemoryCard(it)}
        }

}