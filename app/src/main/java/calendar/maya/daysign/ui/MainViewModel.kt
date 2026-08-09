package calendar.maya.daysign.ui

import android.app.Application
import android.content.Context
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
import org.json.JSONArray
import org.json.JSONObject
import calendar.maya.daysign.ui.widget.DaysignWidget
import androidx.glance.appwidget.updateAll
import androidx.core.content.edit
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.peopleDao()
    private val prefs = application.getSharedPreferences("daysign_prefs", Context.MODE_PRIVATE)

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    val currentMayaDate: StateFlow<MayaDate> = _currentDate.map {
        MayaCalendar.maya(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MayaCalendar.maya(LocalDate.now()))

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val allPeople: StateFlow<List<Person>> = dao.getAllPeople()
        .map { entities ->
            entities.map { entity ->
                val person = entity.toDomain()
                val dateForMaya = if (person.sunrise == "before") person.birthDate.minusDays(1) else person.birthDate
                person.copy(mayaDate = MayaCalendar.maya(dateForMaya))
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val peopleByKin: StateFlow<Map<Int, List<Person>>> = allPeople
        .map { list ->
            list.groupBy { it.mayaDate?.kin ?: 0 }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val people: StateFlow<List<Person>> = combine(allPeople, _searchQuery) { list, query ->
        if (query.isEmpty()) list
        else list.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<Group>> = dao.getAllGroups().map { entities ->
        entities.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _favoritesIds = MutableStateFlow<Set<Int>>(
        prefs.getStringSet("favorites_ids", emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()
    )
    val favoritesIds = _favoritesIds.asStateFlow()

    fun toggleFavorite(personId: Int) {
        val current = _favoritesIds.value.toMutableSet()
        if (current.contains(personId)) {
            current.remove(personId)
        } else {
            current.add(personId)
        }
        _favoritesIds.value = current
        prefs.edit {
            putStringSet("favorites_ids", current.map { it.toString() }.toSet())
        }
    }

    private val _defaultGroupId = MutableStateFlow<Int?>(
        prefs.getInt("default_group_id", -1).takeIf { it != -1 }
    )
    val defaultGroupId = _defaultGroupId.asStateFlow()

    val effectiveDefaultGroupMembers: StateFlow<List<Int>?> = combine(
        _defaultGroupId,
        groups,
        _favoritesIds
    ) { defaultId, allGroups, favIds ->
        if (defaultId != null) {
            allGroups.find { it.id == defaultId }?.memberIds
        } else if (favIds.isNotEmpty()) {
            favIds.toList()
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setDefaultGroup(id: Int?) {
        _defaultGroupId.value = id
        prefs.edit {
            if (id != null) {
                putInt("default_group_id", id)
            } else {
                remove("default_group_id")
            }
        }
    }

    private val _navigateToCharacter = MutableSharedFlow<Int>()
    val navigateToCharacter = _navigateToCharacter.asSharedFlow()

    fun navigateToCharacter(characterId: Int) {
        viewModelScope.launch {
            _navigateToCharacter.emit(characterId)
        }
    }

    private val _navigateToPage = MutableSharedFlow<Int>()
    val navigateToPage = _navigateToPage.asSharedFlow()

    private val _pendingPersonId = MutableStateFlow<Int?>(null)
    val pendingPersonId = _pendingPersonId.asStateFlow()

    fun navigateToPerson(id: Int) {
        viewModelScope.launch {
            _pendingPersonId.value = id
            _navigateToPage.emit(2) // Switch to People tab
        }
    }

    fun consumePendingPersonId() {
        _pendingPersonId.value = null
    }

    fun navigateToPage(pageIndex: Int, date: LocalDate? = null) {
        date?.let { setCurrentDate(it) }
        viewModelScope.launch {
            _navigateToPage.emit(pageIndex)
        }
    }

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
            DaysignWidget().updateAll(getApplication())
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            dao.deletePerson(person.toEntity())
            DaysignWidget().updateAll(getApplication())
        }
    }

    fun addGroup(group: Group, onCreated: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val id = dao.insertGroup(group.toEntity())
            onCreated?.invoke(id.toInt())
            DaysignWidget().updateAll(getApplication())
        }
    }

    fun addPersonToGroup(personId: Int, groupId: Int) {
        viewModelScope.launch {
            val group = dao.getGroupById(groupId)?.toDomain()
            group?.let {
                if (personId !in it.memberIds) {
                    val updatedIds = it.memberIds + personId
                    dao.insertGroup(it.copy(memberIds = updatedIds).toEntity())
                    DaysignWidget().updateAll(getApplication())
                }
            }
        }
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            if (_defaultGroupId.value == group.id) {
                setDefaultGroup(null)
            }
            dao.deleteGroup(group.toEntity())
            DaysignWidget().updateAll(getApplication())
        }
    }

    fun importData(jsonString: String) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonString)
                val peopleArray = root.getJSONArray("people")
                val groupsArray = root.getJSONArray("groups")

                val newPeople = mutableListOf<PersonEntity>()
                for (i in 0 until peopleArray.length()) {
                    val p = peopleArray.getJSONObject(i)
                    val dateStr = p.optString("date", p.optString("birthDate", ""))
                    val dateParts = dateStr.split("-")
                    val date = if (dateParts.size == 3) {
                        LocalDate.of(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt())
                    } else {
                        LocalDate.now()
                    }
                    newPeople.add(
                        PersonEntity(
                            id = p.getInt("id"),
                            name = p.getString("name"),
                            birthDate = date,
                            gender = p.getString("gender"),
                            sunrise = p.getString("sunrise")
                        )
                    )
                }

                val newGroups = mutableListOf<GroupEntity>()
                for (i in 0 until groupsArray.length()) {
                    val g = groupsArray.getJSONObject(i)
                    val membersArray = g.getJSONArray("members")
                    val members = mutableListOf<Int>()
                    for (j in 0 until membersArray.length()) {
                        members.add(membersArray.getString(j).toInt())
                    }
                    newGroups.add(
                        GroupEntity(
                            id = g.getInt("id"),
                            name = g.getString("name"),
                            description = g.optString("description", ""),
                            memberIds = members
                        )
                    )
                }

                dao.clearAllPeople()
                dao.clearAllGroups()
                newPeople.forEach { dao.insertPerson(it) }
                newGroups.forEach { dao.insertGroup(it) }
                DaysignWidget().updateAll(getApplication())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun generateExportJson(): String {
        try {
            val root = JSONObject()
            
            val peopleArray = JSONArray()
            allPeople.value.forEach { person ->
                val p = JSONObject()
                p.put("id", person.id)
                p.put("name", person.name)
                p.put("date", person.birthDate.toString())
                p.put("gender", person.gender)
                p.put("sunrise", person.sunrise)
                peopleArray.put(p)
            }
            root.put("people", peopleArray)

            val groupsArray = JSONArray()
            groups.value.forEach { group ->
                val g = JSONObject()
                g.put("id", group.id)
                g.put("name", group.name)
                g.put("description", group.description)
                val membersArray = JSONArray()
                group.memberIds.forEach { membersArray.put(it.toString()) }
                g.put("members", membersArray)
                groupsArray.put(g)
            }
            root.put("groups", groupsArray)
            root.put("version", 2)
            
            return root.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}

// Mappers
fun PersonEntity.toDomain() = Person(id, name, birthDate, gender, sunrise)
fun Person.toEntity() = PersonEntity(id, name, birthDate, gender, sunrise)
fun GroupEntity.toDomain() = Group(id, name, description, memberIds)
fun Group.toEntity() = GroupEntity(id, name, description, memberIds)
