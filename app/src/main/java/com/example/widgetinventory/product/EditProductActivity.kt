package com.example.widgetinventory.product

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.widgetinventory.database.DatabaseHelper
import com.example.widgetinventory.databinding.ActivityEditProductBinding
import com.example.widgetinventory.home.HomeActivity
import com.example.widgetinventory.model.Product
import com.google.android.material.snackbar.Snackbar

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding
    private lateinit var dbHelper: DatabaseHelper
    private var product: Product? = null
    private var productIdArg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupToolbar()
        loadProductData()
        setupInputs()
        setupSaveButton()
        bindValidation()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Editar Producto"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setTitleTextColor(getColor(android.R.color.white))
        binding.toolbar.navigationIcon?.setTint(getColor(android.R.color.white))
        binding.toolbar.setNavigationOnClickListener {
            navigateToDetail()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateToDetail()
        return true
    }

    /** Cargar producto por ID desde el Intent y precargar campos **/
    private fun loadProductData() {
        productIdArg = intent.getStringExtra("PRODUCT_ID")
            ?: intent.getStringExtra("PRODUCT_CODE")
                    ?: intent.getStringExtra("product_id")
                    ?: intent.getStringExtra("product_code")

        if (productIdArg.isNullOrBlank()) {
            Toast.makeText(this, "No se recibió el identificador del producto", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        product = dbHelper.getProductById(productIdArg!!)
        if (product == null) {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        product?.let { p ->
            // Mostrar Id en el TextView (ya no hay etId)
            binding.tvId.text = "Id: ${p.id}"

            // Precargar campos a modificar
            binding.etName.setText(p.name ?: "")
            binding.etPrice.setText(
                if (p.price % 1.0 == 0.0) p.price.toLong().toString()
                else p.price.toString()
            )
            binding.etQuantity.setText(p.quantity.toString())
        }

        // Estado inicial del botón
        updateButtonEnabledState()
    }

    /** Configurar filtros y tipos de entrada **/
    private fun setupInputs() {
        // Criterio 6: Nombre máx 40 chars
        binding.etName.filters = arrayOf(InputFilter.LengthFilter(40))

        // Criterio 7: Precio solo números (permitimos decimal) máx 20 dígitos
        binding.etPrice.keyListener = DigitsKeyListener.getInstance("0123456789.")
        binding.etPrice.filters = arrayOf(InputFilter.LengthFilter(20))

        // Criterio 8: Cantidad solo números máx 4 dígitos
        binding.etQuantity.keyListener = DigitsKeyListener.getInstance("0123456789")
        binding.etQuantity.filters = arrayOf(InputFilter.LengthFilter(4))
    }

    /** Activar/Desactivar botón según validación **/
    private fun bindValidation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateButtonEnabledState()
            }
        }

        binding.etName.addTextChangedListener(textWatcher)
        binding.etPrice.addTextChangedListener(textWatcher)
        binding.etQuantity.addTextChangedListener(textWatcher)
    }

    private fun updateButtonEnabledState() {
        val nameOk = !binding.etName.text.isNullOrBlank()
        val priceOk = !binding.etPrice.text.isNullOrBlank()
        val qtyOk = !binding.etQuantity.text.isNullOrBlank()

        val priceValid = isPriceValid(binding.etPrice.text?.toString())
        val qtyValid = isQuantityValid(binding.etQuantity.text?.toString())

        binding.btnEdit.isEnabled = nameOk && priceOk && qtyOk && priceValid && qtyValid
    }

    private fun isPriceValid(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        if (value.count { it == '.' } > 1) return false
        if (value == "." || value == "-") return false
        return try {
            value.toDouble()
            true
        } catch (_: NumberFormatException) {
            false
        }
    }

    private fun isQuantityValid(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        if (!TextUtils.isDigitsOnly(value)) return false
        return try {
            val q = value.toInt()
            q >= 0
        } catch (_: NumberFormatException) {
            false
        }
    }

    /** Guardar cambios: actualizar en SQLite y navegar a Home mostrando lista actualizada **/
    private fun setupSaveButton() {
        binding.btnEdit.setOnClickListener {
            if (!binding.btnEdit.isEnabled) return@setOnClickListener

            val name = binding.etName.text?.toString()?.trim().orEmpty()
            val priceStr = binding.etPrice.text?.toString()?.trim().orEmpty()
            val qtyStr = binding.etQuantity.text?.toString()?.trim().orEmpty()

            if (name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
                Snackbar.make(binding.root, "Completa todos los campos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = try { priceStr.toDouble() } catch (e: Exception) { Double.NaN }
            val quantity = try { qtyStr.toInt() } catch (e: Exception) { -1 }

            if (price.isNaN() || quantity < 0) {
                Snackbar.make(binding.root, "Revisa los valores de precio y cantidad", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val current = product ?: run {
                Snackbar.make(binding.root, "Producto no disponible", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val updated = current.copy(
                name = name,
                price = price,
                quantity = quantity
            )

            val rows = dbHelper.updateProduct(updated)
            if (rows > 0) {
                Snackbar.make(binding.root, "Producto actualizado", Snackbar.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                Snackbar.make(binding.root, "Error al actualizar el producto", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToDetail() {
        val intent = Intent(this, DetailProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", productIdArg ?: product?.id)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
