package br.edu.ifsp.dmo1.plannersensorial.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Task
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(private var tasks: List<Task> = emptyList(),
                  private val onItemClick: (Task) -> Unit ): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

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
        holder.titulo.text = task.title
        holder.horario.text = formatarTimestamp(task.data)
        holder.prioridade.text = task.statusLevel.toString()
        holder.btnOpen.setOnClickListener {
            onItemClick(task)
        }
    }

    fun formatarTimestamp(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val formatador = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatador.format(date)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo = itemView.findViewById<TextView>(R.id.txtTitulo)
        val horario = itemView.findViewById<TextView>(R.id.txtHorario)
        val prioridade = itemView.findViewById<Button>(R.id.btnPrioridade)
        val btnOpen = itemView.findViewById<ImageButton>(R.id.btnOpenTask)
    }

    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }

}