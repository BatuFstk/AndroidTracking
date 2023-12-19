package com.example.followpat

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale




class HomeActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var doktorName: String
    private lateinit var doktorSurname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hastapage)

        // Kullanıcının UID'sini ve doktor bilgilerini al
        userId = intent.getStringExtra("UserId") ?: ""
        doktorName = intent.getStringExtra("DoktorName") ?: ""
        doktorSurname = intent.getStringExtra("DoktorSurname") ?: ""


        getDoktorBilgileri(userId)
        // Firestore'dan hasta bilgilerini çek
        getHastaBilgileri(userId)

        // Hoşgeldiniz mesajını göster
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
                Toast.makeText(this, "Doktor bilgileri alınamadı: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showWelcomeAlert() {
        val welcomeMessage = "Hoşgeldiniz, Sayın Dr. $doktorName $doktorSurname"

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Hoşgeldiniz")
            .setMessage(welcomeMessage)
            .setPositiveButton("Tamam") { dialog, _ ->
                // Tamam butonuna basılınca bir şey yapılmasını istiyorsanız buraya ekleyebilirsiniz
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }
    private fun showErrorAlert(errorMessage: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Hata")
            .setMessage(errorMessage)
            .setPositiveButton("Tamam") { dialog, _ ->
                // Tamam butonuna basılınca bir şey yapılmasını istiyorsanız buraya ekleyebilirsiniz
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }
    private fun showWelcomeMessage() {
        val welcomeTextView = TextView(this)
        welcomeTextView.text = "Hoşgeldiniz, Sayın Dr. $doktorName $doktorSurname"
        welcomeTextView.setTextColor(ContextCompat.getColor(this, R.color.cadet_blue))// Cadet Blue rengi
        welcomeTextView.textSize = 18f
        welcomeTextView.setTypeface(null, Typeface.BOLD)
        welcomeTextView.setPadding(8, 8, 8, 8)

        // Hoşgeldiniz mesajını containerLayout'a ekle
        val containerLayout: LinearLayout = findViewById(R.id.containerLayout)
        containerLayout.addView(welcomeTextView)

        // Buton gibi görünen kareyi oluştur
        val doktorButton = Button(this)
        doktorButton.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        doktorButton.text = "Doktor: $doktorName $doktorSurname"
        doktorButton.setTextColor(Color.WHITE)
        doktorButton.setBackgroundResource(R.drawable.button_style) // R.drawable.button_style drawable'ını oluşturun
        doktorButton.setOnClickListener {
            // Butona tıklandığında yapılacak işlemleri buraya ekleyin
            // Örneğin, doktor bilgilerini gösteren bir sayfaya yönlendirebilirsiniz.
            Toast.makeText(this, "Doktor Bilgileri Gösterildi", Toast.LENGTH_SHORT).show()
        }

        // Butonu containerLayout'a ekle
        containerLayout.addView(doktorButton)
    }


    override fun onBackPressed() {
        // Geri tuşuna basıldığında aktiviteyi sıfırlama mantığını ekle
        super.onBackPressed()

        // Sıfırlanacak görünümleri veya verileri temizle
        val containerLayout: LinearLayout = findViewById(R.id.containerLayout)
        containerLayout.removeAllViews()

        // Aktiviteyi yeniden başlatma
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("UserId", userId)
        startActivity(intent)
        finish() // Şu anki aktiviteyi kapatarak yeni bir tane başlat
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
                    val acceptDateTimestamp = document.getTimestamp("AcceptDate") ?: Timestamp(Date())
                    val resultDateTimestamp = document.getTimestamp("ResultDate") ?: Timestamp(Date())
                    val barcode = document.getString("Barcode") ?: ""

                    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a z", Locale.getDefault())

                    val dogumTarihi = dateFormatter.format(birthTimestamp.toDate())
                    val kabulTarihi = dateFormatter.format(entryDateTimestamp.toDate())
                    val onayTarihi = dateFormatter.format(acceptDateTimestamp.toDate())
                    val sonucTarihi = dateFormatter.format(resultDateTimestamp.toDate())

                    val hasta = HastaBilgisi(name, surname, tc, dogumTarihi, gender, kabulTarihi, onayTarihi, sonucTarihi, barcode)
                    hastaBilgileri.add(hasta)
                }

                // Hasta bilgilerini ekrana ekleme işlemleri burada yapılacak
                showHastaBilgileri(hastaBilgileri)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Hasta bilgileri alınamadı: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showHastaBilgileri(hastaBilgileri: List<HastaBilgisi>) {
        val containerLayout: LinearLayout = findViewById(R.id.containerLayout)

        // Eğer daha önce detaylar eklenmişse, eski detayları temizle
        containerLayout.removeAllViews()

        // Hasta bilgilerini dinamik olarak ekleyin
        for (hasta in hastaBilgileri) {
            val textView = TextView(this)
            textView.text = "${hasta.name} ${hasta.surname}"
            textView.setTextColor(resources.getColor(R.color.black))
            textView.textSize = 16f
            textView.setPadding(8, 8, 8, 8)

            // Hasta adına tıklandığında tüm bilgileri göster
            textView.setOnClickListener {
                showHastaDetay(hasta, containerLayout)
            }

            containerLayout.addView(textView)
        }
    }

    private fun showHastaDetay(hasta: HastaBilgisi, containerLayout: LinearLayout) {
        // Hasta detaylarını göstermek için yeni bir TextView oluşturun
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
        detayTextView.setTextColor(resources.getColor(R.color.black))
        detayTextView.textSize = 16f
        detayTextView.setPadding(8, 8, 8, 8)

        // Eğer daha önce detaylar eklenmişse, eski detayları temizle
        containerLayout.removeAllViews()

        // Detayları containerLayout'a ekle
        containerLayout.addView(detayTextView)

        // "BASINÇ DEĞERİNİ GETİR" adlı butonu oluşturun
        val basincButton = Button(this)
        basincButton.text = "BASINÇ DEĞERİNİ GETİR"
        basincButton.setTextColor(Color.WHITE)
        basincButton.setBackgroundResource(R.color.purple_500) // Mor renkte
        basincButton.setTypeface(null, Typeface.BOLD) // Kalın yazı stili
        basincButton.setOnClickListener {
            // Butona tıklandığında yapılacak işlemleri buraya ekleyin
            // Örneğin, basınç değerini getiren bir fonksiyonu çağırabilirsiniz
            // ve sonucu kullanıcıya gösterebilirsiniz.
            Toast.makeText(this, "Basınç Değeri Getirildi", Toast.LENGTH_SHORT).show()
        }

        // Butonu containerLayout'a ekle
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
