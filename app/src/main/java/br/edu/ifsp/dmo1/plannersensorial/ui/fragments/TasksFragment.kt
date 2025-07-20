package br.edu.ifsp.dmo1.plannersensorial.ui.fragments

import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.adapters.CalendarViewBindingAdapter.setDate
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.databinding.FragmentHomeBinding
import br.edu.ifsp.dmo1.plannersensorial.databinding.FragmentTasksBinding
import br.edu.ifsp.dmo1.plannersensorial.helper.ReconhecimentoHelper
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Priorities
import br.edu.ifsp.dmo1.plannersensorial.ui.Base64Converter
import br.edu.ifsp.dmo1.plannersensorial.ui.viewModel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

import java.util.*


class TasksFragment : Fragment() {
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var binding: FragmentTasksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]


    }

    private fun onsetButtonListeners(taskId: String) {
        binding.btnDeletar.setOnClickListener {
            taskViewModel.deleteTask(taskId) {
                Toast.makeText(requireContext(), "Tarefa deletada com sucesso!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = arguments?.getString("taskId")
        if (!taskId.isNullOrEmpty()) {
            taskViewModel.getTaskById(taskId)
            onsetButtonListeners(taskId)
        }

        taskViewModel.selectedTask.observe(viewLifecycleOwner) { task ->
            task?.let {

                binding.taskTitle.text = it.title
                binding.taskDescription.text = it.descricao
                binding.taskDate.text = formatarTimestamp(it.data)


                if (it.imageTask.isNotEmpty()) {
                    val bitmap = Base64Converter.stringToBitmap(it.imageTask)
                    binding.taskImage.setImageBitmap(bitmap)
                } else {
                    binding.taskImage.setImageResource(R.drawable.ic_task_placeholder)
                }
            }
        }
    }

    fun formatarTimestamp(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

}







