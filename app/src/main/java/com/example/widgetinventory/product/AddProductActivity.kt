package com.example.widgetinventory.product

import android.os.Bundle
import android.text.InputFilter
import androidx.appcompat.app.AppCompatActivity
import com.example.widgetinventory.database.DatabaseHelper
import com.example.widgetinventory.databinding.ActivityAddProductBinding
import com.example.widgetinventory.model.Product
import com.google.android.material.snackbar.Snackbar

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupToolbar()
        setupInputValidation()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Agregar producto"
        }
        binding.toolbar.navigationIcon?.setTint(getColor(android.R.color.white))
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupInputValidation() {
        val digitsOnly = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) if (!source[i].isDigit()) return@InputFilter ""
            null
        }

        // Código: máx 4 y solo dígitos
        binding.etProductCode.filters = arrayOf<InputFilter>(
            InputFilter.LengthFilter(4),
            digitsOnly
        )

        // Nombre: máx 40 (texto libre)
        binding.etProductName.filters = arrayOf<InputFilter>(
            InputFilter.LengthFilter(40)
        )

        // Precio: máx 20 y solo dígitos
        binding.etPrice.filters = arrayOf<InputFilter>(
            InputFilter.LengthFilter(20),
            digitsOnly
        )

        // Cantidad: máx 9 y solo dígitos
        binding.etQuantity.filters = arrayOf<InputFilter>(
            InputFilter.LengthFilter(9),
            digitsOnly
        )
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) saveProduct()
        }
    }

    private fun validateInputs(): Boolean {
        val code = binding.etProductCode.text?.toString()?.trim().orEmpty()
        val name = binding.etProductName.text?.toString()?.trim().orEmpty()
        val priceStr = binding.etPrice.text?.toString()?.trim().orEmpty()
        val qtyStr = binding.etQuantity.text?.toString()?.trim().orEmpty()

        // Código
        if (code.isEmpty()) {
            binding.tilProductCode.error = "Ingrese el código del producto"
            return false
        } else {
            binding.tilProductCode.error = null
        }

        // Nombre
        if (name.isEmpty()) {
            binding.tilProductName.error = "Ingrese el nombre del artículo"
            return false
        } else {
            binding.tilProductName.error = null
        }

        // Precio
        val price = priceStr.toDoubleOrNull()
        if (priceStr.isEmpty()) {
            binding.tilPrice.error = "Ingrese el precio"
            return false
        } else if (price == null || price <= 0.0) {
            binding.tilPrice.error = "Ingrese un precio válido"
            return false
        } else {
            binding.tilPrice.error = null
        }

        // Cantidad
        val qty = qtyStr.toIntOrNull()
        if (qtyStr.isEmpty()) {
            binding.tilQuantity.error = "Ingrese la cantidad"
            return false
        } else if (qty == null || qty < 0) {
            binding.tilQuantity.error = "Ingrese una cantidad válida"
            return false
        } else {
            binding.tilQuantity.error = null
        }

        return true
    }

    private fun saveProduct() {
        val code = binding.etProductCode.text!!.toString().trim()
        val name = binding.etProductName.text!!.toString().trim()
        val price = binding.etPrice.text!!.toString().trim().toDouble()
        val quantity = binding.etQuantity.text!!.toString().trim().toInt()

        val product = Product(id = code, name = name, price = price, quantity = quantity)
        val result = dbHelper.insertProduct(product)

        if (result != -1L) {
            Snackbar.make(binding.root, "Producto guardado", Snackbar.LENGTH_SHORT).show()
            // Devuelve OK para que Home refresque si usas ActivityResult,
            // o simplemente en Home haz loadProducts() en onResume().
            setResult(RESULT_OK)
            finish()
        } else {
            Snackbar.make(binding.root, "Error al guardar", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Opcional: cerrar helper si quieres ser explícito (SQLiteOpenHelper maneja conexiones internamente)
        // dbHelper.close()
    }
}