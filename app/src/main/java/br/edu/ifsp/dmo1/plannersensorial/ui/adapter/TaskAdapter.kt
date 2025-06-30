package br.edu.ifsp.dmo1.plannersensorial.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Task

class TaskAdapter(private var tasks: MutableList<Task>, private val onItemClick: (Task) -> Unit ): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_tasks, parent, false)
        return TaskViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return tasks.size
    }


    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.titulo.text = task.getTitle()
        holder.horario.text = task.getData().toString()
        holder.prioridade.text = task.getPriority().toString()
        holder.btnOpen.setOnClickListener {
            onItemClick(task)
        }
    }


    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo = itemView.findViewById<TextView>(R.id.txtTitulo)
        val horario = itemView.findViewById<TextView>(R.id.txtHorario)
        val prioridade = itemView.findViewById<Button>(R.id.btnPrioridade)
        val btnOpen = itemView.findViewById<ImageButton>(R.id.btnOpenTask)
    }

    fun updateTasks(task: List<Task>){
        this.tasks.clear()
        this.tasks.addAll(task)
        this.notifyDataSetChanged()
    }

}