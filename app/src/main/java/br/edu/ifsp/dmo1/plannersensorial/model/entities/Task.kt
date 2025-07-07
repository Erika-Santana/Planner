package br.edu.ifsp.dmo1.plannersensorial.model.entities
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.Timestamp

@Parcelize
class Task(  var title: String = "",
             var descricao: String = "",
             var statusLevel: Priorities = Priorities.OK,
             var data: Timestamp = Timestamp.now(),
             var uid: String = ""): Parcelable {

    constructor() : this("", "", Priorities.OK, Timestamp.now(), "")

    override fun toString(): String {
        return "Task(title='$title', descricao='$descricao', statusLevel=$statusLevel, data=$data, uid='$uid')"
    }


}