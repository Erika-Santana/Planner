package br.edu.ifsp.dmo1.plannersensorial.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo1.plannersensorial.databinding.ActivityProfileBinding
import br.edu.ifsp.dmo1.plannersensorial.ui.Base64Converter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.bumptech.glide.Glide

class ProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val editar = intent.getBooleanExtra("editar", false)

        setListeners(editar)
    }

    private fun setListeners(editar: Boolean) {

        if (editar) {
            reloadInfos()
            galeryAccessAndButton()
            viewBinding.salvar.setOnClickListener{
                updateProfile()
            }
        } else {
            createProfile()
        }
    }

    private fun updateProfile() {

        val firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        val email = firebaseAuth.currentUser!!.email.toString()
        val newCompleteName = viewBinding.nomeCompleto.text.toString()
        val newPublicName = viewBinding.nomePublico.text.toString()
        val bitmap = Base64Converter.drawableToString(
            viewBinding.imagemProfile.drawable
        )

        if (!email.isEmpty() || !newPublicName.isEmpty() || !newCompleteName.isEmpty() || !bitmap.isEmpty()){
            val dados = mapOf(
                "nomeCompleto" to newCompleteName,
                "username" to newPublicName,
                "fotoPerfil" to bitmap
            )

            db.collection("usuarios").document(email)
                .update(dados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

        }else{
            Toast.makeText(this, "Por favor insira os valores!", Toast.LENGTH_SHORT).show()
        }


    }

    private fun reloadInfos() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        val email = firebaseAuth.currentUser!!.email.toString()
        db.collection("usuarios").document(email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    val imageString = document.data!!["fotoPerfil"].toString()
                    val bitmap = Base64Converter.stringToBitmap(imageString)
                    Glide.with(this)
                        .load(bitmap)
                        .circleCrop()
                        .into(viewBinding.imagemProfile)

                    viewBinding.nomePublico.setText(document.getString("username") ?: "")
                    viewBinding.nomeCompleto.setText(document.getString("nomeCompleto") ?: "")
                }
            }
    }

    private fun galeryAccessAndButton(){
        val galeria = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()) {
                uri ->
            if (uri != null) {
                viewBinding.imagemProfile.setImageURI(uri)
            } else {
                Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
            }
        }

        viewBinding.alterarFoto.setOnClickListener {
            galeria.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

    }

    private fun createProfile() {
        galeryAccessAndButton()

        viewBinding.salvar.setOnClickListener{
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth.currentUser != null){
                val email = firebaseAuth.currentUser!!.email.toString()
                val username = viewBinding.nomePublico.text.toString()
                val nomeCompleto = viewBinding.nomeCompleto.text.toString()
                val fotoPerfilString = Base64Converter.drawableToString(
                    viewBinding.imagemProfile.drawable
                )
                if (!username.isEmpty() || !nomeCompleto.isEmpty() || !fotoPerfilString.isEmpty()){
                    val db = Firebase.firestore

                    val dados = hashMapOf(
                        "nomeCompleto" to nomeCompleto,
                        "username" to username,
                        "fotoPerfil" to fotoPerfilString
                    )
                    db.collection("usuarios").document(email)
                        .set(dados)
                        .addOnSuccessListener {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                }else{
                    Toast.makeText(this, "Por favor, insira os valores e a foto!", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}