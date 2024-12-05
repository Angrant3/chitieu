package com.example.qlchitieu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.qlchitieu.R
import com.example.qlchitieu.database.Transaction
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BudgetFragment : Fragment(R.layout.fragment_budget) {

    private lateinit var tvAccountBalance: TextView
    private lateinit var databaseRef: DatabaseReference
    private var accountBalance: Double = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAccountBalance = view.findViewById(R.id.tvAccountBalance)
        val btnDeposit: Button = view.findViewById(R.id.btnDeposit)
        val btnWithdraw: Button = view.findViewById(R.id.btnWithdraw)
        val btnTransactionHistory: Button = view.findViewById(R.id.btnTransactionHistory)

        // Khởi tạo Firebase Database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("account_balance")

        // Lấy số dư hiện tại từ Firebase
        loadAccountBalance()

        btnDeposit.setOnClickListener {
            showTransactionDialog("Nạp tiền")
        }

        btnWithdraw.setOnClickListener {
            showTransactionDialog("Rút tiền")
        }

        btnTransactionHistory.setOnClickListener {
            showTransactionHistory()
        }
    }

    private fun loadAccountBalance() {
        databaseRef.get().addOnSuccessListener { snapshot ->
            accountBalance = snapshot.getValue(Double::class.java) ?: 0.0
            tvAccountBalance.text = "Số dư hiện tại: $accountBalance đồng"
        }
    }

    private fun showTransactionDialog(action: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_transaction, null)
        val amountEditText: EditText = dialogView.findViewById(R.id.etAmount)
        val bankSpinner: Spinner = dialogView.findViewById(R.id.spBank)

        val banks = arrayOf("Ngân hàng A", "Ngân hàng B", "Ngân hàng C")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, banks)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bankSpinner.adapter = spinnerAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("$action")
            .setView(dialogView)
            .setPositiveButton("Xác nhận") { _, _ ->
                val amount = amountEditText.text.toString().toDoubleOrNull()
                if (amount != null) {
                    performTransaction(action, amount)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun performTransaction(action: String, amount: Double) {
        if (action == "Rút tiền" && amount > accountBalance) {
            Toast.makeText(requireContext(), "Số dư không đủ!", Toast.LENGTH_SHORT).show()
            return
        }

        accountBalance = if (action == "Nạp tiền") {
            accountBalance + amount
        } else {
            accountBalance - amount
        }

        databaseRef.setValue(accountBalance)
            .addOnSuccessListener {
                tvAccountBalance.text = "Số dư hiện tại: $accountBalance đồng"
                saveTransactionToHistory(action, amount)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Giao dịch thất bại!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveTransactionToHistory(action: String, amount: Double) {
        val transactionRef = FirebaseDatabase.getInstance().getReference("transactions")
        val transaction = Transaction(
            id = transactionRef.push().key ?: "",
            amount = if (action == "Rút tiền") -amount else amount,
            type = action,
            description = "$action với số tiền $amount",
            date = System.currentTimeMillis()
        )
        transactionRef.child(transaction.id).setValue(transaction)
    }

    private fun showTransactionHistory() {
        val transactionRef = FirebaseDatabase.getInstance().getReference("transactions")

        transactionRef.get().addOnSuccessListener { snapshot ->
            val transactions = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }

            val historyText = transactions.joinToString("\n") {
                val dateFormatted = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm:ss", it.date)
                "$dateFormatted: ${it.description} - ${it.amount} đồng"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Lịch sử giao dịch")
                .setMessage(if (historyText.isEmpty()) "Không có giao dịch nào." else historyText)
                .setPositiveButton("Đóng", null)
                .show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Không thể tải lịch sử giao dịch.", Toast.LENGTH_SHORT).show()
        }
    }

}
