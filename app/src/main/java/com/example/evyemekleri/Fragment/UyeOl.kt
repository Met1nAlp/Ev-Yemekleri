package com.example.evyemekleri.Fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.FragmentUyeOlBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream


class UyeOl : Fragment() {

    private var _binding: FragmentUyeOlBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference



    private lateinit var permissionLauncher     : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private var secilenGorsel : Uri? = null
    private var secilenBitmap : Bitmap? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUyeOlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()

        binding.uyeolImageView.setOnClickListener {
            goselSec()
        }

        binding.uyeolButton.setOnClickListener {
            val isim = binding.uyeolIsimEditText.text.toString().trim()
            val soyisim = binding.uyeolSoyisimEditText.text.toString().trim()
            val eposta = binding.uyeolEpostaEditText.text.toString().trim()
            val parola = binding.uyeolParolaEditText.text.toString().trim()
            val parolatekrar = binding.uyeolParolatekrarEditText.text.toString().trim()

            if (eposta.isEmpty() || parola.isEmpty() || parolatekrar.isEmpty()) {
                Toast.makeText(requireContext(), "E-posta veya şifre boş olamaz!", Toast.LENGTH_SHORT).show()
            } else if (parola != parolatekrar) {
                Toast.makeText(requireContext(), "Şifreler eşleşmiyor!", Toast.LENGTH_SHORT).show()
            } else {
                KullaniciOlustur(isim, soyisim, eposta, parola)
            }
        }
    }

    private fun KullaniciOlustur(isim: String, soyisim: String, eposta: String, parola: String )
    {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        var gorselBase64: String? = null
        if (secilenBitmap != null) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                secilenBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                gorselBase64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Görsel işleme hatası: ${e.message}", Toast.LENGTH_LONG).show()
                return
            }
        }

        auth.createUserWithEmailAndPassword(eposta, parola)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful)
                {
                    val kullanici = auth.currentUser
                    val kullaniciAdi = "$isim$soyisim".replace("\\s+".toRegex(), "").lowercase()

                    val kullaniciid = kullanici?.uid

                    val kullaniciBilgileri = mapOf(
                        "ad" to isim,
                        "soyad" to soyisim,
                        "email" to eposta,
                        "tamAd" to "$isim $soyisim",
                        "fotograf" to gorselBase64
                    )

                    database.child("kullanicilar").child(kullaniciid!!).child("KullanıcıBilgileri").setValue(kullaniciBilgileri)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainerView, Giris())
                                .addToBackStack(null)
                                .commit()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Kayıt başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                    val sharedPreferences = requireContext().getSharedPreferences("KullaniciBilgileri", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("eposta", eposta)
                    editor.putString("parola", parola)
                    editor.apply()
                } else {
                    Toast.makeText(requireContext(), "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goselSec()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(requireContext() , Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity() , Manifest.permission.READ_MEDIA_IMAGES))
                {
                    // izini tekrar ister

                    view?.let {

                        Snackbar.make(it,"Galeriye ulaşıp görsel seçilmeli" , Snackbar.LENGTH_INDEFINITE).setAction(
                            "izin ver" ,
                            View.OnClickListener
                            {
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }
                        ).show()
                    }
                }
                else
                {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else
            {
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery) ;

            }
        }
        else
        {
            if (ContextCompat.checkSelfPermission(requireContext() , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity() , Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    // izini tekrar ister

                    view?.let {

                        Snackbar.make(it,"Galeriye ulaşıp görsel seçilmeli" , Snackbar.LENGTH_INDEFINITE).setAction(
                            "izin ver" ,
                            View.OnClickListener
                            {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        ).show()
                    }
                }
                else
                {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else
            {
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery) ;

            }
        }
    }

    private fun registerLauncher()
    {
        activityResultLauncher =registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->

            if (result.resultCode == AppCompatActivity.RESULT_OK)
            {
                val intentFromResult = result.data

                if (intentFromResult != null)
                {
                    secilenGorsel = intentFromResult.data


                    try
                    {
                        if (Build.VERSION.SDK_INT >= 28)
                        {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver , secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.uyeolImageView.setImageBitmap(secilenBitmap)
                        }
                        else
                        {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver , secilenGorsel)
                            binding.uyeolImageView.setImageBitmap(secilenBitmap)
                        }
                    }
                    catch (e: Exception)
                    {
                        println( e.localizedMessage )
                    }
                }
            }

        }


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission() )
        { result ->

            if (result)
            {
                //izin verildi

                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery) ;
            }
            else
            {
                Toast.makeText(requireContext() , "İzin verilmedi" , Toast.LENGTH_SHORT).show()
            }


        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
