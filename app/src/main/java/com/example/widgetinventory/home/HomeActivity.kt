package com.example.widgetinventory.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.widgetinventory.adapter.ProductAdapter
import com.example.widgetinventory.databinding.ActivityHomeBinding
import com.example.widgetinventory.login.LoginActivity
import com.example.widgetinventory.model.Product
import com.example.widgetinventory.product.AddProductActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupToolbar()
        setupFab()
        loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(productList) { product ->
            onProductClick(product)
        }

        binding.recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = productAdapter
        }
    }

    private fun setupToolbar() {
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun setupFab() {
        // Aquí va la navegación a AddProductActivity
        binding.fabAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProducts() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewProducts.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            productList.clear()
            productList.addAll(
                listOf(
                    Product("001", "Laptop Dell", 2500000.00),
                    Product("002", "Mouse Logitech", 85000.50),
                    Product("003", "Teclado Mecánico", 350000.00),
                    Product("004", "Monitor Samsung 24\"", 890000.00),
                    Product("005", "Webcam HD", 125000.75)
                )
            )

            productAdapter.updateProducts(productList)

            binding.progressBar.visibility = View.GONE
            binding.recyclerViewProducts.visibility = View.VISIBLE
        }, 2000)
    }

    private fun onProductClick(product: Product) {
        Toast.makeText(this, "Producto: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        prefs.edit { clear() }

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}