package com.example.myapitest.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.data.model.Car
import com.squareup.picasso.Picasso

// RZ - O CarAdapter é o "maestro" da lista. Ele pega a lista de carros (dados) 
// e diz ao RecyclerView como criar e preencher cada linha (item_car_layout).

class CarAdapter(
    private var carList: List<Car>,
    private val onCarClick: (Car) -> Unit // RZ - Adicionado callback de clique para abrir o mapa
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    // RZ - O ViewHolder é quem segura as referências para os componentes visuais de cada item
    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCar: ImageView = view.findViewById(R.id.image)
        val tvName: TextView = view.findViewById(R.id.model)
        val tvYear: TextView = view.findViewById(R.id.year)
        val tvLicense: TextView = view.findViewById(R.id.license)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_layout, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        
        holder.tvName.text = car.name
        holder.tvYear.text = car.year
        holder.tvLicense.text = car.licence

        if (car.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(car.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imgCar)
        }

        // RZ - Configura o clique no item para chamar o callback
        holder.itemView.setOnClickListener {
            onCarClick(car)
        }
    }

    override fun getItemCount(): Int = carList.size

    fun updateData(newList: List<Car>) {
        carList = newList
        notifyDataSetChanged()
    }
}
