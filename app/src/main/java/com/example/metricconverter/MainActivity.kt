package com.example.metricconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metricconverter.ui.theme.MetricConverterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetricConverterTheme {
                MetricConverterScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricConverterScreen() {
    var selectedMetric by remember { mutableStateOf("") }
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var conversionResults by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    val metrics = mapOf(
        "Panjang" to listOf("Kilometer", "Hektometer", "Dekameter", "Desimeter", "Centimeter", "Millimeter"),
        "Massa" to listOf("Ton", "Hektogram", "Dekagram", "Gram", "Desigram", "Centigram", "Milligram"),
        "Suhu" to listOf("Celsius", "Fahrenheit"),
        "Waktu" to listOf("Hari", "Jam", "Menit"),
        "Arus Listrik" to listOf("Ampere", "Milliampere", "Mikroampere"),
        "Jumlah Zat" to listOf("Mol", "Milimol", "Mikromol")
    )

    val defaultUnits = mapOf(
        "Panjang" to "Meter",
        "Massa" to "Kilogram",
        "Suhu" to "Kelvin",
        "Waktu" to "Detik",
        "Arus Listrik" to "Ampere",
        "Jumlah Zat" to "Mol"
    )

    val conversionRates = mapOf(
        "Panjang" to mapOf(
            "Meter" to mapOf(
                "Kilometer" to 0.001,
                "Hektometer" to 0.01,
                "Dekameter" to 0.1,
                "Desimeter" to 10.0,
                "Centimeter" to 100.0,
                "Millimeter" to 1000.0
            )
        ),
        "Massa" to mapOf(
            "Kilogram" to mapOf(
                "Ton" to 0.001,
                "Hektogram" to 10.0,
                "Dekagram" to 100.0,
                "Gram" to 1000.0,
                "Desigram" to 10000.0,
                "Centigram" to 100000.0,
                "Milligram" to 1000000.0
            )
        ),
        "Suhu" to mapOf(
            "Kelvin" to mapOf(
                "Celsius" to { x: Double -> x - 273.15 },
                "Fahrenheit" to { x: Double -> (x - 273.15) * 9 / 5 + 32 }
            )
        ),
        "Waktu" to mapOf(
            "Detik" to mapOf(
                "Menit" to 1.0 / 60,
                "Jam" to 1.0 / 3600,
                "Hari" to 1.0 / 86400
            )
        ),
        "Arus Listrik" to mapOf(
            "Ampere" to mapOf(
                "Milliampere" to 1000.0,
                "Mikroampere" to 1000000.0
            )
        ),
        "Jumlah Zat" to mapOf(
            "Mol" to mapOf(
                "Milimol" to 1000.0,
                "Mikromol" to 1000000.0
            )
        )
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Metric Converter", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF6200EE))
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Convert Metrics", fontSize = 24.sp, color = Color(0xFF6200EE))

                        Spacer(modifier = Modifier.height(16.dp))

                        DropdownMenuMetric(
                            label = "Metrik:",
                            options = metrics.keys.toList(),
                            selectedOption = selectedMetric,
                            onOptionSelected = {
                                selectedMetric = it
                                conversionResults = emptyList() // reset results
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = {
                                inputValue = it
                                conversionResults = convertAll(
                                    selectedMetric,
                                    defaultUnits[selectedMetric],
                                    it.text,
                                    conversionRates
                                )
                            },
                            label = { Text("Masukkan Nilai") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Menampilkan hasil konversi
                        conversionResults.forEach { (unit, value) ->
                            Text("Hasil $unit: $value", fontSize = 18.sp, color = Color(0xFF6200EE))
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuMetric(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(text = label, color = Color.Gray)
        ElevatedButton(onClick = { expanded = true }) {
            Text(text = selectedOption.ifEmpty { "Pilih" })
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Fungsi untuk mengonversi ke semua satuan
fun convertAll(
    metric: String,
    fromUnit: String?,
    input: String,
    conversionRates: Map<String, Map<String, Map<String, Any>>>
): List<Pair<String, String>> {
    val value = input.toDoubleOrNull() ?: return listOf("Invalid input" to "")
    if (fromUnit.isNullOrEmpty()) return listOf("Conversion not available" to "")

    val conversions = mutableListOf<Pair<String, String>>()

    // Mendapatkan peta konversi dari unit dasar
    val unitRates = conversionRates[metric]?.get(fromUnit) ?: return listOf("Conversion not available" to "")

    // Melakukan konversi ke semua unit yang tersedia
    unitRates.forEach { (toUnit, rate) ->
        val convertedValue = when (rate) {
            is Double -> value * rate
            is Function1<*, *> -> (rate as (Double) -> Double)(value)
            else -> return@forEach
        }
        conversions.add(toUnit to convertedValue.toString())
    }

    return conversions
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MetricConverterTheme {
        MetricConverterScreen()
    }
}
