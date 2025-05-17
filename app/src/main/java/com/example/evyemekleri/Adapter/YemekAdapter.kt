package com.example.evyemekleri.Adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.RecyclerRowYemeklerBinding
import com.example.evyemekleri.Model.Yemek
import com.example.yemekvetarifleri.Fragment.Detay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class YemekAdapter(val context: Context, val menulerListesi: ArrayList<Yemek>) : RecyclerView.Adapter<YemekAdapter.Yemek_Holder>()
{
    class Yemek_Holder(val binding : RecyclerRowYemeklerBinding) : RecyclerView.ViewHolder(binding.root)
    {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Yemek_Holder
    {
        val binding : RecyclerRowYemeklerBinding = RecyclerRowYemeklerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Yemek_Holder(binding)
    }

    override fun getItemCount(): Int
    {
        return menulerListesi.size
    }

    override fun onBindViewHolder(holder: Yemek_Holder, position: Int)
    {
        val yemek = menulerListesi[position]
        val auth = FirebaseAuth.getInstance()
        val kullaniciid = auth.currentUser?.uid
        val db = FirebaseDatabase.getInstance()

        holder.binding.textView.text = yemek.isim

        // Görseli Firebase'den al
        db.getReference("kullanicilar")
            .child(kullaniciid!!)
            .child("menu")
            .child(yemek.tur)
            .child(yemek.isim)
            .child("gorsel")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                val gorselBase64 = dataSnapshot.value?.toString()

                if (!gorselBase64.isNullOrEmpty())
                {
                    try
                    {
                        val imageBytes = Base64.decode(gorselBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        holder.binding.rowImageView.setImageBitmap(bitmap)
                        holder.binding.rowImageView.visibility = android.view.View.VISIBLE
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()

                    }
                }
            }

        val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("menuAdi", yemek.tur)
                putString("adi", yemek.isim)
            }

            val fragment = Detay()
            fragment.arguments = bundle

            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}