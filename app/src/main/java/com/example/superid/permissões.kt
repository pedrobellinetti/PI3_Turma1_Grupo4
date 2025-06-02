package com.example.superid

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun PermissionScreen(
    modifier: Modifier = Modifier,
    permission: String,
    permissionActionLabel: String,
    onPermissionGranted: () -> Unit
) {
    // Configura um launcher para solicitar permissões.
    // O callback é executado após a resposta do usuário à solicitação.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Se a permissão foi concedida, executa a ação de callback.
        if (granted) {
            onPermissionGranted()
        }
    }

    // Layout da tela de permissão.
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            modifier = modifier.align(Alignment.Center),
            onClick = {
                // Lança a solicitação da permissão quando o botão é clicado.
                launcher.launch(permission)
            }
        ) {
            Text(permissionActionLabel)
        }
    }
}

/**
 * ## WithPermission Composable
 *
 * Um Composable de nível superior que gerencia a exibição de conteúdo baseado no status de uma permissão.
 *
 * Ele verifica se uma permissão já foi concedida. Se sim, exibe o 'content' fornecido.
 * Se não, ele exibe a 'PermissionScreen' para solicitar a permissão. Uma vez concedida, o 'content' é mostrado.
 *
 * @param modifier Modificador para aplicar aos layouts internos.
 * @param permission A permissão a ser verificada e solicitada, se necessário.
 * @param permissionActionLabel O texto do botão para a tela de solicitação de permissão.
 * @param content O conteúdo Composable a ser exibido quando a permissão for concedida.
 */
@Composable
fun WithPermission(
    modifier: Modifier = Modifier,
    permission: String,
    permissionActionLabel: String,
    content: @Composable () -> Unit
) {
    // Obtém o contexto atual do aplicativo.
    val context = LocalContext.current

    // Estado mutável para rastrear se a permissão foi concedida.
    // Inicializa verificando o status atual da permissão.
    var permissionGranted by remember {
        mutableStateOf(context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
    }

    // Verifica o status da permissão.
    if (!permissionGranted) {
        // Se a permissão não foi concedida, exibe a tela de solicitação.
        // O callback atualiza o estado 'permissionGranted' quando a permissão é obtida.
        PermissionScreen(
            modifier = modifier,
            permission = permission,
            permissionActionLabel = permissionActionLabel
        ) {
            permissionGranted = true
        }
    } else {
        // Se a permissão foi concedida, exibe o conteúdo principal.
        Surface(modifier = modifier) {
            content()
        }
    }
}