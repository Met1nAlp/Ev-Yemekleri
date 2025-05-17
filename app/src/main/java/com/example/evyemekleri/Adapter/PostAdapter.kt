package com.example.evyemekleri.Adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.evyemekleri.Model.Post
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.RecyclerRowPostBinding
import com.example.yemekvetarifleri.Fragment.Detay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values

class PostAdapter (val context: Context , var postListesi : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>()
{
    class PostHolder(val binding : RecyclerRowPostBinding) : RecyclerView.ViewHolder(binding.root)
    {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder
    {
        val binding : RecyclerRowPostBinding = RecyclerRowPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PostHolder(binding)
    }

    override fun getItemCount(): Int
    {
        return postListesi.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int)
    {
        val yemek = postListesi[position]
        val auth = FirebaseAuth.getInstance()
        val kullaniciid = auth.currentUser?.uid
        val db = FirebaseDatabase.getInstance()


        holder.binding.gonderenTextview.text = yemek.gonderenadi
        holder.binding.yemekadiTextview.text = yemek.yemekadi

        val myRef = db.getReference("kullanicilar")
            .child(yemek.gonderenid!!)
            .child("menu")
            .child(yemek.yemektur)
            .child(yemek.yemekadi)

        holder.binding.anasayfaBegenImageView.setOnClickListener {


            val begenirengi = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.kirmizi))

            holder.binding.anasayfaBegenImageView.imageTintList = begenirengi

            val kisiadiRef = db.getReference("kullanicilar")
                .child(kullaniciid!!)
                .child("KullanıcıBilgileri")
                .child("tamAd")

            kisiadiRef.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val tamAd = snapshot.getValue(String::class.java)

                    myRef.child("begenenler")
                        .child(kullaniciid!!)
                        .setValue(tamAd)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Failed to read tamAd value: ${error.message}")
                }
            })


        }

        holder.binding.anasayfaYorumyapImageView.setOnClickListener {

            holder.binding.anasayfaYorumyapLinearlayout.visibility = android.view.View.VISIBLE

            val yorumrengi = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.colorAccentDark))
            val eskirenk = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.mavibirrenk))

            holder.binding.anasayfaYorumyapImageView.imageTintList = yorumrengi

            holder.binding.anasayfaYorumukkaydetButton.setOnClickListener {
                val yorum = holder.binding.anasayfaYorumTextView.text.toString()

                val sonuc = YorumDenetleyici.yorumDenetle(yorum)

                if(sonuc.first == true)
                {
                    myRef.child("yorumlar")
                        .child(kullaniciid!!)
                        .setValue(yorum)
                        .addOnSuccessListener {

                            Toast.makeText(this.context, "Yorum yapma işlemi başarılı.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {

                            Toast.makeText(this.context, "Yorum yapma işlemi başarısız.", Toast.LENGTH_SHORT).show()
                        }
                }
                else
                {
                    Toast.makeText(this.context, "Uygunsuz kelime bulundu: ${sonuc.second}", Toast.LENGTH_SHORT).show()
                }

                holder.binding.anasayfaYorumyapLinearlayout.visibility = android.view.View.GONE
                holder.binding.anasayfaYorumTextView.text.clear()
                holder.binding.anasayfaYorumyapImageView.imageTintList = eskirenk

            }
        }





        val gorselBase64 = yemek.gorsel
        if (!gorselBase64.isNullOrEmpty())
        {
            try
            {
                val imageBytes = Base64.decode(gorselBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.postImageView.setImageBitmap(bitmap)
                holder.binding.postImageView.visibility = android.view.View.VISIBLE
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                holder.binding.postImageView.visibility = android.view.View.GONE
            }
        }
        else
        {
            holder.binding.postImageView.visibility = android.view.View.GONE
        }


        val gorselBase65 = yemek.gonderenfotograf
        if (!gorselBase65.isNullOrEmpty())
        {
            try
            {
                val imageBytes = Base64.decode(gorselBase65, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.gonderenfotografImageView.setImageBitmap(bitmap)
                holder.binding.gonderenfotografImageView.visibility = android.view.View.VISIBLE
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                holder.binding.gonderenfotografImageView.visibility = android.view.View.GONE
            }
        }
        else
        {
            holder.binding.gonderenfotografImageView.visibility = android.view.View.GONE
        }

        val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("menuAdi", yemek.yemektur)
                putString("adi", yemek.yemekadi)
            }

            val sharedPreferences = holder.itemView.context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("profilSahibiId", yemek.gonderenid)
            editor.apply()

            val fragment = Detay()
            fragment.arguments = bundle

            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    fun updateList(newList: ArrayList<Post>)
    {
        postListesi = newList
        notifyDataSetChanged()
    }

    object YorumDenetleyici {
        private val karaListe = arrayListOf(
            // Hakaret ve Küfür
            "aptal", "salak", "moron", "geri zekalı", "beyinsiz", "mal", "şerefsiz",
            "alçak", "kaltak", "orospu", "piç", "kahpe", "siktir", "lanet", "boku",
            "zavallı", "rezil", "iğrenç", "pislik", "ahmak", "budala", "dangalak",
            "şapşal", "hıyar", "gavat", "keriz", "enayi", "hödük", "bitch Pulse", "fuck",
            "ananı", "sikeyim", "sikerim", "boktan", "götveren", "ibne", "pezevenk",
            "puşt", "siktirgit", "ananı avradını", "göt lalesi", "sikik", "kancık",
            "yavşak herif", "boku yemiş", "şerefsiz herif", "allahsız",

            // Agresif ve Tehdit İçeren İfadeler
            "seni döverim", "gebertirim", "öldürürüm", "sus lan", "kes sesini",
            "haddini bil", "terbiyesiz", "ağzını topla", "yakarım", "mahvederim",
            "seni bitiririm", "ezik", "kafanı kırarım", "sana gününü gösteririm",
            "belanı veririm", "yüzüne tükürürüm", "sana haddini bildiririm",
            "adam ol", "kafayı yersin", "sana fena yaparım", "seni harcarım",
            "kafana sıkarım", "sana dünyayı dar ederim", "ölücen lan", "süründürürüm",
            "ananı ağlatırım", "bittin sen", "gözünü patlatırım",

            // Ayrımcı ve Nefret Dili
            "ırkçı", "faşist", "yobaz", "dinci", "terörist", "geri kalmış", "cahil",
            "köylü", "zenci", "çingene", "arap", "kürt", "yahudi", "ermeni",
            "gavur", "homofobik", "lezbo", "top", "buzerant", "engelli", "özürlü",
            "çomar", "keko", "maganda", "ayak takımı", "iğrenç herif", "pis herif",
            "lgbt karşıtı", "dinsiz", "ateist köpek",

            // Cinsel İçerikli İfadeler (Genişletilmiş, İngilizce Klavye Varyasyonları Dahil)
            "seks", "porno", "erotik", "çıplak", "sikiş", "sikis", "am", "amcik",
            "yarrak", "yarak", "göt", "got", "meme", "sik", "amcık", "taşak", "tasak",
            "zikiş", "zikis", "fuck", "penis", "vajina", "sperm", "orgazm", "sevişme",
            "sevisme", "fışkırt", "fiskirt", "anal", "oral", "mastürbasyon",
            "masturbasyon", "döl", "dol", "zıbar", "zibar", "sikmek", "göt deliği",
            "got deligi", "sikişme", "sikisime", "kızlığını bozarım", "kizligini bozarim",
            "sokarım", "sokarim", "yalarım", "yalarim", "boşalma", "bosalma",
            "sekreter fantezisi", "yarragimi", "yarağımı", "yaragimi", "sikimi",
            "götümü", "gotumu", "amımı", "amimi", "yarak", "yarrag", "sikim", "götüm",
            "gotum", "amım", "amim", "sokuş", "sokus", "yalaş", "yalas", "zıplat",
            "ziplat", "siki tutuş", "siki tutus", "götüne girsin", "gotune girsin",
            "amına koyayım", "amina koyayim", "sikin üstünde", "sikin ustunde",
            "yarağa doy", "yaraga doy", "götünü siktir", "gotunu siktir", "amdan",
            "yarraktan", "yaraktan", "sikten", "götten", "gotten", "siki yala",
            "yarak yala", "am yala", "göt yala", "got yala", "sik sok", "yarak sok",
            "amına girsin", "amina girsin", "götüne sokayım", "gotune sokayim",
            "sikişelim", "sikisilim", "yarrak gibi", "yarak gibi", "am gibi",
            "göt gibi", "got gibi", "sikerler", "siktirirler", "yarağın dibi",
            "yaragin dibi", "sikin kökü", "amın feri", "amin feri", "götün başı",
            "gotun basi", "sike sike", "yarağı yedi", "yaragi yedi", "amdan sikiş",
            "amdan sikis", "göte sok", "gote sok", "amcık sik", "amcik sik",
            "yarrak yedi", "yarak yedi", "sikin dibi", "sikilmek", "siktirmek",
            "yarraga otur", "yaraga otur", "amına sok", "amina sok", "götüne siktir",
            "gotune siktir", "siki kalkmış", "siki kalkmis", "yarağı kalkmış",
            "yaragi kalkmis", "amın ortası", "amin ortasi", "götün dibi", "gotun dibi",

            // Genel Saygısızlık
            "saçma", "boş yapma", "ne saçmalıyorsun", "kapa çeneni", "işine bak",
            "defol", "salla", "umrumda değil", "ne lan", "bana ne", "yavşak",
            "kro", "eziksin", "loser", "salakça", "gereksiz", "aptalca",
            "ne bok yersen ye", "sana mı sorcam", "geç bunları", "yeter be",
            "ne boş adamsın", "sallapati", "adi herif", "ne kasıyorsun",
            "kendini bi bok sanma", "boş boş konuşma", "gıcık herif", "ne ukalasın",
            "kelleci fir fir" , "kelleci fır fır", "kürt"
        )

        // Yorum denetleme fonksiyonu
        fun yorumDenetle(yorum: String): Pair<Boolean, String>
        {
            for (kelime in karaListe)
            {
                if (yorum.lowercase().contains(kelime.lowercase()))
                {
                    return Pair(false, "Yorumda uygunsuz kelime bulundu: $kelime")
                }
            }
            return Pair(true, "Yorum uygun.")
        }
    }

}