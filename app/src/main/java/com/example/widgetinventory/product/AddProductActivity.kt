package com.example.widgetinventory.product

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.widgetinventory.R
import com.example.widgetinventory.databinding.ActivityAddProductBinding
import com.google.android.material.snackbar.Snackbar

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Configurar el color del icono de navegación
        binding.toolbar.navigationIcon?.setTint(getColor(android.R.color.white))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupInputValidation() {
        // Limitar código del producto a 4 dígitos
        binding.etProductCode.filters = arrayOf(
            android.text.InputFilter.LengthFilter(4)
        )

        // Limitar nombre del artículo a 40 caracteres
        binding.etProductName.filters = arrayOf(
            android.text.InputFilter.LengthFilter(40)
        )

        // Limitar precio a 20 dígitos
        binding.etPrice.filters = arrayOf(
            android.text.InputFilter.LengthFilter(20)
        )
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveProduct()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val code = binding.etProductCode.text.toString().trim()
        val name = binding.etProductName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().trim()

        when {
            code.isEmpty() -> {
                binding.tilProductCode.error = "Ingrese el código del producto"
                return false
            }
            name.isEmpty() -> {
                binding.tilProductCode.error = null
                binding.tilProductName.error = "Ingrese el nombre del artículo"
                return false
            }
            price.isEmpty() -> {
                binding.tilProductName.error = null
                binding.tilPrice.error = "Ingrese el precio"
                return false
            }
            quantity.isEmpty() -> {
                binding.tilPrice.error = null
                binding.tilQuantity.error = "Ingrese la cantidad"
                return false
            }
            else -> {
                // Limpiar todos los errores
                binding.tilProductCode.error = null
                binding.tilProductName.error = null
                binding.tilPrice.error = null
                binding.tilQuantity.error = null
                return true
            }
        }
    }

    private fun saveProduct() {
        // Aquí implementarás la lógica para guardar el producto
        // Por ejemplo, guardarlo en una base de datos o enviarlo a un servidor

        val code = binding.etProductCode.text.toString()
        val name = binding.etProductName.text.toString()
        val price = binding.etPrice.text.toString()
        val quantity = binding.etQuantity.text.toString()

        // Mostrar mensaje de éxito
        Snackbar.make(
            binding.root,
            "Producto guardado exitosamente",
            Snackbar.LENGTH_SHORT
        ).show()

        // Volver a la pantalla anterior después de guardar
        finish()
    }
}