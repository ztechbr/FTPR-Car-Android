package com.example.myapitest.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.data.api.RetrofitClient
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.repository.CarRepository
import com.example.myapitest.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// RZ - Refatorada para o pacote ui.main para organizar melhor o código
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var carAdapter: CarAdapter
    private lateinit var carRepository: CarRepository

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
        
        // RZ - Exibe a informação do usuário logado na Toolbar e no Rodapé
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
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        fetchItems()
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(emptyList()) { car ->
            // RZ - "Achata" o carro para garantir que os dados corretos sejam passados
            val displayCar = car.nestedValue ?: car
            
            val intent = Intent(this, CarMapActivity::class.java).apply {
                putExtra("ID", car.id) // O ID vem do envelope ItemResponse
                putExtra("NAME", displayCar.name)
                putExtra("YEAR", displayCar.year)
                putExtra("LICENCE", displayCar.licence)
                putExtra("IMAGE_URL", displayCar.imageUrl)
                putExtra("LAT", displayCar.place?.lat ?: 0.0)
                putExtra("LONG", displayCar.place?.long ?: 0.0)
            }
            startActivity(intent)
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

        // RZ - Fecha o aplicativo literalmente (limpa da memória)
        binding.btnCloseApp.setOnClickListener {
            finishAffinity()
        }

        binding.addCta.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }
    }

    private fun fetchItems() {
        lifecycleScope.launch {
            binding.swipeRefreshLayout.isRefreshing = true
            val baseUrl = getString(com.example.myapitest.R.string.URLAcesso)
            logError("RZ Console: GET $baseUrl" + "items")

            try {
                val cars = carRepository.getCars()
                
                if (cars.isNullOrEmpty()) {
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
                binding.tvEmptyMessage.visibility = View.VISIBLE
                binding.tvEmptyMessage.text = "Erro ao carregar dados"
            }
            
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun logError(message: String) {
        binding.tvErrorLog.text = message
    }
}
