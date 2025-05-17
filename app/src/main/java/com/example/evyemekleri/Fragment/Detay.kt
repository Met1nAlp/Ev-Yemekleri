package com.example.yemekvetarifleri.Fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.evyemekleri.Fragment.AnaSayfa
import com.example.evyemekleri.Fragment.Menu
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.FragmentDetayBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class Detay : Fragment()
{

    private var _binding : FragmentDetayBinding? = null
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
        _binding = FragmentDetayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        var yemekAdi : String
        var malzemeler : String
        var tarif : String
        var gorselBase64 : String?

        val bundle = arguments
        val menuAdi = bundle?.getString("menuAdi")
        val adi = bundle?.getString("adi")

        val auth = FirebaseAuth.getInstance()
        val kullaniciid = auth.currentUser?.uid
        val db = FirebaseDatabase.getInstance()

        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        val profilSahibiId = sharedPreferences.getString("profilSahibiId", null)
        var myRef =db.getReference()

        if (profilSahibiId == null)
        {
            myRef = db.getReference("kullanicilar").child(kullaniciid!!).child("menu").child(menuAdi!!).child(adi!!)

            myRef.get().addOnSuccessListener { dataSnapshot ->

                if (dataSnapshot.exists()) {
                    yemekAdi = dataSnapshot.child("yemekAdi").value?.toString() ?: ""
                    malzemeler = dataSnapshot.child("malzemeler").value?.toString() ?: ""
                    tarif = dataSnapshot.child("tarif").value?.toString() ?: ""
                    gorselBase64 = dataSnapshot.child("gorsel").value?.toString()

                    binding.detayIsimTextView.text = yemekAdi
                    binding.detayMalzemelerListeTextView.text = malzemeler
                    binding.detayYapilisadimiTextView.text = tarif

                    if (!gorselBase64.isNullOrEmpty()) {
                        try {
                            val imageBytes = android.util.Base64.decode(gorselBase64, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.detayImageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            println("Görsel yükleme hatası: ${e.message}")
                            Toast.makeText(requireContext(), "Görsel yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Yemek bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
                .addOnFailureListener { e ->
                    println("Firebase hatası: ${e.message}")
                    Toast.makeText(requireContext(), "Veri alınırken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }


            binding.detayFloatingActionButton.setOnClickListener {

                val popupMenu = androidx.appcompat.widget.PopupMenu(requireContext(), binding.detayFloatingActionButton)
                popupMenu.menuInflater.inflate(R.menu.fab_detay, popupMenu.menu)

                popupMenu.menu.findItem(R.id.detay_fab_yemegisil).isVisible = true
                popupMenu.menu.findItem(R.id.menu_fab_duzenle).isVisible = true
                popupMenu.menu.findItem(R.id.detay_fab_begen).isVisible = false
                popupMenu.menu.findItem(R.id.detay_fab_yorumyap).isVisible = false

                popupMenu.setOnMenuItemClickListener { menuItem ->

                    when(menuItem.itemId)
                    {

                        R.id.detay_fab_yemegisil -> {
                            myRef.removeValue().addOnSuccessListener {
                                Toast.makeText(requireContext(), "Yemek Silindi", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainerView, Menu())
                                    .addToBackStack(null)
                                    .commit()
                            }
                            true
                        }

                        else -> false
                    }
                }

                popupMenu.show()
            }
        }
        else
        {
            myRef = db.getReference("kullanicilar").child(profilSahibiId!!).child("menu").child(menuAdi!!).child(adi!!)

            myRef.get().addOnSuccessListener { dataSnapshot ->

                println("Veri var mı: ${dataSnapshot.exists()}")
                println("Veri içeriği: ${dataSnapshot.value}")

                if (dataSnapshot.exists()) {
                    yemekAdi = dataSnapshot.child("yemekAdi").value?.toString() ?: ""
                    malzemeler = dataSnapshot.child("malzemeler").value?.toString() ?: ""
                    tarif = dataSnapshot.child("tarif").value?.toString() ?: ""
                    gorselBase64 = dataSnapshot.child("gorsel").value?.toString()

                    binding.detayIsimTextView.text = yemekAdi
                    binding.detayMalzemelerListeTextView.text = malzemeler
                    binding.detayYapilisadimiTextView.text = tarif

                    if (!gorselBase64.isNullOrEmpty()) {
                        try {
                            val imageBytes = android.util.Base64.decode(gorselBase64, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.detayImageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            println("Görsel yükleme hatası: ${e.message}")
                            Toast.makeText(requireContext(), "Görsel yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Yemek bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
                .addOnFailureListener { e ->
                    println("Firebase hatası: ${e.message}")
                    Toast.makeText(requireContext(), "Veri alınırken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }


            binding.detayFloatingActionButton.setOnClickListener {

                val popupMenu = androidx.appcompat.widget.PopupMenu(requireContext(), binding.detayFloatingActionButton)
                popupMenu.menuInflater.inflate(R.menu.fab_detay, popupMenu.menu)


                popupMenu.menu.findItem(R.id.detay_fab_begen).isVisible = true
                popupMenu.menu.findItem(R.id.detay_fab_yorumyap).isVisible = true
                popupMenu.menu.findItem(R.id.detay_fab_yemegisil).isVisible = false
                popupMenu.menu.findItem(R.id.menu_fab_duzenle).isVisible = false

                popupMenu.setOnMenuItemClickListener { menuItem ->

                    when(menuItem.itemId)
                    {
                        R.id.detay_fab_begen -> {
                            Toast.makeText(requireContext(), "Beğenme İşlemi Yapıldı", Toast.LENGTH_SHORT).show()
                            true
                        }

                        R.id.detay_fab_yorumyap -> {
                            Toast.makeText(requireContext(), "Yorum Yapma İşlemi Yapıldı", Toast.LENGTH_SHORT).show()
                            true
                        }

                        else -> false
                    }
                }

                popupMenu.show()
            }
        }






        binding.detayBottomNavigationView.selectedItemId = R.id.bn_bar_menu

        binding.detayBottomNavigationView.setOnItemSelectedListener {

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

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

}