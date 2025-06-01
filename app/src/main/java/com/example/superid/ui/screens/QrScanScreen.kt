package com.example.superid.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.superid.WithPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@ExperimentalGetImage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    onLoginAprovado: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    var mensagem by remember { mutableStateOf("Escaneie o QR code para login: ") }
    var isScanningActive by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escaneie o QR Code", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        // Box para empilhar os elementos.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // O AndroidView que exibe a câmera. Ele deve estar por baixo.
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

            // O WithPermission deve estar por cima para mostrar o botão de permissão
            // quando a permissão não for concedida.
            WithPermission(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                permission = Manifest.permission.CAMERA,
                permissionActionLabel = "Conceder permissão da câmera"
            ) {
                // Conteúdo que só será exibido se a permissão for concedida.
                // Neste caso, o LaunchedEffect que inicializa a câmera.
                LaunchedEffect(previewView, lifecycleOwner) {
                    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                    val preview = androidx.camera.core.Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val scanner = BarcodeScanning.getClient()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        if (isScanningActive) {
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val inputImage = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                scanner.process(inputImage)
                                    .addOnSuccessListener { barcodes ->
                                        if (isScanningActive && barcodes.isNotEmpty()) {
                                            val qrCode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }

                                            qrCode?.rawValue?.let { sessionId ->
                                                isScanningActive = false

                                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                                if (userId != null) {
                                                    val db = Firebase.firestore
                                                    db.collection("login").document(sessionId)
                                                        .update(
                                                            "status", "approved",
                                                            "user", userId,
                                                            "loggedInAt", FieldValue.serverTimestamp()
                                                        )
                                                        .addOnSuccessListener {
                                                            mensagem = "Login via QR Code aprovado!"
                                                            Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                                                            cameraProvider.unbindAll() // Importante desvincular
                                                            onLoginAprovado()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            mensagem = "Erro ao autorizar login: ${e.message}"
                                                            Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
                                                            isScanningActive = true
                                                        }
                                                } else {
                                                    mensagem = "Erro: Usuário não logado no aplicativo."
                                                    Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
                                                    isScanningActive = true
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace()
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    try {
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                    } catch (exc: Exception) {
                        mensagem = "Erro ao iniciar câmera: ${exc.message}"
                        Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
                        onBack()
                    }
                }
            }
        }
    }
}