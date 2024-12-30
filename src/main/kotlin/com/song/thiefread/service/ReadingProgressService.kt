package com.song.thiefread.service

import com.intellij.openapi.components.*
import com.song.thiefread.model.ReadingProgress

@State(
    name = "ThiefReadProgress",
    storages = [Storage("thief-read-progress.xml")]
)
class ReadingProgressService : PersistentStateComponent<ReadingProgressService.State> {
    data class State(
        var bookPath: String = "",
        var position: Int = 0,
        var chapterIndex: Int = 0,
        var scrollPosition: Int = 0,
        var lastRead: Long = 0
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun saveProgress(progress: ReadingProgress) {
        myState.bookPath = progress.bookPath
        myState.position = progress.position
        myState.chapterIndex = progress.chapterIndex
        myState.scrollPosition = progress.scrollPosition
        myState.lastRead = System.currentTimeMillis()
    }

    fun getProgress(bookPath: String): ReadingProgress? {
        return if (myState.bookPath == bookPath) {
            ReadingProgress(
                bookPath = myState.bookPath,
                position = myState.position,
                chapterIndex = myState.chapterIndex,
                scrollPosition = myState.scrollPosition
            )
        } else null
    }
} 