package br.edu.ifsp.dmo1.plannersensorial.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Task
import br.edu.ifsp.dmo1.plannersensorial.ui.Base64Converter
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapterHorizontal(private var tasks: List<Task> = emptyList(),
                            private val onItemClick: (Task) -> Unit ): RecyclerView.Adapter<TaskAdapterHorizontal.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return TaskViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return tasks.size
    }


    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        val bitmap = Base64Converter.stringToBitmap(task.imageTask)
        holder.imagem.setImageBitmap(bitmap)
        holder.titulo.text = task.title
        holder.horario.text = formatarTimestamp(task.data)
        holder.prioridade.text = task.statusLevel.toString()
    }

    fun formatarTimestamp(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val formatador = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatador.format(date)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagem = itemView.findViewById<ImageView>(R.id.taskImage)
        val titulo = itemView.findViewById<TextView>(R.id.taskTitulo)
        val horario = itemView.findViewById<TextView>(R.id.taskHoras)
        val prioridade = itemView.findViewById<Button>(R.id.btnPrioridade)
    }

    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }

}