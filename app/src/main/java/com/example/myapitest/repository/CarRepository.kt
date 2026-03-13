package com.example.myapitest.repository

import com.example.myapitest.data.FirestoreManager
import com.example.myapitest.data.api.CarApi
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.ItemResponse

// RZ - O CarRepository coordena a persistência de dados.
// O foco principal é a API Local (Node/Express), e o Firestore (rzcarapp) 
// atua como uma réplica para manter os dados sincronizados na nuvem.

class CarRepository(private val carApi: CarApi) {

    // RZ - Busca a lista de carros da API Local e mapeia o envelope
    suspend fun getCars(): List<Car>? {
        return try {
            val response = carApi.fetchCars()
            if (response.isSuccessful) {
                response.body()?.map { item ->
                    item.value.copy(id = item.id)
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // RZ - Salva o carro na API Local e, se tiver sucesso, replica no Firestore
    suspend fun saveCar(car: Car): Boolean {
        return try {
            // RZ - Envia para a API Local (Foco Principal)
            val response = carApi.saveCar(ItemResponse(value = car))
            if (response.isSuccessful) {
                val savedCar = response.body()?.value?.copy(id = response.body()?.id)
                if (savedCar != null) {
                    // RZ - Réplica no Firestore
                    FirestoreManager.saveCarToFirestore(savedCar)
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // RZ - Atualiza o carro na API Local e replica a mudança no Firestore
    suspend fun updateCar(car: Car): Boolean {
        if (car.id == null) return false
        return try {
            val response = carApi.updateCar(car.id, ItemResponse(id = car.id, value = car))
            if (response.isSuccessful) {
                // RZ - Réplica no Firestore
                FirestoreManager.saveCarToFirestore(car)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // RZ - Remove o carro da API Local e também da réplica no Firestore
    suspend fun deleteCar(id: String): Boolean {
        return try {
            val response = carApi.deleteCar(id)
            if (response.isSuccessful) {
                // RZ - Réplica no Firestore
                FirestoreManager.deleteCarFromFirestore(id)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
}
