package com.example.myapitest.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivitySelectLocationBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

// RZ - Activity para selecionar localização manualmente no mapa
class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySelectLocationBinding
    private lateinit var mMap: GoogleMap
    private var initialLat: Double = 0.0
    private var initialLong: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RZ - Tenta pegar a localização inicial passada pela tela anterior
        initialLat = intent.getDoubleExtra("LAT", 0.0)
        initialLong = intent.getDoubleExtra("LONG", 0.0)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // RZ - Botão de retorno caso o usuário desista
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnConfirmLocation.setOnClickListener {
            val center = mMap.cameraPosition.target
            val resultIntent = Intent().apply {
                putExtra("LAT", center.latitude)
                putExtra("LONG", center.longitude)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        // RZ - Se uma localização inicial foi fornecida (diferente de 0), inicia o mapa nela.
        // Caso contrário, tenta pegar o GPS atual ou mantém no ponto zero.
        if (initialLat != 0.0 || initialLong != 0.0) {
            val startLatLng = LatLng(initialLat, initialLong)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 15f))
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                    }
                }
            }
        }
    }
}
