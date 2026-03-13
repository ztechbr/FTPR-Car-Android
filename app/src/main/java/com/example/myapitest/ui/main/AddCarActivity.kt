package com.example.myapitest.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.data.FirebaseStorageManager
import com.example.myapitest.data.api.RetrofitClient
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.Place
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.example.myapitest.repository.CarRepository
import com.example.myapitest.ui.login.LoginActivity
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// RZ - Activity para cadastrar um novo carro com melhorias de UX e Localização
class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCarBinding
    private lateinit var carRepository: CarRepository
    private var selectedImageUri: Uri? = null

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

        // RZ - Exibe info do usuário e configura logout
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUserInfo.text = "${user?.phoneNumber} Logado"
        
        val carApi = RetrofitClient.getInstance(this)
        carRepository = CarRepository(carApi)

        setupView()
    }

    private fun setupView() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        binding.ivCarPreview.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnGetLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnSave.setOnClickListener {
            saveCar()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                binding.etLat.setText(location.latitude.toString())
                binding.etLong.setText(location.longitude.toString())
                logConsole("RZ Console: Localização capturada com sucesso!")
            } else {
                logConsole("RZ Console: Não foi possível obter a localização. Verifique o GPS.")
            }
        }
    }

    private fun saveCar() {
        val name = binding.etName.text.toString()
        val year = binding.etYear.text.toString()
        val licence = binding.etLicence.text.toString()
        val latStr = binding.etLat.text.toString()
        val lngStr = binding.etLong.text.toString()

        if (name.isEmpty() || year.isEmpty() || licence.isEmpty() || selectedImageUri == null || latStr.isEmpty() || lngStr.isEmpty()) {
            logConsole("RZ Console: Erro! Preencha todos os campos e selecione a foto.")
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            logConsole("RZ Console: Iniciando upload da imagem...")

            val imageUrl = FirebaseStorageManager.uploadImage(selectedImageUri!!)

            if (imageUrl != null) {
                logConsole("RZ Console: Imagem OK! Salvando dados na API Local...")
                val car = Car(
                    name = name,
                    year = year,
                    licence = licence,
                    imageUrl = imageUrl,
                    place = Place(latStr.toDouble(), lngStr.toDouble())
                )

                val success = carRepository.saveCar(car)

                if (success) {
                    logConsole("RZ Console: Sucesso! Carro cadastrado e replicado no Firestore.")
                    Toast.makeText(this@AddCarActivity, "Carro salvo!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    logConsole("RZ Console: Erro ao salvar na API Local.")
                }
            } else {
                logConsole("RZ Console: Erro no upload para o Storage.")
            }

            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
        }
    }

    private fun logConsole(message: String) {
        binding.tvErrorLog.text = message
    }
}
