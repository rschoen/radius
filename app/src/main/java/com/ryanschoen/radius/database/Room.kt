package com.ryanschoen.radius.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VenueDao {
    @Query("select * from databasevenue where active=1 order by distance asc")
    fun getActiveVenues(): LiveData<List<DatabaseVenue>>


    @Query("select * from databasevenue where active=1 and hidden=0 order by distance asc")
    fun getVisibleActiveVenues(): LiveData<List<DatabaseVenue>>

    @Query("select distance from databasevenue where active=1 and hidden=0 order by distance limit 1 offset :n")
    fun getNthVenueDistance(n: Int): LiveData<Double>

    @Query("select max(distance) from databasevenue where active=1 and hidden=0")
    fun getMaximumVenueDistance(): LiveData<Double>

    @Query("select max(distance) from databasevenue where visited=1 and active=1 and hidden=0 and distance <=  (select min(distance) from databasevenue where visited=0 and active=1 and hidden=0 )")
    fun getMaximumAllVisitedDistance(): LiveData<Double>

    @Query("delete from databasevenue")
    fun deleteVenuesData()

    @Query("update databasevenue set visited=:visited, hidden=:hidden, lastUserUpdate=strftime('%s', 'now') where id=:id")
    fun setVenueState(id: String, visited: Boolean, hidden: Boolean)

    @Query("update databasevenue set visited=:visited, hidden=:hidden, lastUserUpdate=:timestamp where id=:id")
    fun setVenueStateWithoutTimestamp(id: String, visited: Boolean, hidden: Boolean, timestamp: Int)

    @Query("update databasevenue set active=0,distance=-1")
    fun deactivateAllVenues()

    @Query("update databasevenue set active=1 where distance>=0 and distance < :distance")
    fun activateVenuesInRange(distance: Double)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertVenue(item: DatabaseVenue)

    @Update
    fun updateVenue(item: DatabaseVenue)

    @Query("select * from databasevenue where id= :id")
    fun getVenueById(id: String): DatabaseVenue

    @Query("update databasevenue set name='', url='', lat=0.0, lng=0.0, reviews=0, rating=0.0, imageUrl=''")
    fun clearDownloadedNetworkData()
}

@Database(
    version = 2,
    entities = [DatabaseVenue::class],
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ]
)
abstract class VenuesDatabase : RoomDatabase() {
    abstract val venueDao: VenueDao
}


private lateinit var INSTANCE: VenuesDatabase

fun getDatabase(context: Context): VenuesDatabase {
    synchronized(VenuesDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                VenuesDatabase::class.java,
                "venues"
            )
                .allowMainThreadQueries()
                .build()
        }
    }
    return INSTANCE
}

