package com.example.qlchitieu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.qlchitieu.R
import com.example.qlchitieu.adapter.TransactionAdapter
import com.example.qlchitieu.database.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private lateinit var barChart: BarChart
    private val transactionAdapter = TransactionAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo BarChart
        barChart = view.findViewById(R.id.barChart)

        // Lấy dữ liệu giao dịch từ adapter hoặc Firebase
        val transactions = getTransactions() // Bạn cần lấy dữ liệu từ Firebase hoặc adapter

        // Tạo danh sách các entry cho biểu đồ cột
        val entries = mutableListOf<BarEntry>()

        // Giả sử bạn muốn tính tổng thu và chi theo ngày
        val incomeByDate = mutableMapOf<Long, Double>()
        val expenseByDate = mutableMapOf<Long, Double>()

        // Phân loại giao dịch theo ngày
        transactions.forEach { transaction ->
            val date = transaction.date // Lấy ngày giao dịch
            if (transaction.type == "Withdraw") {
                expenseByDate[date] = expenseByDate.getOrDefault(date, 0.0) + transaction.amount
            } else {
                incomeByDate[date] = incomeByDate.getOrDefault(date, 0.0) + transaction.amount
            }
        }

        // Chuyển dữ liệu thu/chi thành các Entry cho biểu đồ
        var index = 0f
        incomeByDate.forEach { (date, amount) ->
            entries.add(BarEntry(index, amount.toFloat())) // Thêm thu vào cột
            index++
        }

        expenseByDate.forEach { (date, amount) ->
            entries.add(BarEntry(index, -amount.toFloat())) // Thêm chi vào cột (âm)
            index++
        }

        // Tạo dataset từ các entries
        val dataSet = BarDataSet(entries, "Thu/Chi")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() // Chọn màu cho cột

        // Cập nhật dữ liệu cho biểu đồ
        val barData = BarData(dataSet)
        barChart.data = barData

        // Cập nhật lại biểu đồ
        barChart.invalidate()
    }

    // Hàm giả lập lấy dữ liệu (thực tế bạn cần lấy dữ liệu từ Firebase)
    private fun getTransactions(): List<Transaction> {
        // Lấy danh sách giao dịch từ Firebase hoặc adapter
        return listOf(
            Transaction(amount = 1000.0, type = "Deposit", date = System.currentTimeMillis(), description = "Deposit 1"),
            Transaction(amount = 500.0, type = "Withdraw", date = System.currentTimeMillis(), description = "Withdraw 1")
        )
    }
}
