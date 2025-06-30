package br.edu.ifsp.dmo1.plannersensorial.model.entities
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.Timestamp

@Parcelize
class Task(private val title: String,
           private val description: String,
           private val priority: Priorities = Priorities.OK,
           private val data: Timestamp = Timestamp.now(),
           private val uid: String = ""): Parcelable {

    constructor() : this("", "", Priorities.OK, Timestamp.now(), "")

    fun getTitle(): String{
        return title
    }
    fun getDescription(): String{
        return description
    }
    fun getData(): Timestamp{
        return data
    }
    fun getPriority(): Priorities{
        return priority
    }
    fun getUID(): String {
        return uid
    }


}