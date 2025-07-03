package com.exemple.appfog.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentHomeBinding
import com.exemple.appfog.databinding.FragmentLoginBinding
import com.exemple.appfog.util.showBottomSheet
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlin.toString


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
    }

    private fun initListener(){
        binding.BTLogin.setOnClickListener {
            validateData()
        }

        binding.imageButton4.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment2_to_infoFragment2)
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment2_to_registerFragment)
        }
    }
    private fun validateData() {
        val email = binding.EDEmail.text.toString().trim()
        val senha = binding.EDSenha.text.toString().trim()

        when {
            email.isBlank() -> {
                showBottomSheet(message = getString(R.string.email_empty))
            }
            senha.isBlank() -> {
                showBottomSheet(message = getString(R.string.password_empty))
            }
            else -> {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { authentication ->
                        if (authentication.isSuccessful) {
                            findNavController().navigate(R.id.action_loginFragment2_to_homeFragment2)
                        } else {
                            showBottomSheet(message = "Erro ao fazer login.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        val mensagemErro = when (exception) {
                            is FirebaseAuthWeakPasswordException ->
                                getString(R.string.password_short)
                            is FirebaseAuthInvalidCredentialsException ->
                                "Email ou senha inválidos."
                            is FirebaseAuthUserCollisionException ->
                                getString(R.string.account_already_registered)
                            is FirebaseNetworkException ->
                                "Sem conexão com a internet."
                            else -> "Erro ao fazer login."
                        }

                        // Exibir a mensagem de erro corretamente
                        showBottomSheet(message = mensagemErro)
                    }

            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}