package com.chameleon.game

import kotlinx.serialization.Serializable

@Serializable
data class TopicCard(
    val title: String,
    val words: List<List<String>> // 4x4 grid of words
) {
    fun getWordAt(row: Int, col: Int): String? {
        return words.getOrNull(row)?.getOrNull(col)
    }
    
    companion object {
        fun createSampleCards(): List<TopicCard> = listOf(
            TopicCard(
                title = "Animals",
                words = listOf(
                    listOf("Dog", "Cat", "Lion", "Tiger"),
                    listOf("Elephant", "Giraffe", "Zebra", "Monkey"),
                    listOf("Bird", "Fish", "Snake", "Frog"),
                    listOf("Bear", "Wolf", "Fox", "Rabbit")
                )
            ),
            TopicCard(
                title = "Food",
                words = listOf(
                    listOf("Pizza", "Burger", "Pasta", "Salad"),
                    listOf("Apple", "Banana", "Orange", "Grape"),
                    listOf("Bread", "Cheese", "Milk", "Egg"),
                    listOf("Chicken", "Beef", "Fish", "Rice")
                )
            ),
            TopicCard(
                title = "Movies",
                words = listOf(
                    listOf("Action", "Comedy", "Drama", "Horror"),
                    listOf("Romance", "Thriller", "Fantasy", "Sci-Fi"),
                    listOf("Animation", "Documentary", "Musical", "Western"),
                    listOf("Adventure", "Mystery", "Crime", "War")
                )
            ),
            TopicCard(
                title = "Sports",
                words = listOf(
                    listOf("Football", "Basketball", "Tennis", "Golf"),
                    listOf("Swimming", "Running", "Cycling", "Boxing"),
                    listOf("Soccer", "Baseball", "Hockey", "Volleyball"),
                    listOf("Skiing", "Surfing", "Climbing", "Wrestling")
                )
            )
        )
    }
}
