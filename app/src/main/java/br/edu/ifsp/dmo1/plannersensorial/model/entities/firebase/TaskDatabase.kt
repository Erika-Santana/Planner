package br.edu.ifsp.dmo1.plannersensorial.model.entities.firebase
import android.icu.util.Calendar
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Priorities
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Task
import br.edu.ifsp.dmo1.plannersensorial.model.entities.TaskReloadType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class TaskDatabase {

    fun createTask(title: String, descricao: String, statusLevel: Priorities, data: Timestamp, onResult: (Boolean) -> Unit){
        val usuario = getUsuarioUid()
        if (usuario != null){
            val db = Firebase.firestore
                val task = hashMapOf(
                    "title" to title,
                    "descricao" to descricao,
                    "statusLevel" to statusLevel,
                    "data" to data,
                    "uid" to usuario)

                db.collection("tasks")
                    .add(task)
                    .addOnSuccessListener {
                        onResult(true)
                    }
                    .addOnFailureListener{
                        onResult(false)
                    }

        }

    }

    private fun getUsuarioUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }


    suspend fun selectTaskByTopic(topic: TaskReloadType): List<Task>{

        val listTask: List<Task>

        when(topic){
            TaskReloadType.DAY -> listTask = reloadByDay()
            TaskReloadType.WEEK -> listTask = reloadByWeek()
            TaskReloadType.MONTH -> listTask = reloadByMonth()
            else -> listTask = reloadByDay()
        }

        return listTask
    }

    suspend fun reloadByDay(): List<Task> {

        val usuario = getUsuarioUid() ?: return emptyList()
        val dayAtual = Calendar.getInstance() //Aqui quando você pega a instancia de Calendar, ele já
        //vem pré setted com o dia atual.
        //Colocamos como tudo zero, para pegar as tarefas em que a pessoa pode acabar criando de madrugada do dia.
        dayAtual.set(Calendar.HOUR_OF_DAY, 0 )
        dayAtual.set(Calendar.MINUTE, 0)
        dayAtual.set(Calendar.SECOND, 0)

        //Pegamos o dia atual + 1, para representar o dia seguinte e dessa forma servir como parametro de parada,
        // para buscar as tarefas do dia em que a pessoa pegou a tarefa.
        val tomorrowDay = Calendar.getInstance()
        tomorrowDay.add(Calendar.DAY_OF_MONTH, 1)
        tomorrowDay.set(Calendar.HOUR_OF_DAY, 0)
        tomorrowDay.set(Calendar.MINUTE, 0)
        tomorrowDay.set(Calendar.SECOND, 0)

        val db = Firebase.firestore
        val lista = db.collection("tasks")
            .whereEqualTo("uid", usuario)
            .whereGreaterThanOrEqualTo("data", Timestamp(dayAtual.time))
            .whereLessThan("data", Timestamp(tomorrowDay.time))
            .get()
            .await()

        return lista.mapNotNull { it.toObject(Task::class.java) }

    }

    suspend fun reloadByWeek(): List<Task>{
        val usuario = getUsuarioUid() ?: return emptyList()
        val dayAtual = Calendar.getInstance()
        dayAtual.set(Calendar.HOUR_OF_DAY, 0 )
        dayAtual.set(Calendar.MINUTE, 0)
        dayAtual.set(Calendar.SECOND, 0)

        val nextWeek = Calendar.getInstance()
        nextWeek.add(Calendar.DAY_OF_MONTH, 7)
        nextWeek.set(Calendar.HOUR_OF_DAY, 0)
        nextWeek.set(Calendar.MINUTE, 0)
        nextWeek.set(Calendar.SECOND, 0)

        val db = Firebase.firestore
        val lista = db.collection("tasks")
            .whereEqualTo("uid", usuario)
            .whereGreaterThanOrEqualTo("data", Timestamp(dayAtual.time))
            .whereLessThan("data", Timestamp(nextWeek.time))
            .get()
            .await()

        return lista.mapNotNull { it.toObject(Task::class.java) }
    }

   suspend fun reloadByMonth(): List<Task> {
       val usuario = getUsuarioUid() ?: return emptyList()
       val monthAtual = Calendar.getInstance()
       monthAtual.set(Calendar.DAY_OF_MONTH, 1)
       monthAtual.set(Calendar.HOUR_OF_DAY, 0)
       monthAtual.set(Calendar.MINUTE, 0)
       monthAtual.set(Calendar.SECOND, 0)

       val allDaysMonth = Calendar.getInstance()
       allDaysMonth.set(Calendar.DAY_OF_MONTH, 1)
       allDaysMonth.add(Calendar.MONTH, 1)
       allDaysMonth.set(Calendar.HOUR_OF_DAY, 0)
       allDaysMonth.set(Calendar.MINUTE, 0)
       allDaysMonth.set(Calendar.SECOND, 0)

       val db = Firebase.firestore
       val lista = db.collection("tasks")
           .whereEqualTo("uid", usuario)
           .whereGreaterThanOrEqualTo("data", Timestamp(monthAtual.time))
           .whereLessThan("data", Timestamp(allDaysMonth.time))
           .get()
           .await()

       return lista.mapNotNull { it.toObject(Task::class.java) }
   }
}