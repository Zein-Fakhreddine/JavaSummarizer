package com.zeinfakhreddine.summarizer

internal class Paragraph(val sentences: ArrayList<String>, val index: Int) {

    val words: ArrayList<Word> = ArrayList()
    private val _totalWeight: Int? = null
    val totalWeight: Int
        get() {
            if (_totalWeight == null)
                return calculateTotalWeight()

            return _totalWeight
        }

    /**
     * Splits the sentences into words and adds it to the array list
     */
    init {
        sentences.forEach {
            it.split(" ").mapTo(words) {
                Word(it, 0)
            }
        }
    }

    /**
     *Calculates total weight of each word in paragraph
     */
    private fun calculateTotalWeight(): Int {
        var total = 0
        words.forEach { total += it.weight }
        return total
    }

    /**
     * Combines all the words
     */
    override fun toString(): String {
        val wordArray = words.map { it.word }

        return "<p> ${wordArray.joinToString(" ")} </p>"
    }

    data class Word(val word: String, var weight: Int)

    class WeightComparator : Comparator<Paragraph> {
        override fun compare(s1: Paragraph, s2: Paragraph): Int {
            if (s1.totalWeight == s2.totalWeight)
                return 0
            else if (s1.totalWeight > s2.totalWeight)
                return -1
            else
                return 1
        }
    }

    class IndexComparator : Comparator<Paragraph>{
        override fun compare(s1: Paragraph, s2: Paragraph): Int {
            if (s1.index == s2.index)
                return 0
            else if (s1.index > s2.index)
                return 1
            else
                return -1
        }
    }
}