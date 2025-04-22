package com.example.projeto_integrador3

import android.os.Bundle
import android.util.Log // Para regristrar o cliclo de vida dos estados da tela
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.projeto_integrador3.ui.theme.Projeto_Integrador3Theme

class MainActivity : ComponentActivity() {
    val TAG = "MainActivity"
    var estado = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Projeto_Integrador3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        //No final da execução do onCreate, o estado da tela será "CRIADA"
        //CRIADA não significa APARENTE para o usuario
        estado = "CREATED"
        Log.i(TAG, estado) // i de info
    }
    override fun onStart() {
        super.onStart()
        estado = "STARTED"
        Log.i(TAG,estado)
    }

    override fun onResume() {
        super.onResume()
        estado = "Tela aparente para o usuario"
        Log.i(TAG, estado)
    }

    override fun onRestart() {
        super.onRestart()
        estado = "RESTARTED"
        Log.i(TAG,estado)
    }

    override fun onPause() {
        super.onPause()
        estado = "PAUSED"
        Log.i(TAG,estado)
    }

    override fun onStop() {
        super.onStop()
        estado = "STOPPED"
        Log.i(TAG,estado)
    }

    override fun onDestroy() {
        super.onDestroy()
        estado = "DESTROYED"
        Log.i(TAG,estado)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Projeto_Integrador3Theme {
        Greeting("Android")
    }
}