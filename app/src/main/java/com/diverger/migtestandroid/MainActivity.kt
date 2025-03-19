package com.diverger.migtestandroid

import android.os.Bundle
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
import com.diverger.mig_android_sdk.MIGAndroidSDKScreen
import com.diverger.mig_android_sdk.MadridInGameAndroidModule
import com.diverger.mig_android_sdk.MadridInGameAndroidModuleEntryPoint
import com.diverger.migtestandroid.ui.theme.MIGTestAndroidTheme

 class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*enableEdgeToEdge()
        setContent {

            //MadridInGameAndroidModule(email = "adriortega19@gmail.com", accessToken = "8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY")
            //MIGAndroidSDKScreen(email = " ")
            //MIGAndroidSDKScreen(email = "joseluis.fernandez@diverger.ai")
        }*/

        /*MadridInGameAndroidModuleEntryPoint.launch(
            context = this,
            email = "adriortega19@gmail.com",
            userName = "Adri",
            dni = "03427404J",
            accessToken = "8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"
        )*/

        MadridInGameAndroidModuleEntryPoint.launch(
            context = this,
            email = "hamzahods@gmail.com",
            userName = "HamzaTest3",
            dni = "03427404J",
            accessToken = "8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"
        )

        finish()
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
    MIGTestAndroidTheme {
        Greeting("Android")
    }
}