package br.edu.ifsp.dmo1.plannersensorial.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo1.plannersensorial.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivitySignUpBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setListerners()
    }

    private fun setListerners() {
        viewBinding.botao.setOnClickListener {
            val login = viewBinding.login.text.toString()
            val senha = viewBinding.digiteSenha.text.toString()
            val confirmaSenha = viewBinding.confirmeSenha.text.toString()

            if (login.isEmpty() || senha.isEmpty() || confirmaSenha.isEmpty()) {
                Toast.makeText(this, "Por favor preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                if (senha == confirmaSenha) {
                    firebaseAuth
                        .createUserWithEmailAndPassword(login, senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                startActivity(Intent(this, ProfileActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, "As senhas são diferentes. Por favor insira os valores iguais!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}