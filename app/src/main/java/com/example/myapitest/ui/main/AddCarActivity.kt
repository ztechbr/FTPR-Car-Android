package com.example.myapitest.ui.main

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.data.FirebaseStorageManager
import com.example.myapitest.data.api.RetrofitClient
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.Place
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.example.myapitest.repository.CarRepository
import kotlinx.coroutines.launch

// RZ - Activity para cadastrar um novo carro.
// Realiza o upload da imagem para o Storage e salva os dados na API Local + Firestore.
class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCarBinding
    private lateinit var carRepository: CarRepository
    private var selectedImageUri: Uri? = null

    // RZ - Launcher para selecionar imagem da galeria
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivCarPreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val carApi = RetrofitClient.getInstance(this)
        carRepository = CarRepository(carApi)

        binding.ivCarPreview.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveCar()
        }
    }

    private fun saveCar() {
        val name = binding.etName.text.toString()
        val year = binding.etYear.text.toString()
        val licence = binding.etLicence.text.toString()
        val lat = binding.etLat.text.toString().toDoubleOrNull() ?: 0.0
        val lng = binding.etLong.text.toString().toDoubleOrNull() ?: 0.0

        if (name.isEmpty() || year.isEmpty() || licence.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Preencha todos os campos e selecione uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false

            // RZ - 1. Upload da imagem para o Firebase Storage
            val imageUrl = FirebaseStorageManager.uploadImage(selectedImageUri!!)

            if (imageUrl != null) {
                // RZ - 2. Salva na API Local (com réplica automática no Firestore pelo Repository)
                val car = Car(
                    name = name,
                    year = year,
                    licence = licence,
                    imageUrl = imageUrl,
                    place = Place(lat, lng)
                )

                val success = carRepository.saveCar(car)

                if (success) {
                    Toast.makeText(this@AddCarActivity, "Carro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddCarActivity, "Erro ao salvar na API Local", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@AddCarActivity, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
            }

            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
        }
    }
}
