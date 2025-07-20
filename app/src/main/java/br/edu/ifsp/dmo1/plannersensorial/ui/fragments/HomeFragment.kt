package br.edu.ifsp.dmo1.plannersensorial.ui.fragments
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.databinding.FragmentHomeBinding
import br.edu.ifsp.dmo1.plannersensorial.model.entities.TaskReloadType
import br.edu.ifsp.dmo1.plannersensorial.ui.adapter.TaskAdapter
import br.edu.ifsp.dmo1.plannersensorial.ui.adapter.TaskAdapterHorizontal
import br.edu.ifsp.dmo1.plannersensorial.ui.viewModel.TaskViewModel
import com.google.android.material.button.MaterialButtonToggleGroup

class HomeFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskAdapterHorizontal: TaskAdapterHorizontal
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var _binding: FragmentHomeBinding

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
                        R.id.btnAll -> taskViewModel.carregarTasksDoUsuario(TaskReloadType.ALL)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding.root
    }

       override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            toggleButton()
            configAdapter()
           taskViewModel.taskListHorizontal.observe(viewLifecycleOwner) { tarefasFixas ->
               taskAdapterHorizontal.updateTasks(tarefasFixas)

               if (tarefasFixas.isEmpty()) {
                   _binding.naoExistemTarefas.visibility = View.VISIBLE
               } else {
                   _binding.naoExistemTarefas.visibility = View.GONE
               }
           }
           taskViewModel.taskList.observe(viewLifecycleOwner) { tarefas ->

               taskAdapter.updateTasks(tarefas)

               if (tarefas.isEmpty()) {
                   _binding.naoExistemTarefas2.visibility = View.VISIBLE
               } else {
                   _binding.naoExistemTarefas2.visibility = View.GONE
               }
           }
           taskViewModel.carregarTasksDoUsuario(TaskReloadType.DAY)
           taskViewModel.carregarTasksDoDiaParaHorizontal()
        }


   override fun onStart() {
        super.onStart()
        taskViewModel.carregarTasksDoUsuario(TaskReloadType.DAY)
        taskViewModel.carregarTasksDoDiaParaHorizontal()
    }

    fun configAdapter() {
        taskAdapter = TaskAdapter { task ->
            val bundle = Bundle().apply { putString("taskId", task.id) }
            val fragment = TasksFragment().apply { arguments = bundle }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }


        taskAdapterHorizontal = TaskAdapterHorizontal { task ->
            val bundle = Bundle().apply { putString("taskId", task.toString()) }
            val fragment = TasksFragment().apply { arguments = bundle }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        _binding.recyclerViewHorizontal.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        _binding.recyclerViewHorizontal.adapter = taskAdapterHorizontal

        _binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        _binding.recyclerView.adapter = taskAdapter
    }

    override fun onResume() {
        super.onResume()
        taskViewModel.carregarTasksDoUsuario(TaskReloadType.DAY)
        taskViewModel.carregarTasksDoDiaParaHorizontal()
    }
}