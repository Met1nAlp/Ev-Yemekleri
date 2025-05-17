package com.example.evyemekleri.Model



import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.evyemekleri.Fragment.EklemeVeGoruntuleme
import com.example.evyemekleri.Fragment.Menu
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.RecyclerRowBinding

class MenuAdapter(val context: Context, val menulerListesi: ArrayList<Menuler>) : RecyclerView.Adapter<MenuAdapter.Menu_Holder>()
{
    class Menu_Holder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)
    {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Menu_Holder
    {
        val binding : RecyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Menu_Holder(binding)
    }

    override fun getItemCount(): Int
    {
        return menulerListesi.size
    }

    override fun onBindViewHolder(holder: Menu_Holder, position: Int)
    {
        val menu = menulerListesi[position]

        holder.binding.textView.text = menu.adi

        val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager

        holder.itemView.setOnClickListener {

            val bundle = Bundle()

            bundle.putString("menuAdi" , menu.adi)

            val fragment = EklemeVeGoruntuleme()
            fragment.arguments = bundle

            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()


        }

    }
}