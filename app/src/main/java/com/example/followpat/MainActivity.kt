package com.example.followpat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var tcKimlikNoEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tcKimlikNoEditText = findViewById(R.id.tcKimlikNoEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        resultTextView = findViewById(R.id.resultTextView)

        loginButton.setOnClickListener {
            // Kullanıcı adı ve şifreyi al
            val tcKimlikNo = tcKimlikNoEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Firestore referansını al
            val db = FirebaseFirestore.getInstance()
            val userDataCollection = db.collection("UserData")

            // Firestore sorgusu yap
            userDataCollection
                .whereEqualTo("Username", tcKimlikNo)
                .whereEqualTo("Password", password)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Kullanıcı bilgileri doğru, giriş başarılı
                        resultTextView.text = "Giriş Başarılı"

                        // Giriş başarılı olduğunda HomeActivity'ye geç
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("UserId", documents.documents[0].id)
                        startActivity(intent)
                        finish() // Bu sayfanın kapatılması (isteğe bağlı)


                    } else {
                        // Kullanıcı bilgileri hatalı, giriş başarısız
                        resultTextView.text = "Kullanıcı adı veya şifre hatalı"
                    }
                }
                .addOnFailureListener { exception ->
                    // Firestore sorgusu başarısız oldu
                    Toast.makeText(this, "Hata: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}