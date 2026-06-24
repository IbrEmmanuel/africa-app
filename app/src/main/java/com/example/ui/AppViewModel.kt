package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Login : Screen()
    object Register : Screen()
    data class OtpVerification(val emailOrPhone: String, val type: String) : Screen()
    object Home : Screen()
    object Wallet : Screen()
    object Transfer : Screen()
    object FxConverter : Screen()
    object MerchantPayments : Screen()
    object VirtualCards : Screen()
    object TransactionHistory : Screen()
    object Profile : Screen()
    object Support : Screen()
    object AdminDashboard : Screen()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application)

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<Screen>()

    // Local DB Flow States
    val user: StateFlow<UserEntity?> = repository.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val wallets: StateFlow<List<WalletEntity>> = repository.walletsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cards: StateFlow<List<VirtualCardEntity>> = repository.cardsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tickets: StateFlow<List<SupportTicketEntity>> = repository.ticketsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val beneficiaries: StateFlow<List<BeneficiaryEntity>> = repository.beneficiariesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI States for Forms & Chats
    val fxRatesMap: Map<String, Double> get() = repository.fxRates

    // AI Assistant state
    private val _aiMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf("Hello! I am your AfriFlow AI Financial Assistant. How can I help you optimize your money across Africa and globally today?" to false)
    )
    val aiMessages: StateFlow<List<Pair<String, Boolean>>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Status / Alert message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Live fraud config for demo
    private val _fraudScoreFactor = MutableStateFlow(0.12)
    val fraudScoreFactor: StateFlow<Double> = _fraudScoreFactor.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultData()
        }
    }

    fun navigateTo(screen: Screen) {
        screenHistory.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
        } else {
            _currentScreen.value = Screen.Home
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // AUTH ACTIONS
    fun login(email: String) {
        viewModelScope.launch {
            repository.loginUser(email)
            navigateTo(Screen.OtpVerification(email, "login"))
        }
    }

    fun register(name: String, email: String, phone: String, country: String, code: String) {
        viewModelScope.launch {
            repository.registerUser(name, email, phone, country, code)
            navigateTo(Screen.OtpVerification(email, "register"))
        }
    }

    fun verifyOtp(otp: String) {
        if (otp == "1234" || otp.length == 4) {
            showToast("OTP Verified Successfully!")
            navigateTo(Screen.Home)
        } else {
            showToast("Invalid OTP. Please enter 1234 for demo verification.")
        }
    }

    fun logout() {
        _currentScreen.value = Screen.Onboarding
        screenHistory.clear()
    }

    // DEPOSIT FUNDS
    fun makeDeposit(walletId: String, amount: Double, source: String) {
        viewModelScope.launch {
            repository.depositFunds(walletId, amount, source)
            showToast("Deposited $amount $walletId successfully!")
        }
    }

    // TRANSFER FUNDS
    fun sendMoney(
        fromWallet: String,
        amount: Double,
        recipient: String,
        toWallet: String,
        description: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            // Rule checking: KYC Level 1 limit is $200. KYC Level 2 is $5000.
            val currentKyc = user.value?.kycLevel ?: 1
            val fromRate = fxRatesMap[fromWallet] ?: 1.0
            val amountInUsd = amount / fromRate

            if (currentKyc == 1 && amountInUsd > 200.0) {
                showToast("KYC Level 1 Transfer limit is $200 USD. Upgrade your KYC to complete this transaction!")
                onComplete(false)
                return@launch
            }

            // Real-time Risk scoring (Demo "Fraud Detection")
            if (amountInUsd > 1000.0 && _fraudScoreFactor.value > 0.3) {
                showToast("Suspicious Transaction Flagged by AfriShield Engine! Multi-factor phone step needed.")
                // Upgrade KYC level or double-verify
                onComplete(false)
                return@launch
            }

            val success = repository.transferFunds(fromWallet, amount, recipient, toWallet, description)
            if (success) {
                // Referral cashback trigger for Bronze/Silver/Gold/Platinum
                val rewardBonus = amountInUsd * 0.01 // 1% cashback reward
                repository.addReward(rewardBonus)
                showToast("Sent $amount $fromWallet to $recipient. Earned ${"%.2f".format(rewardBonus)} USD reward bonus!")
                onComplete(true)
            } else {
                showToast("Insufficient balance in your $fromWallet wallet!")
                onComplete(false)
            }
        }
    }

    // SWAP WALLET CURRENCIES
    fun swapCurrency(fromWallet: String, toWallet: String, amount: Double) {
        viewModelScope.launch {
            val success = repository.convertCurrency(fromWallet, toWallet, amount)
            if (success) {
                showToast("Successfully swapped $amount $fromWallet to $toWallet!")
            } else {
                showToast("Failed to convert currency. Check your balance.")
            }
        }
    }

    // VIRTUAL CARDS ACTIONS
    fun generateCard(cardType: String, limit: Double) {
        viewModelScope.launch {
            val success = repository.createVirtualCard(cardType, limit)
            if (success) {
                showToast("Your USD $cardType Virtual Card is active!")
            } else {
                showToast("Error creating virtual card.")
            }
        }
    }

    fun toggleCardStatus(cardId: String) {
        viewModelScope.launch {
            repository.toggleCardFreeze(cardId)
            showToast("Card status updated!")
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
            showToast("Virtual card deleted successfully.")
        }
    }

    // SUPPORT TICKETS
    fun fileTicket(subject: String, description: String) {
        viewModelScope.launch {
            repository.createSupportTicket(subject, description)
            showToast("Ticket created! Our financial support team is on it.")
        }
    }

    // AI CHAT SERVICE
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentChat = _aiMessages.value.toMutableList()
        currentChat.add(text to true)
        _aiMessages.value = currentChat

        viewModelScope.launch {
            _isAiLoading.value = true
            val reply = repository.askAiFinancialAssistant(text)
            val updatedChat = _aiMessages.value.toMutableList()
            updatedChat.add(reply to false)
            _aiMessages.value = updatedChat
            _isAiLoading.value = false
        }
    }

    // KYC ACTIONS
    fun uploadKycDocs(level: Int) {
        viewModelScope.launch {
            repository.upgradeKyc(level)
            showToast("KYC Document Uploaded! verified to Level $level")
        }
    }

    // ADMIN CONTROLS FOR DEMO
    fun updateFraudRisk(factor: Double) {
        _fraudScoreFactor.value = factor
        showToast("Risk threshold calibrated to ${"%.2f".format(factor)}")
    }
}
