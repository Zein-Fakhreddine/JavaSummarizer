package com.zeinfakhreddine.summarizer

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.ArrayList

class Summarizer(url: String, var NUMBER_OF_PARAGRAPHS: Int = 3, var title: String? = null) {
    /**
     *Uses Levenshtein distance to calculate the difference between a given String (this) and a param
     */
    fun String.distanceTo(b: String): Int {
        // i == 0
        val costs = IntArray(b.length + 1)
        for (j in costs.indices)
            costs[j] = j
        for (i in 1..this.length) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i
            var nw = i - 1
            for (j in 1..b.length) {
                val cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), if (this[i - 1] == b[j - 1]) nw else nw + 1)
                nw = costs[j]
                costs[j] = cj
            }
        }
        return costs[b.length]
    }

    private val paragraphs = ArrayList<Paragraph>()
    var summary: String? = null

    /**
     * Loads Document with JSOUP and adds a title if one is not given
     */
    init {
        val doc: Document = Jsoup.connect(url).userAgent("Mozilla").get()
        if (title == null)
            title = doc.title()

        unfluff(doc)
    }

    /**
     * Unfluffs the document to just get paragraphs that have to do with the article
     * Adds the gotten paragraphs to a global array list of paragraphs
     */
    fun unfluff(doc: Document){
        val divs: Elements = doc.select("div")
        var mainDiv: Element? = null
        var amount = 0

        divs.forEach {
            //Only get the first level of children for each div element
            val detachedDivChildren = Elements()
            it.children().mapTo(detachedDivChildren) {
                Element(Tag.valueOf(it.tagName()),
                        it.baseUri(), it.attributes().clone())
            }

            val amountOfP = detachedDivChildren.select("p").size

            if (amountOfP > amount) {
                amount = amountOfP
                mainDiv = it
            }
        }

        if(mainDiv == null){
            summary = "Error not able to find paragraphs"
            return
        }

        mainDiv?.select("p")?.forEach { element ->
            //Makes sure the paragraph has to due with the article and not something else. e.x: capture for picture
            var badP = false
            element.children().forEach {
                if (it.select("span").size > 0)
                    badP = true
            }

            if (!badP) {
                val sentences = element.text().split("(?<![DSJ]r|Mrs?)[.?!](?!\\S)").joinToString("").split("\n")

                paragraphs.add(Paragraph(sentences = ArrayList(sentences)))
            }
        }

        if(paragraphs.size >= NUMBER_OF_PARAGRAPHS + 1)
            addWeights()
        else{
            summary = "Error not able to find paragraphs"
            return
        }
    }

    /**
     * Adds weight to each word in each paragraph
     * the weight for each word is based on  the sum of its distance* to each word in the title
     */
    fun addWeights() {
        val allWords = ArrayList<Paragraph.Word>()

        paragraphs.forEach { allWords.addAll(it.words) }
        allWords.forEach {
            this.paragraphs
            val firstWord = it
            title?.split(" ")?.forEach {
                firstWord.weight += firstWord.word.distanceTo(it)
            }
        }

        loadSummary()
    }

    /**
     * Loads the summary of the article
     * it adds the first article of the paragraph (for introduction)
     * then adds the top 5 weighted articles
     */
    fun loadSummary() {
        if(summary != null)
            return
        summary = ""
        summary += paragraphs[0].toString()
        paragraphs.removeAt(0)
        Collections.sort(paragraphs, Paragraph.WeightComparator())
        val itr = paragraphs.iterator()
        var i = 0
        while (itr.hasNext() && i != NUMBER_OF_PARAGRAPHS) {
            val paragraphText = itr.next().toString()
            summary += paragraphText
            i++
        }
    }
}