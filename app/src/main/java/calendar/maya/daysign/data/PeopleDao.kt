package calendar.maya.daysign.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleDao {
    @Query("SELECT * FROM people")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Query("SELECT * FROM user_groups")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)
}
