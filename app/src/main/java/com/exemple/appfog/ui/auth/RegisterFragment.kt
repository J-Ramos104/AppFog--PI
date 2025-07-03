package com.exemple.appfog.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentRegisterBinding
import com.exemple.appfog.util.setBackAction
import com.exemple.appfog.util.showBottomSheet
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.BTVoltar.setBackAction(this)


        initRegister()
    }

    private fun initRegister() {
        binding.BTRegister.setOnClickListener {
            ValidaRegister()
        }

    }
    private fun ValidaRegister() {
        val email = binding.EDEmail.text.toString().trim()
        val senha = binding.EDSenha.text.toString().trim()
        val usuario = binding.EDUsuario.text.toString().trim()
        val confSenha = binding.EDConfSenha.text.toString().trim()

        // Validação básica dos campos
        when {
            usuario.isBlank() -> showBottomSheet(message = getString(R.string.usuario_empty))
            email.isBlank() -> showBottomSheet(message = getString(R.string.email_empty))
            senha.length < 6 -> showBottomSheet(message = getString(R.string.password_short))
            senha.isBlank() -> showBottomSheet(message = getString(R.string.password_empty))
            confSenha.isBlank() -> showBottomSheet(message = getString(R.string.confirm_senha_empty))
            senha != confSenha -> showBottomSheet(message = getString(R.string.password_different))
            else -> {
                // Todos os campos válidos, criar usuário no Firebase
                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(usuario)
                                .build()

                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        android.util.Log.d("Firebase", "Nome do usuário atualizado com sucesso.")

                                        Toast.makeText(
                                            requireContext(),
                                            "Usuário cadastrado com sucesso!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Limpar campos só após o update do perfil
                                        binding.EDEmail.setText("")
                                        binding.EDSenha.setText("")
                                        binding.EDUsuario.setText("")
                                        binding.EDConfSenha.setText("")
                                    } else {
                                        // Se falhar ao atualizar nome, mostra erro
                                        showBottomSheet(message = "Erro ao atualizar nome do usuário.")
                                    }
                                }
                        } else {
                            // Caso falha no cadastro, trata erro aqui
                            val exception = task.exception
                            val mensagemErro = when (exception) {
                                is FirebaseAuthWeakPasswordException ->
                                    getString(R.string.password_short)
                                is FirebaseAuthInvalidCredentialsException ->
                                    "Digite um email válido."
                                is FirebaseAuthUserCollisionException ->
                                    getString(R.string.account_already_registered)
                                is FirebaseNetworkException ->
                                    "Sem conexão com a internet."
                                else -> "Erro ao cadastrar usuário."
                            }
                            showBottomSheet(message = mensagemErro)
                        }
                    }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}