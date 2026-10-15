package app.cashjournal.ui.onboarding

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Country
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SegmentedControl
import app.cashjournal.ui.components.SegmentItem
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.ToggleSwitch
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.GlassBorder
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate300
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Bell
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.CloudSlash
import com.adamglin.phosphoricons.regular.DeviceMobile
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.MapPin
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.Thermometer

enum class WeatherOption {
    APPROX_LOCATION,
    SEARCH_CITY,
    NOT_NOW
}

@Composable
fun OnboardingScreen(
    onFinish: (language: AppLanguage, currencyCode: String, name: String, railOnRight: Boolean, shakeToSwap: Boolean, loadExample: Boolean) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var language by remember { mutableStateOf(AppLanguage.ENGLISH) }
    var selectedCountry by remember { mutableStateOf(Country.COUNTRIES[0]) } // Default Indonesia
    var userName by remember { mutableStateOf("") }
    var weatherOption by remember { mutableStateOf(WeatherOption.APPROX_LOCATION) }
    var railOnRight by remember { mutableStateOf(true) }
    var shakeToSwap by remember { mutableStateOf(false) }
    var loadExampleData by remember { mutableStateOf(true) }
    var isCompleted by remember { mutableStateOf(false) }

    val texts = getOnboardingTexts(language)
    val view = LocalView.current
    val hapticTick = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }

    if (isCompleted) {
        // Confirmation Screen (CheckCircle scales in over 600ms)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate100),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.CheckCircle,
                    contentDescription = "Ready",
                    tint = Cobalt,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(1.1f)
                )

                Text(
                    text = texts.readyTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Charcoal
                )

                Button(
                    onClick = {
                        onFinish(language, selectedCountry.currencyCode, userName, railOnRight, shakeToSwap, loadExampleData)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .height(56.dp)
                        .padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = "Go to Dashboard",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // 1. Progress Segments (7 steps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 1..8) {
                    val isActive = i <= step
                    val segmentColor by animateColorAsState(
                        targetValue = if (isActive) Cobalt else Slate200,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "progressSegment"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(segmentColor)
                    )
                }
            }

            // 2. Navigation Top Bar (Back button on steps 2 to 8)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (step > 1) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                hapticTick()
                                step--
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.CaretLeft,
                            contentDescription = "Back",
                            tint = Charcoal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Step Content (Animated Horizontal Slide)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "onboardingStepTransition"
                ) { currentStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (currentStep) {
                            1 -> Step1Welcome(texts = texts)
                            2 -> Step2Language(
                                selectedLanguage = language,
                                onSelect = {
                                    hapticTick()
                                    language = it
                                },
                                texts = texts
                            )
                            3 -> Step3RegionCurrency(
                                selectedCountry = selectedCountry,
                                onSelect = {
                                    hapticTick()
                                    selectedCountry = it
                                },
                                isIndonesian = language == AppLanguage.INDONESIAN,
                                texts = texts
                            )
                            4 -> Step4Name(
                                name = userName,
                                onNameChange = { if (it.length <= 40) userName = it },
                                texts = texts
                            )
                            5 -> Step5Weather(
                                selectedOption = weatherOption,
                                onSelect = {
                                    hapticTick()
                                    weatherOption = it
                                },
                                texts = texts
                            )
                            6 -> Step6Permissions(texts = texts)
                            7 -> Step7Layout(
                                railOnRight = railOnRight,
                                onToggleRail = {
                                    hapticTick()
                                    railOnRight = it
                                },
                                shakeToSwap = shakeToSwap,
                                onToggleShake = {
                                    hapticTick()
                                    shakeToSwap = it
                                },
                                texts = texts
                            )
                            8 -> Step8Ready(
                                loadExample = loadExampleData,
                                onToggleExample = {
                                    hapticTick()
                                    loadExampleData = it
                                },
                                texts = texts
                            )
                        }
                    }
                }
            }

            // 4. Primary Bottom Button (56dp tall, 16dp corners)
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    hapticTick()
                    if (step < 8) {
                        step++
                    } else {
                        isCompleted = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Cobalt.copy(alpha = 0.25f),
                        spotColor = Cobalt.copy(alpha = 0.35f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cobalt)
            ) {
                Text(
                    text = if (step == 1) texts.getStarted else if (step == 8) texts.finishBtn else texts.continueBtn,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            // Optional "Skip" button on Step 4 (Name)
            if (step == 4) {
                TextButton(
                    onClick = {
                        hapticTick()
                        userName = ""
                        step++
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = texts.skip,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Slate500
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Step 1: Welcome
// ----------------------------------------------------------------------------
@Composable
private fun Step1Welcome(texts: OnboardingTexts) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            text = texts.welcomeTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Charcoal,
            lineHeight = 38.sp
        )

        Text(
            text = texts.welcomeSubtitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Slate600,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Three Promise Rows
        PromiseRow(
            icon = PhosphorIcons.Regular.ShieldCheck,
            title = texts.promise1,
            tint = Cobalt
        )

        PromiseRow(
            icon = PhosphorIcons.Regular.DeviceMobile,
            title = texts.promise2,
            tint = Emerald
        )

        PromiseRow(
            icon = PhosphorIcons.Regular.CloudSlash,
            title = texts.promise3,
            tint = Persimmon
        )
    }
}

@Composable
private fun PromiseRow(icon: ImageVector, title: String, tint: Color) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Charcoal
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Step 2: Language
// ----------------------------------------------------------------------------
@Composable
private fun Step2Language(
    selectedLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    texts: OnboardingTexts
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = texts.chooseLanguageTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Charcoal,
            lineHeight = 38.sp
        )

        Text(
            text = texts.chooseLanguageSubtitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(8.dp))

        LanguageCard(
            language = AppLanguage.ENGLISH,
            isSelected = selectedLanguage == AppLanguage.ENGLISH,
            onClick = { onSelect(AppLanguage.ENGLISH) }
        )

        LanguageCard(
            language = AppLanguage.INDONESIAN,
            isSelected = selectedLanguage == AppLanguage.INDONESIAN,
            onClick = { onSelect(AppLanguage.INDONESIAN) }
        )
    }
}

@Composable
private fun LanguageCard(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Cobalt else GlassBorder,
        label = "langBorder"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor,
        borderWidth = if (isSelected) 2.dp else 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = language.nativeName,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
                Text(
                    text = language.displayName,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Slate500
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Cobalt, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Step 3: Region and Currency
// ----------------------------------------------------------------------------
@Composable
private fun Step3RegionCurrency(
    selectedCountry: Country,
    onSelect: (Country) -> Unit,
    isIndonesian: Boolean,
    texts: OnboardingTexts
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCountries = remember(searchQuery, isIndonesian) {
        if (searchQuery.isBlank()) {
            Country.COUNTRIES
        } else {
            Country.COUNTRIES.filter { country ->
                country.localizedName(isIndonesian).contains(searchQuery, ignoreCase = true) ||
                country.currencyCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = texts.regionTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Charcoal,
            lineHeight = 38.sp
        )

        Text(
            text = texts.regionSubtitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Slate600
        )

        // Search Bar
        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                    contentDescription = "Search",
                    tint = Slate500,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        color = Charcoal
                    ),
                    cursorBrush = SolidColor(Cobalt),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = texts.searchCountry,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 14.sp,
                                color = Slate500
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Country List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredCountries.forEach { country ->
                val isSelected = country.code == selectedCountry.code
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) Cobalt else GlassBorder,
                    label = "countryBorder"
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = borderColor,
                    borderWidth = if (isSelected) 2.dp else 1.dp,
                    onClick = { onSelect(country) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Currency Badge (40dp square, 12dp radius)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Cobalt.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = country.currencyCode,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Cobalt
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = country.localizedName(isIndonesian),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Charcoal
                                )
                                Text(
                                    text = country.localizedCurrencyName(isIndonesian),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Cobalt, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Preview Under List
        Text(
            text = "${texts.mainCurrencyPrefix}: ${selectedCountry.localizedCurrencyName(isIndonesian)} (${selectedCountry.currencyCode})",
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Slate600,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ----------------------------------------------------------------------------
// Step 4: Name
// ----------------------------------------------------------------------------
@Composable
private fun Step4Name(
    name: String,
    onNameChange: (String) -> Unit,
    texts: OnboardingTexts
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = texts.nameTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Charcoal,
            lineHeight = 38.sp
        )

        Text(
            text = texts.nameSubtitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Name Text Field in GlassCard
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(18.dp)) {
                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Charcoal
                    ),
                    cursorBrush = SolidColor(Cobalt),
                    decorationBox = { innerTextField ->
                        if (name.isEmpty()) {
                            Text(
                                text = texts.namePlaceholder,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Slate400
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(
            text = "${name.length} / 40",
            fontFamily = SpaceGroteskFamily,
            fontSize = 12.sp,
            color = Slate500,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// ----------------------------------------------------------------------------
// Step 5: Weather
// ----------------------------------------------------------------------------
@Composable
private fun Step5Weather(
    selectedOption: WeatherOption,
    onSelect: (WeatherOption) -> Unit,
    texts: OnboardingTexts
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = texts.weatherTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Charcoal,
            lineHeight = 38.sp
        )

        Text(
            text = texts.weatherSubtitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(8.dp))

        WeatherCard(
            title = texts.weatherApproxLocation,
            subtitle = texts.weatherApproxDesc,
            icon = PhosphorIcons.Regular.MapPin,
            isSelected = selectedOption == WeatherOption.APPROX_LOCATION,
            onClick = { onSelect(WeatherOption.APPROX_LOCATION) }
        )

        WeatherCard(
            title = texts.weatherSearchCity,
            subtitle = texts.weatherSearchDesc,
            icon = PhosphorIcons.Regular.MagnifyingGlass,
            isSelected = selectedOption == WeatherOption.SEARCH_CITY,
            onClick = { onSelect(WeatherOption.SEARCH_CITY) }
        )

        WeatherCard(
            title = texts.weatherNotNow,
            subtitle = texts.weatherNotNowDesc,
            icon = PhosphorIcons.Regular.Thermometer,
            isSelected = selectedOption == WeatherOption.NOT_NOW,
            onClick = { onSelect(WeatherOption.NOT_NOW) }
        )

        Text(
            text = texts.weatherFooter,
            fontFamily = PlusJakartaSansFamily,
            fontSize = 11.sp,
            color = Slate500,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun WeatherCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Cobalt else GlassBorder,
        label = "weatherBorder"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor,
        borderWidth = if (isSelected) 2.dp else 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Cobalt.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Cobalt,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Charcoal
                    )
                    Text(
                        text = subtitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Cobalt, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Step 6: Transparent Privacy & Permissions
// ----------------------------------------------------------------------------
@Composable
private fun Step6Permissions(
    texts: OnboardingTexts
) {
    val context = LocalContext.current
    var hasLocationPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotifPerm by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPerm = isGranted
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifPerm = isGranted
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = texts.permissionsTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Charcoal,
            lineHeight = 38.sp
        )

        Text(
            text = texts.permissionsSubtitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 1. Approximate Location Card
        PermissionItemCard(
            title = texts.permLocationTitle,
            description = texts.permLocationDesc,
            icon = PhosphorIcons.Regular.MapPin,
            isGranted = hasLocationPerm,
            allowedText = texts.permAllowed,
            grantText = texts.permGrant,
            onRequest = {
                locationLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        )

        // 2. Notifications Card
        PermissionItemCard(
            title = texts.permNotifTitle,
            description = texts.permNotifDesc,
            icon = PhosphorIcons.Regular.Bell,
            isGranted = hasNotifPerm,
            allowedText = texts.permAllowed,
            grantText = texts.permGrant,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        // 3. Local Storage Card (Sandboxed & Private)
        PermissionItemCard(
            title = texts.permStorageTitle,
            description = texts.permStorageDesc,
            icon = PhosphorIcons.Regular.DeviceMobile,
            isGranted = true,
            allowedText = texts.permAllowed,
            grantText = texts.permGrant,
            onRequest = {}
        )
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    allowedText: String,
    grantText: String,
    onRequest: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) Emerald.copy(alpha = 0.12f) else Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) Emerald else Cobalt,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Charcoal
                )
                Text(
                    text = description,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = Slate500,
                    lineHeight = 16.sp
                )
            }

            if (isGranted) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Emerald.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Check,
                        contentDescription = "Allowed",
                        tint = Emerald,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = allowedText,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Emerald
                    )
                }
            } else {
                Surface(
                    onClick = onRequest,
                    shape = RoundedCornerShape(12.dp),
                    color = Cobalt,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = grantText,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Step 7: Layout
// ----------------------------------------------------------------------------
@Composable
private fun Step7Layout(
    railOnRight: Boolean,
    onToggleRail: (Boolean) -> Unit,
    shakeToSwap: Boolean,
    onToggleShake: (Boolean) -> Unit,
    texts: OnboardingTexts
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = texts.layoutTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Charcoal,
            lineHeight = 38.sp
        )

        Text(
            text = texts.layoutSubtitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Slate600
        )

        // Side Segmented Control + Mini Phone Preview
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Mini phone wireframe preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate100)
                        .padding(12.dp),
                    contentAlignment = if (railOnRight) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    // Mini rail pill indicator
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(64.dp)
                            .shadow(4.dp, RoundedCornerShape(9.dp))
                            .background(Cobalt, RoundedCornerShape(9.dp))
                    )
                }

                // Left / Right segmented control
                val segmentItems = listOf(
                    SegmentItem(false, texts.menuLeft, Cobalt),
                    SegmentItem(true, texts.menuRight, Cobalt)
                )

                SegmentedControl(
                    items = segmentItems,
                    selectedValue = railOnRight,
                    onValueSelected = onToggleRail
                )
            }
        }

        // Toggle: Shake to Swap Sides
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onToggleShake(!shakeToSwap) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = texts.shakeTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Charcoal
                    )
                    Text(
                        text = texts.shakeSubtitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }

                ToggleSwitch(
                    checked = shakeToSwap,
                    onCheckedChange = onToggleShake
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Step 8: Ready / Example Data
// ----------------------------------------------------------------------------
@Composable
private fun Step8Ready(
    loadExample: Boolean,
    onToggleExample: (Boolean) -> Unit,
    texts: OnboardingTexts
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = texts.readyTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Charcoal,
            lineHeight = 38.sp
        )

        Text(
            text = texts.readySubtitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card 1: Load Example Data (Default selected)
        ExampleDataCard(
            title = texts.exampleTitle,
            subtitle = texts.exampleDesc,
            isSelected = loadExample,
            onClick = { onToggleExample(true) }
        )

        // Card 2: Start from Scratch
        ExampleDataCard(
            title = texts.scratchTitle,
            subtitle = texts.scratchDesc,
            isSelected = !loadExample,
            onClick = { onToggleExample(false) }
        )
    }
}

@Composable
private fun ExampleDataCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Cobalt else GlassBorder,
        label = "readyBorder"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor,
        borderWidth = if (isSelected) 2.dp else 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Charcoal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 13.sp,
                    color = Slate500,
                    lineHeight = 18.sp
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Cobalt, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
