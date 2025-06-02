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

/**
 * Tela para escanear QR Codes e realizar login via autenticação externa.
 * Solicita permissão da câmera e utiliza o ML Kit Barcode Scanning para processar imagens.
 * Em caso de QR Code válido, atualiza o status de login no Firestore.
 *
 * @param onLoginAprovado Callback executado quando o login é aprovado com sucesso.
 * @param onBack Callback para retornar à tela anterior.
 */
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@ExperimentalGetImage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    onLoginAprovado: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current // Contexto atual para operações Android.
    val lifecycleOwner = LocalLifecycleOwner.current // Owner do ciclo de vida para a câmera.
    val previewView = remember { PreviewView(context) } // View para exibir a prévia da câmera.

    var mensagem by remember { mutableStateOf("Escaneie o QR code para login: ") } // Mensagem exibida ao usuário.
    var isScanningActive by remember { mutableStateOf(true) } // Controla se o scanner está ativo.

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
        // Box que empilha a prévia da câmera e o controle de permissão.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // AndroidView para exibir a prévia da câmera, ocupando todo o espaço.
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

            // O Composable 'WithPermission' gerencia a solicitação da permissão da câmera.
            // Se a permissão não for concedida, ele exibe um botão de solicitação.
            WithPermission(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                permission = Manifest.permission.CAMERA,
                permissionActionLabel = "Conceder permissão da câmera"
            ) {
                // Bloco executado somente se a permissão da câmera for concedida.
                LaunchedEffect(previewView, lifecycleOwner) {
                    val cameraProvider = ProcessCameraProvider.getInstance(context).get() // Obtém o provedor da câmera.
                    val preview = androidx.camera.core.Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider) // Associa a prévia à PreviewView.
                    }

                    val scanner = BarcodeScanning.getClient() // ML Kit para leitura de códigos de barra.
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Processa apenas o frame mais recente.
                        .build()

                    // Configura o analisador de imagem para processar frames da câmera.
                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        if (isScanningActive) { // Verifica se o scanner está ativo.
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                // Cria um InputImage a partir do MediaImage do ImageProxy.
                                val inputImage = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                // Processa a imagem para detectar códigos de barra.
                                scanner.process(inputImage)
                                    .addOnSuccessListener { barcodes ->
                                        // Se o scanner estiver ativo e códigos de barra forem detectados.
                                        if (isScanningActive && barcodes.isNotEmpty()) {
                                            // Encontra o primeiro QR Code.
                                            val qrCode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }

                                            qrCode?.rawValue?.let { sessionId ->
                                                isScanningActive = false // Desativa o scanner após o primeiro QR code.

                                                val userId = FirebaseAuth.getInstance().currentUser?.uid // Obtém o UID do usuário logado.
                                                if (userId != null) {
                                                    val db = Firebase.firestore // Instância do Firestore.
                                                    // Atualiza o documento de login no Firestore.
                                                    db.collection("login").document(sessionId)
                                                        .update(
                                                            "status", "approved", // Define status como aprovado.
                                                            "user", userId, // Associa o usuário.
                                                            "loggedInAt", FieldValue.serverTimestamp() // Registra timestamp do login.
                                                        )
                                                        .addOnSuccessListener {
                                                            mensagem = "QR Code escaneado! Acesso liberado."
                                                            Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                                                            cameraProvider.unbindAll() // Desvincula a câmera para parar o preview.
                                                            onLoginAprovado() // Callback de sucesso de login.
                                                        }
                                                        .addOnFailureListener { e ->
                                                            mensagem = "Erro ao autorizar login: ${e.message}"
                                                            Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
                                                            isScanningActive = true // Reativa o scanner em caso de falha.
                                                        }
                                                } else {
                                                    mensagem = "Erro: Usuário não logado no aplicativo."
                                                    Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
                                                    isScanningActive = true // Reativa o scanner se não houver usuário logado.
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace() // Imprime o erro em caso de falha no scanner.
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close() // Fecha o ImageProxy para liberar o buffer.
                                    }
                            } else {
                                imageProxy.close() // Fecha o ImageProxy se mediaImage for nulo.
                            }
                        } else {
                            imageProxy.close() // Fecha o ImageProxy se o scanner não estiver ativo.
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA // Seleciona a câmera traseira padrão.
                    cameraProvider.unbindAll() // Garante que nenhuma câmera esteja vinculada antes de vincular novamente.
                    try {
                        // Vincula o ciclo de vida da câmera, seletor, prévia e análise de imagem.
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                    } catch (exc: Exception) {
                        mensagem = "Erro ao iniciar câmera: ${exc.message}"
                        Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
                        onBack() // Volta à tela anterior em caso de erro na câmera.
                    }
                }
            }
        }
    }
}