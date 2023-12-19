package com.example.followpat

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var doktorName: String
    private lateinit var doktorSurname: String
    private val cardMargin: Int = 16 // Eklediğim satır

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hastapage)

        userId = intent.getStringExtra("UserId") ?: ""
        doktorName = intent.getStringExtra("DoktorName") ?: ""
        doktorSurname = intent.getStringExtra("DoktorSurname") ?: ""

        getDoktorBilgileri(userId)
        getHastaBilgileri(userId)
        showWelcomeMessage()
    }

    private fun getDoktorBilgileri(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocument = db.collection("UserData").document(uid)

        userDocument.get()
            .addOnSuccessListener { userSnapshot ->
                doktorName = userSnapshot.getString("DoktorName") ?: ""
                doktorSurname = userSnapshot.getString("DoktorSurname") ?: ""
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Doktor bilgileri alınamadı: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showWelcomeMessage() {
        val welcomeTextView = TextView(this)
        welcomeTextView.text = "Hoşgeldiniz, Sayın Dr. $doktorName $doktorSurname"
        welcomeTextView.setTextColor(
            getColor(
                R.color.cadet_blue
            )
        )
        welcomeTextView.textSize = 18f
        welcomeTextView.setTypeface(null, Typeface.BOLD)
        welcomeTextView.setPadding(8, 8, 8, 8)

        val containerLayout: LinearLayout = findViewById(R.id.containerLayout)
        containerLayout.addView(welcomeTextView)

        val doktorButton = Button(this)
        doktorButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        doktorButton.text = "Doktor: $doktorName $doktorSurname"
        doktorButton.setTextColor(Color.WHITE)
        doktorButton.setBackgroundResource(R.drawable.button_style)
        doktorButton.setOnClickListener {
            Toast.makeText(this, "Doktor Bilgileri Gösterildi", Toast.LENGTH_SHORT).show()
        }

        containerLayout.addView(doktorButton)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val containerLayout: LinearLayout = findViewById(R.id.containerLayout)
        containerLayout.removeAllViews()

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("UserId", userId)
        startActivity(intent)
        finish()
    }

    private fun getHastaBilgileri(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val patientsCollection = db.collection("UserData").document(uid).collection("Patients")

        patientsCollection.get()
            .addOnSuccessListener { documents ->
                val hastaBilgileri = mutableListOf<HastaBilgisi>()

                for (document in documents) {
                    val name = document.getString("Name") ?: ""
                    val surname = document.getString("Surname") ?: ""
                    val tc = document.getString("Tc") ?: ""
                    val birthTimestamp = document.getTimestamp("Birth") ?: Timestamp(Date())
                    val gender = document.getLong("Gender")?.toInt() ?: 0
                    val entryDateTimestamp = document.getTimestamp("EntryDate") ?: Timestamp(Date())
                    val acceptDateTimestamp =
                        document.getTimestamp("AcceptDate") ?: Timestamp(Date())
                    val resultDateTimestamp =
                        document.getTimestamp("ResultDate") ?: Timestamp(Date())
                    val barcode = document.getString("Barcode") ?: ""

                    val dateFormatter =
                        SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a z", Locale.getDefault())

                    val dogumTarihi = dateFormatter.format(birthTimestamp.toDate())
                    val kabulTarihi = dateFormatter.format(entryDateTimestamp.toDate())
                    val onayTarihi = dateFormatter.format(acceptDateTimestamp.toDate())
                    val sonucTarihi = dateFormatter.format(resultDateTimestamp.toDate())

                    val hasta = HastaBilgisi(
                        name,
                        surname,
                        tc,
                        dogumTarihi,
                        gender,
                        kabulTarihi,
                        onayTarihi,
                        sonucTarihi,
                        barcode
                    )
                    hastaBilgileri.add(hasta)
                }

                showHastaBilgileri(hastaBilgileri)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Hasta bilgileri alınamadı: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showHastaBilgileri(hastaBilgileri: List<HastaBilgisi>) {
        val containerLayout: LinearLayout = findViewById(R.id.containerLayout)
        containerLayout.removeAllViews()

        for (hasta in hastaBilgileri) {
            val cardView = createCardView(hasta)
            containerLayout.addView(cardView)
        }
    }

    private fun createCardView(hasta: HastaBilgisi): View {
        val cardView = LinearLayout(this)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(cardMargin, cardMargin, cardMargin, cardMargin)
        cardView.layoutParams = layoutParams
        cardView.orientation = LinearLayout.VERTICAL
        cardView.setBackgroundColor(getColor(R.color.cadet_blue))
        cardView.setPadding(cardMargin, cardMargin, cardMargin, cardMargin)

        val textView = TextView(this)
        textView.text = "${hasta.name} ${hasta.surname}"
        textView.setTextColor(getColor(R.color.black))
        textView.textSize = 16f

        textView.setOnClickListener {
            showHastaDetay(hasta, cardView)
        }

        cardView.addView(textView)
        return cardView
    }

    private fun showHastaDetay(hasta: HastaBilgisi, containerLayout: LinearLayout) {
        val detayTextView = TextView(this)
        detayTextView.text =
            "Ad: ${hasta.name}\n" +
                    "Soyad: ${hasta.surname}\n" +
                    "Tc: ${hasta.tc}\n" +
                    "Doğum Tarihi: ${hasta.dogumTarihi}\n" +
                    "Cinsiyet: ${hasta.gender}\n" +
                    "Kabul Tarihi: ${hasta.kabulTarihi}\n" +
                    "Onay Tarihi: ${hasta.onayTarihi}\n" +
                    "Sonuç Tarihi: ${hasta.sonucTarihi}\n" +
                    "Barcode: ${hasta.barcode}"
        detayTextView.setTextColor(getColor(R.color.black))
        detayTextView.textSize = 16f
        detayTextView.setPadding(8, 8, 8, 8)

        containerLayout.removeAllViews()
        containerLayout.addView(detayTextView)

        val basincButton = Button(this)
        basincButton.text = "BASINÇ DEĞERİNİ GETİR"
        basincButton.setTextColor(Color.WHITE)
        basincButton.setBackgroundResource(R.color.purple_500)
        basincButton.setTypeface(null, Typeface.BOLD)
        basincButton.setOnClickListener {
            Toast.makeText(this, "Basınç Değeri Getirildi", Toast.LENGTH_SHORT).show()
        }

        containerLayout.addView(basincButton)
    }
}

data class HastaBilgisi(
    val name: String,
    val surname: String,
    val tc: String,
    val dogumTarihi: String,
    val gender: Int,
    val kabulTarihi: String,
    val onayTarihi: String,
    val sonucTarihi: String,
    val barcode: String
)
