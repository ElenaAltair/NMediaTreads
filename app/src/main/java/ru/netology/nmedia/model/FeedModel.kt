package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

// для хранения текущего состояния экрана создаём специальный класс:
data class FeedModel(
    // в этом классе опишем 5 свойств
    val posts: List<Post> = emptyList(), // список постов (если списка нет, то он пустой)
    val loading: Boolean = false, // состояние: загрузка / не загрузка
    val error: Boolean = false, // состояние: ошибка / не ошибка
    val empty: Boolean = false, // состояние: пустой список постов / не пустой список постов
    val refreshing: Boolean = false,
    val liked: Boolean = false, // состояние не лайкнуто/ лайкнуто
)
