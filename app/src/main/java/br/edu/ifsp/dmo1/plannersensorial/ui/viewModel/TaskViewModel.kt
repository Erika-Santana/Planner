package br.edu.ifsp.dmo1.plannersensorial.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Priorities
import br.edu.ifsp.dmo1.plannersensorial.model.entities.Task
import br.edu.ifsp.dmo1.plannersensorial.model.entities.TaskReloadType
import br.edu.ifsp.dmo1.plannersensorial.model.entities.firebase.TaskDatabase
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class TaskViewModel: ViewModel() {

    private var databaseTask: TaskDatabase = TaskDatabase()

    private val _status = MutableLiveData<Boolean>()
    val status: LiveData<Boolean> = _status

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>> = _taskList


    fun carregarTasksDoUsuario(type: TaskReloadType) {
        viewModelScope.launch {
            val lista = TaskDatabase().selectTaskByTopic(type)
            _taskList.postValue(lista)
        }
    }

    fun verificaCreateTask(title: String, descricao: String, statusLevel: Priorities, data: Timestamp) {
        databaseTask.createTask(title, descricao, statusLevel, data) { status ->
            _status.postValue(status)
        }
    }

}