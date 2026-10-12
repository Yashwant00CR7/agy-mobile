package com.yashwant.agymobile.util

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class MarkdownLinkParserTest {

    @Test
    fun testResolveTargetPdf() {
        val target = FileOpener.resolveTarget("file:///storage/emulated/0/Download/Offer.pdf")
        assertTrue(target is LinkTarget.Document)
        assertEquals("Offer.pdf", (target as LinkTarget.Document).label)
        assertEquals("application/pdf", target.mimeType)
    }

    @Test
    fun testResolveTargetUrlEncoded() {
        val target = FileOpener.resolveTarget("file:///storage/emulated/0/Download/My%20Offer%20Letter.pdf")
        assertTrue(target is LinkTarget.Document)
        val doc = target as LinkTarget.Document
        assertEquals("My Offer Letter.pdf", doc.file.name)
    }

    @Test
    fun testResolveTargetDirectory() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "test_dir_link")
        tempDir.mkdirs()
        val target = FileOpener.resolveTarget(tempDir.absolutePath)
        assertTrue(target is LinkTarget.Directory)
        tempDir.delete()
    }

    @Test
    fun testResolveTargetWeb() {
        val target = FileOpener.resolveTarget("https://google.com")
        assertTrue(target is LinkTarget.Web)
        assertEquals("https://google.com", (target as LinkTarget.Web).url)
    }

    @Test
    fun testResolveTargetImage() {
        val target = FileOpener.resolveTarget("file:///storage/emulated/0/Download/photo.png")
        assertTrue(target is LinkTarget.Image)
        assertEquals("photo.png", (target as LinkTarget.Image).label)
    }

    @Test
    fun testResolveTargetUnknown() {
        val target = FileOpener.resolveTarget("customscheme://foo/bar")
        assertTrue(target is LinkTarget.Unknown)
        assertEquals("customscheme://foo/bar", (target as LinkTarget.Unknown).raw)
    }

    @Test
    fun testResolveTargetCommonDocumentWithoutExtension() {
        val target = FileOpener.resolveTarget("/workspace/myproject/LICENSE")
        assertTrue(target is LinkTarget.Document)
        assertEquals("LICENSE", (target as LinkTarget.Document).label)
        assertEquals("text/plain", target.mimeType)
    }

    @Test
    fun testParseAnnotatedMarkdownWithMarkdownLink() {
        val raw = "Here is the [Offer Letter](file:///storage/emulated/0/Download/Offer.pdf) you requested."
        val annotated = MarkdownLinkParser.parseAnnotatedMarkdown(raw)
        assertTrue(annotated.text.contains("📄 Offer Letter"))
        val annotations = annotated.getStringAnnotations("URL", 0, annotated.length)
        assertEquals(1, annotations.size)
        assertEquals("file:///storage/emulated/0/Download/Offer.pdf", annotations[0].item)
    }

    @Test
    fun testParseAnnotatedMarkdownWithUrlEncodedPdf() {
        val raw = "1. [Gmail - Congratulations! Vendasta Offer.pdf](file:///storage/emulated/0/Download/Gmail%20-%20Congratulations!%20Vendasta%20Offer%20for%C2%A0Developer%20l.pdf)"
        val annotated = MarkdownLinkParser.parseAnnotatedMarkdown(raw)
        assertTrue(annotated.text.contains("📄 Gmail - Congratulations! Vendasta Offer.pdf"))
        val annotations = annotated.getStringAnnotations("URL", 0, annotated.length)
        assertEquals(1, annotations.size)
        assertTrue(annotations[0].item.contains("Gmail%20-%20Congratulations!"))
    }

    @Test
    fun testParseAnnotatedMarkdownWithBareUrlAndTrailingPeriod() {
        val raw = "Check out https://antigravity.google.com."
        val annotated = MarkdownLinkParser.parseAnnotatedMarkdown(raw)
        assertTrue(annotated.text.contains("🔗 https://antigravity.google.com"))
        assertTrue(annotated.text.endsWith("."))
        val annotations = annotated.getStringAnnotations("URL", 0, annotated.length)
        assertEquals(1, annotations.size)
        assertEquals("https://antigravity.google.com", annotations[0].item)
    }

    @Test
    fun testParseAnnotatedMarkdownWithBareAndroidPath() {
        val raw = "Saved to /storage/emulated/0/Download/report.pdf."
        val annotated = MarkdownLinkParser.parseAnnotatedMarkdown(raw)
        assertTrue(annotated.text.contains("📄 report.pdf"))
        val annotations = annotated.getStringAnnotations("URL", 0, annotated.length)
        assertEquals(1, annotations.size)
        assertEquals("/storage/emulated/0/Download/report.pdf", annotations[0].item)
    }

    @Test
    fun testParseAnnotatedMarkdownWithBoldAndCode() {
        val raw = "Found **3 items** in `/storage/emulated/0/Download`."
        val annotated = MarkdownLinkParser.parseAnnotatedMarkdown(raw)
        assertEquals("Found 3 items in 📁 Download.", annotated.text)
        val annotations = annotated.getStringAnnotations("URL", 0, annotated.length)
        assertEquals(1, annotations.size)
        assertEquals("/storage/emulated/0/Download", annotations[0].item)
    }

    @Test
    fun testParseAnnotatedMarkdownWithNormalCode() {
        val raw = "Run `git status` to see branch info."
        val annotated = MarkdownLinkParser.parseAnnotatedMarkdown(raw)
        assertEquals("Run git status to see branch info.", annotated.text)
    }

    @Test
    fun testParseBlocksHeadings() {
        val raw = """
            # Project Heading
            Intro paragraph.
            ## Sub Section
            More details.
            ### 1. Temporary and Test Files (Highly safe to delete)
            File details.
        """.trimIndent()

        val blocks = MarkdownLinkParser.parseBlocks(raw)
        assertEquals(6, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Heading)
        assertEquals(1, (blocks[0] as MarkdownBlock.Heading).level)
        assertEquals("Project Heading", (blocks[0] as MarkdownBlock.Heading).text)

        assertTrue(blocks[1] is MarkdownBlock.Paragraph)
        assertEquals("Intro paragraph.", (blocks[1] as MarkdownBlock.Paragraph).text)

        assertTrue(blocks[2] is MarkdownBlock.Heading)
        assertEquals(2, (blocks[2] as MarkdownBlock.Heading).level)
        assertEquals("Sub Section", (blocks[2] as MarkdownBlock.Heading).text)

        assertTrue(blocks[4] is MarkdownBlock.Heading)
        assertEquals(3, (blocks[4] as MarkdownBlock.Heading).level)
        assertEquals("1. Temporary and Test Files (Highly safe to delete)", (blocks[4] as MarkdownBlock.Heading).text)
    }

    @Test
    fun testParseBlocksBulletAndNumberedLists() {
        val raw = """
            Items to review:
            * temp.pdf
            * temp (1).pdf
            1. First step
            2. Second step
        """.trimIndent()

        val blocks = MarkdownLinkParser.parseBlocks(raw)
        assertEquals(5, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Paragraph)
        assertTrue(blocks[1] is MarkdownBlock.BulletItem)
        assertEquals("temp.pdf", (blocks[1] as MarkdownBlock.BulletItem).text)
        assertTrue(blocks[2] is MarkdownBlock.BulletItem)
        assertEquals("temp (1).pdf", (blocks[2] as MarkdownBlock.BulletItem).text)

        assertTrue(blocks[3] is MarkdownBlock.NumberedItem)
        assertEquals("1.", (blocks[3] as MarkdownBlock.NumberedItem).number)
        assertEquals("First step", (blocks[3] as MarkdownBlock.NumberedItem).text)

        assertTrue(blocks[4] is MarkdownBlock.NumberedItem)
        assertEquals("2.", (blocks[4] as MarkdownBlock.NumberedItem).number)
        assertEquals("Second step", (blocks[4] as MarkdownBlock.NumberedItem).text)
    }

    @Test
    fun testParseBlocksWithCodeBlock() {
        val raw = """
            ### Code snippet
            ```kotlin
            val x = 42
            ```
            End note.
        """.trimIndent()

        val blocks = MarkdownLinkParser.parseBlocks(raw)
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Heading)
        assertEquals("Code snippet", (blocks[0] as MarkdownBlock.Heading).text)

        assertTrue(blocks[1] is MarkdownBlock.Code)
        assertEquals("kotlin", (blocks[1] as MarkdownBlock.Code).language)
        assertEquals("val x = 42", (blocks[1] as MarkdownBlock.Code).code)

        assertTrue(blocks[2] is MarkdownBlock.Paragraph)
        assertEquals("End note.", (blocks[2] as MarkdownBlock.Paragraph).text)
    }
}
