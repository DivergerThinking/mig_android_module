package com.diverger.migtestandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.diverger.mig_android_sdk.MadridInGameAndroidModuleEntryPoint
import com.diverger.mig_android_sdk.data.MadridInGameUserData
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

//        MadridInGameAndroidModuleEntryPoint.launch(
//            context = this,
//            email = "adriortega19@gmail.com",
//            userName = "Adri",
//            //dni = "03427404J",
//            accessToken = "8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"
//        )

        val madridInGameUserData = MadridInGameUserData(
            email = "adriortega19@gmail.com",
            userName = "Adri",
//            //dni = "03427404J",
        )

        MadridInGameAndroidModuleEntryPoint.launch(
            context = this,
            madridInGameUserData = madridInGameUserData,
            accessToken = "8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"
        )

//        MadridInGameAndroidModuleEntryPoint.launch(
//            context = this,
//            email = "hamzahods35@gmail.com",
//            userName = "",
//            dni = "09987644D",
//            accessToken = "8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"
//        )

//        MadridInGameAndroidModuleEntryPoint.launch(
//            context = this,
//            email = "hamza.elhamdaoui124@diverger.com",
//            userName = "hamzaelhamdaoui124",
//            //dni = "",
//            accessToken = "8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"
//        )

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