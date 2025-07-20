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

    private val _taskListHorizontal = MutableLiveData<List<Task>>()
    val taskListHorizontal: LiveData<List<Task>> = _taskListHorizontal

    private val _selectedTask = MutableLiveData<Task?>()
    val selectedTask: LiveData<Task?> = _selectedTask


    fun carregarTasksDoUsuario(type: TaskReloadType) {
        viewModelScope.launch {
            val lista = databaseTask.selectTaskByTopic(type)
            _taskList.postValue(lista)
        }
    }

    fun carregarTasksDoDiaParaHorizontal() {
        viewModelScope.launch {
            val lista = databaseTask.reloadByDay()
            _taskListHorizontal.postValue(lista)
        }
    }

    fun createTaskAndReturn(
        title: String,
        descricao: String,
        statusLevel: Priorities,
        data: Timestamp,
        image: String,
        callback: (Boolean, Task?) -> Unit
    ) {
        databaseTask.createTask(title, descricao, statusLevel, data, image) { success, task ->
            callback(success, task)
            _status.postValue(true)

            if (success) {
                carregarTasksDoUsuario(TaskReloadType.DAY)
                carregarTasksDoDiaParaHorizontal()
            }
        }
    }

    fun getTaskById(taskId: String) {
        databaseTask.getTaskById(taskId) { task ->
            _selectedTask.postValue(task)
        }
    }
    fun deleteTask(taskId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            databaseTask.deleteTaskById(taskId) { success ->
                if (success) {
                    carregarTasksDoUsuario(TaskReloadType.ALL)
                }
                callback(success)
            }
        }
    }


}