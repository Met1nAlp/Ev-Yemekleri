package com.example.yemekvetarifleri.Fragment // veya com.example.evyemekleri.Fragment, projenize göre düzeltin

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import com.example.evyemekleri.Fragment.AnaSayfa
import com.example.evyemekleri.Fragment.Giris
import com.example.evyemekleri.Fragment.Menu
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.FragmentProfilBinding // View Binding kullandığınızı varsayıyorum
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Profil : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private lateinit var database: FirebaseDatabase

    private var takipcilerListener: ValueEventListener? = null
    private var takipedilenlerListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()

        if (auth.currentUser == null)
        {
            Toast.makeText(requireContext() , "Oturum açılmamış", Toast.LENGTH_SHORT).show()

            if (isAdded && activity != null) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, Giris())
                    .addToBackStack(null)
                    .commitAllowingStateLoss() // veya commit()
            }
        }
}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        val profilSahibiId = bundle?.getString("takipci_id")
        val isOtherProfile = bundle?.getBoolean("isOtherProfile", false) ?: false

        // Başlangıç Görünürlük Ayarları
        if (isOtherProfile)
        {
            // Başka bir kullanıcının profili
            binding.profilCikisYapButton.visibility = View.GONE
            binding.profilTakipEtTextview.visibility = View.VISIBLE
            binding.profilTakibiBirakTextview.visibility = View.GONE
            binding.profilTakipEtTextview.text = "Yükleniyor..."
            binding.profilTakipEtTextview.isClickable = false
            binding.profilTakibiBirakTextview.isClickable = false
            binding.profilMenuyegitButton.visibility = View.INVISIBLE
        }
        else
        {
            // Kendi profili
            binding.profilCikisYapButton.visibility = View.VISIBLE
            binding.profilTakipEtTextview.visibility = View.GONE
            binding.profilTakibiBirakTextview.visibility = View.GONE
            binding.profilMenuyegitButton.visibility = View.GONE
        }

        val targetProfileId = if (isOtherProfile && profilSahibiId != null) profilSahibiId else auth.currentUser?.uid

        if (targetProfileId != null)
        {
            // Takipçi Sayısı Dinleyicisi
            takipcilerListener = database.getReference()
                .child("kullanicilar")
                .child(targetProfileId)
                .child("takipciler")
                .addValueEventListener(object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        if (_binding == null) {
                            Log.w("Profil", "Takipçiler onDataChange called after view destroyed")
                            return
                        }
                        val count = snapshot.childrenCount
                        binding.profilTakipcilerTextview.text = count.toString()
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        Log.e("Profil", "Takipçi sayısı yüklenirken hata: ${error.message}")
                    }
                })


            takipedilenlerListener = database.getReference()
                .child("kullanicilar")
                .child(targetProfileId)
                .child("takipEdilenler")
                .addValueEventListener(object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {

                        if (_binding == null) {
                            Log.w("Profil", "Takip Edilenler onDataChange called after view destroyed")
                            return
                        }
                        val count = snapshot.childrenCount
                        binding.profilTakipTextview.text = count.toString()

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Profil", "Takip edilen sayısı yüklenirken hata: ${error.message}")
                    }
                })


            val userDetailsId = if (isOtherProfile && profilSahibiId != null) profilSahibiId else auth.currentUser?.uid

            if (userDetailsId != null) {
                val kullaniciRef = database.getReference("kullanicilar")
                    .child(userDetailsId)
                    .child("KullanıcıBilgileri")

                kullaniciRef.addListenerForSingleValueEvent(object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        if (_binding == null) {
                            Log.w("Profil", "Kullanıcı Bilgileri onDataChange called after view destroyed")
                            return
                        }

                        if (snapshot.exists())
                        {
                            val isim = snapshot.child("tamAd").value?.toString() ?: "İsim Yok"
                            val eposta = snapshot.child("email").value?.toString() ?: "E-posta Yok"
                            val fotograf = snapshot.child("fotograf").value?.toString() ?: ""

                            binding.profilIsimTextview.text = isim
                            binding.profilSoyisimTextview.text = eposta

                            if (fotograf.isNotEmpty())
                            {
                                try
                                {
                                    val imageBytes = Base64.decode(fotograf, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    binding.profileImageview.setImageBitmap(bitmap)
                                }
                                catch (e: IllegalArgumentException)
                                {
                                    Log.e("Profil", "Base64 decode hatası: ${e.message}")
                                }
                            }

                            // Diğer profil ise takip durumu kontrolünü yap
                            if (isOtherProfile && profilSahibiId != null) {
                                // Kullanıcı adı çekildikten sonra takip durumunu kontrol et
                                takipDurumunuKontrolEt(profilSahibiId, isim)
                            }
                            else
                            {

                            }

                        }
                        else
                        {
                            Toast.makeText(requireContext(), "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
                            // Hata durumunda başka profil ise butonları ayarla
                            if (isOtherProfile) {
                                binding.profilTakipEtTextview.text = "Hata"
                                binding.profilTakipEtTextview.visibility = View.VISIBLE
                                binding.profilTakibiBirakTextview.visibility = View.GONE
                                binding.profilTakipEtTextview.isClickable = false
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {
                        Log.e("Profil", "Kullanıcı bilgileri çekme hatası: ${error.message}")
                        Toast.makeText(requireContext(), "Bilgiler alınamadı: ${error.message}", Toast.LENGTH_SHORT).show()
                        // Hata durumunda başka profil ise butonları ayarla
                        if (isOtherProfile) {
                            binding.profilTakipEtTextview.text = "Hata"
                            binding.profilTakipEtTextview.visibility = View.VISIBLE
                            binding.profilTakibiBirakTextview.visibility = View.GONE
                            binding.profilTakipEtTextview.isClickable = false
                        }
                    }
                })
            } else {
                // current user veya profilSahibiId null ise
                Toast.makeText(requireContext(), "Profil bilgileri yüklenemedi (ID yok)", Toast.LENGTH_SHORT).show()
                if (!isOtherProfile)
                {
                    // Kendi profilinde ID yoksa çıkış yap
                    cikisYap()
                }
                else
                {
                    // Başka profil ama ID yoksa hata göster
                    binding.profilTakipEtTextview.text = "Hata (ID)"
                    binding.profilTakipEtTextview.visibility = View.VISIBLE
                    binding.profilTakibiBirakTextview.visibility = View.GONE
                    binding.profilTakipEtTextview.isClickable = false
                }
            }

        }
        else
        {
            // targetProfileId null ise
            Toast.makeText(requireContext(), "Profil bilgileri yüklenemedi (Target ID yok)", Toast.LENGTH_SHORT).show()

            if (!isOtherProfile)
            {
                cikisYap()
            }
            else
            {
                // Başka profil ama ID yoksa hata göster
                binding.profilTakipEtTextview.text = "Hata (Target ID)"
                binding.profilTakipEtTextview.visibility = View.VISIBLE
                binding.profilTakibiBirakTextview.visibility = View.GONE
                binding.profilTakipEtTextview.isClickable = false
            }
        }



        if (!isOtherProfile)
        {
            binding.profilCikisYapButton.setOnClickListener {
                cikisYap()
            }
        }


        setupBottomNavigation()
    }


    private fun cikisYap()
    {
        FirebaseAuth.getInstance().signOut()

        if (isAdded && activity != null)
        {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, Giris())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }
        Toast.makeText(requireContext(), "Çıkış yapıldı", Toast.LENGTH_SHORT).show()
    }


    private fun takipEt(takipEdilecekId: String, takipEdilecekTamAd: String)
    {
        val mevcutKullaniciId = auth.currentUser?.uid
        if (mevcutKullaniciId == null) {
            Toast.makeText(requireContext(), "Oturum açılmamış", Toast.LENGTH_SHORT).show()
            return
        }

        if (_binding == null || !isAdded) return


        binding.profilTakipEtTextview.isClickable = false
        binding.profilTakibiBirakTextview.isClickable = false
        binding.profilTakipEtTextview.text = "İşleniyor..."


        database.getReference("kullanicilar")
            .child(mevcutKullaniciId)
            .child("KullanıcıBilgileri")
            .child("tamAd")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                val mevcutKullaniciIsim = dataSnapshot.value?.toString() ?: "Bilinmeyen Kullanıcı"


                val followingUpdate = database.getReference("kullanicilar")
                    .child(mevcutKullaniciId).child("takipEdilenler").child(takipEdilecekId)
                    .setValue(takipEdilecekTamAd)
                val followersUpdate = database.getReference("kullanicilar")
                    .child(takipEdilecekId).child("takipciler").child(mevcutKullaniciId)
                    .setValue(mevcutKullaniciIsim)

                // Başarı Durumu
                followingUpdate.addOnSuccessListener {
                    followersUpdate.addOnSuccessListener {
                        Log.d("TakipEt", "Takip etme başarılı.")

                        binding.profilTakipEtTextview.text = "Takip Ediliyor"
                        binding.profilTakibiBirakTextview.visibility = View.VISIBLE
                        binding.profilMenuyegitButton.visibility = View.VISIBLE

                        if (_binding == null || !isAdded){}

                        Toast.makeText(requireContext(), "$takipEdilecekTamAd takip edildi", Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener { e -> handleTakipEtHata(e, takipEdilecekId, takipEdilecekTamAd, "Takipçi ekleme hatası") }
                }.addOnFailureListener { e -> handleTakipEtHata(e, takipEdilecekId, takipEdilecekTamAd, "Takip edilen ekleme hatası") }

            }
            .addOnFailureListener { e -> handleTakipEtHata(e, takipEdilecekId, takipEdilecekTamAd, "Mevcut kullanıcı adı alınamadı") }


    }

    // Takip Etme Hata Yönetimi Yardımcı Fonksiyonu
    private fun handleTakipEtHata(e: Exception, id: String, ad: String, logMsg: String) {
        Log.e("TakipEt", "$logMsg: ${e.message}")
        Toast.makeText(requireContext(), "Takip etme hatası", Toast.LENGTH_SHORT).show()


        if (_binding == null || !isAdded) return

        binding.profilTakipEtTextview.visibility = View.VISIBLE
        binding.profilTakibiBirakTextview.visibility = View.GONE
        binding.profilTakipEtTextview.text = "Takip Et"
        binding.profilTakipEtTextview.isClickable = true
        binding.profilTakibiBirakTextview.isClickable = false
        binding.profilMenuyegitButton.visibility = View.INVISIBLE

        binding.profilTakipEtTextview.setOnClickListener {
            takipEt(id, ad)
        }
    }



    private fun takibiBirak(takibiBirakilacakId: String)
    {
        val mevcutKullaniciId = auth.currentUser?.uid
        if (mevcutKullaniciId == null) {
            Toast.makeText(requireContext(), "Oturum açılmamış", Toast.LENGTH_SHORT).show()
            return
        }

        if (_binding == null || !isAdded) return


        binding.profilTakipEtTextview.isClickable = false
        binding.profilTakibiBirakTextview.isClickable = false
        binding.profilTakibiBirakTextview.text = "İşleniyor..."

        database.getReference("kullanicilar")
            .child(takibiBirakilacakId)
            .child("KullanıcıBilgileri")
            .child("tamAd")
            .get()
            .addOnSuccessListener { nameSnapshot ->
                val takibiBirakilacakTamAd = nameSnapshot.value?.toString() ?: "İsim Yok"


                val unfollowTask = database.getReference("kullanicilar")
                    .child(mevcutKullaniciId).child("takipEdilenler").child(takibiBirakilacakId)
                    .removeValue()
                val removeFollowerTask = database.getReference("kullanicilar")
                    .child(takibiBirakilacakId).child("takipciler").child(mevcutKullaniciId)
                    .removeValue()


                unfollowTask.addOnSuccessListener {
                    removeFollowerTask.addOnSuccessListener {
                        Log.d("TakibiBirak", "Takip bırakma başarılı.")

                        if (_binding == null || !isAdded){}

                        Toast.makeText(requireContext(), "$takibiBirakilacakTamAd takibi bırakıldı", Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener { e -> handleTakibiBirakHata(e, takibiBirakilacakId, "Takipçi silme hatası") }
                }.addOnFailureListener { e -> handleTakibiBirakHata(e, takibiBirakilacakId, "Takip edilen silme hatası") }

            }.addOnFailureListener { e -> handleTakibiBirakHata(e, takibiBirakilacakId, "Takibi bırakılacak kullanıcı adı alınamadı") }


    }


    private fun handleTakibiBirakHata(e: Exception, id: String, logMsg: String)
    {
        Log.e("TakibiBirak", "$logMsg: ${e.message}")
        Toast.makeText(requireContext(), "Takip bırakma hatası", Toast.LENGTH_SHORT).show()


        if (_binding == null || !isAdded) return


        binding.profilTakipEtTextview.visibility = View.GONE
        binding.profilTakibiBirakTextview.visibility = View.VISIBLE
        binding.profilTakibiBirakTextview.text = "Takibi Bırak"
        binding.profilTakibiBirakTextview.isClickable = true
        binding.profilTakipEtTextview.isClickable = false
        binding.profilMenuyegitButton.visibility = View.VISIBLE

        binding.profilTakibiBirakTextview.setOnClickListener {
            takibiBirak(id)
        }
    }


    private fun takipDurumunuKontrolEt(profilSahibiId: String, profilSahibiTamAd: String)
    {
        val mevcutKullaniciId = auth.currentUser?.uid
        if (mevcutKullaniciId == null)
        {
            Toast.makeText(requireContext(), "Oturum açılmamış", Toast.LENGTH_SHORT).show()

            if (_binding == null || !isAdded) return

            binding.profilTakipEtTextview.text = "Hata (Oturum)"
            binding.profilTakipEtTextview.visibility = View.VISIBLE
            binding.profilTakibiBirakTextview.visibility = View.GONE
            binding.profilTakipEtTextview.isClickable = false
            return
        }

        if (_binding == null || !isAdded) return

        database.getReference("kullanicilar")
            .child(mevcutKullaniciId).child("takipEdilenler").child(profilSahibiId)
            .get()
            .addOnSuccessListener { dataSnapshot ->

                if (_binding == null || !isAdded){}

                binding.profilTakipEtTextview.isClickable = true
                binding.profilTakibiBirakTextview.isClickable = true

                if (dataSnapshot.exists())
                {
                    // --- KULLANICI TAKİP EDİYOR ---
                    binding.profilTakipEtTextview.visibility = View.GONE
                    binding.profilTakibiBirakTextview.visibility = View.VISIBLE
                    binding.profilTakibiBirakTextview.text = "Takibi Bırak"
                    binding.profilMenuyegitButton.visibility = View.VISIBLE
                    // Takibi Bırak listener'ı ata
                    binding.profilTakibiBirakTextview.setOnClickListener {
                        takibiBirak(profilSahibiId)
                    }
                }
                else
                {
                    // --- KULLANICI TAKİP ETMİYOR ---
                    binding.profilTakibiBirakTextview.visibility = View.GONE
                    binding.profilTakipEtTextview.visibility = View.VISIBLE
                    binding.profilTakipEtTextview.text = "Takip Et"
                    binding.profilMenuyegitButton.visibility = View.INVISIBLE
                    // Takip Et listener'ı ata
                    binding.profilTakipEtTextview.setOnClickListener {
                        takipEt(profilSahibiId, profilSahibiTamAd)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TakipKontrol", "Takip durumu kontrol hatası: ${e.message}")
                Toast.makeText(requireContext(), "Takip durumu kontrol edilemedi", Toast.LENGTH_SHORT).show()
                // Fragment görünümü hala bağlı mı kontrolü
                if (_binding == null || !isAdded){}

                binding.profilTakipEtTextview.text = "Hata"
                binding.profilTakipEtTextview.visibility = View.VISIBLE
                binding.profilTakibiBirakTextview.visibility = View.GONE
                binding.profilTakipEtTextview.isClickable = false
            }

        binding.profilMenuyegitButton.setOnClickListener {

            if (_binding == null || !isAdded || activity == null){}


            val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("profilSahibiId", profilSahibiId)
            editor.apply()


            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, Menu())
                .addToBackStack(null)
                .commitAllowingStateLoss() // veya commit()
        }
    }

    // Bottom Navigation Kurulumu
    private fun setupBottomNavigation() {
        // Fragment görünümü hala bağlı mı kontrolü
        if (_binding == null) return

        binding.profilBottomNavigationView.selectedItemId = R.id.bn_bar_profil
        binding.profilBottomNavigationView.setOnItemSelectedListener {
            // Fragment görünümü hala bağlı mı kontrolü
            if (_binding == null || !isAdded || activity == null) return@setOnItemSelectedListener false

            when (it.itemId) {
                R.id.bn_bar_anasayfa -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, AnaSayfa())
                        .addToBackStack(null)
                        .commitAllowingStateLoss() // veya commit()
                    true
                }
                R.id.bn_bar_menu -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, Menu())
                        .addToBackStack(null)
                        .commitAllowingStateLoss() // veya commit()
                    true
                }
                R.id.bn_bar_profil -> {
                    // Zaten profil sayfasında, bir şey yapmaya gerek yok
                    true
                }
                else -> false
            }
        }
    }


    override fun onDestroyView()
    {
        super.onDestroyView()

        val currentProfileId = arguments?.getString("takipci_id") ?: auth.currentUser?.uid // Hangi profilin ID'si kullanılıyorsa onu al

        if (currentProfileId != null)
        {
            takipcilerListener?.let {

                database.getReference().child("kullanicilar").child(currentProfileId).child("takipciler").removeEventListener(it)
                Log.d("Profil", "$currentProfileId takipcilerListener kaldırıldı")
            }
            takipedilenlerListener?.let {

                database.getReference().child("kullanicilar").child(currentProfileId).child("takipEdilenler").removeEventListener(it)
                Log.d("Profil", "$currentProfileId takipedilenlerListener kaldırıldı")
            }
        }
        else
        {
            Log.w("Profil", "onDestroyView: Profil ID bulunamadığı için dinleyiciler kaldırılamadı.")
        }


        _binding = null
        Log.d("Profil", "onDestroyView: _binding null yapıldı")
    }
}