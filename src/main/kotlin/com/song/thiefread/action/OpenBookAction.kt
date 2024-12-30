package com.song.thiefread.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowManager
import com.song.thiefread.util.SimpleEpubParser
import com.song.thiefread.ui.ReadingPanel
import java.io.File

class OpenBookAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.extension == "epub" }
            .withTitle("选择 EPUB 电子书")
        
        FileChooser.chooseFile(descriptor, project, null) { virtualFile ->
            val file = File(virtualFile.path)
            val parser = SimpleEpubParser(file)
            val book = parser.parseBook()
            
            // 打开阅读窗口
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("ThiefRead")
            toolWindow?.show {
                // 获取阅读窗口内容并更新
                val content = toolWindow.contentManager.getContent(0)
                (content?.component as? ReadingPanel)?.loadBook(book)
            }
            parser.close()
        }
    }
} 