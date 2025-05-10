import com.example.superid.ui.screens.TermsScreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.superid.MainActivity
import androidx.preference.PreferenceManager

class TermsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val hasAcceptedTerms = sharedPreferences.getBoolean("hasAcceptedTerms", false)

        if (hasAcceptedTerms) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContent {
            TermsScreen(onAccept = {
                val editor = sharedPreferences.edit()
                editor.putBoolean("hasAcceptedTerms", true)
                editor.apply()

                startActivity(Intent(this, MainActivity::class.java))
            })
        }
    }
}

