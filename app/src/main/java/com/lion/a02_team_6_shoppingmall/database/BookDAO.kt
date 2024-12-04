package com.lion.a02_team_6_shoppingmall.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookDAO {
    @Insert
    fun insertBookData(bookVO: BookVO)

    @Query("""
        select * from BookTable order by bookIdx desc
    """)
    fun selectBookDataAll(): List<BookVO>

    @Query("""
        select * from BookTable
        where bookIdx = :bookIdx
    """)
    fun selectBookDataByBookIdx(bookIdx:Int):BookVO

    @Query("""
        SELECT * FROM BookTable
        WHERE bookType = :bookType
        ORDER BY bookIdx DESC
    """)
    fun selectBookDataByTypes(bookType: Int): List<BookVO>

    @Query("""
        select * from BookTable
        where bookTitle LIKE '%' || :bookTitle || '%' ORDER BY bookIdx DESC
    """)
    fun selectBookDataByBookTitle(bookTitle:String): List<BookVO>

    @Update
    fun modifyBookData(bookVO: BookVO)

    @Delete
    fun deleteBookData(bookVO: BookVO)
}