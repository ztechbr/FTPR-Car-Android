package com.example.myapitest.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.data.FirestoreManager
import com.example.myapitest.data.api.RetrofitClient
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.repository.CarRepository
import com.example.myapitest.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var carAdapter: CarAdapter
    private lateinit var carRepository: CarRepository
    private var lastLoadedCars: List<com.example.myapitest.data.model.Car> = emptyList()
    
    // RZ - Debounce para evitar abertura dupla acidental do card
    private var isDetailOpening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val userInfo = "${user.phoneNumber} Logado"
        binding.tvUserInfo.text = userInfo
        binding.tvUserFooter.text = "Usuário: ${user.phoneNumber}"
        
        val carApi = RetrofitClient.getInstance(this)
        carRepository = CarRepository(carApi)

        setupRecyclerView()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        isDetailOpening = false // RZ - Reseta a trava ao voltar para a lista
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        fetchItems()
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(emptyList()) { car ->
            // RZ - Trava de segurança para não abrir 2x o mesmo card
            if (!isDetailOpening) {
                isDetailOpening = true
                val intent = Intent(this, CarMapActivity::class.java).apply {
                    putExtra("ID", car.id)
                    putExtra("NAME", car.name)
                    putExtra("YEAR", car.year)
                    putExtra("LICENCE", car.licence)
                    putExtra("IMAGE_URL", car.imageUrl)
                    putExtra("LAT", car.place?.lat ?: 0.0)
                    putExtra("LONG", car.place?.long ?: 0.0)
                }
                startActivity(intent)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = carAdapter
        }
    }

    private fun setupView() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnCloseApp.setOnClickListener {
            finishAffinity()
        }

        binding.addCta.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }

        binding.btnBackup.setOnClickListener {
            if (lastLoadedCars.isNotEmpty()) {
                lifecycleScope.launch {
                    logError("RZ Console: Iniciando Backup no Firestore...")
                    val success = FirestoreManager.backupAllToFirestore(this@MainActivity, lastLoadedCars)
                    if (success) {
                        Toast.makeText(this@MainActivity, "Backup concluído!", Toast.LENGTH_SHORT).show()
                        logError("RZ Console: Backup concluído!")
                    } else {
                        logError("RZ Console Erro: Falha no Backup.")
                    }
                }
            } else {
                Toast.makeText(this, "Nenhum dado carregado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchItems() {
        lifecycleScope.launch {
            binding.swipeRefreshLayout.isRefreshing = true
            try {
                val cars = carRepository.getCars()
                lastLoadedCars = cars
                
                if (cars.isEmpty()) {
                    carAdapter.updateData(emptyList())
                    binding.tvEmptyMessage.visibility = View.VISIBLE
                    logError("RZ Console: Não Existem Carros Cadastrados.")
                } else {
                    carAdapter.updateData(cars)
                    binding.tvEmptyMessage.visibility = View.GONE
                    logError("RZ Console: Sucesso! ${cars.size} carros carregados.")
                }
            } catch (e: Exception) {
                logError("RZ Console Erro: ${e.message}")
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun logError(message: String) {
        binding.tvErrorLog.text = message
    }
}
