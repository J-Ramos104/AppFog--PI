package com.exemple.appfog.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentHomeBinding
import com.exemple.appfog.databinding.FragmentInfoBinding
import com.exemple.appfog.util.setBackAction


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Chamando a função de extensão
        binding.voltar.setBackAction(this)

        initHome()
    }

    private fun initHome(){
        binding.buttonHome.setOnClickListener {
            initHome()
        }
        binding.buttonHist.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment2_to_histFragment)
        }

        binding.buttonConf.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment2_to_confFragment)
        }
        binding.btAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment2_to_addFragment)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}