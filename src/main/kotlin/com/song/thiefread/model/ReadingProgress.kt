package com.song.thiefread.model

/**
 * 阅读进度
 */
data class ReadingProgress(
    val bookPath: String,
    val position: Int,
    val chapterIndex: Int = 0,
    val scrollPosition: Int = 0
) 