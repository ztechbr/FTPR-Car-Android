package com.example.myapitest.repository

import com.example.myapitest.data.FirestoreManager
import com.example.myapitest.data.api.CarApi
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.ItemResponse

class CarRepository(private val carApi: CarApi) {

    // RZ - Busca a lista e usa uma busca recursiva para achar o carro real no JSON aninhado
    suspend fun getCars(): List<Car> {
        return try {
            val response = carApi.fetchCars()
            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                body.map { item ->
                    val realCar = findDeepData(item.value)
                    realCar.copy(id = item.id)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // RZ - Função recursiva: se o carro atual não tem nome mas tem um "value" dentro, mergulha mais fundo
    private fun findDeepData(car: Car): Car {
        return if (!car.name.isNullOrEmpty()) {
            car
        } else if (car.nestedValue != null) {
            findDeepData(car.nestedValue)
        } else {
            car
        }
    }

    suspend fun saveCar(car: Car): Boolean {
        return try {
            val response = carApi.saveCar(ItemResponse(value = car))
            if (response.isSuccessful) {
                val body = response.body()
                val savedCar = body?.let { findDeepData(it.value).copy(id = it.id) }
                if (savedCar != null) {
                    FirestoreManager.saveCarToFirestore(savedCar)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateCar(car: Car): Boolean {
        if (car.id == null) return false
        return try {
            val response = carApi.updateCar(car.id, ItemResponse(id = car.id, value = car))
            if (response.isSuccessful) {
                FirestoreManager.saveCarToFirestore(car)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCar(id: String): Boolean {
        return try {
            val response = carApi.deleteCar(id)
            if (response.isSuccessful) {
                FirestoreManager.deleteCarFromFirestore(id)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
