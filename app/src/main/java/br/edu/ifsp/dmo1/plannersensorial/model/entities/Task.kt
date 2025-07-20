package br.edu.ifsp.dmo1.plannersensorial.model.entities
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.Timestamp

@Parcelize
class Task( var id: String = "",
            var title: String = "",
             var descricao: String = "",
                var stabilty: Int = 0,
             var statusLevel: Priorities = Priorities.OK,
             var data: Timestamp = Timestamp.now(),
             var uid: String = "",
              var imageTask: String = ""): Parcelable {

    constructor() : this("","", "", 0, Priorities.OK, Timestamp.now(), "", "")

    override fun toString(): String {
        return "Task(id='$id', title='$title', descricao='$descricao', statusLevel=$statusLevel, data=$data, uid='$uid')"
    }


}