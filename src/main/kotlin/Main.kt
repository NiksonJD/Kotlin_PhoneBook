package phonebook

import java.io.File
import java.util.Collections.swap
import kotlin.math.*

val tmpdir = System.getProperty("java.io.tmpdir")
val lines = File("$tmpdir/find.txt").readLines().toSet()
val dataBase = File("$tmpdir/directory.txt").readLines().toSet()
val changedDB = dataBase.map { "${it.substringAfter(" ")} ${it.substringBefore(" ")}" }

fun time() = System.currentTimeMillis()

fun elapsedTime(start: Long): String {
    val delta = time() - start
    return "${delta / 60000} min. ${(delta / 1000) % 60} sec. ${delta % 1000} ms."
}

fun linearSearch(start2: Long, hasError: Boolean): Long {
    val start = time()
    val count = lines.count { line -> dataBase.any { line in it } }
    val end2 = time()
    val end = if (hasError) elapsedTime(start2 - (end2 - start)) else elapsedTime(start2)
    printResult(count, hasError, start, end, start2, 1)
    return (end2 - start2) * 10
}

fun bubbleSortJumpSearch(sleepTime: Long) {
    fun jumpSearch(list: List<String>, target: String): Int {
        val blockSize = sqrt(list.size.toDouble()).toInt()
        var (left, right) = listOf(0, blockSize)
        while (right < list.size && list[right] <= target) left = right.also { right += blockSize }
        run { for (i in left..min(right, list.size - 1)) if (target in list[i]) return 1 }
        return 0
    }

    val startBubbleSortJumpSearch = time().also { println("\nStart searching (bubble sort + jump search)...") }
    lateinit var sDB: MutableList<String>
    val thread = Thread {
        sDB = changedDB.toMutableList().apply {
            for (i in 0 until size - 1) for (j in i + 1 until size)
                if (get(i) > get(j)) swap(this, i, j)
        }
    }
    thread.start().also { Thread.sleep(sleepTime) }
    if (thread.isAlive) {
        thread.interrupt().also { linearSearch(startBubbleSortJumpSearch, true) }
    } else {
        val end = elapsedTime(startBubbleSortJumpSearch)
        val start = time()
        val count = lines.count { line -> jumpSearch(sDB, line) != 0 }
        printResult(count, false, start, end, startBubbleSortJumpSearch)
    }
}

fun quickSortBinarySearch() {
    fun binarySearch(set: MutableList<String>, target: String): Int {
        var (low, high) = 0 to set.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            when {
                set[mid].contains(target) -> return mid
                set[mid] < target -> low = mid + 1
                else -> high = mid - 1
            }
        }
        return -1
    }

    fun quickSort(list: List<String>): List<String> {
        if (list.size < 2) return list
        val pivot = list.first()
        val (smaller, larger) = list.drop(1).partition { it < pivot }
        return quickSort(smaller) + pivot + quickSort(larger)
    }

    val startQuickSortBinarySearch = time().also { println("\nStart searching (quick sort + binary search)...") }
    val sDB = quickSort(changedDB).toMutableList()
    val end = elapsedTime(startQuickSortBinarySearch)
    val start = time()
    val count = lines.count { line -> binarySearch(sDB, line) != -1 }
    printResult(count, false, start, end, startQuickSortBinarySearch)
}

fun hashTableSearch() {
    val startHashTableSearch = time().also { println("\nStart searching (hash table)...") }
    val hTab = dataBase.associate { it.substringAfter(" ").hashCode() to it.substringBefore(" ") }
    val end = elapsedTime(startHashTableSearch)
    val start = time()
    val count = lines.count { line -> hTab[line.hashCode()] != null }
    printResult(count, false, start, end, startHashTableSearch, 2)
}

fun printResult(foundCount: Int, hasError: Boolean, start: Long, end: String, start2: Long, check: Int = 0) {
    if (hasError) {
        println("Found $foundCount / ${lines.count()} entries. Time taken: $end")
        println("Sorting time: ${elapsedTime(start2)} - STOPPED, moved to linear search")
        println("Searching time: ${elapsedTime(start)}")
    } else {
        println("Found $foundCount / ${lines.count()} entries. Time taken: ${elapsedTime(start2)}")
        when (check) {
            0 -> println("Sorting time: $end\nSearching time: ${elapsedTime(start)}")
            1 -> ""
            2 -> println("Creating time: $end\nSearching time: ${elapsedTime(start)}")
        }
    }
}

fun main() {
    bubbleSortJumpSearch(println("Start searching (linear search)...").run { linearSearch(time(), false) })
    quickSortBinarySearch()
    hashTableSearch()
}