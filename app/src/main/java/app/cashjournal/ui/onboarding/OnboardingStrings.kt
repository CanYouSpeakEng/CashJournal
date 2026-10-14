package app.cashjournal.ui.onboarding

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    INDONESIAN("in", "Indonesian", "Bahasa Indonesia")
}

data class OnboardingTexts(
    val welcomeTitle: String,
    val welcomeSubtitle: String,
    val promise1: String,
    val promise2: String,
    val promise3: String,
    val getStarted: String,

    val chooseLanguageTitle: String,
    val chooseLanguageSubtitle: String,
    val continueBtn: String,

    val regionTitle: String,
    val regionSubtitle: String,
    val searchCountry: String,
    val mainCurrencyPrefix: String,

    val nameTitle: String,
    val nameSubtitle: String,
    val namePlaceholder: String,
    val skip: String,

    val weatherTitle: String,
    val weatherSubtitle: String,
    val weatherApproxLocation: String,
    val weatherApproxDesc: String,
    val weatherSearchCity: String,
    val weatherSearchDesc: String,
    val weatherNotNow: String,
    val weatherNotNowDesc: String,
    val weatherFooter: String,
    val locationOffWarning: String,

    val layoutTitle: String,
    val layoutSubtitle: String,
    val menuLeft: String,
    val menuRight: String,
    val shakeTitle: String,
    val shakeSubtitle: String,

    val readyTitle: String,
    val readySubtitle: String,
    val scratchTitle: String,
    val scratchDesc: String,
    val exampleTitle: String,
    val exampleDesc: String,
    val exampleNeedsNet: String,
    val finishBtn: String
)

val EnglishOnboardingTexts = OnboardingTexts(
    welcomeTitle = "Your money, on your phone.",
    welcomeSubtitle = "Track accounts, spending, and cash flow. Everything stays on this device.",
    promise1 = "No account to create.",
    promise2 = "Your data stays on this phone.",
    promise3 = "Nothing is uploaded or synced.",
    getStarted = "Get started",

    chooseLanguageTitle = "Choose your language",
    chooseLanguageSubtitle = "You can change this anytime in Settings.",
    continueBtn = "Continue",

    regionTitle = "Where are you from?",
    regionSubtitle = "Your main currency will match your country. You can change it later in Settings.",
    searchCountry = "Search country",
    mainCurrencyPrefix = "Main currency",

    nameTitle = "What should we call you?",
    nameSubtitle = "We use this only for your greeting.",
    namePlaceholder = "Your name",
    skip = "Skip",

    weatherTitle = "Show local weather?",
    weatherSubtitle = "Weather uses your approximate location, rounded to about 1 km. Only that rounded location is sent to the weather service, Open-Meteo.",
    weatherApproxLocation = "Use my approximate location",
    weatherApproxDesc = "Rounded to ~1 km for privacy",
    weatherSearchCity = "Search for my city",
    weatherSearchDesc = "Select a city manually",
    weatherNotNow = "Not now",
    weatherNotNowDesc = "Keep weather hidden",
    weatherFooter = "Weather data by Open-Meteo.com",
    locationOffWarning = "Location is off. You can allow it later in Settings, or search for your city.",

    layoutTitle = "Where should the menu sit?",
    layoutSubtitle = "You can change this anytime. If you turn on shake, you can also move it by shaking the phone.",
    menuLeft = "Left side",
    menuRight = "Right side",
    shakeTitle = "Shake to swap sides",
    shakeSubtitle = "Optional. A firm shake moves the menu to the other side.",

    readyTitle = "You're all set",
    readySubtitle = "Choose how you'd like to start.",
    scratchTitle = "Start from scratch",
    scratchDesc = "Begin empty. Add your accounts when you're ready.",
    exampleTitle = "Load example data",
    exampleDesc = "Sample accounts and transactions so you can explore. Remove them anytime in Settings.",
    exampleNeedsNet = "Needs an internet connection once",
    finishBtn = "Finish"
)

val IndonesianOnboardingTexts = OnboardingTexts(
    welcomeTitle = "Uang Anda, di ponsel Anda.",
    welcomeSubtitle = "Catat rekening, pengeluaran, dan arus kas. Semuanya tersimpan di perangkat ini.",
    promise1 = "Tanpa perlu membuat akun.",
    promise2 = "Data Anda tetap ada di ponsel ini.",
    promise3 = "Tidak ada yang diunggah atau disinkronkan.",
    getStarted = "Mulai sekarang",

    chooseLanguageTitle = "Pilih bahasa Anda",
    chooseLanguageSubtitle = "Anda dapat mengubahnya kapan saja di Pengaturan.",
    continueBtn = "Lanjutkan",

    regionTitle = "Dari mana Anda berasal?",
    regionSubtitle = "Mata uang utama Anda akan disesuaikan dengan negara Anda. Anda dapat mengubahnya nanti di Pengaturan.",
    searchCountry = "Cari negara",
    mainCurrencyPrefix = "Mata uang utama",

    nameTitle = "Siapa nama panggilan Anda?",
    nameSubtitle = "Kami hanya menggunakan ini untuk sapaan Anda.",
    namePlaceholder = "Nama Anda",
    skip = "Lewati",

    weatherTitle = "Tampilkan cuaca lokal?",
    weatherSubtitle = "Cuaca menggunakan perkiraan lokasi Anda, dibulatkan ke sekitar 1 km. Hanya lokasi yang dibulatkan tersebut yang dikirim ke layanan cuaca Open-Meteo.",
    weatherApproxLocation = "Gunakan perkiraan lokasi saya",
    weatherApproxDesc = "Dibulatkan ke ~1 km demi privasi",
    weatherSearchCity = "Cari kota saya",
    weatherSearchDesc = "Pilih kota secara manual",
    weatherNotNow = "Nanti saja",
    weatherNotNowDesc = "Biarkan cuaca tersembunyi",
    weatherFooter = "Data cuaca oleh Open-Meteo.com",
    locationOffWarning = "Lokasi dimatikan. Anda dapat mengizinkannya nanti di Pengaturan, atau cari kota Anda.",

    layoutTitle = "Di mana posisi menu navigasi?",
    layoutSubtitle = "Anda dapat mengubah ini kapan saja. Jika Anda mengaktifkan goyang, Anda juga dapat memindahkannya dengan menggoyangkan ponsel.",
    menuLeft = "Sisi kiri",
    menuRight = "Sisi kanan",
    shakeTitle = "Goyang untuk tukar posisi",
    shakeSubtitle = "Opsional. Goyangan mantap akan memindahkan menu ke sisi lain.",

    readyTitle = "Semua sudah siap",
    readySubtitle = "Pilih bagaimana Anda ingin memulai.",
    scratchTitle = "Mulai dari awal",
    scratchDesc = "Mulai kosong. Tambahkan rekening saat Anda siap.",
    exampleTitle = "Muat data contoh",
    exampleDesc = "Contoh rekening dan transaksi agar Anda bisa menjelajah. Hapus kapan saja di Pengaturan.",
    exampleNeedsNet = "Perlu koneksi internet sekali",
    finishBtn = "Selesai"
)

fun getOnboardingTexts(language: AppLanguage): OnboardingTexts {
    return when (language) {
        AppLanguage.ENGLISH -> EnglishOnboardingTexts
        AppLanguage.INDONESIAN -> IndonesianOnboardingTexts
    }
}
