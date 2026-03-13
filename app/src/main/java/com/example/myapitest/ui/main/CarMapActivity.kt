package com.example.myapitest.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.R
import com.example.myapitest.data.FirebaseStorageManager
import com.example.myapitest.data.api.RetrofitClient
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.Place
import com.example.myapitest.repository.CarRepository
import com.example.myapitest.ui.login.LoginActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class CarMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var carRepository: CarRepository
    private var carId: String? = null
    private var imageUrl: String? = null
    private var selectedImageUri: Uri? = null

    private lateinit var ivCarPhoto: ImageView
    private lateinit var etCarName: EditText
    private lateinit var etCarYear: EditText
    private lateinit var etCarLicence: EditText
    private lateinit var tvErrorLog: TextView
    private lateinit var btnChangePhoto: MaterialButton

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            ivCarPhoto.setImageURI(it)
            logStatus("RZ Console: Nova foto selecionada! Clique em ATUALIZAR para salvar.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_map)

        carRepository = CarRepository(RetrofitClient.getInstance(this))
        setupViews()
        loadCarData()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupViews() {
        ivCarPhoto = findViewById(R.id.ivCarPhoto)
        etCarName = findViewById(R.id.etCarName)
        etCarYear = findViewById(R.id.etCarYear)
        etCarLicence = findViewById(R.id.etCarLicence)
        tvErrorLog = findViewById(R.id.tvErrorLog)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)

        findViewById<TextView>(R.id.tvUserInfo).text = "${FirebaseAuth.getInstance().currentUser?.phoneNumber} Logado"

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        // RZ - Botão para trocar a foto (abre galeria)
        btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btnUpdate).setOnClickListener { updateCar() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { showDeleteConfirmation() }
    }

    private fun loadCarData() {
        carId = intent.getStringExtra("ID")
        imageUrl = intent.getStringExtra("IMAGE_URL")
        val name = intent.getStringExtra("NAME") ?: ""
        val year = intent.getStringExtra("YEAR") ?: ""
        val licence = intent.getStringExtra("LICENCE") ?: ""

        etCarName.setText(name)
        etCarYear.setText(year)
        etCarLicence.setText(licence)

        logStatus("Carregado: $name")

        if (!imageUrl.isNullOrEmpty()) {
            // RZ - Picasso carregando a foto atual do carro nos detalhes
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.fotopadrao)
                .error(R.drawable.fotopadrao)
                .into(ivCarPhoto)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val lat = intent.getDoubleExtra("LAT", 0.0)
        val lng = intent.getDoubleExtra("LONG", 0.0)
        val carLocation = LatLng(lat, lng)
        googleMap.addMarker(MarkerOptions().position(carLocation).title(etCarName.text.toString()))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15f))
    }

    private fun updateCar() {
        val name = etCarName.text.toString()
        val year = etCarYear.text.toString()
        val licence = etCarLicence.text.toString()

        if (name.isEmpty() || year.isEmpty() || licence.isEmpty()) {
            logStatus("Erro: Preencha todos os campos.")
            return
        }

        lifecycleScope.launch {
            logStatus("Salvando alterações...")
            
            var finalImageUrl = imageUrl

            // RZ - Se o usuário selecionou uma nova foto, faz o upload para o Firebase Storage
            if (selectedImageUri != null) {
                logStatus("Fazendo upload da nova foto...")
                val uploadedUrl = FirebaseStorageManager.uploadImage(selectedImageUri!!)
                if (uploadedUrl != null) {
                    finalImageUrl = uploadedUrl
                } else {
                    logStatus("Erro no upload da foto. Verifique conexão.")
                    return@launch
                }
            }

            // RZ - Cria o objeto Car limpo (sem nestedValue) para enviar para a API
            val carToUpdate = Car(
                id = carId,
                name = name,
                year = year,
                licence = licence,
                imageUrl = finalImageUrl,
                place = Place(intent.getDoubleExtra("LAT", 0.0), intent.getDoubleExtra("LONG", 0.0))
            )

            // RZ - Chama o método PATCH da API através do repositório
            val success = carRepository.updateCar(carToUpdate)
            
            if (success) {
                Toast.makeText(this@CarMapActivity, "Carro atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                logStatus("Erro ao atualizar API. Verifique se o servidor está rodando.")
            }
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Apagar Carro")
            .setMessage("Tem certeza que deseja remover este carro?")
            .setPositiveButton("Sim") { _, _ -> deleteCar() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deleteCar() {
        carId?.let { id ->
            lifecycleScope.launch {
                logStatus("Apagando carro...")
                if (carRepository.deleteCar(id)) {
                    Toast.makeText(this@CarMapActivity, "Carro removido!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    logStatus("Erro ao apagar carro da API.")
                }
            }
        }
    }

    private fun logStatus(message: String) {
        tvErrorLog.text = "RZ Console: $message"
    }
}
