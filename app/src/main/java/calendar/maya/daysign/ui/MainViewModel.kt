package calendar.maya.daysign.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import calendar.maya.daysign.data.AppDatabase
import calendar.maya.daysign.data.GroupEntity
import calendar.maya.daysign.data.PersonEntity
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.model.Group
import calendar.maya.daysign.model.MayaDate
import calendar.maya.daysign.model.Person
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.peopleDao()

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    val currentMayaDate: StateFlow<MayaDate> = _currentDate.map {
        MayaCalendar.maya(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MayaCalendar.maya(LocalDate.now()))

    val people: StateFlow<List<Person>> = dao.getAllPeople().map { entities ->
        entities.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<Group>> = dao.getAllGroups().map { entities ->
        entities.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentDate(date: LocalDate) {
        _currentDate.value = date
    }

    fun nextDay() {
        _currentDate.value = _currentDate.value.plusDays(1)
    }

    fun backDay() {
        _currentDate.value = _currentDate.value.minusDays(1)
    }

    fun addPerson(person: Person) {
        viewModelScope.launch {
            dao.insertPerson(person.toEntity())
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            dao.deletePerson(person.toEntity())
        }
    }

    fun addGroup(group: Group) {
        viewModelScope.launch {
            dao.insertGroup(group.toEntity())
        }
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            dao.deleteGroup(group.toEntity())
        }
    }
}

// Mappers
fun PersonEntity.toDomain() = Person(id, name, birthDate, gender, sunrise)
fun Person.toEntity() = PersonEntity(id, name, birthDate, gender, sunrise)
fun GroupEntity.toDomain() = Group(id, name, description, memberIds)
fun Group.toEntity() = GroupEntity(id, name, description, memberIds)
