package com.song.thiefread.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.song.thiefread.ui.ReadingPanel

class ReadingWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val readingPanel = ReadingPanel()
        val content = ContentFactory.getInstance().createContent(readingPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
} 