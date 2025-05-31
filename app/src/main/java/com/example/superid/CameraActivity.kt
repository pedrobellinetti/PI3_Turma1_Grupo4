package com.example.superid

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.example.superid.ui.theme.SuperIDTheme
import java.io.File
import java.util.concurrent.Executors // Para o executar a captura de imagem
import androidx.concurrent.futures.await

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                        innerPadding ->
                    WithPermission(
                        modifier = Modifier.padding(innerPadding),
                        permission = Manifest.permission.CAMERA,
                        permissionActionLabel = "Permitir Câmera..."
                    ) {
                        // Composable que será carregado após...
                        CameraAppTirarFoto()
                    }
                }
            }
        }
    }
}

@Composable
fun CameraAppTirarFoto() {
    TakePhotoScreen()
}


@Composable
fun TakePhotoScreen() {
    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
    }
    var zoomLevel by remember {
        mutableFloatStateOf(0.0f)
    }
    var imageCaptureUseCase by remember {
        mutableStateOf(ImageCapture.Builder().build())
    }
    var currentZoomState by remember { mutableStateOf<ZoomState?>(null) }


    val localContext = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()){
        CameraPreview(
            lensFacing = lensFacing,
            zoomLevel = zoomLevel,
            ImageCaptureUseCase = imageCaptureUseCase,
        )
        // Coluna principal para os controles na parte inferior da tela
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Centraliza os elementos na coluna
        ) {
            // Linha para os botões Frontal/Traseira
            Row(
                modifier = Modifier.padding(bottom = 8.dp), // Espaçamento entre as linhas de botões
                verticalAlignment = Alignment.CenterVertically
            ){
                Button(
                    onClick = { lensFacing = CameraSelector.LENS_FACING_FRONT },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp) // Ocupa o espaço disponível
                ){
                    Text(text = "Frontal")
                }
                Button(
                    onClick = { lensFacing = CameraSelector.LENS_FACING_BACK },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ){
                    Text(text = "Traseira")
                }
            }

            // Linha para os botões de Zoom
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        currentZoomState?.let {
                            zoomLevel = 0.0f // Sem zoom
                        }
                    },
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(text = "Zoom 1x")
                }
                Button(
                    onClick = {
                        currentZoomState?.let {
                            zoomLevel = 0.5f // Metade do zoom máximo
                        }
                    },
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(text = "Zoom 2x")
                }
                Button(
                    onClick = {
                        currentZoomState?.let {
                            zoomLevel = 1.0f // Zoom máximo
                        }
                    },
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(text = "Zoom Max")
                }
            }

            // Botão "Tirar foto"
            Button(
                onClick = {
                    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                        File(localContext.externalCacheDir, "image.jpg")
                    ).build()

                    val callback = object: ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            Log.i("Camera", "Imagem salva no diretório dentro do app: ${outputFileResults.savedUri}")
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("Camera", "Imagem não foi salva: " + exception.message)
                            exception.printStackTrace()
                        }
                    }
                    imageCaptureUseCase.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), callback)
                },
                modifier = Modifier.fillMaxWidth() // Faz o botão ocupar toda a largura disponível
            ) {
                Text("Tirar foto")
            }
        }
    }
}


@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    zoomLevel: Float,
    ImageCaptureUseCase: ImageCapture
) {
    val previewUseCase = remember {
        androidx.camera.core.Preview.Builder()
            .build()
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    var cameraControlState by remember {
        mutableStateOf<CameraControl?>(null)
    }

    val localContext = LocalContext.current

    val lifecycleOwner = localContext as LifecycleOwner

    fun rebindCameraProvider() {
        cameraProvider?.let { cameraProvider ->
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                ImageCaptureUseCase
            )
            cameraControlState = camera.cameraControl
        }
    }

    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider.getInstance(localContext).await()
        rebindCameraProvider()
    }
    LaunchedEffect(lensFacing) {
        rebindCameraProvider()
    }
    LaunchedEffect(zoomLevel, cameraControlState) {
        cameraControlState?.setLinearZoom(zoomLevel)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).also {
                previewUseCase.setSurfaceProvider(it.surfaceProvider)
            }
        }
    )
}