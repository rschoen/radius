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