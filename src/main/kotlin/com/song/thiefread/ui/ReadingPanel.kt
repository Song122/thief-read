package com.song.thiefread.ui

import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBList
import com.song.thiefread.model.Book
import com.song.thiefread.model.Chapter
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.KeyEvent
import javax.swing.*

class ReadingPanel : SimpleToolWindowPanel(true, true) {
    private var currentBook: Book? = null
    private var currentChapterIndex = 0
    private var isShowingToc = true  // 是否显示目录
    
    private val contentArea = JEditorPane().apply {
        isEditable = false
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        font = Font("JetBrains Mono", Font.PLAIN, 13)
    }
    
    private val chapterList = JBList<String>().apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val selectedIndex = selectedIndex
                if (selectedIndex >= 0) {
                    currentChapterIndex = selectedIndex
                    showContent()
                }
            }
        }
    }

    private val mainPanel = JPanel(CardLayout())
    private val tocPanel = JPanel(BorderLayout()).apply {
        add(JLabel("目录", SwingConstants.CENTER).apply {
            border = BorderFactory.createEmptyBorder(5, 0, 5, 0)
        }, BorderLayout.NORTH)
        add(JBScrollPane(chapterList), BorderLayout.CENTER)
    }
    private val contentPanel = JBScrollPane(contentArea).apply {
        verticalScrollBar.unitIncrement = 16
    }

    init {
        mainPanel.add(tocPanel, "TOC")
        mainPanel.add(contentPanel, "CONTENT")
        
        setContent(mainPanel)
        
        // 修改双击事件处理
        chapterList.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                if (e.clickCount == 2) {
                    val selectedIndex = chapterList.selectedIndex
                    if (selectedIndex >= 0) {
                        currentChapterIndex = selectedIndex
                        showContent()
                        (mainPanel.layout as CardLayout).show(mainPanel, "CONTENT")  // 显式切换到内容视图
                    }
                }
            }
        })

        // 添加鼠标移动监听，当鼠标移开时返回目录
        contentArea.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseExited(e: java.awt.event.MouseEvent) {
                if (contentArea.selectedText?.isNotEmpty() == true) {
                    return  // 如果有文本被选中，不要切换
                }
                showToc()
            }
        })

        // 添加键盘快捷键
        contentArea.inputMap.apply {
            put(KeyStroke.getKeyStroke("LEFT"), "previousChapter")
            put(KeyStroke.getKeyStroke("RIGHT"), "nextChapter")
            put(KeyStroke.getKeyStroke("ESCAPE"), "showToc")
        }
        
        contentArea.actionMap.apply {
            put("previousChapter", object : AbstractAction() {
                override fun actionPerformed(e: java.awt.event.ActionEvent) {
                    showPreviousChapter()
                }
            })
            put("nextChapter", object : AbstractAction() {
                override fun actionPerformed(e: java.awt.event.ActionEvent) {
                    showNextChapter()
                }
            })
            put("showToc", object : AbstractAction() {
                override fun actionPerformed(e: java.awt.event.ActionEvent) {
                    showToc()
                }
            })
        }

        // 添加紧急切换快捷键
        contentArea.inputMap.apply {
            // 添加 Ctrl+Q (或 Cmd+Q) 快速切换到假代码视图
            put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().menuShortcutKeyMask), "toggleFakeCode")
        }
        
        contentArea.actionMap.apply {
            put("toggleFakeCode", object : AbstractAction() {
                override fun actionPerformed(e: java.awt.event.ActionEvent) {
                    showFakeCode()
                }
            })
        }
    }

    private fun showToc() {
        isShowingToc = true
        (mainPanel.layout as CardLayout).show(mainPanel, "TOC")
    }

    private fun formatAsComment(text: String): String {
        return text.lines().joinToString("\n") { line ->
            if (line.isBlank()) "//\n" else "// $line"
        }
    }

    private fun showContent() {
        currentBook?.chapters?.getOrNull(currentChapterIndex)?.let { chapter ->
            contentArea.contentType = "text/html"
            contentArea.text = """
                <html>
                <body style='font-family: JetBrains Mono; font-size: 13pt; background-color: #2b2b2b;'>
                <div style='color: #808080;'>/**</div>
                <div style='color: #808080;'> * @author Generated</div>
                <div style='color: #808080;'> * @version 1.0</div>
                <div style='color: #808080;'> */</div>
                <div style='color: #cc7832;'>public class</div> <span style='color: #a9b7c6;'>Chapter${currentChapterIndex + 1}</span> {
                    
                <div style='color: #808080;'>    /**</div>
                <div style='color: #808080;'>     * ${chapter.title}</div>
                <div style='color: #808080;'>     */</div>
                <div style='color: #cc7832;'>    private static final</div> <span style='color: #a9b7c6;'>String CONTENT = </span>
                
                ${chapter.content.lines().mapIndexed { index, line ->
                    """    <div style='color: #6a8759;'>    ${if (index == 0) "\"" else "    + \""}${
                        line.replace("\"", "\\\"")
                    }${if (index == chapter.content.lines().lastIndex) "\";" else "\""}</div>"""
                }.joinToString("\n")}
                
                }
                </body>
                </html>
            """.trimIndent()
            contentArea.caretPosition = 0  // 确保滚动到顶部
        }
    }

    private fun showPreviousChapter() {
        currentBook?.let { book ->
            if (currentChapterIndex > 0) {
                currentChapterIndex--
                chapterList.selectedIndex = currentChapterIndex
                showContent()
            }
        }
    }

    private fun showNextChapter() {
        currentBook?.let { book ->
            if (currentChapterIndex < book.chapters.size - 1) {
                currentChapterIndex++
                chapterList.selectedIndex = currentChapterIndex
                showContent()
            }
        }
    }

    private fun showFakeCode() {
        contentArea.text = """
            public class CodeAnalysis {
                // Regular code inspection
                @Override
                public void analyze() {
                    // Running standard checks
                    performStaticAnalysis();
                    checkCodeStyle();
                }
            }
        """.trimIndent()
    }

    fun loadBook(book: Book) {
        currentBook = book
        currentChapterIndex = 0
        
        // 更新章节列表
        val chapterTitles = book.chapters.map { it.title }
        chapterList.setListData(chapterTitles.toTypedArray())
        
        // 显示目录
        showToc()
    }
} 