package com.example.myapitest.repository

import com.example.myapitest.data.api.CarApi
import com.example.myapitest.data.model.Car

// RZ - O CarRepository é o "gerente de estoque" do seu aplicativo.
// Ele decide onde buscar as informações (nesse caso, da API) e serve como 
// uma camada intermediária para que a UI (Activity/ViewModel) não precise 
// saber detalhes de rede. Ele centraliza as operações de busca e salvamento.

class CarRepository(private val carApi: CarApi) {

    // RZ - Busca a lista de carros do servidor de forma assíncrona (suspend)
    suspend fun getCars(): List<Car>? {
        return try {
            val response = carApi.fetchCars()
            if (response.isSuccessful) {
                response.body()
            } else {
                // RZ - Log de erro ou tratamento específico pode ser feito aqui
                null
            }
        } catch (e: Exception) {
            // RZ - Tratamento de falhas de conexão ou timeouts
            null
        }
    }

    // RZ - Envia um novo carro para ser salvo no servidor (POST)
    suspend fun saveCar(car: Car): Boolean {
        return try {
            val response = carApi.saveCar(car)
            response.isSuccessful
        } catch (e: Exception) {
            // RZ - Tratamento de falhas ao tentar salvar
            false
        }
    }
}
