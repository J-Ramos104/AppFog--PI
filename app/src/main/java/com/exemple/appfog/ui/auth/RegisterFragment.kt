package com.exemple.appfog.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentRegisterBinding
import com.exemple.appfog.util.setBackAction
import com.exemple.appfog.util.showBottomSheet
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase // Import para Firebase Realtime Database
import android.util.Log

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // Instâncias do Firebase
    private val auth = FirebaseAuth.getInstance()
    private lateinit var database: FirebaseDatabase // Instância do Firebase Realtime Database

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

        // Inicializa a instância do Firebase Realtime Database
        database = FirebaseDatabase.getInstance()

        initRegister()
    }

    private fun initRegister() {
        binding.BTRegister.setOnClickListener {
            validateRegisterData()
        }
    }

    private fun validateRegisterData() {
        val email = binding.EDEmail.text.toString().trim()
        val senha = binding.EDSenha.text.toString().trim()
        val usuario = binding.EDUsuario.text.toString().trim()
        val confSenha = binding.EDConfSenha.text.toString().trim()

        // Validação básica dos campos
        when {
            usuario.isBlank() -> showBottomSheet(message = getString(R.string.usuario_empty))
            email.isBlank() -> showBottomSheet(message = getString(R.string.email_empty))
            senha.isBlank() -> showBottomSheet(message = getString(R.string.password_empty))
            senha.length < 6 -> showBottomSheet(message = getString(R.string.password_short))
            confSenha.isBlank() -> showBottomSheet(message = getString(R.string.confirm_senha_empty))
            senha != confSenha -> showBottomSheet(message = getString(R.string.password_different))
            else -> {
                // Todos os campos válidos, criar usuário no Firebase Authentication
                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sucesso na criação do usuário no Firebase Authentication
                            val firebaseUser = auth.currentUser
                            firebaseUser?.let { user ->
                                val userId = user.uid
                                Log.d("RegisterFragment", "Usuário do Auth criado: ${user.email}, UID: $userId")

                                // 1. Opcional: Atualizar o displayName no Firebase Authentication
                                // Isso é útil para algumas ferramentas do Firebase, mas o Realtime Database será o local principal.
                                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(usuario)
                                    .build()

                                user.updateProfile(profileUpdates)
                                    .addOnCompleteListener { updateProfileTask ->
                                        if (updateProfileTask.isSuccessful) {
                                            Log.d("RegisterFragment", "Nome de exibição atualizado no Auth.")
                                        } else {
                                            Log.w("RegisterFragment", "Falha ao atualizar nome de exibição no Auth: ${updateProfileTask.exception?.message}")
                                            // Não mostramos erro grave aqui, pois a funcionalidade principal é salvar no DB
                                        }
                                    }

                                // 2. *Implementação do Firebase Realtime Database:*
                                // Salvar dados adicionais do usuário (como o nome) no nó 'usuarios' no Realtime Database
                                val userData = hashMapOf(
                                    "nome" to usuario,
                                    "email" to email,
                                    "dataCriacao" to System.currentTimeMillis() // Opcional: Adicionar timestamp de criação
                                    // Adicione outros campos de perfil aqui se necessário (ex: "fotoPerfil" = "url_da_foto")
                                )

                                // Define um nó no Realtime Database em 'usuarios/{userId}' com os dados do usuário
                                database.getReference("usuarios").child(userId).setValue(userData)
                                    .addOnSuccessListener {
                                        // Dados do usuário salvos com sucesso no Realtime Database
                                        Log.d("RegisterFragment", "Dados do usuário salvos no Realtime Database para UID: $userId")

                                        Toast.makeText(
                                            requireContext(),
                                            "Cadastro realizado com sucesso!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Limpar campos após o sucesso
                                        binding.EDEmail.setText("")
                                        binding.EDSenha.setText("")
                                        binding.EDUsuario.setText("")
                                        binding.EDConfSenha.setText("")

                                    }
                                    .addOnFailureListener { e ->
                                        // Erro ao salvar dados no Realtime Database
                                        Log.e("RegisterFragment", "Erro ao salvar dados no Realtime Database: ${e.message}", e)
                                        showBottomSheet(message = "Erro ao finalizar cadastro. Tente novamente.")
                                        // Opcional: Deslogar o usuário ou apagar a conta se a etapa do DB falhar criticamente
                                    }

                            } ?: run {
                                // Se firebaseUser for nulo (cenário raro após isSuccessful)
                                Log.e("RegisterFragment", "Usuário do Firebase é nulo após criação bem-sucedida.")
                                showBottomSheet(message = "Erro interno ao processar cadastro.")
                            }
                        } else {
                            // Caso falha na criação do usuário no Firebase Authentication
                            val exception = task.exception
                            val mensagemErro = when (exception) {
                                is FirebaseAuthWeakPasswordException ->
                                    getString(R.string.password_short)
                                is FirebaseAuthInvalidCredentialsException ->
                                    "O formato do email é inválido ou o email já está em uso."
                                is FirebaseAuthUserCollisionException ->
                                    getString(R.string.account_already_registered)
                                is FirebaseNetworkException ->
                                    "Sem conexão com a internet. Verifique sua conexão."
                                else -> "Erro desconhecido ao cadastrar usuário: ${exception?.message}"
                            }
                            Log.e("RegisterFragment", "Falha na criação do usuário do Auth: $mensagemErro", exception)
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