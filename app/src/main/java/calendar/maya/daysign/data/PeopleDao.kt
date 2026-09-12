package calendar.maya.daysign.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleDao {
    @Query("SELECT * FROM people")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(people: List<PersonEntity>)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: Int): PersonEntity?

    @Query("SELECT * FROM user_groups")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("SELECT * FROM user_groups WHERE id = :id")
    suspend fun getGroupById(id: Int): GroupEntity?

    @Query("DELETE FROM people")
    suspend fun clearAllPeople()

    @Query("DELETE FROM user_groups")
    suspend fun clearAllGroups()
}
