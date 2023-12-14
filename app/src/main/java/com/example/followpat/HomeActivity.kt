package com.example.followpat

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hastapage)

        // Kullanıcının UID'sini al
        userId = intent.getStringExtra("UserId") ?: ""

        // Firestore'dan hasta bilgilerini çek
        getHastaBilgileri(userId)
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
        // Hasta detaylarını göstermek için yeni bir TextView oluşturun ve containerLayout'a ekleyin
        val detayTextView = TextView(this)
        detayTextView.text = "Ad: ${hasta.name}\nSoyad: ${hasta.surname}\nTc: ${hasta.tc}\nDoğum Tarihi: ${hasta.dogumTarihi}\nCinsiyet: ${hasta.gender}\nKabul Tarihi: ${hasta.kabulTarihi}\nOnay Tarihi: ${hasta.onayTarihi}\nSonuç Tarihi: ${hasta.sonucTarihi}\nBarcode: ${hasta.barcode}"
        detayTextView.setTextColor(resources.getColor(R.color.black))
        detayTextView.textSize = 16f
        detayTextView.setPadding(8, 8, 8, 8)

        // Eğer daha önce detaylar eklenmişse, eski detayları temizle
        containerLayout.removeAllViews()

        // Detayları containerLayout'a ekle
        containerLayout.addView(detayTextView)
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
