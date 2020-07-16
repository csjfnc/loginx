package com.ware.loginx_unico

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class LoginUnico{

  private val loginRepository by lazy {
      LoginRepository()
  }

    fun getOauth2(code: String){
        loginRepository.oAuthRepository(code, sucesso = {
            Log.e("Token", it.toString())
        }, falha = {
            Log.e("Erro Presenter", it.toString())
        })
    }

}

class LoginRepository(private val jwtWebCliet: JwtWebCliet = JwtWebCliet()){

    fun oAuthRepository(code: String, sucesso: (String?) -> Unit, falha: (error: String?) -> Unit){
        jwtWebCliet.oauth2(code = code, sucesso = {jwt -> jwt?.let{
            Log.i("TOKEN", jwt)
        }}, falha = falha)
    }
}


class JwtWebCliet(private val loginService: LoginService = AppRetrofit().loginService){
    private fun<T> executeRequisicao(
        call: Call<T>, sucesso: (jwt: T?) -> Unit,
        falha: (error: String?) -> Unit){

        call.enqueue(object : Callback<T>{
            override fun onResponse(call: Call<T>, response: Response<T>){
                if (response.isSuccessful){
                    return sucesso(response.body())
                }else{
                    falha("ERRO NAO REQUISIÇÃO")
                }
            }

            override fun onFailure(call: Call<T>?, t: Throwable?) {
                if (t != null) {
                    falha(t.message)
                }
            }
        })
    }

    fun oauth2(code: String, sucesso: (String?) -> Unit, falha: (String?) -> Unit){
        executeRequisicao(loginService.oauth2(code), sucesso, falha)
    }

}

class AppRetrofit {
    private val client by lazy {
        val interceptador = HttpLoggingInterceptor()
        interceptador.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(interceptador)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder().baseUrl("http://172.16.1.233:3009/").addConverterFactory(GsonConverterFactory.create())
            .client(client).build()
    }

    val loginService: LoginService by lazy {
        retrofit.create(LoginService::class.java)
    }
}

interface LoginService{

    @GET("user/oauth2")
    fun oauth2(@Query("code") code: String): Call<String>
}
