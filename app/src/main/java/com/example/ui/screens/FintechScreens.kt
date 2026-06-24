package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.*
import com.example.ui.AppViewModel
import com.example.ui.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color Definitions
val PrimaryBlue = Color(0xFF0066FF)
val SuccessGreen = Color(0xFF00C853)
val AccentGold = Color(0xFFFFC107)
val DarkBackground = Color(0xFFF8F9FF) // Light Bento body background
val LightSurface = Color(0xFFFFFFFF)  // White Bento card surface
val TextGray = Color(0xFF6B7280)      // Slate/Gray-500 for secondary text
val TextPrimary = Color(0xFF1B1B1F)   // Dark Bento text for primary content

@Composable
fun MainContent(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Setup base theme inside our screen router
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Decorative Elements (Linear Gradient Ambient Glow)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(PrimaryBlue.copy(alpha = 0.06f), Color.Transparent)
                            ),
                            radius = 600f,
                            center = Offset(size.width * 0.1f, size.height * 0.15f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(SuccessGreen.copy(alpha = 0.04f), Color.Transparent)
                            ),
                            radius = 700f,
                            center = Offset(size.width * 0.9f, size.height * 0.8f)
                        )
                    }
            )

            // Dynamic Screen Router
            Crossfade(targetState = currentScreen, label = "screen_fade") { screen ->
                when (screen) {
                    is Screen.Splash -> SplashScreen(viewModel)
                    is Screen.Onboarding -> OnboardingScreen(viewModel)
                    is Screen.Login -> LoginScreen(viewModel)
                    is Screen.Register -> RegisterScreen(viewModel)
                    is Screen.OtpVerification -> OtpVerificationScreen(viewModel, screen.emailOrPhone, screen.type)
                    is Screen.Home -> ScaffoldWrapper(viewModel, activeTab = "Home") { HomeDashboard(viewModel) }
                    is Screen.Wallet -> ScaffoldWrapper(viewModel, activeTab = "Wallet") { WalletScreen(viewModel) }
                    is Screen.Transfer -> ScaffoldWrapper(viewModel, activeTab = "Transfer") { TransferScreen(viewModel) }
                    is Screen.VirtualCards -> ScaffoldWrapper(viewModel, activeTab = "Cards") { CardsScreen(viewModel) }
                    is Screen.Support -> ScaffoldWrapper(viewModel, activeTab = "Support") { SupportScreen(viewModel) }
                    is Screen.Profile -> ScaffoldWrapper(viewModel, activeTab = "Profile") { ProfileScreen(viewModel) }
                    is Screen.AdminDashboard -> ScaffoldWrapper(viewModel, activeTab = "Admin") { AdminDashboardScreen(viewModel) }
                    else -> HomeDashboard(viewModel)
                }
            }

            // Toast overlay
            toastMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
                        .testTag("toast_alert"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
                LaunchedEffect(msg) {
                    delay(3500)
                    viewModel.clearToast()
                }
            }
        }
    }
}

