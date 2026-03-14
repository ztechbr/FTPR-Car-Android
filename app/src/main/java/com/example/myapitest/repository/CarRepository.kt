package com.example.myapitest.repository

import com.example.myapitest.data.api.CarApi
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.ItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ApiResult(val success: Boolean, val message: String)

class CarRepository(private val carApi: CarApi) {

    // RZ - Busca a lista e garante que cada item seja o carro "real" (sem aninhamento)
    suspend fun getCars(): List<Car> = withContext(Dispatchers.IO) {
        try {
            val response = carApi.fetchCars()
            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                body.map { item ->
                    // Busca recursiva para encontrar o objeto que contém os dados (name, licence, etc)
                    val realCar = findDeepData(item.value, 0)
                    // Preserva o ID do envelope raiz
                    realCar.copy(id = item.id ?: realCar.id ?: realCar.licence)
                }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // RZ - Função recursiva robusta para "desembrulhar" o JSON da sua API
    private fun findDeepData(car: Car, depth: Int): Car {
        if (depth > 5) return car
        
        // Um objeto é considerado "real" se tiver nome ou placa preenchidos
        val hasRealData = !car.name.isNullOrBlank() || !car.licence.isNullOrBlank()
        
        return if (hasRealData) {
            car
        } else if (car.nestedValue != null) {
            findDeepData(car.nestedValue, depth + 1)
        } else {
            car
        }
    }

    // RZ - Envia o carro "limpo" para a API para evitar aninhamento infinito
    suspend fun saveCarDetailed(car: Car): ApiResult = withContext(Dispatchers.IO) {
        try {
            val idToSend = car.licence ?: ""
            // RZ - remove o nestedValue antes de enviar para não duplicar o JSON no servidor
            val cleanCar = car.copy(nestedValue = null)
            val response = carApi.saveCar(ItemResponse(id = idToSend, value = cleanCar))
            if (response.isSuccessful) {
                ApiResult(true, "Sucesso!")
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                ApiResult(false, "API ${response.code()}: $errorMsg")
            }
        } catch (e: Exception) {
            ApiResult(false, "Erro Conexão: ${e.message}")
        }
    }

    suspend fun updateCarDetailed(car: Car): ApiResult = withContext(Dispatchers.IO) {
        val id = car.id ?: car.licence ?: return@withContext ApiResult(false, "ID ausente.")
        try {
            val cleanCar = car.copy(nestedValue = null)
            val response = carApi.updateCar(id, ItemResponse(id = id, value = cleanCar))
            if (response.isSuccessful) ApiResult(true, "Sucesso!")
            else ApiResult(false, "API ${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            ApiResult(false, "Erro Conexão: ${e.message}")
        }
    }

    suspend fun deleteCarDetailed(id: String): ApiResult = withContext(Dispatchers.IO) {
        try {
            val response = carApi.deleteCar(id)
            if (response.isSuccessful) ApiResult(true, "Sucesso!")
            else ApiResult(false, "API ${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            ApiResult(false, "Erro Conexão: ${e.message}")
        }
    }

    suspend fun updateCar(car: Car): Boolean = updateCarDetailed(car).success
    suspend fun deleteCar(id: String): Boolean = deleteCarDetailed(id).success
}
