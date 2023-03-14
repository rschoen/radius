package com.ryanschoen.radius.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VenueDao {
    @Query("select * from databasevenue where active=TRUE order by distance asc")
    fun getActiveVenues(): LiveData<List<DatabaseVenue>>


    @Query("select * from databasevenue where active=TRUE and hidden=FALSE order by distance asc")
    fun getVisibleActiveVenues(): LiveData<List<DatabaseVenue>>

    @Query("select distance from databasevenue where active=TRUE and hidden=FALSE order by distance limit 1 offset :n")
    fun getNthVenueDistance(n: Int): LiveData<Double>

    @Query("select max(distance) from databasevenue where active=TRUE and hidden=FALSE")
    fun getMaximumVenueDistance(): LiveData<Double>

    @Query("select max(distance) from databasevenue where visited=TRUE and active=TRUE and hidden=FALSE and distance <=  (select min(distance) from databasevenue where visited=FALSE and active=TRUE and hidden=FALSE )")
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

    @Query("update databasevenue set hidden= NOT hidden where id=:id")
    fun toggleVenueIsHidden(id: String)
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

