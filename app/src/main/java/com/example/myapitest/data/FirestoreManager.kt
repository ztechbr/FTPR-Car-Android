package com.example.myapitest.data

import com.example.myapitest.data.model.Car
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// RZ - Gerenciador do Firestore Database (rzcarapp).
// A função dele é espelhar os dados da API no banco de dados do Google, 
// garantindo que as informações e referências de imagem estejam seguras.

object FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_NAME = "rzcarapp"

    // RZ - Salva ou atualiza os dados do carro no Firestore
    suspend fun saveCarToFirestore(car: Car): Boolean {
        return try {
            // RZ - O ID é necessário para salvar no Firestore. Tenta ID, depois placa, senão falha.
            val id = car.id ?: car.licence ?: return false
            db.collection(COLLECTION_NAME)
                .document(id)
                .set(car)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // RZ - Remove o carro do Firestore
    suspend fun deleteCarFromFirestore(id: String): Boolean {
        return try {
            db.collection(COLLECTION_NAME).document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
