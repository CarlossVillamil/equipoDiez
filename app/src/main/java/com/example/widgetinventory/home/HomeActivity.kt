package com.example.widgetinventory.home

import android.appwidget.AppWidgetManager
import android.content.ComponentName
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
import com.example.widgetinventory.database.DatabaseHelper
import com.example.widgetinventory.databinding.ActivityHomeBinding
import com.example.widgetinventory.login.LoginActivity
import com.example.widgetinventory.model.Product
import com.example.widgetinventory.product.AddProductActivity
import com.example.widgetinventory.widget.InventoryWidgetProvider
import com.example.widgetinventory.product.DetailProductActivity

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
        binding.fabAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProducts() {
        val dbHelper = DatabaseHelper(this)
        val products = dbHelper.getAllProducts()

        productList.clear()
        productList.addAll(products)
        productAdapter.updateProducts(productList)
    }

    private fun onProductClick(product: Product) {
        detailsProduct(product)
    }

    private fun detailsProduct(product: Product) {
        val intent = Intent(this, DetailProductActivity::class.java).apply {
            putExtra("product_id", product.id)
            putExtra("product_name", product.name)
            putExtra("product_price", product.price)
        }
        startActivity(intent)
    }

    private fun logout() {
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        prefs.edit { clear() }

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
        updateWidget()
    }

    private fun updateWidget() {
        val intent = Intent(this, InventoryWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, InventoryWidgetProvider::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}