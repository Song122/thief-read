package com.song.thiefread.util

import com.song.thiefread.model.Book
import com.song.thiefread.model.Chapter
import org.jsoup.Jsoup
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipEntry

class SimpleEpubParser(private val epubFile: File) {
    private val zipFile = ZipFile(epubFile)
    
    fun parseBook(): Book {
        val title = extractTitle()
        val chapters = extractChapters()
        return Book(
            path = epubFile.absolutePath,
            title = title,
            chapters = chapters
        )
    }

    private fun extractTitle(): String {
        val entries = zipFile.entries().toList()
        val opfEntry = entries.find { entry -> 
            entry.name.endsWith(".opf") || entry.name.contains("content.opf")
        }
        return opfEntry?.let { entry ->
            zipFile.getInputStream(entry).use { input ->
                val content = input.bufferedReader().use { it.readText() }
                val doc = Jsoup.parse(content)
                doc.select("dc|title").firstOrNull()?.text()
                    ?: doc.select("title").firstOrNull()?.text()
                    ?: epubFile.nameWithoutExtension
            }
        } ?: epubFile.nameWithoutExtension
    }

    private fun extractChapters(): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val entries = zipFile.entries().toList()
            .filter { it.name.endsWith(".html") || it.name.endsWith(".xhtml") }
            .sortedBy { it.name }

        entries.forEachIndexed { index, entry ->
            zipFile.getInputStream(entry).use { input ->
                val content = input.bufferedReader().use { it.readText() }
                val doc = Jsoup.parse(content)
                
                // 提取章节标题
                val title = when {
                    // 如果是目录页，使用目录结构
                    doc.select("nav[epub|type=toc]").isNotEmpty() -> {
                        val tocItems = doc.select("nav[epub|type=toc] a").map { it.text() }
                        if (tocItems.isNotEmpty()) "目录" else "未知章节"
                    }
                    // 否则尝试从内容中提取标题
                    else -> {
                        doc.select("h1").firstOrNull()?.text()
                            ?: doc.select("h2").firstOrNull()?.text()
                            ?: doc.select("title").firstOrNull()?.text()
                            ?: doc.select("header").firstOrNull()?.text()
                            ?: if (index == 0) "封面" else "第${index}章"
                    }
                }
                
                // 提取章节内容
                val contentText = when {
                    // 如果是目录页，保留HTML结构以保持目录格式
                    doc.select("nav[epub|type=toc]").isNotEmpty() -> {
                        doc.select("nav[epub|type=toc]").outerHtml()
                    }
                    // 否则只提取文本内容
                    else -> {
                        doc.body().text()
                    }
                }
                
                chapters.add(Chapter(
                    title = title,
                    content = contentText,
                    index = index
                ))
            }
        }
        return chapters
    }
    
    fun close() {
        zipFile.close()
    }
} 