package com.example.evyemekleri.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evyemekleri.Adapter.PostAdapter
import com.example.evyemekleri.Model.Post
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.FragmentAnaSayfaBinding
import com.example.yemekvetarifleri.Fragment.Profil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values
import java.util.Collections

class AnaSayfa : Fragment() {

    private var _binding: FragmentAnaSayfaBinding? = null
    private val binding get() = _binding!!

    private val bulunanKullanicilar = mutableListOf<Pair<String, String>>() // Pair<ID, TamAd>
    private val takipEdilenKullanicilar = mutableListOf<String>()
    private val gonderiler = mutableListOf<Post>()
    private val goruntulenenGonderiler = mutableSetOf<String>() // Gösterilen gönderilerin ID'lerini tutar
    private lateinit var gonderiAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnaSayfaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.anaSayfaBottomNavigationView.selectedItemId = R.id.bn_bar_anasayfa

        binding.anaSayfaBottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
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

        // RecyclerView'ı ayarla
        binding.anaSayfaRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        gonderiAdapter = PostAdapter(requireContext(), ArrayList())
        binding.anaSayfaRecyclerView.adapter = gonderiAdapter

        // Takip edilen kullanıcıları ve gönderilerini yükle
        takipEdilenKullanicilariYukle()

        binding.aramaButton.setOnClickListener {
            val aramaMetni = binding.aramaEditText.text.toString()
            if (aramaMetni.isNotEmpty()) {
                kullaniciAra(aramaMetni)
            } else {
                Toast.makeText(requireContext(), "Lütfen bir isim girin", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun takipEdilenKullanicilariYukle() {
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()

        // Takip edilen kullanıcıları al
        database.getReference("kullanicilar")
            .child(currentUserId)
            .child("takipEdilenler")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    takipEdilenKullanicilar.clear()
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key ?: continue
                        takipEdilenKullanicilar.add(userId)
                    }
                    // Takip edilen kullanıcıların gönderilerini yükle
                    gonderileriYukle()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AnaSayfa", "Takip edilen kullanıcılar yüklenirken hata: ${error.message}")
                }
            })
    }

    private fun gonderileriYukle() {
        val database = FirebaseDatabase.getInstance()
        gonderiler.clear()

        for (userId in takipEdilenKullanicilar) {
            database.getReference("kullanicilar")
                .child(userId)
                .child("menu")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (menuSnapshot in snapshot.children) {
                            val menuAdi = menuSnapshot.key ?: continue
                            
                            for (yemekSnapshot in menuSnapshot.children) {
                                val yemekAdi = yemekSnapshot.key ?: continue
                                
                                // Kullanıcı bilgilerini al
                                val kullaniciEmail = yemekSnapshot.child("kullaniciEmail").value?.toString() ?: continue
                                val malzemeler = yemekSnapshot.child("malzemeler").value?.toString() ?: ""
                                val tarif = yemekSnapshot.child("tarif").value?.toString() ?: ""
                                val gorsel = yemekSnapshot.child("gorsel").value?.toString() ?: ""

                                // Kullanıcının adını ve fotoğrafını almak için
                                database.getReference("kullanicilar")
                                    .child(userId)
                                    .child("KullanıcıBilgileri")
                                    .get()
                                    .addOnSuccessListener { kullaniciBilgileriSnapshot ->
                                        val gonderenAdi = kullaniciBilgileriSnapshot.child("tamAd").value?.toString() ?: "Bilinmeyen Kullanıcı"
                                        val fotograf = kullaniciBilgileriSnapshot.child("fotograf").value?.toString() ?: ""

                                        val post = Post(
                                            gonderenAdi,
                                            userId,
                                            fotograf,
                                            menuAdi,
                                            yemekAdi,
                                            malzemeler,
                                            tarif,
                                            gorsel
                                        )
                                        if (!goruntulenenGonderiler.contains("$userId:$yemekAdi")) {
                                            gonderiler.add(post)
                                            goruntulenenGonderiler.add("$userId:$yemekAdi")
                                            
                                            // Gönderileri karıştır ve adapter'ı güncelle
                                            Collections.shuffle(gonderiler)
                                            gonderiAdapter.updateList(ArrayList(gonderiler))
                                        }
                                    }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("AnaSayfa", "Gönderiler yüklenirken hata: ${error.message}")
                    }
                })
        }
    }

    private fun kullaniciAra(aramaMetni: String) {
        val database = FirebaseDatabase.getInstance()
        val kullaniciRef = database.getReference("kullanicilar")

        kullaniciRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bulunanKullanicilar.clear()
                for (kullaniciSnapshot in snapshot.children) {
                    val tamAd = kullaniciSnapshot.child("KullanıcıBilgileri/tamAd").value?.toString()
                    val kullaniciId = kullaniciSnapshot.key ?: ""
                    if (tamAd != null && tamAd.contains(aramaMetni, ignoreCase = true)) {
                        bulunanKullanicilar.add(Pair(kullaniciId, tamAd))
                    }
                }
                if (bulunanKullanicilar.isNotEmpty()) {
                    showAramaSonuclariPopup()
                } else {
                    Toast.makeText(requireContext(), "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AnaSayfa", "Veri çekme hatası: ${error.message}")
            }
        })
    }

    private fun showAramaSonuclariPopup() {
        val popup = PopupMenu(requireContext(), binding.aramaButton)

        bulunanKullanicilar.forEachIndexed { index, (id, tamAd) ->
            popup.menu.add(0, index, 0, tamAd)
        }

        popup.setOnMenuItemClickListener { item ->
            val (selectedId, selectedName) = bulunanKullanicilar[item.itemId]
            Toast.makeText(context, "Seçilen: $selectedName (ID: $selectedId)", Toast.LENGTH_SHORT).show()

            val profilFragment = Profil().apply {
                arguments = Bundle().apply {
                    putString("takipci_id", selectedId)
                    putBoolean("isOtherProfile", true)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, profilFragment)
                .addToBackStack(null)
                .commit()

            true
        }

        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popup)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
            mPopup.javaClass
                .getDeclaredMethod("setWidth", Int::class.java)
                .invoke(mPopup, 600)
        } catch (e: Exception) {
            Log.e("PopupMenu", "Popup genişlik ayarı hatası", e)
        }

        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}