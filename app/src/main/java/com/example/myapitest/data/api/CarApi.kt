package com.example.myapitest.data.api

import com.example.myapitest.data.model.Car
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// RZ - O Retrofit é como um "tradutor" ou um "garçom" para o seu aplicativo. 
// Imagine que a API (o servidor na internet) fala uma língua e o Android fala outra. 
// O Retrofit pega as funções que a gente define aqui e as transforma em chamadas de rede reais, 
// facilitando muito a busca e o envio de dados sem que a gente precise escrever todo o código 
// chato de conexão manual. A função dele é simplificar a comunicação com serviços web.

interface CarApi {

    @GET("car")
    suspend fun fetchCars(): Response<List<Car>>

    @POST("car")
    suspend fun saveCar(@Body car: Car): Response<Car>
}
