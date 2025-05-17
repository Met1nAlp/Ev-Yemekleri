package com.example.evyemekleri.Fragment


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evyemekleri.Model.MenuAdapter
import com.example.evyemekleri.Model.Menuler
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.FragmentMenuBinding
import com.example.yemekvetarifleri.Fragment.Profil


class Menu : Fragment()
{

    private lateinit var _binding : FragmentMenuBinding
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)


        val menulerListesi = ArrayList<Menuler>()

        menulerListesi.add(
            Menuler("Yemek")
        )
        menulerListesi.add(
            Menuler("Meşrubat")
        )
        menulerListesi.add(
            Menuler("Tatlı")
        )
        menulerListesi.add(
            Menuler("Çorba")
        )
        menulerListesi.add(
            Menuler("Meze")
        )

        binding.menuRecyclerView.adapter = MenuAdapter(requireContext(), menulerListesi)
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.menuBottomNavigationView.selectedItemId = R.id.bn_bar_menu

        binding.menuBottomNavigationView.setOnItemSelectedListener {

            when(it.itemId)
            {
                R.id.bn_bar_anasayfa -> {

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, AnaSayfa())
                        .addToBackStack(null)
                        .commit()

                    true
                }


                R.id.bn_bar_menu -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, Menu())
                        .addToBackStack(null)
                        .commit()
                    true

                }

                R.id.bn_bar_profil -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, Profil())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }

        }

    }

    override fun onDestroy()
    {
        super.onDestroy()
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs" , Context.MODE_PRIVATE)
        val kaydet = sharedPreferences.edit()
        kaydet.clear()
        kaydet.apply()
    }


}