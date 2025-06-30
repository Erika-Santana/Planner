package br.edu.ifsp.dmo1.plannersensorial.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.databinding.ActivityMainBinding
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Priorities
import br.edu.ifsp.dmo1.plannersensorial.model.entities.firebase.TaskDatabase
import br.edu.ifsp.dmo1.plannersensorial.ui.Base64Converter
import br.edu.ifsp.dmo1.plannersensorial.ui.fragments.CalendarFragment
import br.edu.ifsp.dmo1.plannersensorial.ui.fragments.HomeFragment
import br.edu.ifsp.dmo1.plannersensorial.ui.fragments.TasksFragment
import br.edu.ifsp.dmo1.plannersensorial.ui.viewModel.TaskViewModel
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var repository: TaskDatabase
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var taskViewModel: TaskViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        repository = TaskDatabase()
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        reloadInfos()
        setOnClickListener()
        viewModelObservers()
    }

    private fun viewModelObservers() {
        taskViewModel.status.observe(this) { sucesso ->
            if (sucesso) {
                Toast.makeText(this, "Task salva!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao salvar task!", Toast.LENGTH_SHORT).show()
            }
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
                        .into(viewBinding.imageHome)
                }
            }
    }

    private fun setDate(dialogView: View, onDateSelected: (Timestamp) -> Unit) {
        val editTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)

        editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                dialogView.context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)

                    TimePickerDialog(
                        dialogView.context,
                        { _, hourOfDay, minute ->
                            selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            selectedDate.set(Calendar.MINUTE, minute)
                            selectedDate.set(Calendar.SECOND, 0)

                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            editTextDate.setText(sdf.format(selectedDate.time))


                            onDateSelected(Timestamp(selectedDate.time))
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.show()
        }
    }



    private fun setOnClickListener() {

        viewBinding.editPerfil.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("editar", true)
            startActivity(intent)
        }

        viewBinding.bottomNavigation.setOnItemSelectedListener { item ->

            if (item.itemId == R.id.sair){
                logout()
                true
            }else{
                val fragment = when (item.itemId) {
                    R.id.menu_home -> HomeFragment()
                    R.id.menu_tasks -> TasksFragment()
                    R.id.menu_calendar -> CalendarFragment()
                    else -> null
                }

                //Aqui ele vai fazer a substituição do fragmento atual que seria a Home, para
                //o novo fragmento
                fragment?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, it)
                        .commit()
                    true
                } ?: false
            }

        }

        viewBinding.criarTarefa.setOnClickListener{

            val dialogView = layoutInflater.inflate(R.layout.fragment_insert_task, null)

            var dataSelecionada: Timestamp? = null

            setDate(dialogView) { timestamp ->
                dataSelecionada = timestamp
            }

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Nova Tarefa")
                .setView(dialogView)
                .setPositiveButton("Salvar") { dialog, _ ->

                    val statusCheck = dialogView.findViewById<RadioGroup>(R.id.radioGroupPriority)
                     val radioId = statusCheck.checkedRadioButtonId

                    val statusValue = when(radioId){
                        R.id.radioOk -> Priorities.OK
                        R.id.radioImportant -> Priorities.IMPORTANTE
                        R.id.radioLight -> Priorities.LEVE
                        else -> Priorities.OK
                    }

                    if (dataSelecionada != null) {
                        val title = dialogView.findViewById<EditText>(R.id.editTextTitleTask).text.toString()
                        val descricao = dialogView.findViewById<EditText>(R.id.editTextDescriptionTask).text.toString()

                        taskViewModel.verificaCreateTask(
                            title, descricao,
                            statusValue, dataSelecionada!!
                        )
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Selecione a data e hora", Toast.LENGTH_SHORT).show()
                    }


                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()

        }

    }

    private fun logout() {
        firebaseAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /*A minha Main vai ter apenas o bottom navigation, e por padrão ela vai inicializar a home fragment,
    * que vai ser o principal logo após o usuário logar. Após isso, a partir do menu, as outras funcionalidades serão
    * todas Fragments.*/
}