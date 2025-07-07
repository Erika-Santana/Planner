package br.edu.ifsp.dmo1.plannersensorial.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.databinding.FragmentHomeBinding
import br.edu.ifsp.dmo1.plannersensorial.helper.ReconhecimentoHelper
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Task
import br.edu.ifsp.dmo1.plannersensorial.model.entities.TaskReloadType
import br.edu.ifsp.dmo1.plannersensorial.ui.adapter.TaskAdapter
import br.edu.ifsp.dmo1.plannersensorial.ui.viewModel.TaskViewModel
import com.google.android.material.button.MaterialButtonToggleGroup

class HomeFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var _binding: FragmentHomeBinding
    private lateinit var reconhecimentoHelper: ReconhecimentoHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        arguments?.let {

        }
    }

    fun toggleButton(){
        val toggleGroup = view?.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupFiltro)
        if (toggleGroup != null) {

            toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {

                        R.id.btnDay -> taskViewModel.carregarTasksDoUsuario(TaskReloadType.DAY)
                        R.id.btnSemana -> taskViewModel.carregarTasksDoUsuario(TaskReloadType.WEEK)
                        R.id.btnMonth -> taskViewModel.carregarTasksDoUsuario(TaskReloadType.MONTH)
                    }
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //O view biding de uma fragment precisa ser feito dentro do onCreateView
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Toast.makeText(context, "Hello, world!", Toast.LENGTH_LONG).show();

        return _binding.root
    }

       override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            toggleButton()
            configAdapter()
           taskViewModel.taskList.observe(viewLifecycleOwner) { tarefas ->
               taskAdapter.updateTasks(tarefas)
           }
            taskViewModel.carregarTasksDoUsuario(TaskReloadType.DAY)
        }


    fun configAdapter() {
        val listEmpty = taskViewModel.taskList.value ?: emptyList()
        val listaMutable = listEmpty.toMutableList()

        taskAdapter = TaskAdapter(listaMutable) { task ->
            val fragment = TaskDetailsFragment.newInstance(task)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        _binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        _binding.recyclerView.adapter = taskAdapter
    }
}