package br.edu.ifsp.dmo1.plannersensorial.ui.activities

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.databinding.ActivityMainBinding
import br.edu.ifsp.dmo1.plannersensorial.helper.CameraHelper
import br.edu.ifsp.dmo1.plannersensorial.helper.NotificacaoHelper
import br.edu.ifsp.dmo1.plannersensorial.helper.ReconhecimentoHelper
import br.edu.ifsp.dmo1.plannersensorial.helper.TaskNotificationReceiver
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Priorities
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Task
import br.edu.ifsp.dmo1.plannersensorial.model.entities.TaskReloadType
import br.edu.ifsp.dmo1.plannersensorial.model.entities.firebase.TaskDatabase
import br.edu.ifsp.dmo1.plannersensorial.ui.Base64Converter
import br.edu.ifsp.dmo1.plannersensorial.ui.fragments.HomeFragment
import br.edu.ifsp.dmo1.plannersensorial.ui.fragments.TasksFragment
import br.edu.ifsp.dmo1.plannersensorial.ui.viewModel.TaskViewModel
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity(), CameraHelper.Callback {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var repository: TaskDatabase
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var notificacaoHelper: NotificacaoHelper
    private lateinit var cameraHelper: CameraHelper
    private lateinit var originalBitmap: Bitmap
    private val REQUEST_CAMERA_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        cameraHelper = CameraHelper(this, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("task_channel", "Task Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Canal para notificações de tarefas"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        repository = TaskDatabase()
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        notificacaoHelper = NotificacaoHelper(this)


        val imagensGenericas = listOf(
            R.drawable.imagem_generica2,
            R.drawable.imagem_generica3,
            R.drawable.imagem_generica4,
            R.drawable.imagem_generica5,
            R.drawable.imagem_generica6

        )

        val imagemEscolhida = imagensGenericas.random()

        originalBitmap = BitmapFactory.decodeResource(resources, imagemEscolhida)

        val taskId = intent.getStringExtra("taskId")
        if (!taskId.isNullOrEmpty()) {
            val bundle = Bundle().apply { putString("taskId", taskId) }
            val fragment = TasksFragment().apply { arguments = bundle }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        reloadInfos()
        setOnClickListener()
        viewModelObservers()
    }

    private fun viewModelObservers() {
        taskViewModel.status.observe(this) { sucesso -> }
    }

    private fun reloadInfos() {
        val email = firebaseAuth.currentUser?.email ?: return
        val db = Firebase.firestore

        db.collection("usuarios").document(email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    val imageString = document.data?.get("fotoPerfil").toString()
                    val bitmap = Base64Converter.stringToBitmap(imageString)
                    Glide.with(this).load(bitmap).circleCrop().into(viewBinding.imageHome)
                    viewBinding.textName.text = document.data!!["username"].toString()
                }
            }
    }

    private fun setDate(dialogView: View, onDateSelected: (Timestamp) -> Unit) {
        val editTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)
        editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
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
                        calendar.get(Calendar.MINUTE), true
                    ).show()
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    fun agendarNotificacao(task: Task) {
        val intent = Intent(this, TaskNotificationReceiver::class.java).apply {
            putExtra("id", task.id)
            putExtra("title", task.title)
            putExtra("descricao", task.descricao)
        }
        val requestCode = task.id.hashCode().absoluteValue
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intentd = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intentd.data = android.net.Uri.parse("package:$packageName")
                startActivity(intentd)
                return
            }
        }

        Log.d("AGENDAMENTO", "Agendando notificação para: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(task.data.toDate())}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            task.data.toDate().time,
            pendingIntent
        )
    }

    private fun setOnClickListener() {

        viewBinding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.sair -> {
                    logout()
                    true
                }
                R.id.menu_add -> {
                    criarTarefa()
                    true
                }
                R.id.menu_edit_perfil -> {
                    startActivity(Intent(this, ProfileActivity::class.java).apply {
                        putExtra("editar", true)
                    })
                    true
                }
                else -> {
                    val fragment = when (item.itemId) {
                        R.id.menu_home -> HomeFragment()
                        else -> null
                    }
                    fragment?.let {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, it)
                            .commit()
                        true
                    } ?: false
                }
            }
        }

        viewBinding.criarTarefa.setOnClickListener {
            criarTarefa()
        }
    }


    private fun criarTarefa(){
        val dialogView = layoutInflater.inflate(R.layout.fragment_insert_task, null)
        val fabMic = dialogView.findViewById<FloatingActionButton>(R.id.fabMic)
        val fabCam = dialogView.findViewById<FloatingActionButton>(R.id.fabCamera)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescriptionTask)
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTitleTask)
        var dataSelecionada: Timestamp? = null

        val reconhecimentoHelper = ReconhecimentoHelper(this, object : ReconhecimentoHelper.Callback {
            override fun onReconhecimentoFinalizado(texto: String?) {
                editTextDescription.setText(texto ?: "")
            }
            override fun onErroReconhecimento(mensagem: String) {
                Toast.makeText(this@MainActivity, mensagem, Toast.LENGTH_SHORT).show()
            }
        })

        fabMic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                reconhecimentoHelper.iniciarReconhecimento("pt-BR")
            } else {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
            }
        }

        fabCam.setOnClickListener {
            tirarFoto(dialogView)
        }

        setDate(dialogView) { timestamp -> dataSelecionada = timestamp }

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Nova Tarefa")
            .setView(dialogView)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()

        alertDialog.show()

        val btnSalvar = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btnSalvar.setOnClickListener {
            val statusCheck = dialogView.findViewById<RadioGroup>(R.id.radioGroupPriority)
            val radioId = statusCheck.checkedRadioButtonId
            val statusValue = when (radioId) {
                R.id.radioOk -> Priorities.OK
                R.id.radioImportant -> Priorities.IMPORTANTE
                R.id.radioLight -> Priorities.LEVE
                else -> Priorities.OK
            }
            val title = editTextTitle.text.toString()
            val descricao = editTextDescription.text.toString()

            if (dataSelecionada == null) {
                Toast.makeText(this, "Selecione a data e hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (title.isBlank()) {
                Toast.makeText(this, "Preencha o título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (descricao.isBlank()){
                Toast.makeText(this, "Preencha a descrição", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imageString = Base64Converter.drawableToString(BitmapDrawable(resources, originalBitmap))

            val imagensGenericas = listOf(
                R.drawable.imagem_generica2,
                R.drawable.imagem_generica3,
                R.drawable.imagem_generica4,
                R.drawable.imagem_generica5,
                R.drawable.imagem_generica6

            )
            val imagemEscolhida = imagensGenericas.random()

            originalBitmap = BitmapFactory.decodeResource(resources, imagemEscolhida)
            taskViewModel.createTaskAndReturn(title, descricao, statusValue, dataSelecionada!!, imageString) { sucesso, task ->
                if (sucesso && task != null) {
                    notificacaoHelper.exibirNotificacao("Tarefa criada!", "Sua tarefa foi adicionada com sucesso.")
                    agendarNotificacao(task)
                    taskViewModel.carregarTasksDoDiaParaHorizontal()
                    taskViewModel.carregarTasksDoUsuario(TaskReloadType.DAY)
                } else {
                    notificacaoHelper.exibirNotificacao("Erro", "Tarefa não foi criada!")
                }
            }

            alertDialog.dismiss()
        }
    }

    private fun tirarFoto(dialogView: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraHelper.tirarFoto(dialogView)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE)
        }
    }


    override fun onFotoRecebida(bitmap: Bitmap?, dialogView: View) {
        val imageView = dialogView.findViewById<ImageView>(R.id.imageTask)

        if (bitmap != null) {
            originalBitmap = bitmap
            imageView.setImageBitmap(bitmap)
        } else {

            val imagensGenericas = listOf(
                R.drawable.imagem_generica1,
                R.drawable.imagem_generica2,
                R.drawable.imagem_generica3,
                R.drawable.imagem_generica4,
                R.drawable.imagem_generica5,
                R.drawable.imagem_generica6

            )

            val imagemEscolhida = imagensGenericas.random()

            imageView.setImageResource(imagemEscolhida)

            val placeholder = ContextCompat.getDrawable(dialogView.context, imagemEscolhida)
            imageView.setImageDrawable(placeholder)
        }
    }

    private fun logout() {
        firebaseAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
