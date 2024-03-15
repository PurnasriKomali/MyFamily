package com.purnasri.myfamily

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDoa {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contactModel: ContactModel?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(contactModelList: List<ContactModel?>?)

    @get:Query("SELECT * FROM contactmodel")
    val allContacts: LiveData<List<ContactModel?>?>?

}
