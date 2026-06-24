package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val country: String,
    val referralCode: String,
    val kycLevel: Int = 1,
    val registrationCompleted: Boolean = false,
    val balanceRewards: Double = 0.0
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String, // e.g. "NGN", "USD", "EUR"
    val currencyName: String,
    val symbol: String,
    val balance: Double,
    val flag: String
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val walletId: String,
    val type: String, // "Deposit", "Transfer", "Withdrawal", "Merchant Payment"
    val amount: Double,
    val recipientName: String = "",
    val senderName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Completed", // "Completed", "Pending", "Failed"
    val reference: String,
    val description: String = ""
)

@Entity(tableName = "virtual_cards")
data class VirtualCardEntity(
    @PrimaryKey val id: String,
    val cardHolder: String,
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String,
    val cardType: String, // "Visa", "Mastercard"
    val isFrozen: Boolean = false,
    val dailyLimit: Double = 1000.0,
    val spentAmount: Double = 0.0
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val status: String = "Open", // "Open", "Resolved"
    val timestamp: Long = System.currentTimeMillis(),
    val lastMessage: String = ""
)

@Entity(tableName = "beneficiaries")
data class BeneficiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val accountNumberOrPhone: String,
    val country: String,
    val bankName: String = ""
)
