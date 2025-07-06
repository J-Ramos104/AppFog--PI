package com.exemple.appfog.ui.home

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentHistBinding
import com.exemple.appfog.util.setBackAction
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// Mantenha esta data class em um arquivo separado e único (ex: SensorModels.kt)
// para evitar o erro de "Redeclaration".
// Não a coloque dentro de nenhum Fragment.
data class DailySensorReading(
    val average: Float = 0.0f,
    val peak: Float = 0.0f,
    val timestamp: Long = 0L
)

class HistFragment : Fragment() {
    private var _binding: FragmentHistBinding? = null
    private val binding get() = _binding!!
    // Instâncias Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // Listeners do Firebase
    private var sensorDataListener: ValueEventListener? = null
    private var houseNameListener: ValueEventListener? = null
    private var pumpStatusListener: ValueEventListener? = null

    // Referências do Database
    private var databaseRefSensor: DatabaseReference? = null
    private var databaseRefHouse: DatabaseReference? = null
    private var databaseRefPump: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Configura o botão voltar
        binding.voltar.setBackAction(this)

        // Inicializa os botões e navegação
        initHist()

        // Inicializa o gráfico (com dados dummy inicialmente, serão substituídos pelos dados do Firebase)
        initChart()

        // Carrega todos os dados do Firebase (gráfico, nome da casa, status da bomba)
        loadFirebaseData()
    }

    private fun initHist() {
        binding.buttonHist.setOnClickListener {
            Toast.makeText(requireContext(), "Você já está na tela de histórico.", Toast.LENGTH_SHORT).show()
        }
        binding.buttonHome.setOnClickListener {
            findNavController().navigate(R.id.action_histFragment_to_homeFragment22)
        }
        binding.buttonConf.setOnClickListener {
            findNavController().navigate(R.id.action_histFragment_to_confFragment)
        }
    }

    private fun initChart() {
        val chartSmoke = binding.chartSmoke

        val entries = mutableListOf<Entry>()
        val dataSet = LineDataSet(entries, "Nível de Fumaça (Média Diária)").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(true)
            setDrawCircles(true)
        }

        val lineData = LineData(dataSet)
        chartSmoke.data = lineData

        chartSmoke.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            description.isEnabled = false
            legend.isEnabled = true

            xAxis.apply {
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = DateAxisValueFormatter()
                setLabelCount(7, true)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false

            animateX(1000)
        }

        chartSmoke.invalidate()
    }

    // Função centralizada para carregar todos os dados do Firebase
    private fun loadFirebaseData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("HistFragment", "Carregando dados para o UID: $userId")

            // Carrega o histórico do sensor para o gráfico
            loadSensorHistory(userId)
            // Carrega o nome da casa
            loadHouseName(userId)
            // Carrega o status da bomba d'água
            loadPumpStatus(userId)

        } else {
            // Usuário não logado
            Toast.makeText(requireContext(), "Faça login para ver o histórico e detalhes da casa.", Toast.LENGTH_SHORT).show()
            // Limpa o gráfico e os textos se o usuário não estiver logado
            binding.chartSmoke.clear()
            binding.chartSmoke.invalidate()

            binding.textViewHouseName.text = "Nenhuma Casa Registrada"
            binding.textViewPumpState.text = "N/A"
            binding.textViewPumpFunctioning.text = "N/A"
        }
    }

    private fun loadSensorHistory(userId: String) {
        databaseRefSensor = database.getReference("casas").child(userId)
            .child("sensorData")
            .child("mq2ReadingsDailyAverage")

        sensorDataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = mutableListOf<Entry>()
                val labels = mutableListOf<String>()
                var index = 0f

                if (snapshot.exists()) {
                    Log.d("HistFragment", "Dados de histórico do sensor recebidos: ${snapshot.value}")

                    val sortedData = snapshot.children.sortedBy { it.key }
                    val last7Days = sortedData.takeLast(7)

                    if (last7Days.isEmpty()) {
                        binding.chartSmoke.setNoDataText("Nenhum dado de fumaça recente encontrado.")
                        Toast.makeText(requireContext(), "Nenhum dado de fumaça recente encontrado.", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.chartSmoke.setNoDataText("") // Limpa o texto "no data" se houver dados
                        last7Days.forEach { dailySnapshot ->
                            val dailyReading = dailySnapshot.getValue(DailySensorReading::class.java)
                            if (dailyReading != null) {
                                entries.add(Entry(index, dailyReading.average))
                                labels.add(dailySnapshot.key ?: "")
                                index++
                            }
                        }
                    }

                    val dataSet = LineDataSet(entries, "Nível de Fumaça (Média Diária)").apply {
                        color = Color.RED
                        valueTextColor = Color.BLACK
                        lineWidth = 2f
                        circleRadius = 4f
                        setDrawValues(true)
                        setDrawCircles(true)

                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_chart_fill)
                        setDrawFilled(true)
                    }

                    val lineData = LineData(dataSet)

                    binding.chartSmoke.data = lineData
                    binding.chartSmoke.xAxis.valueFormatter = DateAxisValueFormatter(labels)
                    binding.chartSmoke.xAxis.setLabelCount(labels.size, true)
                    binding.chartSmoke.xAxis.axisMinimum = 0f
                    binding.chartSmoke.xAxis.axisMaximum = labels.size - 1f

                    binding.chartSmoke.invalidate()
                    binding.chartSmoke.animateX(1000)
                } else {
                    Log.d("HistFragment", "Nenhum histórico de sensor encontrado para o usuário: $userId")
                    binding.chartSmoke.setNoDataText("Nenhum dado de fumaça encontrado.")
                    binding.chartSmoke.clear()
                    binding.chartSmoke.invalidate()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistFragment", "Erro ao ler histórico do sensor: ${error.message}", error.toException())
                Toast.makeText(requireContext(), "Erro ao carregar histórico: ${error.message}", Toast.LENGTH_LONG).show()
                binding.chartSmoke.setNoDataText("Erro ao carregar dados.")
                binding.chartSmoke.clear()
                binding.chartSmoke.invalidate()
            }
        }
        databaseRefSensor?.addValueEventListener(sensorDataListener as ValueEventListener)
    }

    private fun loadHouseName(userId: String) {
        databaseRefHouse = database.getReference("casas").child(userId).child("nomeCasa")

        houseNameListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val houseName = snapshot.getValue(String::class.java)
                if (houseName != null) {
                    binding.textViewHouseName.text = houseName
                    Log.d("HistFragment", "Nome da casa carregado: $houseName")
                } else {
                    binding.textViewHouseName.text = "Casa Principal"
                    Log.d("HistFragment", "Nome da casa não encontrado para o usuário: $userId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistFragment", "Erro ao ler nome da casa: ${error.message}", error.toException())
                binding.textViewHouseName.text = "Erro ao carregar nome"
            }
        }
        databaseRefHouse?.addValueEventListener(houseNameListener as ValueEventListener)
    }

    private fun loadPumpStatus(userId: String) {
        databaseRefPump = database.getReference("casas").child(userId).child("bombaDeAgua")

        pumpStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val estado = snapshot.child("estado").getValue(String::class.java) ?: "Desconhecido"
                    val funcionamento = snapshot.child("funcionamento").getValue(String::class.java) ?: "Desconhecido"

                    binding.textViewPumpState.text = estado
                    binding.textViewPumpFunctioning.text = funcionamento
                    Log.d("HistFragment", "Status da bomba carregado: Estado=$estado, Funcionamento=$funcionamento")
                } else {
                    binding.textViewPumpState.text = "N/A"
                    binding.textViewPumpFunctioning.text = "N/A"
                    Log.d("HistFragment", "Nenhum status de bomba encontrado para o usuário: $userId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistFragment", "Erro ao ler status da bomba: ${error.message}", error.toException())
                binding.textViewPumpState.text = "Erro"
                binding.textViewPumpFunctioning.text = "Erro"
            }
        }
        databaseRefPump?.addValueEventListener(pumpStatusListener as ValueEventListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove todos os listeners do Realtime Database para evitar vazamentos de memória
        if (databaseRefSensor != null && sensorDataListener != null) {
            databaseRefSensor?.removeEventListener(sensorDataListener as ValueEventListener)
        }
        if (databaseRefHouse != null && houseNameListener != null) {
            databaseRefHouse?.removeEventListener(houseNameListener as ValueEventListener)
        }
        if (databaseRefPump != null && pumpStatusListener != null) {
            databaseRefPump?.removeEventListener(pumpStatusListener as ValueEventListener)
        }
        _binding = null
    }

    class DateAxisValueFormatter(private val labels: List<String> = emptyList()) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index >= 0 && index < labels.size) {
                try {
                    val originalDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(labels[index])
                    SimpleDateFormat("dd/MM", Locale.getDefault()).format(originalDate)
                } catch (e: Exception) {
                    labels[index]
                }
            } else {
                ""
            }
        }
    }
}