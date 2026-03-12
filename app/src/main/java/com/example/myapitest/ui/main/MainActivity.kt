package com.example.myapitest.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
        
        // RZ - Verifica se o usuário está autenticado no Firebase
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // RZ - Inicializa o repositório com a API configurada via RetrofitClient
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
        // RZ - Configura o RecyclerView com clique para abrir o mapa
        carAdapter = CarAdapter(emptyList()) { car ->
            val intent = Intent(this, CarMapActivity::class.java).apply {
                putExtra("LAT", car.place.lat)
                putExtra("LONG", car.place.long)
                putExtra("NAME", car.name)
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

        // RZ - Configura o Swipe to Refresh para atualizar a lista manualmente
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }
    }

    private fun fetchItems() {
        // RZ - Usa o lifecycleScope para rodar a chamada de rede de forma assíncrona
        lifecycleScope.launch {
            binding.swipeRefreshLayout.isRefreshing = true
            val cars = carRepository.getCars()
            if (cars != null) {
                carAdapter.updateData(cars)
            } else {
                Toast.makeText(this@MainActivity, "Erro ao carregar carros", Toast.LENGTH_SHORT).show()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}
