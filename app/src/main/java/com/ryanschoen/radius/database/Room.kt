package com.ryanschoen.radius.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VenueDao {
    @Query("select * from databasevenue where active=TRUE order by distance asc")
    fun getVenues(): LiveData<List<DatabaseVenue>>

    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg venues: DatabaseVenue)*/

    @Query("select distance from databasevenue where active=TRUE order by distance limit 1 offset :n")
    fun getNthVenueDistance(n: Int): LiveData<Double>

    @Query("select max(distance) from databasevenue where active=true")
    fun getMaximumVenueDistance(): LiveData<Double>

    @Query("select max(distance) from databasevenue where visited=TRUE and active=TRUE and distance <=  (select min(distance) from databasevenue where visited=FALSE and active=TRUE)")
    fun getMaximumAllVisitedDistance(): LiveData<Double>

    @Query("delete from databasevenue")
    fun deleteVenuesData()

    @Query("update databasevenue set visited=:visited where id=:id")
    fun setVenueVisited(id: String, visited: Boolean)

    @Query("update databasevenue set active=FALSE,distance=-1")
    fun deactivateAllVenues()

    @Query("update databasevenue set active=TRUE where distance>=0 and distance < :distance")
    fun activateVenuesInRange(distance: Double)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertVenue(item: DatabaseVenue)

    @Update
    fun updateVenue(item: DatabaseVenue)

    @Query("select * from databasevenue where id= :id")
    fun getVenueById(id: String): DatabaseVenue
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

