package com.example.widgetinventory.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.widgetinventory.model.Product

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "InventoryDB"
        private const val DATABASE_VERSION = 1

        private const val TABLE_PRODUCTS = "products"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODE = "code"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PRICE = "price"
        private const val COLUMN_QUANTITY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CODE TEXT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_QUANTITY INTEGER NOT NULL
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
    }

    fun insertProduct(product: Product): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CODE, product.id)
            put(COLUMN_NAME, product.name)
            put(COLUMN_PRICE, product.price)
            put(COLUMN_QUANTITY, product.quantity)
        }
        val result = db.insert(TABLE_PRODUCTS, null, values)
        db.close()
        return result
    }

    fun getAllProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_PRODUCTS,
            arrayOf(COLUMN_CODE, COLUMN_NAME, COLUMN_PRICE, COLUMN_QUANTITY),
            null, null, null, null,
            "$COLUMN_NAME ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val code = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CODE))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                list.add(Product(code, name, price, quantity))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }

    fun getProductById(productCode: String): Product? {
        val db = readableDatabase
        var product: Product? = null

        val cursor = db.query(
            TABLE_PRODUCTS,
            arrayOf(COLUMN_CODE, COLUMN_NAME, COLUMN_PRICE, COLUMN_QUANTITY),
            "$COLUMN_CODE = ?",
            arrayOf(productCode),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val code = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CODE))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
            val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))

            product = Product(code, name, price, quantity)
        }

        cursor.close()
        db.close()
        return product
    }

    fun deleteProduct(productCode: String): Int {
        val db = writableDatabase
        val result = db.delete(
            TABLE_PRODUCTS,
            "$COLUMN_CODE = ?",
            arrayOf(productCode)
        )
        db.close()
        return result
    }

    fun updateProduct(product: Product): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, product.name)        // NOT NULL en la tabla
            put(COLUMN_PRICE, product.price)      // REAL
            put(COLUMN_QUANTITY, product.quantity) // INTEGER
        }

        val rows = db.update(
            TABLE_PRODUCTS,
            values,
            "$COLUMN_CODE = ?",
            arrayOf(product.id) // Usamos el "code" como ID lógico
        )
        db.close()
        return rows
    }

}