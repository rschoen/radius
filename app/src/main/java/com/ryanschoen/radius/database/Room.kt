package com.ryanschoen.radius.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VenueDao {
    @Query("select * from databasevenue")
    fun getVenues(): LiveData<List<DatabaseVenue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg venues: DatabaseVenue)

    @Query("select distance from databasevenue order by distance limit 1 offset 10")
    fun getTenthVenue(): LiveData<Double>

    @Query("select max(distance) from databasevenue")
    fun getMaximumVenueDistance(): LiveData<Double>

    @Query("select max(distance) from databasevenue where visited=TRUE and distance <=  (select min(distance)  from databasevenue where visited=FALSE)")
    fun getMaximumAllVisitedDistance(): LiveData<Double>

    @Query("delete from databasevenue")
    fun deleteVenuesData()

    @Query("update databasevenue set visited=:visited where id=:id")
    fun setVenueVisited(id: String, visited: Boolean)
}

@Database(entities = [DatabaseVenue::class], version=1)
abstract class VenuesDatabase : RoomDatabase() {
    abstract val venueDao: VenueDao
}


private lateinit var INSTANCE: VenuesDatabase

fun getDatabase(context: Context): VenuesDatabase {
    synchronized(VenuesDatabase::class.java) {
        if(!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext, VenuesDatabase::class.java, "venues")
                .build()
        }
    }
    return INSTANCE
}