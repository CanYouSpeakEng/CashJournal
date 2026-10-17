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
    val knockSensitivityTitle: String,
    val knockSensitivityLow: String,
    val knockSensitivityMed: String,
    val knockSensitivityHigh: String,
    val phoneSimulatePrompt: String,

    val permissionsTitle: String,
    val permissionsSubtitle: String,
    val permLocationTitle: String,
    val permLocationDesc: String,
    val permNotifTitle: String,
    val permNotifDesc: String,
    val permStorageTitle: String,
    val permStorageDesc: String,
    val permStorageTag: String,
    val permStorageTagDesc: String,
    val permAllowed: String,
    val permGrant: String,

    val readyTitle: String,
    val readySubtitle: String,
    val scratchTitle: String,
    val scratchDesc: String,
    val keepExistingTitle: String,
    val keepExistingDesc: String,
    val exampleTitle: String,
    val exampleDesc: String,
    val exampleExistingDesc: String,
    val exampleNeedsNet: String,
    val finishBtn: String
)

val EnglishOnboardingTexts = OnboardingTexts(
    welcomeTitle = "Your money, on your phone.",
    welcomeSubtitle = "Track accounts, spending, and cash flow. Everything stays on this device.",
    promise1 = "No account to create.",
    promise2 = "Your data stays on this phone.",
    promise3 = "Zero financial data leaves your phone. Only approximate coordinates are sent to fetch weather.",
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
    layoutSubtitle = "Choose the side that feels most natural. You can also shake or tap your phone to move it.",
    menuLeft = "Left side",
    menuRight = "Right side",
    shakeTitle = "Shake or tap phone to switch sides",
    shakeSubtitle = "Optional. Move the sidebar by firmly shaking or tapping your phone.",
    knockSensitivityTitle = "Knock Sensitivity",
    knockSensitivityLow = "Low",
    knockSensitivityMed = "Medium",
    knockSensitivityHigh = "High",
    phoneSimulatePrompt = "Tap phone preview to test moving the menu",

    permissionsTitle = "Transparent Privacy & Access",
    permissionsSubtitle = "Everything in CashJournal stays strictly offline on your device. We only request permissions for features you choose to use.",
    permLocationTitle = "Approximate Location",
    permLocationDesc = "For local weather without tracking. Rounded to ~1 km for privacy.",
    permNotifTitle = "Daily Reminders",
    permNotifDesc = "Optional evening reminders to record expenses. No ads, zero promotional spam.",
    permStorageTitle = "Local Storage & Offline Export",
    permStorageDesc = "CashJournal saves your records in a private on-device sandbox. Zero filesystem or storage permissions are needed or requested.",
    permStorageTag = "Device Sandbox",
    permStorageTagDesc = "No permission needed",
    permAllowed = "Allowed",
    permGrant = "Allow",

    readyTitle = "You're all set",
    readySubtitle = "Choose how you'd like to start.",
    scratchTitle = "Start from scratch",
    scratchDesc = "Begin empty. Add your accounts when you're ready.",
    keepExistingTitle = "Keep Existing Accounts",
    keepExistingDesc = "Preserve your accounts and records. No sample data will be added.",
    exampleTitle = "Load Demo Mode",
    exampleDesc = "Sample accounts and transactions to explore the app. Remove anytime in Settings.",
    exampleExistingDesc = "Adds sample accounts and transactions alongside your current data.",
    exampleNeedsNet = "Needs an internet connection once",
    finishBtn = "Finish"
)

val IndonesianOnboardingTexts = OnboardingTexts(
    welcomeTitle = "Uang Anda, di ponsel Anda.",
    welcomeSubtitle = "Catat rekening, pengeluaran, dan arus kas. Semuanya tersimpan di perangkat ini.",
    promise1 = "Tanpa perlu membuat akun.",
    promise2 = "Data Anda tetap ada di ponsel ini.",
    promise3 = "Tidak ada data finansial yang meninggalkan ponsel Anda. Hanya koordinat perkiraan yang dikirim untuk mengambil cuaca.",
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
    layoutSubtitle = "Pilih sisi yang paling nyaman untuk Anda. Anda juga bisa menggoyang atau mengetuk ponsel untuk memindahkannya.",
    menuLeft = "Sisi kiri",
    menuRight = "Sisi kanan",
    shakeTitle = "Goyang atau ketuk ponsel untuk tukar posisi",
    shakeSubtitle = "Opsional. Goyang mantap atau ketuk bodi ponsel untuk memindahkan menu ke sisi lain.",
    knockSensitivityTitle = "Sensitivitas Ketukan",
    knockSensitivityLow = "Rendah",
    knockSensitivityMed = "Sedang",
    knockSensitivityHigh = "Tinggi",
    phoneSimulatePrompt = "Ketuk gambar ponsel untuk mencoba memindahkan menu",

    permissionsTitle = "Privasi & Akses Transparan",
    permissionsSubtitle = "Semua data CashJournal tersimpan sepenuhnya di perangkat Anda. Kami hanya meminta izin untuk fitur yang Anda pilih.",
    permLocationTitle = "Perkiraan Lokasi",
    permLocationDesc = "Untuk cuaca lokal tanpa pelacakan. Dibulatkan ke ~1 km demi privasi.",
    permNotifTitle = "Pengingat Harian",
    permNotifDesc = "Pengingat malam opsional untuk mencatat pengeluaran. Tanpa iklan atau spam.",
    permStorageTitle = "Penyimpanan Lokal & Ekspor Offline",
    permStorageDesc = "CashJournal menyimpan catatan Anda di sandbox perangkat terisolasi. Tanpa izin penyimpanan atau akses berkas apa pun yang diperlukan atau diminta.",
    permStorageTag = "Sandbox Perangkat",
    permStorageTagDesc = "Tanpa perlu izin",
    permAllowed = "Diizinkan",
    permGrant = "Izinkan",

    readyTitle = "Semua sudah siap",
    readySubtitle = "Pilih bagaimana Anda ingin memulai.",
    scratchTitle = "Mulai dari awal",
    scratchDesc = "Mulai kosong. Tambahkan rekening saat Anda siap.",
    keepExistingTitle = "Pertahankan Rekening Saat Ini",
    keepExistingDesc = "Simpan rekening dan catatan Anda yang ada. Tanpa menambahkan data demo.",
    exampleTitle = "Muat Mode Demo",
    exampleDesc = "Contoh rekening dan transaksi agar Anda bisa menjelajah. Hapus kapan saja di Pengaturan.",
    exampleExistingDesc = "Menambahkan rekening dan transaksi demo berdampingan dengan data Anda saat ini.",
    exampleNeedsNet = "Perlu koneksi internet sekali",
    finishBtn = "Selesai"
)

fun getOnboardingTexts(language: AppLanguage): OnboardingTexts {
    return when (language) {
        AppLanguage.ENGLISH -> EnglishOnboardingTexts
        AppLanguage.INDONESIAN -> IndonesianOnboardingTexts
    }
}
