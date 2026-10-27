package app.unwritten.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import app.unwritten.domain.Book
import app.unwritten.domain.Kind
import app.unwritten.domain.Review
import app.unwritten.domain.Rule
import app.unwritten.domain.Style

class Converters {
    @TypeConverter fun kind(k: Kind): String = k.name
    @TypeConverter fun kind(s: String): Kind = Kind.of(s)
    @TypeConverter fun style(k: Style): String = k.name
    @TypeConverter fun style(s: String): Style = Style.of(s)
    @TypeConverter fun rule(k: Rule): String = k.name
    @TypeConverter fun rule(s: String): Rule = Rule.entries.firstOrNull { it.name == s } ?: Rule.NONE
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM book ORDER BY created, id") suspend fun books(): List<Book>
    @Query("SELECT * FROM review ORDER BY at, id") suspend fun reviews(): List<Review>

    @Insert suspend fun insert(b: Book): Long
    @Update suspend fun update(b: Book)
    @Insert suspend fun insert(r: Review): Long
}

@Database(entities = [Book::class, Review::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ShopDb : RoomDatabase() {
    abstract fun dao(): ShopDao

    companion object {
        fun create(c: Context): ShopDb = Room.databaseBuilder(c, ShopDb::class.java, "unwritten.db").build()
    }
}