@Composable
fun ScaffoldWrapper(
    viewModel: AppViewModel,
    activeTab: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = LightSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .background(Color.Transparent)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val items = listOf(
                    Triple("Home", Icons.Default.Home, Screen.Home),
                    Triple("Wallet", Icons.Default.Star, Screen.Wallet), // Standard Star as fallback wallet icon
                    Triple("Transfer", Icons.Default.Send, Screen.Transfer),
                    Triple("Cards", Icons.Default.AccountBox, Screen.VirtualCards), // standard icon as fallback
                    Triple("Support", Icons.Default.MailOutline, Screen.Support),
                    Triple("Profile", Icons.Default.Person, Screen.Profile)
                )

                items.forEach { (name, icon, targetScreen) ->
                    NavigationBarItem(
                        selected = activeTab == name,
                        onClick = { viewModel.navigateTo(targetScreen) },
                        icon = { Icon(icon, contentDescription = name, modifier = Modifier.size(24.dp)) },
                        label = { Text(name, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        ),
                        modifier = Modifier.testTag("nav_tab_$name")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            content()
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(viewModel: AppViewModel) {
    val scale = remember { Animatable(0.2f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(1f, animationSpec = tween(1000))
        delay(2000)
        viewModel.navigateTo(Screen.Onboarding)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .background(
                    Brush.verticalGradient(listOf(PrimaryBlue, SuccessGreen)),
                    shape = RoundedCornerShape(32.dp)
                )
                .scale(scale.value)
                .alpha(alpha.value),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "af",
                color = Color.White,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "AfriFlow",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(alpha.value)
        )

        Text(
            text = "One Wallet. All Africa.",
            color = SuccessGreen,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(top = 8.dp)
                .alpha(alpha.value)
        )
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(viewModel: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AfriFlow",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.size(8.dp).background(SuccessGreen, CircleShape))
        }

        // Illustration Center Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
                .height(280.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive Mini Map overlay drawing
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    // Draw random dynamic glowing fintech curves
                    val path1 = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, height * 0.7f)
                        quadraticTo(width * 0.3f, height * 0.2f, width * 0.6f, height * 0.8f)
                        quadraticTo(width * 0.8f, height * 0.9f, width, height * 0.5f)
                    }
                    drawPath(
                        path = path1,
                        brush = Brush.horizontalGradient(listOf(PrimaryBlue, SuccessGreen)),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(PrimaryBlue, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Pan-African Wallet", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Column {
                        Text(
                            text = "Borderless\nPayments.",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 36.sp
                        )
                        Text(
                            text = "Connect NGN, GHS, USD, EUR, CFA instantly.",
                            color = TextGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Send and receive money, convert currencies, pay merchants, and manage custom virtual cards with zero boundary delays.",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = { viewModel.navigateTo(Screen.Login) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("get_started_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.navigateTo(Screen.Register) }
            ) {
                Text("Create New Account", color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 3. LOGIN SCREEN
@Composable
fun LoginScreen(viewModel: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome Back", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Log in securely to your AfriFlow Wallet", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = LightSurface,
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = TextGray,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("login_email"),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = LightSurface,
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = TextGray,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    viewModel.login(email)
                } else {
                    viewModel.showToast("Please enter an email address.")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("login_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Verify securely with OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ", color = TextGray)
            Text(
                "Register here",
                color = SuccessGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { viewModel.navigateTo(Screen.Register) }
            )
        }
    }
}

// 4. REGISTER SCREEN
@Composable
fun RegisterScreen(viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Nigeria") }
    var referral by remember { mutableStateOf("") }

    val countries = listOf("Nigeria", "Ghana", "Kenya", "Senegal", "Cameroon", "United Kingdom", "United States")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Create Account", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp))
        Text("Sign up and start transacting globally", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = LightSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = LightSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = LightSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Simple Country Selector Row
        Text("Select Country:", color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
        LazyRow(modifier = Modifier.padding(bottom = 12.dp)) {
            items(countries) { item ->
                Card(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { country = item },
                    colors = CardDefaults.cardColors(
                        containerColor = if (country == item) PrimaryBlue else LightSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item,
                        color = if (country == item) Color.White else TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        OutlinedTextField(
            value = referral,
            onValueChange = { referral = it },
            label = { Text("Referral Code (Optional)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = LightSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank()) {
                    viewModel.register(name, email, phone, country, referral)
                } else {
                    viewModel.showToast("Please fill in name and email.")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("register_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Register Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// 5. OTP VERIFICATION SCREEN
@Composable
fun OtpVerificationScreen(viewModel: AppViewModel, dest: String, type: String) {
    var otpCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "OTP",
            tint = AccentGold,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Enter Security Code", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "We sent a 4-digit security OTP to $dest. Enter it to secure your session.",
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Custom stylized input
        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 4) otpCode = it },
            label = { Text("Verification Code (Try 1234)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SuccessGreen,
                unfocusedBorderColor = LightSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .width(220.dp)
                .padding(bottom = 32.dp)
                .testTag("otp_input"),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        )

        Button(
            onClick = { viewModel.verifyOtp(otpCode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("verify_otp_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Verify OTP Code", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// 6. HOME DASHBOARD
@Composable
fun HomeDashboard(viewModel: AppViewModel) {
    val user by viewModel.user.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var activeWalletIndex by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.fullName?.take(1)?.uppercase() ?: "C",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Hello,", color = TextGray, fontSize = 12.sp)
                        Text(user?.fullName ?: "Chidi Mensah", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Quick Admin Toggle
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.AdminDashboard) },
                    modifier = Modifier
                        .background(LightSurface, RoundedCornerShape(12.dp))
                        .size(40.dp)
                        .testTag("admin_toggle_btn")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Admin Panel", tint = AccentGold)
                }
            }
        }

        // Active balance glassmorphism card
        item {
            if (wallets.isNotEmpty()) {
                val activeWallet = wallets.getOrNull(activeWalletIndex) ?: wallets[0]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TOTAL ACTIVE BALANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = activeWallet.id,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "${activeWallet.symbol}${String.format("%,.2f", activeWallet.balance)}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KYC Level ${user?.kycLevel ?: 1} verified",
                                color = Color(0xFFB9F6CA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeWallet.currencyName,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Horizontal multi-currency list
        item {
            Text(
                "My Currencies",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                items(wallets.size) { index ->
                    val wallet = wallets[index]
                    val isSelected = activeWalletIndex == index
                    val itemTextColor = if (isSelected) Color.White else TextPrimary
                    Card(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .width(130.dp)
                            .clickable { activeWalletIndex = index },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PrimaryBlue else LightSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(wallet.flag, fontSize = 20.sp)
                                Text(wallet.id, color = itemTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${wallet.symbol}${String.format("%,.0f", wallet.balance)}",
                                color = itemTextColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val actions = listOf(
                    Triple("Send", Icons.Default.Send, Screen.Transfer),
                    Triple("Funding", Icons.Default.Add, Screen.Wallet),
                    Triple("Swap", Icons.Default.Refresh, Screen.Wallet), // standard refresh for swap
                    Triple("Cards", Icons.Default.AccountBox, Screen.VirtualCards)
                )

                actions.forEach { (label, icon, target) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(target) }
                            .testTag("action_$label")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(LightSurface, CircleShape)
                                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = label, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                        }
                        Text(
                            label,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Interactive AI Coaching box of the day
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { viewModel.navigateTo(Screen.Support) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SuccessGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Coach", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AFRIFLOW AI COACH", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Based on your wallets, you can optimize interest swapping USD/NGN today. Talk with our advisor now!",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = "Go", tint = SuccessGreen)
                }
            }
        }

        // Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Transactions", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "View All",
                    color = SuccessGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.TransactionHistory) }
                )
            }
        }

        // Transaction Items
        if (transactions.isEmpty()) {
            item {
                Text("No recent transactions", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            items(transactions.take(5)) { tx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (tx.type == "Deposit") SuccessGreen.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (tx.type == "Deposit") Icons.Default.Add else Icons.Default.Send,
                                contentDescription = tx.type,
                                tint = if (tx.type == "Deposit") SuccessGreen else PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (tx.type == "Deposit") "Funded from ${tx.senderName}" else "Sent to ${tx.recipientName}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(tx.description, color = TextGray, fontSize = 12.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val sign = if (tx.type == "Deposit") "+" else "-"
                        val color = if (tx.type == "Deposit") SuccessGreen else TextPrimary
                        Text(
                            text = "$sign${tx.walletId} ${String.format("%,.1f", tx.amount)}",
                            color = color,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tx.status,
                            color = if (tx.status == "Completed") SuccessGreen else AccentGold,
                            fontSize = 11.sp
                        )
                    }
                }
                Divider(color = LightSurface.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

// 7. WALLET SCREEN (Funding & Swapping)
@Composable
fun WalletScreen(viewModel: AppViewModel) {
    val wallets by viewModel.wallets.collectAsState()

    var selectedDepositWallet by remember { mutableStateOf("USD") }
    var depositAmount by remember { mutableStateOf("") }
    var depositSource by remember { mutableStateOf("Flutterwave Direct Card") }

    var swapFrom by remember { mutableStateOf("USD") }
    var swapTo by remember { mutableStateOf("NGN") }
    var swapAmount by remember { mutableStateOf("") }

    val activeDepositOption = wallets.find { it.id == selectedDepositWallet }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Funding & Currency Swap", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Text("Fund your wallet or swap instant live FX", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 24.dp))
        }

        // Funding Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Fund Your Wallet", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Top-up instantly with safe gateways", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        wallets.take(4).forEach { wallet ->
                            val isSel = selectedDepositWallet == wallet.id
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .background(
                                        if (isSel) PrimaryBlue else Color(0xFFE2E8F0),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedDepositWallet = wallet.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("${wallet.flag} ${wallet.id}", color = if (isSel) Color.White else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        label = { Text("Top Up Amount (${activeDepositOption?.symbol ?: "$"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("deposit_amount_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = depositSource,
                        onValueChange = { depositSource = it },
                        label = { Text("Payment Method") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val amt = depositAmount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                viewModel.makeDeposit(selectedDepositWallet, amt, depositSource)
                                depositAmount = ""
                            } else {
                                viewModel.showToast("Enter a valid amount")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_deposit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Secure Deposit", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // FX Swap Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Instant FX Currency Converter", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Zero margins, standard rates", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Swap From", color = TextGray, fontSize = 11.sp)
                            LazyRow(modifier = Modifier.padding(top = 4.dp)) {
                                items(listOf("USD", "NGN", "GHS", "EUR")) { id ->
                                    val isSel = swapFrom == id
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .background(
                                                if (isSel) PrimaryBlue else Color(0xFFE2E8F0),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { swapFrom = id }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(id, color = if (isSel) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Icon(Icons.Default.Refresh, contentDescription = "Arrow", tint = AccentGold, modifier = Modifier.padding(horizontal = 8.dp))

                        // To selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Swap To", color = TextGray, fontSize = 11.sp)
                            LazyRow(modifier = Modifier.padding(top = 4.dp)) {
                                items(listOf("NGN", "USD", "GHS", "EUR")) { id ->
                                    val isSel = swapTo == id
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .background(
                                                if (isSel) SuccessGreen else Color(0xFFE2E8F0),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { swapTo = id }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(id, color = if (isSel) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = swapAmount,
                        onValueChange = { swapAmount = it },
                        label = { Text("Amount to Swap") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .testTag("swap_amount_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Estimate exchange rates
                    val fromRate = viewModel.fxRatesMap[swapFrom] ?: 1.0
                    val toRate = viewModel.fxRatesMap[swapTo] ?: 1.0
                    val rate = toRate / fromRate
                    val inputAmt = swapAmount.toDoubleOrNull() ?: 0.0
                    val outputAmt = inputAmt * rate

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Exchange Rate:", color = TextGray, fontSize = 12.sp)
                        Text("1 $swapFrom = ${String.format("%.4f", rate)} $swapTo", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("You Will Receive:", color = TextGray, fontSize = 12.sp)
                        Text("${String.format("%,.2f", outputAmt)} $swapTo", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amt = swapAmount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                if (swapFrom == swapTo) {
                                    viewModel.showToast("Cannot swap to the same wallet!")
                                    return@Button
                                }
                                viewModel.swapCurrency(swapFrom, swapTo, amt)
                                swapAmount = ""
                            } else {
                                viewModel.showToast("Enter swap amount")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_swap_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Convert Instantly", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// 8. TRANSFER SCREEN
@Composable
fun TransferScreen(viewModel: AppViewModel) {
    val wallets by viewModel.wallets.collectAsState()
    val beneficiaries by viewModel.beneficiaries.collectAsState()

    var senderWalletId by remember { mutableStateOf("USD") }
    var recipientWalletId by remember { mutableStateOf("USD") }
    var amountText by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }

    var activeStep by remember { mutableStateOf(1) } // 1: Recipient, 2: Amount & Confirm, 3: Completed

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Money Transfer", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Text("Send money globally & locally instantly", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 24.dp))
        }

        // Stepper Progress Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Recipient", "Amount", "Receipt").forEachIndexed { index, title ->
                    val stepNum = index + 1
                    val isCompleted = activeStep > stepNum
                    val isActive = activeStep == stepNum
                    val color = if (isCompleted || isActive) SuccessGreen else TextGray

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isActive) SuccessGreen else if (isCompleted) SuccessGreen.copy(0.2f) else Color(0xFFE2E8F0),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCompleted) "✓" else stepNum.toString(),
                                color = if (isActive) Color.White else if (isCompleted) SuccessGreen else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (index < 2) {
                        Divider(
                            modifier = Modifier
                                .width(24.dp)
                                .padding(horizontal = 4.dp),
                            color = if (activeStep > stepNum) SuccessGreen else Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        if (activeStep == 1) {
            // Recipient Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Enter Recipient Details", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Or choose from saved recipients", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Recipient Name / Phone") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("recipient_name_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Recent Recipients:", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        beneficiaries.forEach { ben ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        recipientName = ben.name
                                        if (ben.country == "Ghana") recipientWalletId = "GHS"
                                        if (ben.country == "United States") recipientWalletId = "USD"
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(PrimaryBlue.copy(0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(ben.name.take(1).uppercase(), color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(ben.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("${ben.bankName} • ${ben.country}", color = TextGray, fontSize = 11.sp)
                                    }
                                }
                                Icon(Icons.Default.ArrowBack, contentDescription = "Select", tint = SuccessGreen, modifier = Modifier.alpha(0.5f))
                            }
                            Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        }

                        Button(
                            onClick = {
                                if (recipientName.isNotBlank()) {
                                    activeStep = 2
                                } else {
                                    viewModel.showToast("Please enter recipient name")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(48.dp)
                                .testTag("next_step_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next: Enter Amount", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        if (activeStep == 2) {
            // Amount & Confirm
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Set Amount & Confirm", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Sending to: $recipientName", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                        Text("Select Sender Wallet:", color = TextGray, fontSize = 11.sp)
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            wallets.take(3).forEach { w ->
                                val isSel = senderWalletId == w.id
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(
                                            if (isSel) PrimaryBlue else Color(0xFFE2E8F0),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { senderWalletId = w.id }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("${w.flag} ${w.id} (${w.symbol}${w.balance.toInt()})", color = if (isSel) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text("Select Recipient Destination Wallet:", color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            wallets.take(4).forEach { w ->
                                val isSel = recipientWalletId == w.id
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(
                                            if (isSel) SuccessGreen else Color(0xFFE2E8F0),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { recipientWalletId = w.id }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("${w.flag} ${w.id}", color = if (isSel) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Transfer Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .testTag("transfer_amount_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = descriptionText,
                            onValueChange = { descriptionText = it },
                            label = { Text("Reference / Purpose") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // FX Conversion Summary
                        val sRate = viewModel.fxRatesMap[senderWalletId] ?: 1.0
                        val rRate = viewModel.fxRatesMap[recipientWalletId] ?: 1.0
                        val rawAmt = amountText.toDoubleOrNull() ?: 0.0
                        val converted = (rawAmt / sRate) * rRate

                        if (senderWalletId != recipientWalletId) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text("FX Rate Applied: 1 $senderWalletId = ${"%.4f".format(rRate / sRate)} $recipientWalletId", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Recipient gets: ${"%.2f".format(converted)} $recipientWalletId", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { activeStep = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Back")
                            }

                            Button(
                                onClick = {
                                    val amt = amountText.toDoubleOrNull()
                                    if (amt != null && amt > 0) {
                                        viewModel.sendMoney(
                                            senderWalletId,
                                            amt,
                                            recipientName,
                                            recipientWalletId,
                                            descriptionText.ifBlank { "Sent via AfriFlow Super Wallet" }
                                        ) { success ->
                                            if (success) {
                                                activeStep = 3
                                            }
                                        }
                                    } else {
                                        viewModel.showToast("Please enter a valid amount")
                                    }
                                },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(48.dp)
                                    .testTag("confirm_transfer_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Send Money Now", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        if (activeStep == 3) {
            // Receipt / Success
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(SuccessGreen.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = SuccessGreen, modifier = Modifier.size(36.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Transfer Initiated", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Transaction processed instantly", color = TextGray, fontSize = 12.sp)

                        Divider(modifier = Modifier.padding(vertical = 20.dp), color = DarkBackground.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recipient:", color = TextGray, fontSize = 13.sp)
                            Text(recipientName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Sent:", color = TextGray, fontSize = 13.sp)
                            Text("$senderWalletId $amountText", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status:", color = TextGray, fontSize = 13.sp)
                            Text("Completed Successfully", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ref ID:", color = TextGray, fontSize = 13.sp)
                            Text("AFR-${(100000..999999).random()}", color = TextGray, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                activeStep = 1
                                amountText = ""
                                recipientName = ""
                                descriptionText = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Send Another Transfer", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// 9. VIRTUAL CARDS SCREEN
@Composable
fun CardsScreen(viewModel: AppViewModel) {
    val cards by viewModel.cards.collectAsState()

    var selectedCardId by remember { mutableStateOf("") }
    var cardTypeInput by remember { mutableStateOf("Visa") }
    var cardLimitInput by remember { mutableStateOf("1000") }
    var showCreateCard by remember { mutableStateOf(false) }

    val selectedCard = cards.find { it.id == selectedCardId } ?: cards.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("USD Virtual Cards", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Secure online payments global shopping", color = TextGray, fontSize = 13.sp)
                }

                Button(
                    onClick = { showCreateCard = !showCreateCard },
                    colors = ButtonDefaults.buttonColors(containerColor = if (showCreateCard) PrimaryBlue else SuccessGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (showCreateCard) "View Cards" else "New Card", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showCreateCard) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Generate USD Card", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Visa or Mastercard processed globally", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

                        Row(modifier = Modifier.padding(bottom = 16.dp)) {
                            listOf("Visa", "Mastercard").forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .background(
                                            if (cardTypeInput == type) PrimaryBlue else DarkBackground,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { cardTypeInput = type }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(type, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = cardLimitInput,
                            onValueChange = { cardLimitInput = it },
                            label = { Text("Daily Limit ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBackground,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                                .testTag("card_limit_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val limit = cardLimitInput.toDoubleOrNull() ?: 1000.0
                                viewModel.generateCard(cardTypeInput, limit)
                                showCreateCard = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_create_card"),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Generate USD Virtual Card", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Cards Slider / Selector
            item {
                if (cards.isEmpty()) {
                    Text("No virtual cards. Tap New Card to generate one.", color = TextGray, modifier = Modifier.padding(vertical = 24.dp))
                } else {
                    LazyRow(modifier = Modifier.padding(bottom = 24.dp)) {
                        items(cards) { card ->
                            val isSelected = selectedCard?.id == card.id
                            // Elegant rotating card visualization
                            Card(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .width(300.dp)
                                    .height(180.dp)
                                    .clickable { selectedCardId = card.id },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (card.isFrozen) Color(0xFF1E2530) else if (card.cardType == "Visa") PrimaryBlue else Color(0xFFFF5722)
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Ambient design details
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(
                                            color = Color.White.copy(0.1f),
                                            radius = 150f,
                                            center = Offset(size.width * 0.9f, size.height * 0.1f)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(card.cardType.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                            if (card.isFrozen) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.Red, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("FROZEN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Text("USD Wallet", color = Color.White.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Text(
                                            text = if (card.isFrozen) "•••• •••• •••• ••••" else card.cardNumber,
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("HOLDER", color = Color.White.copy(0.6f), fontSize = 9.sp)
                                                Text(card.cardHolder, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("EXPIRY / CVV", color = Color.White.copy(0.6f), fontSize = 9.sp)
                                                Text("${card.expiryDate} • ${if (card.isFrozen) "•••" else card.cvv}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Card Operations
            selectedCard?.let { card ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = LightSurface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Card Control Center", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Customize details, status and limits", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

                            // Limit gauges spending tracking bar
                            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Daily Spending Gauge", color = TextGray, fontSize = 12.sp)
                                    Text("$${card.spentAmount} of $${card.dailyLimit}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = (card.spentAmount / card.dailyLimit).toFloat().coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = SuccessGreen,
                                    trackColor = DarkBackground
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleCardStatus(card.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                        .testTag("freeze_card_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text(if (card.isFrozen) "Unfreeze Card" else "Freeze Card")
                                }

                                Button(
                                    onClick = { viewModel.deleteCard(card.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("delete_card_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Terminate", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 10. SUPPORT & AI FINANCIAL ASSISTANT SCREEN
@Composable
fun SupportScreen(viewModel: AppViewModel) {
    val aiMessages by viewModel.aiMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val tickets by viewModel.tickets.collectAsState()

    var activeTab by remember { mutableStateOf("AI Assistant") } // "AI Assistant" or "My Tickets"
    var chatInput by remember { mutableStateOf("") }

    var ticketSubject by remember { mutableStateOf("") }
    var ticketMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Support & AI Coach", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Text("Instant smart coaching or ticketing", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 24.dp))
        }

        // Tab Selector
        item {
            Row(modifier = Modifier.padding(bottom = 20.dp)) {
                listOf("AI Assistant", "My Tickets").forEach { tab ->
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(
                                if (activeTab == tab) SuccessGreen else LightSurface,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeTab = tab }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(tab, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (activeTab == "AI Assistant") {
            // Interactive Chat Window inside LazyColumn (can bundle chats inside card/items)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val chatScrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(chatScrollState)
                        ) {
                            aiMessages.forEach { (msg, isUser) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isUser) PrimaryBlue else DarkBackground
                                        ),
                                        modifier = Modifier.widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            text = msg,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            if (isAiLoading) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkBackground)
                                    ) {
                                        Text(
                                            text = "Analyzing your transaction behaviors...",
                                            color = SuccessGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            LaunchedEffect(aiMessages.size, isAiLoading) {
                                delay(100)
                                chatScrollState.animateScrollTo(chatScrollState.maxValue)
                            }
                        }
                    }
                }
            }

            // Input Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Ask about budgets, FX swaps, or coach advice...", color = TextGray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            unfocusedBorderColor = LightSurface,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                viewModel.sendChatMessage(chatInput)
                                chatInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(SuccessGreen, CircleShape)
                            .size(48.dp)
                            .testTag("send_chat_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        } else {
            // Traditional Tickets
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Open New Support Ticket", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Expect responses within 15 minutes", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

                        OutlinedTextField(
                            value = ticketSubject,
                            onValueChange = { ticketSubject = it },
                            label = { Text("Subject") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBackground,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("ticket_subject"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = ticketMessage,
                            onValueChange = { ticketMessage = it },
                            label = { Text("Describe Your Issue") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBackground,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (ticketSubject.isNotBlank() && ticketMessage.isNotBlank()) {
                                    viewModel.fileTicket(ticketSubject, ticketMessage)
                                    ticketSubject = ""
                                    ticketMessage = ""
                                } else {
                                    viewModel.showToast("Please fill in subject and description")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("file_ticket_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("File Support Ticket", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            item {
                Text("Your Tickets History", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }

            items(tickets) { ticket ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ticket.subject, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(ticket.lastMessage, color = TextGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (ticket.status == "Open") AccentGold.copy(0.2f) else SuccessGreen.copy(0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(ticket.status, color = if (ticket.status == "Open") AccentGold else SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Divider(color = LightSurface.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

// 11. PROFILE & KYC SCREEN
@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val user by viewModel.user.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(PrimaryBlue, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user?.fullName?.take(2)?.uppercase() ?: "CH",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(user?.fullName ?: "Chidi Mensah", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(user?.email ?: "chidi@afriflow.com", color = TextGray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))
        }

        // KYC Verification Gauge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("KYC Verification Level", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Box(
                            modifier = Modifier
                                .background(SuccessGreen.copy(0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Level ${user?.kycLevel ?: 1}", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = when (user?.kycLevel ?: 1) {
                            1 -> "Phone verified. Limited transfers up to $200 USD."
                            2 -> "National ID verified. Access limits up to $5,000 USD."
                            3 -> "Selfie biometric complete. Full digital banking unlocked."
                            else -> "Proof of Address verified. Unlimited corporate scale."
                        },
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )

                    // KYC Levels Gauge / Upload slots
                    if ((user?.kycLevel ?: 1) < 4) {
                        val nextLevel = (user?.kycLevel ?: 1) + 1
                        Button(
                            onClick = { viewModel.uploadKycDocs(nextLevel) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("kyc_upgrade_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Upload Docs for Level $nextLevel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enterprise Verified Account", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Referral Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("AfriFlow Referral Program", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Share code and earn 1% cashback on all referee conversions", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(user?.referralCode ?: "AFRI_CODE_X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "COPY",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { viewModel.showToast("Referral Code copied!") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Tier rewards accrued:", color = TextGray, fontSize = 12.sp)
                        Text("$${user?.balanceRewards ?: 0.0} USD", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Log out
        item {
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.2f), contentColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Secure Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 12. EXECUTIVE ADMIN DASHBOARD PANEL (Saves and manages parameters)
@Composable
fun AdminDashboardScreen(viewModel: AppViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val user by viewModel.user.collectAsState()
    val fraudFactor by viewModel.fraudScoreFactor.collectAsState()

    val totalVolumeInUsd = transactions.sumOf { tx ->
        val rate = viewModel.fxRatesMap[tx.walletId] ?: 1.0
        tx.amount / rate
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AfriFlow Admin Hub", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("System Parameters & Flow Monitoring", color = TextGray, fontSize = 13.sp)
                }

                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    modifier = Modifier.background(PrimaryBlue, CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
                }
            }
        }

        // Analytics Row Cards
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL TX VOLUME", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$${String.format("%,.0f", totalVolumeInUsd)}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SYSTEM REVENUE", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$${String.format("%,.2f", totalVolumeInUsd * 0.0075)}", color = SuccessGreen, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Country performance metrics drawing
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Country Flow Metrics (%)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    val shares = listOf("Nigeria (NGN)" to 0.55f, "Ghana (GHS)" to 0.25f, "Senegal (XOF)" to 0.12f, "Other" to 0.08f)
                    shares.forEach { (name, share) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, color = TextGray, fontSize = 12.sp)
                            Box(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                LinearProgressIndicator(
                                    progress = share,
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = PrimaryBlue,
                                    trackColor = DarkBackground
                                )
                            }
                            Text("${(share * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Calibration engine
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AfriShield AI Risk Calibration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Slide parameter to adjust transaction alert sensitivities", color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Alert Threshold Factor:", color = TextGray, fontSize = 12.sp)
                        Text("${"%.2f".format(fraudFactor)}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Slider(
                        value = fraudFactor.toFloat(),
                        onValueChange = { viewModel.updateFraudRisk(it.toDouble()) },
                        valueRange = 0.05f..0.85f,
                        modifier = Modifier.fillMaxWidth().testTag("risk_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentGold,
                            activeTrackColor = AccentGold,
                            inactiveTrackColor = DarkBackground
                        )
                    )
                }
            }
        }

        // Fast actions (Mocking Admin DB overrides)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Database Override Controls", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))

                    Button(
                        onClick = { viewModel.uploadKycDocs(4) },
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("override_kyc_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Instantly Approve Current User KYC (Level 4)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.makeDeposit("USD", 10000.0, "ADMIN_INJECT_EMERGENCY")
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("override_deposit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Inject $10,000 USD Liquidity (Deposit)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
