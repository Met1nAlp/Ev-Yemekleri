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
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.FragmentEklemeVeGoruntulemeBinding
import com.example.evyemekleri.Model.*
import com.example.evyemekleri.Adapter.*
import com.example.yemekvetarifleri.Fragment.Detay
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.IOException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EklemeVeGoruntuleme : Fragment()
{
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel: Uri? = null
    private var secilenBitmap: Bitmap? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var imageView: ImageView

    private lateinit var _binding: FragmentEklemeVeGoruntulemeBinding
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var yemekAdiInput: TextInputLayout
    private lateinit var malzemelerInput: TextInputLayout
    private lateinit var tarifInput: TextInputLayout
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        _binding = FragmentEklemeVeGoruntulemeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val bundle = arguments
        val menuAdi = bundle?.getString("menuAdi")

        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        val profilSahibiId = sharedPreferences.getString("profilSahibiId", null)

        val YemekListesi = ArrayList<Yemek>()
        val MesrubatListesi = ArrayList<Mesrubat>()
        val TatliListesi = ArrayList<Tatli>()
        val CorbaListesi = ArrayList<Corba>()
        val MezeListesi = ArrayList<Meze>()

        val kullanici = auth.currentUser
        val kullaniciId = kullanici?.uid

        val db = FirebaseDatabase.getInstance()
        val myRef = db.getReference("kullanicilar")

        if (profilSahibiId == null)
        {
            when(arguments?.getString("menuAdi"))
            {
                "Yemek" ->
                {
                    var yemekismi : String
                    var yemekmalzeme : String
                    var yemektarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == kullaniciId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key

                                        println(yemekAdi)

                                        yemekismi = childSnapshot.child("yemekAdi").value.toString()
                                        yemekmalzeme = childSnapshot.child("malzemeler").value.toString()
                                        yemektarif = childSnapshot.child("tarif").value.toString()

                                        YemekListesi.add(Yemek( yemekismi , yemekmalzeme , yemektarif , menuAdi ))
                                    }

                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = YemekAdapter(requireContext(), YemekListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
                "Meşrubat" ->
                {
                    var mesrubatismi : String
                    var mesrubatzeme : String
                    var mesrubattarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == kullaniciId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key

                                        mesrubatismi = childSnapshot.child("yemekAdi").value.toString()
                                        mesrubatzeme = childSnapshot.child("malzemeler").value.toString()
                                        mesrubattarif = childSnapshot.child("tarif").value.toString()

                                        MesrubatListesi.add(Mesrubat( mesrubatismi , mesrubatzeme , mesrubattarif , menuAdi ))
                                    }
                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = MesrubatAdapter(requireContext(), MesrubatListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
                "Tatlı" ->
                {
                    var tatliismi : String
                    var tatlizeme : String
                    var tatlitarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == kullaniciId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key

                                        tatliismi = childSnapshot.child("yemekAdi").value.toString()
                                        tatlizeme = childSnapshot.child("malzemeler").value.toString()
                                        tatlitarif = childSnapshot.child("tarif").value.toString()


                                        TatliListesi.add(Tatli( tatliismi , tatlizeme , tatlitarif , menuAdi ))
                                    }

                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = TatliAdapter(requireContext(), TatliListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
                "Çorba" ->
                {
                    var corbaismi : String
                    var corbazeme : String
                    var corbatarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == kullaniciId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key

                                        corbaismi = childSnapshot.child("yemekAdi").value.toString()
                                        corbazeme = childSnapshot.child("malzemeler").value.toString()
                                        corbatarif = childSnapshot.child("tarif").value.toString()

                                        CorbaListesi.add(Corba( corbaismi , corbazeme , corbatarif , menuAdi ))
                                    }

                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = CorbaAdapter(requireContext(), CorbaListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
                "Meze" ->
                {
                    var mezeismi : String
                    var mezemalzeme : String
                    var mezetarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == kullaniciId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key

                                        mezeismi = childSnapshot.child("yemekAdi").value.toString()
                                        mezemalzeme = childSnapshot.child("malzemeler").value.toString()
                                        mezetarif = childSnapshot.child("tarif").value.toString()

                                        MezeListesi.add(Meze( mezeismi , mezemalzeme , mezetarif , menuAdi ))
                                    }

                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = MezeAdapter(requireContext(), MezeListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
            }
        }
        else
        {
            when(arguments?.getString("menuAdi"))
            {
                "Yemek" ->
                {
                    var yemekismi : String
                    var yemekmalzeme : String
                    var yemektarif : String


                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == profilSahibiId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key


                                        yemekismi = childSnapshot.child("yemekAdi").value.toString()
                                        yemekmalzeme = childSnapshot.child("malzemeler").value.toString()
                                        yemektarif = childSnapshot.child("tarif").value.toString()


                                        YemekListesi.add(Yemek( yemekismi , yemekmalzeme , yemektarif , menuAdi ))
                                    }

                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = YemekAdapter(requireContext(), YemekListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
                "Meşrubat" ->
                {
                    var mesrubatismi : String
                    var mesrubatzeme : String
                    var mesrubattarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == profilSahibiId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key

                                        mesrubatismi = childSnapshot.child("yemekAdi").value.toString()
                                        mesrubatzeme = childSnapshot.child("malzemeler").value.toString()
                                        mesrubattarif = childSnapshot.child("tarif").value.toString()

                                        MesrubatListesi.add(Mesrubat( mesrubatismi , mesrubatzeme , mesrubattarif , menuAdi ))
                                    }
                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = MesrubatAdapter(requireContext(), MesrubatListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
                "Tatlı" ->
                {
                    var tatliismi : String
                    var tatlizeme : String
                    var tatlitarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == profilSahibiId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {

                                        tatliismi = childSnapshot.child("yemekAdi").value.toString()
                                        tatlizeme = childSnapshot.child("malzemeler").value.toString()
                                        tatlitarif = childSnapshot.child("tarif").value.toString()


                                        TatliListesi.add(Tatli( tatliismi , tatlizeme , tatlitarif , menuAdi ))
                                    }

                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = TatliAdapter(requireContext(), TatliListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
                "Çorba" ->
                {
                    var corbaismi : String
                    var corbazeme : String
                    var corbatarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == profilSahibiId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key

                                        corbaismi = childSnapshot.child("yemekAdi").value.toString()
                                        corbazeme = childSnapshot.child("malzemeler").value.toString()
                                        corbatarif = childSnapshot.child("tarif").value.toString()

                                        CorbaListesi.add(Corba( corbaismi , corbazeme , corbatarif , menuAdi ))
                                    }

                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = CorbaAdapter(requireContext(), CorbaListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
                "Meze" ->
                {
                    var mezeismi : String
                    var mezemalzeme : String
                    var mezetarif : String

                    myRef.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {
                            for (kullaniciSnapshot in snapshot.children)
                            {
                                if (kullaniciSnapshot.key == profilSahibiId)
                                {
                                    val yemekRef = kullaniciSnapshot.child("menu").child(menuAdi!!)

                                    for (childSnapshot in yemekRef.children)
                                    {
                                        val yemekAdi = childSnapshot.key

                                        mezeismi = childSnapshot.child("yemekAdi").value.toString()
                                        mezemalzeme = childSnapshot.child("malzemeler").value.toString()
                                        mezetarif = childSnapshot.child("tarif").value.toString()

                                        MezeListesi.add(Meze( mezeismi , mezemalzeme , mezetarif , menuAdi ))
                                    }

                                    binding.eklemeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.eklemeRecyclerView.adapter = MezeAdapter(requireContext(), MezeListesi)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError)
                        {
                            println("Hata : ${error.message}")
                        }
                    })
                }
            }
        }

        binding.eklemeFloatingActionButton.setOnClickListener {
            if (binding.eklemeRecyclerView.visibility == View.VISIBLE) {
                // Eğer RecyclerView görünürse, yeni yemek ekleme formunu göster
                val container = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(32, 16, 32, 16)
                    }
                }

                val menuAdi = arguments?.getString("menuAdi")

                imageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        300,
                        300
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                        setMargins(0, 16, 0, 16)
                    }

                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(android.R.drawable.ic_menu_camera)
                    setBackgroundResource(android.R.drawable.picture_frame)
                }

                imageView.setOnClickListener {
                    goselSec()
                }

                container.addView(imageView)

                yemekAdiInput = createTextInput("$menuAdi Adı")
                container.addView(yemekAdiInput)

                malzemelerInput = createTextInput("Malzemeler")
                container.addView(malzemelerInput)

                tarifInput = createTextInput("Tarif")
                container.addView(tarifInput)

                binding.eklemeRecyclerView.visibility = View.GONE
                (view as ViewGroup).addView(container)

                binding.eklemeFloatingActionButton.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_save)
                )
            } else {
                // Eğer form görünürse, kaydetme işlemini yap
                yemekKaydet()
            }
        }
    }

    private fun yemekKaydet()
    {
        val currentUser = auth.currentUser

        val yemekAdi = (yemekAdiInput.editText?.text ?: "").toString().trim()
        val malzemeler = (malzemelerInput.editText?.text ?: "").toString().trim()
        val tarif = (tarifInput.editText?.text ?: "").toString().trim()

        if (yemekAdi.isEmpty() || malzemeler.isEmpty() || tarif.isEmpty())
        {
            Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun", Toast.LENGTH_LONG).show()
            return
        }

        if (currentUser == null)
        {
            Toast.makeText(requireContext(), "Lütfen önce giriş yapın", Toast.LENGTH_LONG).show()
            return
        }

        val kullaniciid = currentUser.uid
        val menuAdi = arguments?.getString("menuAdi")

        // Görsel varsa Base64'e çevir
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

        // Yemek verilerini hazırla
        val yemekVerileri = hashMapOf(
            "yemekAdi" to yemekAdi,
            "malzemeler" to malzemeler,
            "tarif" to tarif,
            "kullaniciId" to kullaniciid,
            "kullaniciEmail" to currentUser.email,
            "eklemeTarihi" to System.currentTimeMillis(),
            "gorsel" to gorselBase64
        )

        database
            .child("kullanicilar")
            .child(kullaniciid)
            .child("menu")
            .child(menuAdi!!)
            .child(yemekAdi)
            .setValue(yemekVerileri)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Yemek başarıyla eklendi", Toast.LENGTH_LONG).show()

                val bundle = Bundle()
                bundle.putString("menuAdi", menuAdi)
                bundle.putString("adi", yemekAdi)
                bundle.putString("malzemeler", malzemeler)
                bundle.putString("tarif", tarif)
                bundle.putString("gorsel", gorselBase64)

                val detay = Detay()
                detay.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, Menu())
                    .addToBackStack(null)
                    .commit()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Kaydetme hatası: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }

    private fun createTextInput(hint: String): TextInputLayout {
        return TextInputLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }

            // TextInputLayout için özellikler
            setHint(hint)
            setHintTextAppearance(R.style.TextInputHintStyle)
            setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_FILLED)
            setBoxBackgroundColor(ContextCompat.getColor(requireContext(), R.color.input_background))
            setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.input_stroke))
            setBoxStrokeWidth(1)
            setBoxCornerRadii(12f, 12f, 12f, 12f)

            addView(EditText(context).apply {
                // this.hint = hint // BUNU KALDIRIYORUZ!
                setPadding(32, 60, 32, 16) // üst padding'i 32 yaparak yazıyı bir satır aşağıdan başlat
                setTextColor(ContextCompat.getColor(requireContext(), R.color.input_text))
                setHintTextColor(ContextCompat.getColor(requireContext(), R.color.input_hint))
                setTextSize(16f)
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                setBackgroundResource(0)
            })
        }
    }


    private fun goselSec()
    {
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            Manifest.permission.READ_MEDIA_IMAGES
        }
        else
        {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), readPermission) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), readPermission))
            {
                Snackbar.make(requireView(), "Galeriye erişim izni gerekli", Snackbar.LENGTH_INDEFINITE)
                    .setAction("İzin Ver")
                    {
                        permissionLauncher.launch(readPermission)
                    }.show()
            }
            else
            {
                permissionLauncher.launch(readPermission)
            }
        }
        else
        {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    private fun registerLauncher()
    {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK)
            {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    secilenGorsel = intentFromResult.data
                    try {
                        secilenGorsel?.let { uri ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                                secilenBitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                    decoder.isMutableRequired = true
                                }
                            } else {
                                @Suppress("DEPRECATION")
                                secilenBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                            }

                            imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Görsel işleme hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } catch (e: OutOfMemoryError) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Görsel çok büyük, lütfen daha küçük bir görsel seçin", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Beklenmeyen hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                activityResultLauncher.launch(intentToGallery)
            } else {
                Toast.makeText(requireContext(), "Galeriye erişim izni gerekli", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        secilenBitmap?.recycle()
        secilenBitmap = null
    }
}