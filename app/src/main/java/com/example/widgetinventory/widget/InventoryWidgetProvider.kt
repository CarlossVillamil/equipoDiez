package com.example.widgetinventory.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.widgetinventory.R
import com.example.widgetinventory.database.DatabaseHelper
import com.example.widgetinventory.login.LoginActivity
import java.text.NumberFormat
import java.util.Locale

class InventoryWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_BALANCE = "com.example.widgetinventory.TOGGLE_BALANCE"
        const val ACTION_MANAGE_INVENTORY = "com.example.widgetinventory.MANAGE_INVENTORY"
        const val PREFS_NAME = "InventoryWidgetPrefs"
        const val PREF_BALANCE_VISIBLE = "balance_visible"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TOGGLE_BALANCE -> {
                toggleBalanceVisibility(context)
            }
            ACTION_MANAGE_INVENTORY -> {
                openLoginActivity(context)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_inventory)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isBalanceVisible = prefs.getBoolean(PREF_BALANCE_VISIBLE, false)

        if (isBalanceVisible) {
            val totalBalance = calculateTotalBalance(context)
            val formattedBalance = formatBalance(totalBalance)
            views.setTextViewText(R.id.tvBalance, "$ $formattedBalance")
            views.setImageViewResource(R.id.ivEyeToggle, R.drawable.ic_eye_closed)
        } else {
            views.setTextViewText(R.id.tvBalance, "$ ****")
            views.setImageViewResource(R.id.ivEyeToggle, R.drawable.ic_eye_open)
        }

        val toggleIntent = Intent(context, InventoryWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_BALANCE
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context, 0, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.ivEyeToggle, togglePendingIntent)

        val manageIntent = Intent(context, InventoryWidgetProvider::class.java).apply {
            action = ACTION_MANAGE_INVENTORY
        }
        val managePendingIntent = PendingIntent.getBroadcast(
            context, 1, manageIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.ivManageIcon, managePendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun calculateTotalBalance(context: Context): Double {
        val dbHelper = DatabaseHelper(context)
        val products = dbHelper.getAllProducts()
        return products.sumOf { it.price * it.quantity }
    }

    private fun formatBalance(balance: Double): String {
        val format = NumberFormat.getInstance(Locale("es", "CO"))
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        return format.format(balance)
    }

    private fun toggleBalanceVisibility(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentState = prefs.getBoolean(PREF_BALANCE_VISIBLE, false)
        prefs.edit().putBoolean(PREF_BALANCE_VISIBLE, !currentState).apply()

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, InventoryWidgetProvider::class.java)
        )
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun openLoginActivity(context: Context) {
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}
