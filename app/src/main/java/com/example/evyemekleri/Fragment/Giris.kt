package com.example.evyemekleri.Fragment


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.FragmentGirisBinding
import com.google.firebase.auth.FirebaseAuth


class Giris : Fragment()
{

    private var _binding: FragmentGirisBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("KullaniciBilgileri", Context.MODE_PRIVATE)
        val eposta = sharedPreferences.getString("eposta", "")
        val parola = sharedPreferences.getString("parola", "")

        if (!eposta.isNullOrEmpty() && !parola.isNullOrEmpty())
        {
            binding.girisEpostaEditText.setText(eposta)
            binding.girisSifreEditText.setText(parola)
        }

        binding.girisGirisyapButton.setOnClickListener {
            val eposta = binding.girisEpostaEditText.text.toString().trim()
            val parola = binding.girisSifreEditText.text.toString().trim()

            if (eposta.isEmpty() || parola.isEmpty())
            {
                Toast.makeText(requireContext(), "E-posta veya şifre boş olamaz!", Toast.LENGTH_SHORT).show()
            }
            else
            {

                auth.signInWithEmailAndPassword(eposta, parola)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                        {
                            // Giriş bilgilerini kaydet
                            val editor = sharedPreferences.edit()
                            editor.putString("eposta", eposta)
                            editor.putString("parola", parola)
                            editor.apply()

                            anaSayfayaYonlendir()
                            Toast.makeText(requireContext(), "Giriş başarılı: ${auth.currentUser?.email}", Toast.LENGTH_SHORT).show()
                        }
                        else
                        {
                            Toast.makeText(requireContext(), "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.girisUyeolTextView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, UyeOl())
                .addToBackStack(null)
                .commit()
        }
    }


    private fun anaSayfayaYonlendir()
    {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, AnaSayfa())
            .commit()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}