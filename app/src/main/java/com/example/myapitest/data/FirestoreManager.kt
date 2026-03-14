package com.example.myapitest.data

import android.content.Context
import com.example.myapitest.R
import com.example.myapitest.data.model.Car
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    // RZ - Backup agora roda explicitamente em Dispatchers.IO para evitar travar a UI (ANR)
    suspend fun backupAllToFirestore(context: Context, cars: List<Car>): Boolean = withContext(Dispatchers.IO) {
        if (cars.isEmpty()) return@withContext false
        
        try {
            val collectionPrefix = context.getString(R.string.FirestoreCollectionName)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fullCollectionPath = "${collectionPrefix}_$timeStamp"

            cars.forEach { car ->
                val docId = car.id ?: car.licence ?: UUID.randomUUID().toString()
                // RZ - Limpa dados aninhados antes de subir para o backup
                val cleanCar = car.copy(nestedValue = null)
                db.collection(fullCollectionPath)
                    .document(docId)
                    .set(cleanCar)
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
