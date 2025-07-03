package com.exemple.appfog.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentConfBinding
import com.exemple.appfog.databinding.FragmentHistBinding
import com.exemple.appfog.util.setBackAction
import com.google.firebase.auth.FirebaseAuth


class ConfFragment : Fragment() {


    private var _binding: FragmentConfBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Forçar reload do usuário para garantir dados atualizados
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
            val user = FirebaseAuth.getInstance().currentUser

            val nome = user?.displayName ?: "Nome não disponível"
            val email = user?.email ?: "Email não disponível"

            binding.nomeUser.text = nome
            binding.emailUser.text = email
        }
        // Chamando a função de extensão
        binding.voltar.setBackAction(this)



        initConf()
    }

    private fun initConf() {
        binding.buttonConf.setOnClickListener {
            initConf()
        }
        binding.buttonHome.setOnClickListener {
            findNavController().navigate(R.id.action_confFragment_to_homeFragment22)
        }

        binding.buttonHist.setOnClickListener {
            findNavController().navigate(R.id.action_confFragment_to_histFragment)
        }

        binding.btSobre.setOnClickListener {
            findNavController().navigate(R.id.action_confFragment_to_infoFragment2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    
}