package com.example.yemekvetarifleri.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.evyemekleri.Fragment.Giris
import com.example.evyemekleri.R
import com.example.evyemekleri.databinding.FragmentBaslangicBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Baslangic : Fragment()
{
    private var _binding: FragmentBaslangicBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBaslangicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            val toplamsure = 500L
            val guncelleme = 50L
            val adim = toplamsure / guncelleme

            var ilerleme = 0

            while (ilerleme < 100)
            {
                ilerleme = ((ilerleme + (100.0 / adim)).toInt()).coerceAtMost(100)
                binding.baslangicProgressBar.progress = ilerleme

                delay(guncelleme)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {

            delay(500)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, Giris())
                .addToBackStack(null)
                .commit()

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}