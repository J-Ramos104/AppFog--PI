package com.exemple.appfog.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentHomeBinding
import com.exemple.appfog.util.setBackAction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log
import androidx.core.content.ContextCompat
// Removidos: SimpleDateFormat e java.util.Date, pois não são mais necessários sem a "Última Atualização"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase   //ponto de entrada para interagir com o Realtime Database.
    private var homeDataListener: ValueEventListener? = null  //armazenar e verifica as mudanças nos dados do Firebase Realtime Database
    private var databaseRef: DatabaseReference? = null // armazena a referência ao local específico no banco de dados que o fragmento (configurou um canal de comunicação com o Firebase Realtime Database).

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.voltar.setBackAction(this)

        initHome()
        loadHomeData()
    }

    private fun initHome(){
        binding.buttonHome.setOnClickListener {
            Toast.makeText(requireContext(), "Você já está na tela inicial.", Toast.LENGTH_SHORT).show()
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

        binding.buttonEditarCasa.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidade de Editar Casa (Implementar)", Toast.LENGTH_SHORT).show()
        }

        binding.buttonDeletarCasa.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidade de Deletar Casa (Implementar)", Toast.LENGTH_SHORT).show()
        }
    }
    //Carregamento de Dados da Casa (loadHomeData())
    private fun loadHomeData() {
        val currentUser = auth.currentUser //Obtém o UID do usuário logado
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("HomeFragment", "Configurando listener para dados da casa do UID: $userId")
            // Define a referência do banco de dados para a casa do usuário(indica o caminho para os dados que queremos acessar)
            databaseRef = database.getReference("casas").child(userId)

            // Cria e anexa o ValueEventListener
            homeDataListener = object : ValueEventListener { //acionado para notificar o app sobre as mudanças nos dados ou para fornecer os dados iniciais.
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("HomeFragment", "Dados da casa recebidos do Realtime Database: ${snapshot.value}")

                        val nomeCasa = snapshot.child("nomeCasa").getValue(String::class.java) ?: "Casa Padrão"
                        val fumacaDetectada = snapshot.child("fumacaDetectada").getValue(Boolean::class.java) ?: false
                        val nivelFumaca = snapshot.child("nivelFumaca").getValue(String::class.java) ?: "Desconhecido"
                        val bombaAtivada = snapshot.child("bombaAtivada").getValue(Boolean::class.java) ?: false

                        // Get current sensor value
                        val currentSensorValue = snapshot.child("sensorData").child("current").child("value").getValue(Long::class.java)

                        binding.textViewNomeCasa.text = nomeCasa

                        // Logic for "Presença de Fumaça"
                        if (fumacaDetectada) {
                            binding.textViewStatusFumaca.text = "Detectada"
                            binding.textViewStatusFumaca.setBackgroundResource(R.drawable.rounded_background_red)
                        } else {
                            binding.textViewStatusFumaca.text = "Sem Fumaça"
                            binding.textViewStatusFumaca.setBackgroundResource(R.drawable.rounded_background_green)
                        }
                        binding.textViewStatusFumaca.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                        // Logic for "Nível da Fumaça"
                        binding.textViewValorNivelFumaca.text = nivelFumaca

                        // Logic for "Bomba de Água"
                        if (bombaAtivada) {
                            binding.textViewStatusBombaAgua.text = "Ligada"
                            binding.textViewStatusBombaAgua.setBackgroundResource(R.drawable.rounded_background_alert)
                        } else {
                            binding.textViewStatusBombaAgua.text = "Ideal"
                            binding.textViewStatusBombaAgua.setBackgroundResource(R.drawable.rounded_background_ideal)
                        }
                        binding.textViewStatusBombaAgua.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                        // Display current sensor reading (if available)
                        if (currentSensorValue != null) {
                            binding.textViewCurrentSensorValue.text = "Valor Atual: $currentSensorValue"
                        } else {
                            binding.textViewCurrentSensorValue.text = "Valor Atual: N/A"
                        }

                    } else {
                        Log.d("HomeFragment", "Nó da casa do usuário não existe ou está vazio. Exibindo valores padrão.")
                        binding.textViewNomeCasa.text = "Nenhuma Casa Registrada"

                        binding.textViewStatusFumaca.text = "N/A"
                        binding.textViewStatusFumaca.setBackgroundResource(R.drawable.rounded_background_gray)
                        binding.textViewStatusFumaca.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                        binding.textViewValorNivelFumaca.text = "N/A"
                        binding.textViewValorNivelFumaca.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

                        binding.textViewStatusBombaAgua.text = "N/A"
                        binding.textViewStatusBombaAgua.setBackgroundResource(R.drawable.rounded_background_gray)
                        binding.textViewStatusBombaAgua.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                        binding.textViewCurrentSensorValue.text = "Valor Atual: N/A"
                        binding.textViewCurrentSensorValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                        Toast.makeText(requireContext(), "Nenhuma casa encontrada. Registre uma!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("HomeFragment", "Erro ao ler dados da casa no Realtime Database: ${error.message}", error.toException())
                    Toast.makeText(requireContext(), "Erro ao carregar dados da casa: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }

            databaseRef?.addValueEventListener(homeDataListener as ValueEventListener)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (databaseRef != null && homeDataListener != null) {
            databaseRef?.removeEventListener(homeDataListener as ValueEventListener)
        }
        _binding = null
    }
}