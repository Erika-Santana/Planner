package br.edu.ifsp.dmo1.plannersensorial.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Task

class TaskDetailsFragment : Fragment() {
    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getParcelable("task")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_details, container, false)
    }
    companion object {
        private const val TASK_KEY = "task"

        fun newInstance(task: Task): TaskDetailsFragment {
            return TaskDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TASK_KEY, task)
                }
            }
        }
    }

}