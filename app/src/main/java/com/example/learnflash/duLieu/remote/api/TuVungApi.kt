package com.example.learnflash.duLieu.remote.api

import com.example.learnflash.duLieu.remote.moHinhReMote.MoHinhTuVungRemote
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Giao diện (Interface) khai báo các phương thức gọi HTTP Request qua Internet
interface TuVungApi {

    // Gọi phương thức GET để tra cứu từ vựng tiếng Anh trên Free Dictionary API
    @GET("api/v2/entries/en/{tuTraCuu}")
    suspend fun traCuuTuVung(@Path("tuTraCuu") tuTraCuu: String): Response<List<MoHinhTuVungRemote>>

    companion object {
        // Địa chỉ URL cơ sở của máy chủ API tra cứu
        private const val URL_CO_SO = "https://api.dictionaryapi.dev/"

        // Khởi tạo đối tượng Retrofit tích hợp GsonConverter để tái sử dụng
        fun khoiTaoApi(): TuVungApi {
            return Retrofit.Builder()
                .baseUrl(URL_CO_SO)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TuVungApi::class.java)
        }
    }
}
