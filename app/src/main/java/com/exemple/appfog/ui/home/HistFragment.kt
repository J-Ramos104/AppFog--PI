package com.exemple.appfog.ui.home

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.exemple.appfog.R
import com.exemple.appfog.databinding.FragmentHistBinding
import com.exemple.appfog.util.setBackAction
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class HistFragment : Fragment() {
    private var _binding: FragmentHistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura o botão voltar
        binding.voltar.setBackAction(this)

        // Inicializa os botões e navegação
        initHist()

        // Inicializa o gráfico
        initChart()
    }

    private fun initHist() {
        // Defina as ações reais dos botões
        binding.buttonHist.setOnClickListener {
            // Por exemplo, atualizar algo ou navegar
            // Aqui coloque a ação real desejada ou remova esse listener
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
        entries.add(Entry(0f, 10f))
        entries.add(Entry(1f, 12f))
        entries.add(Entry(2f, 15f))
        entries.add(Entry(3f, 13f))
        entries.add(Entry(4f, 18f))

        val dataSet = LineDataSet(entries, "Nível de Fumaça").apply {
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
            axisRight.isEnabled = false
            description.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            legend.isEnabled = true
            animateX(1000)
        }

        chartSmoke.invalidate() // Atualiza o gráfico
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
