package com.example.myapitest.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// RZ - CarMapActivity: A tela do mapa que mostra onde o carro está.
// Ela recebe os dados de latitude e longitude e coloca o "pin" no lugar certo.

class CarMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var carLat: Double = 0.0
    private var carLong: Double = 0.0
    private var carName: String = "Carro"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_map)

        // RZ - Passo 1: Pega os dados enviados pelo intent
        carLat = intent.getDoubleExtra("LAT", 0.0)
        carLong = intent.getDoubleExtra("LONG", 0.0)
        carName = intent.getStringExtra("NAME") ?: "Carro"

        // RZ - Passo 2: Inicializa o fragmento do Google Maps
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // RZ - Passo 3: Cria a posição LatLng do carro
        val carLocation = LatLng(carLat, carLong)

        // RZ - Passo 4: Adiciona o marcador (pin) no mapa com o nome do carro
        googleMap.addMarker(
            MarkerOptions()
                .position(carLocation)
                .title(carName)
        )

        // RZ - Passo 5: Move a câmera para a posição do carro com um zoom agradável (15)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15f))
    }
}
