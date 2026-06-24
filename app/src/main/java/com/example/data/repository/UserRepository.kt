package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.*
import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.*

class UserRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val walletDao = db.walletDao()
    private val transactionDao = db.transactionDao()
    private val virtualCardDao = db.virtualCardDao()
    private val supportTicketDao = db.supportTicketDao()
    private val beneficiaryDao = db.beneficiaryDao()

    val userFlow: Flow<UserEntity?> = userDao.getUserFlow()
    val walletsFlow: Flow<List<WalletEntity>> = walletDao.getAllWalletsFlow()
    val transactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    val cardsFlow: Flow<List<VirtualCardEntity>> = virtualCardDao.getAllCardsFlow()
    val ticketsFlow: Flow<List<SupportTicketEntity>> = supportTicketDao.getAllTicketsFlow()
    val beneficiariesFlow: Flow<List<BeneficiaryEntity>> = beneficiaryDao.getAllBeneficiariesFlow()

    // FX rates relative to 1 USD
    val fxRates = mapOf(
        "USD" to 1.0,
        "NGN" to 1500.0,
        "GHS" to 15.2,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "XOF" to 610.0,
        "XAF" to 610.0,
        "KES" to 130.0,
        "ZAR" to 18.2
    )

    suspend fun initializeDefaultData() = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync()
        if (user == null) {
            // Create default user profile
            val defaultUser = UserEntity(
                id = 1,
                fullName = "Chidi Mensah",
                email = "chidi@afriflow.com",
                phoneNumber = "+234 812 345 6789",
                country = "Nigeria",
                referralCode = "AFRI_CHIDI_77",
                kycLevel = 2,
                registrationCompleted = true,
                balanceRewards = 25.0
            )
            userDao.insertUser(defaultUser)

            // Insert default wallets
            val defaultWallets = listOf(
                WalletEntity("NGN", "Nigerian Naira", "₦", 450000.0, "🇳🇬"),
                WalletEntity("USD", "US Dollar", "$", 2500.0, "🇺🇸"),
                WalletEntity("GHS", "Ghanaian Cedi", "GH₵", 8500.0, "🇬🇭"),
                WalletEntity("EUR", "Euro", "€", 1200.0, "🇪🇺"),
                WalletEntity("GBP", "British Pound", "£", 850.0, "🇬🇧"),
                WalletEntity("XOF", "West African CFA", "CFA", 185000.0, "🇸🇳"),
                WalletEntity("XAF", "Central African CFA", "FCFA", 125000.0, "🇨🇲")
            )
            walletDao.insertWallets(defaultWallets)

            // Insert default cards
            val defaultCards = listOf(
                VirtualCardEntity(
                    id = "card_1",
                    cardHolder = "Chidi Mensah",
                    cardNumber = "4532 7812 9011 3456",
                    expiryDate = "12/28",
                    cvv = "382",
                    cardType = "Visa",
                    isFrozen = false,
                    dailyLimit = 1500.0,
                    spentAmount = 340.50
                ),
                VirtualCardEntity(
                    id = "card_2",
                    cardHolder = "Chidi Mensah",
                    cardNumber = "5412 8823 4100 9928",
                    expiryDate = "06/29",
                    cvv = "911",
                    cardType = "Mastercard",
                    isFrozen = true,
                    dailyLimit = 500.0,
                    spentAmount = 0.0
                )
            )
            for (card in defaultCards) {
                virtualCardDao.insertCard(card)
            }

            // Insert default transactions
            val defaultTransactions = listOf(
                TransactionEntity(
                    walletId = "USD",
                    type = "Deposit",
                    amount = 2500.0,
                    senderName = "Wise Direct Deposit",
                    reference = "DEP-WISE-9921",
                    description = "Salary funding via Wise"
                ),
                TransactionEntity(
                    walletId = "NGN",
                    type = "Merchant Payment",
                    amount = 15000.0,
                    recipientName = "Jumia Nigeria",
                    reference = "PAY-JUM-1210",
                    description = "Shopping on Jumia"
                ),
                TransactionEntity(
                    walletId = "GHS",
                    type = "Transfer",
                    amount = 500.0,
                    recipientName = "Kofi Owusu",
                    reference = "TX-KOF-3821",
                    description = "School fees support"
                ),
                TransactionEntity(
                    walletId = "USD",
                    type = "Transfer",
                    amount = 120.0,
                    recipientName = "Amina Diop",
                    reference = "TX-AMN-9023",
                    description = "FX wallet transfer"
                )
            )
            for (tx in defaultTransactions) {
                transactionDao.insertTransaction(tx)
            }

            // Insert default support tickets
            val defaultTickets = listOf(
                SupportTicketEntity(
                    subject = "Virtual card decline on Netflix",
                    status = "Resolved",
                    lastMessage = "We updated your virtual card. Netflix billing is now supported."
                ),
                SupportTicketEntity(
                    subject = "Delayed transfer to GHS Mobile Money",
                    status = "Open",
                    lastMessage = "Our support agents are verifying with the MTN network partner."
                )
            )
            for (ticket in defaultTickets) {
                supportTicketDao.insertTicket(ticket)
            }

            // Insert default beneficiaries
            val defaultBeneficiaries = listOf(
                BeneficiaryEntity(name = "Kofi Owusu", accountNumberOrPhone = "+233 24 123 4567", country = "Ghana", bankName = "MTN Mobile Money"),
                BeneficiaryEntity(name = "Amina Diop", accountNumberOrPhone = "209938411", country = "Senegal", bankName = "Orange Money"),
                BeneficiaryEntity(name = "John Doe", accountNumberOrPhone = "1214829311", country = "United States", bankName = "Stripe Bank Account")
            )
            for (ben in defaultBeneficiaries) {
                beneficiaryDao.insertBeneficiary(ben)
            }
        }
    }

    suspend fun registerUser(fullName: String, email: String, phoneNumber: String, country: String, referralCode: String) = withContext(Dispatchers.IO) {
        val user = UserEntity(
            id = 1,
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            country = country,
            referralCode = referralCode,
            kycLevel = 1,
            registrationCompleted = true,
            balanceRewards = if (referralCode.isNotEmpty()) 10.0 else 0.0
        )
        userDao.insertUser(user)
    }

    suspend fun loginUser(email: String) = withContext(Dispatchers.IO) {
        var user = userDao.getUserSync()
        if (user == null) {
            user = UserEntity(
                id = 1,
                fullName = "Chidi Mensah",
                email = email,
                phoneNumber = "+234 812 345 6789",
                country = "Nigeria",
                referralCode = "WELCOME_AFRI",
                kycLevel = 1,
                registrationCompleted = true,
                balanceRewards = 5.0
            )
            userDao.insertUser(user)
        }
    }

    suspend fun upgradeKyc(level: Int) = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync()
        if (user != null) {
            userDao.updateUser(user.copy(kycLevel = level))
        }
    }

    suspend fun addReward(amount: Double) = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync()
        if (user != null) {
            userDao.updateUser(user.copy(balanceRewards = user.balanceRewards + amount))
        }
    }

    suspend fun depositFunds(walletId: String, amount: Double, sourceName: String) = withContext(Dispatchers.IO) {
        val wallet = walletDao.getWalletById(walletId)
        if (wallet != null) {
            walletDao.updateWallet(wallet.copy(balance = wallet.balance + amount))
            transactionDao.insertTransaction(
                TransactionEntity(
                    walletId = walletId,
                    type = "Deposit",
                    amount = amount,
                    senderName = sourceName,
                    reference = "DEP-${System.currentTimeMillis().toString().takeLast(6)}",
                    description = "Wallet funding"
                )
            )
        }
    }

    suspend fun transferFunds(
        senderWalletId: String,
        amount: Double,
        recipientName: String,
        recipientWalletId: String,
        description: String
    ): Boolean = withContext(Dispatchers.IO) {
        val senderWallet = walletDao.getWalletById(senderWalletId) ?: return@withContext false
        if (senderWallet.balance < amount) return@withContext false

        // Deduct sender
        walletDao.updateWallet(senderWallet.copy(balance = senderWallet.balance - amount))

        // Convert rate
        val senderRate = fxRates[senderWalletId] ?: 1.0
        val recipientRate = fxRates[recipientWalletId] ?: 1.0
        val amountInUsd = amount / senderRate
        val receivedAmount = amountInUsd * recipientRate

        // If recipient wallet is ours, add to it
        val recipientWallet = walletDao.getWalletById(recipientWalletId)
        if (recipientWallet != null) {
            walletDao.updateWallet(recipientWallet.copy(balance = recipientWallet.balance + receivedAmount))
        }

        // Add Transactions
        transactionDao.insertTransaction(
            TransactionEntity(
                walletId = senderWalletId,
                type = "Transfer",
                amount = amount,
                recipientName = recipientName,
                reference = "TX-${System.currentTimeMillis().toString().takeLast(6)}",
                description = description
            )
        )

        // Add as beneficiary if not exist
        val currentBeneficiaries = beneficiaryDao.getAllBeneficiariesFlow().firstOrNull() ?: emptyList()
        if (currentBeneficiaries.none { it.name.lowercase() == recipientName.lowercase() }) {
            beneficiaryDao.insertBeneficiary(
                BeneficiaryEntity(
                    name = recipientName,
                    accountNumberOrPhone = UUID.randomUUID().toString().take(10),
                    country = "Africa",
                    bankName = "AfriFlow Account"
                )
            )
        }

        true
    }

    suspend fun convertCurrency(
        fromWalletId: String,
        toWalletId: String,
        amount: Double
    ): Boolean = withContext(Dispatchers.IO) {
        val fromWallet = walletDao.getWalletById(fromWalletId) ?: return@withContext false
        val toWallet = walletDao.getWalletById(toWalletId) ?: return@withContext false
        if (fromWallet.balance < amount) return@withContext false

        // Rates
        val fromRate = fxRates[fromWalletId] ?: 1.0
        val toRate = fxRates[toWalletId] ?: 1.0
        val amountInUsd = amount / fromRate
        val convertedAmount = amountInUsd * toRate

        // Execute swap
        walletDao.updateWallet(fromWallet.copy(balance = fromWallet.balance - amount))
        walletDao.updateWallet(toWallet.copy(balance = toWallet.balance + convertedAmount))

        // Transaction log
        transactionDao.insertTransaction(
            TransactionEntity(
                walletId = fromWalletId,
                type = "Transfer",
                amount = amount,
                recipientName = "FX Exchange Wallet ($toWalletId)",
                reference = "FXSWAP-${System.currentTimeMillis().toString().takeLast(6)}",
                description = "Swapped to $toWalletId"
            )
        )

        transactionDao.insertTransaction(
            TransactionEntity(
                walletId = toWalletId,
                type = "Deposit",
                amount = convertedAmount,
                senderName = "FX Exchange Wallet ($fromWalletId)",
                reference = "FXSWAP-${System.currentTimeMillis().toString().takeLast(6)}",
                description = "Swapped from $fromWalletId"
            )
        )

        true
    }

    suspend fun createVirtualCard(cardType: String, limit: Double): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync() ?: return@withContext false
        val newCard = VirtualCardEntity(
            id = "card_${System.currentTimeMillis().toString().takeLast(4)}",
            cardHolder = user.fullName,
            cardNumber = "4532 " + (1000..9999).random() + " " + (1000..9999).random() + " " + (1000..9999).random(),
            expiryDate = "08/30",
            cvv = (100..999).random().toString(),
            cardType = cardType,
            isFrozen = false,
            dailyLimit = limit,
            spentAmount = 0.0
        )
        virtualCardDao.insertCard(newCard)
        true
    }

    suspend fun toggleCardFreeze(cardId: String) = withContext(Dispatchers.IO) {
        val cards = virtualCardDao.getAllCardsFlow().firstOrNull() ?: return@withContext
        val card = cards.firstOrNull { it.id == cardId }
        if (card != null) {
            virtualCardDao.updateCard(card.copy(isFrozen = !card.isFrozen))
        }
    }

    suspend fun deleteCard(cardId: String) = withContext(Dispatchers.IO) {
        virtualCardDao.deleteCard(cardId)
    }

    suspend fun createSupportTicket(subject: String, message: String) = withContext(Dispatchers.IO) {
        val newTicket = SupportTicketEntity(
            subject = subject,
            status = "Open",
            lastMessage = message
        )
        supportTicketDao.insertTicket(newTicket)
    }

    // AI financial assistant content provider
    suspend fun askAiFinancialAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return@withContext "API Key not configured. Please add your GEMINI_API_KEY to the AI Studio Secrets panel. \n\n[Alternative Prototype Advice]: Keep saving and tracking your budgets, you are currently managing your wallets very well!"
        }

        // Get context from local Room Database to supply rich financial info
        val user = userDao.getUserSync()
        val wallets = walletDao.getAllWalletsFlow().firstOrNull() ?: emptyList()
        val transactions = transactionDao.getAllTransactionsFlow().firstOrNull() ?: emptyList()
        val cards = virtualCardDao.getAllCardsFlow().firstOrNull() ?: emptyList()

        val walletContext = wallets.joinToString("\n") { "Wallet ${it.id} (${it.currencyName}): Bal: ${it.symbol}${it.balance}" }
        val transactionContext = transactions.take(15).joinToString("\n") {
            "TX: ${it.type} of ${it.amount} ${it.walletId} with status: ${it.status} (Details: ${it.description}, Recipient/Sender: ${it.recipientName}${it.senderName})"
        }
        val cardContext = cards.joinToString("\n") {
            "Virtual Card ${it.cardType} (${it.cardNumber.takeLast(4)}): Limit $${it.dailyLimit}, Spent $${it.spentAmount}, Frozen: ${it.isFrozen}"
        }

        val systemPrompt = """
            You are AfriFlow AI Financial Assistant, an elite AI coach embedded inside the AfriFlow fintech super-app.
            Your job is to provide intelligent, encouraging, hyper-relevant, and strategic financial advice to the user.
            You have access to their real app profile and database context below:
            
            [USER PROFILE]
            Name: ${user?.fullName ?: "Chidi"}
            Country: ${user?.country ?: "Nigeria"}
            KYC Level: ${user?.kycLevel ?: 2}
            Rewards Bal: $${user?.balanceRewards ?: 0.0}
            
            [WALLETS]
            $walletContext
            
            [VIRTUAL CARDS]
            $cardContext
            
            [RECENT TRANSACTIONS]
            $transactionContext
            
            You must analyze this data to answer their questions. Keep your tone professional, friendly, sharp, and African-fintech savvy (understanding multi-currency conversions NGN, GHS, XOF, USD, EUR, etc., Mobile Money ecosystems like MTN/Orange MoMo, and Jumia/card limits).
            Format with bold highlights and emojis. Limit your response to 200-250 words to be very readable on a phone screen.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No advice received. Make sure your internet connection is active."
        } catch (e: Exception) {
            Log.e("UserRepository", "Gemini API Error", e)
            "Error contacting financial assistant: ${e.message ?: "Unknown error"}. Please check your GEMINI_API_KEY in the Secrets panel."
        }
    }
}
