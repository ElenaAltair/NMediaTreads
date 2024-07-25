package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit


class PostRepositoryImpl: PostRepository {
    // создаём OkHttp клиент
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // таймаут на подключение 30 секунд
        .build()
    private val gson = Gson() // создаём gson
    private val typeToken = object : TypeToken<List<Post>>() {}//описываем typeToken для списка постов

    //заводим константы
    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999" // адрес нашего сервера
        // 10.0.2.2 адрес локального компьютера, на котором запущен эмулятор

        // сервер требует чтобы данные на него и от него передавались в формате json
        // завели константу json и конвертируем её в специалный медиа тип
        private val jsonType = "application/json".toMediaType()
    }

    //запрос на список постов
    override fun getAll(): List<Post> {
        //формируем запрос к серверу
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts") // указывем адрес на сервере, по которому мы обращаемся
            .build()

        // данный запрос мы оборачиваем в вызов (т.е. делаем newCall(request))
        return client.newCall(request)
            .execute() // отправляем запрос на выполнения, вызывая метод execute(), после его выполнения сервер пришлёт список наших постов в формате json
            .let { it.body?.string() ?: throw RuntimeException("body is null") } // этот список постов через .let преобразовываем к строке
            .let {
                gson.fromJson(it, typeToken.type) // затем из строки с помощью метода .fromJson преобразовываем к списку постов
            }
    }

    override fun likeById(id: Long) {
        // TODO: do this in homework
        // формируем запрос к серверу
        val request: Request = Request.Builder()
            .post(gson.toJson(id).toRequestBody(jsonType)) // указываем тип запроса (в этом случае запрос типа POST)
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun unlikeById(id: Long) {
        // формируем запрос к серверу
        val request: Request = Request.Builder()
            .delete(gson.toJson(id).toRequestBody(jsonType)) // указываем тип запроса (в этом случае запрос типа POST)
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun save(post: Post) {
        // формируем запрос к серверу
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType)) // указываем тип запроса (в этом случае запрос типа POST)
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete() // указываем тип запроса (в этом случае запрос типа DELETE)
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }
}
