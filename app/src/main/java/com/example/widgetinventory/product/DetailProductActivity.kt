package com.example.widgetinventory.product

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.widgetinventory.database.DatabaseHelper
import com.example.widgetinventory.databinding.ActivityDetailProductBinding
import com.example.widgetinventory.home.HomeActivity
import com.example.widgetinventory.model.Product
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProductBinding
    private lateinit var dbHelper: DatabaseHelper
    private var product: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupToolbar()
        loadProductData()
        setupDeleteButton()
        setupEditButton()
    }

    /** Configuración de la toolbar **/
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Detalle del producto"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.navigationIcon?.setTint(getColor(android.R.color.white))
        binding.toolbar.setNavigationOnClickListener {
            navigateToHome()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateToHome()
        return true
    }

    /** Carga los datos del producto recibido por intent **/
    private fun loadProductData() {
        val productId = intent.getStringExtra("PRODUCT_ID")
            ?: intent.getStringExtra("PRODUCT_CODE")
            ?: intent.getStringExtra("product_id")
            ?: intent.getStringExtra("product_code")

        if (productId.isNullOrBlank()) {
            android.widget.Toast.makeText(
                this,
                "No se recibió el identificador del producto",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        product = dbHelper.getProductById(productId)
        if (product == null) {
            android.widget.Toast.makeText(
                this,
                "Producto no encontrado",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        product?.let { p ->
            // Nombre arriba (negrita a la izquierda)
            binding.tvProductName.text = p.name ?: "—"

            // Valor a la derecha con formato: $ 23.000,00
            binding.tvProductPrice.text = formatCurrency(p.price)

            // Cantidad a la derecha (si quieres con miles: usa NumberFormat.getIntegerInstance(Locale("es","CO")).format(p.quantity))
            binding.tvProductQuantity.text = p.quantity.toString()

            // Total a la derecha con formato: $ 5.888.000,00
            val total = p.price * p.quantity
            binding.tvProductTotal.text = formatCurrency(total)
        }
    }

    /** Botón eliminar con diálogo de confirmación **/
    private fun setupDeleteButton() {
        binding.btnDelete.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Deseas eliminar este producto?")
                .setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .setPositiveButton("Sí") { _: DialogInterface, _: Int ->
                    deleteProduct()
                }
                .create()
            dialog.show()
        }
    }

    private fun deleteProduct() {
        product?.let {
            val result = dbHelper.deleteProduct(it.id)
            if (result > 0) {
                Snackbar.make(binding.root, "Producto eliminado", Snackbar.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                Snackbar.make(binding.root, "Error al eliminar el producto", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    /** Ícono flotante para editar **/
    private fun setupEditButton() {
        binding.fabEdit.setOnClickListener {
            val intent = Intent(this, EditProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", product?.id)
            startActivity(intent)
            finish()
        }
    }

    /** Navegar al Home Inventario **/
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun formatCurrency(value: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return "$ ${nf.format(value)}"
    }
}
