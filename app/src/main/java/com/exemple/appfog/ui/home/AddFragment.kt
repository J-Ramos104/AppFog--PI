package com.exemple.appfog.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentAddBinding
import com.exemple.appfog.util.setBackAction
import com.exemple.appfog.util.showBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    // Instâncias do Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa as instâncias do Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.voltar.setBackAction(this)

        initAdd()
    }

    private fun initAdd() {
        binding.bntS.setOnClickListener { // Certifique-se que o ID do botão está correto (bnt_s no XML)
            validateDataCasa()
        }
    }

    private fun validateDataCasa() {
        val nomeCasa = binding.edit1.text.toString().trim()
        val endereco = binding.edit2.text.toString().trim()
        val local = binding.edit3.text.toString().trim()
        val idSensor = binding.edit4.text.toString().trim()

        // Validação dos campos
        when {
            nomeCasa.isBlank() -> showBottomSheet(message = getString(R.string.nome_casa_empty))
            endereco.isBlank() -> showBottomSheet(message = getString(R.string.endereco_empty))
            idSensor.isBlank() -> showBottomSheet(message = getString(R.string.sensor_id_empty))
            else -> {
                // Todos os campos obrigatórios válidos, prosseguir para salvar no Firebase
                saveHouseToRealtimeDatabase(nomeCasa, endereco, local, idSensor)
            }
        }
    }

    private fun saveHouseToRealtimeDatabase(nomeCasa: String, endereco: String, local: String, idSensor: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("AddFragment", "Tentando salvar casa para o UID: $userId")

            // Cria um mapa com os dados da casa
            val houseData = hashMapOf<String, Any>(
                "nomeCasa" to nomeCasa,
                "endereco" to endereco,
                "local" to local,
                "idSensor" to idSensor,
                "dataRegistro" to System.currentTimeMillis(),
                "fumacaDetectada" to false,
                "nivelFumaca" to "Nenhum",
                "bombaAtivada" to false,
                "buzzerStatus" to false // Initial status for buzzer control from app
            )

            // Initialize sensorData node structure
            val sensorDataInitial = hashMapOf<String, Any>(
                "current" to hashMapOf(
                    "value" to 0,
                    "timestamp" to System.currentTimeMillis()
                ),
                "mq2ReadingsDailyAverage" to hashMapOf<String, Any>() // Empty map, will be populated by ESP32
            )

            val pumpDataInitial = hashMapOf<String, Any>(
                "estado" to "N/A",
                "funcionamento" to "N/A"
            )

            val rootRef = database.getReference("casas").child(userId)

            rootRef.setValue(houseData)
                .addOnSuccessListener {
                    rootRef.child("sensorData").setValue(sensorDataInitial)
                        .addOnSuccessListener {
                            rootRef.child("bombaDeAgua").setValue(pumpDataInitial)
                                .addOnSuccessListener {
                                    Log.d("AddFragment", "Casa e dados iniciais salvos com sucesso no Realtime Database para UID: $userId")
                                    Toast.makeText(requireContext(), "Casa cadastrada com sucesso!", Toast.LENGTH_SHORT).show()

                                    binding.edit1.setText("")
                                    binding.edit2.setText("")
                                    binding.edit3.setText("")
                                    binding.edit4.setText("")

                                    findNavController().navigate(R.id.action_addFragment_to_homeFragment22)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AddFragment", "Erro ao salvar dados iniciais da bomba: ${e.message}", e)
                                    showBottomSheet(message = "Erro ao cadastrar casa: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("AddFragment", "Erro ao salvar dados iniciais do sensor: ${e.message}", e)
                            showBottomSheet(message = "Erro ao cadastrar casa: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("AddFragment", "Erro ao salvar casa no Realtime Database: ${e.message}", e)
                    showBottomSheet(message = "Erro ao cadastrar casa: ${e.message}")
                }
        } else {
            Log.w("AddFragment", "Nenhum usuário logado ao tentar cadastrar casa.")
            Toast.makeText(requireContext(), "Você precisa estar logado para cadastrar uma casa.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}