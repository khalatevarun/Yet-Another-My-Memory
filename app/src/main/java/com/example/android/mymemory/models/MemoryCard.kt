package com.example.android.mymemory.models

import org.intellij.lang.annotations.Identifier

data class MemoryCard(
    val identifier: Int,
    val isFaceUp: Boolean = false,
    var isMatched:Boolean = false
)