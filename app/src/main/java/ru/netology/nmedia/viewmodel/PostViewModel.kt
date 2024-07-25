package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.math.absoluteValue

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()


    // Создаём LiveData для хранения FeedModel(т.е. текущего списка постов с набором флагов, хранящих текущее состояния экрана)
    private val _data = MutableLiveData(FeedModel()) // тип MutableLiveData имеет методы и на чтение, и на запись
    // переменную _data мы делаем приватной, чтобы записывать в неё мы могли только из этого класса
    // переменную data мы делаем публичной, чтобы читатать из неё можно было из любого класса
    val data: LiveData<FeedModel> // тип LiveData только для чтения
        get() = _data

    val edited = MutableLiveData(empty)

    // нами создан класс SingleLiveEvent для события, которое должно обработаться только один раз
    // в нашем случае, это событие создания поста
    private val _postCreated = SingleLiveEvent<Unit>() // в эту переменную можно писать и читать из неё, она приватная
    val postCreated: LiveData<Unit> // из этой переменной можно только читать, она публичная
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        thread { // создаём фоновый поток
            // Начинаем загрузку
            // Оповещаем интерфейс о том, что началась загрузка постов:
            // 1) мы формируем FeedModel c флагом loading = true (символизирующим, что пошла загрузка)
            // 2) и его отправляем в LiveData методом postValue
            _data.postValue(FeedModel(loading = true))
            try {
                // Данные успешно получены
                val posts = repository.getAll()
                // формируем FeedModel c флагами posts = posts, empty = posts.isEmpty()
                // FeedModel здесь будет результатом опетатора try/catch
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                // Получена ошибка
                // формируем FeedModel c флагом error = true
                // FeedModel здесь будет результатом опетатора try/catch
                FeedModel(error = true)
            }.also(_data::postValue) // отправляем FeedModel в LiveData методом postValue

            /* Можно записать так:
            val feedModel = try {
                // Данные успешно получены
                val posts = repository.getAll()
                // формируем FeedModel c флагами posts = posts, empty = posts.isEmpty()
                // FeedModel здесь будет результатом опетатора try/catch
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                // Получена ошибка
                // формируем FeedModel c флагом error = true
                // FeedModel здесь будет результатом опетатора try/catch
                FeedModel(error = true)
            }
            _data.postValue(feedModel) // отправляем FeedModel в LiveData методом postValue
            */
        }
    }

    fun save() {
        edited.value?.let {
            thread {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {

            thread {
                val old = _data.value?.posts.orEmpty() // в переменной old сохраним список постов
                val new = old.onEach { post ->
                            if (post.id == id) {
                                if(!post.likedByMe) {
                                    post.likes += 1
                                    post.likedByMe = true
                                } else {
                                    post.likedByMe = false
                                    post.likes -= 1

                                }
                            }
                        }

                val post: Post = _data.value?.posts.orEmpty()
                    .filter { it.id == id }[0]
                _data.postValue(_data.value?.copy(posts = new))


                try {
                    if(post.likedByMe){

                        FeedModel(liked = true)
                        repository.likeById(id)

                    }else{
                        FeedModel(liked = false)
                        repository.unlikeById(id)

                    }
                    //loadPosts()
                } catch (e: IOException) {
                    _data.postValue(_data.value?.copy(posts = old)) // если произошла ошибка, показать старый список постов
                }
            }
    }

    fun removeById(id: Long) {
        thread {
            // Оптимистичная модель
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id } // показать все посты кроме данного
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old)) // если произошла ошибка, показать старый список постов
            }
        }
    }
}
