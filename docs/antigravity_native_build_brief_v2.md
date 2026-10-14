# Build brief v2: native Android app matching the approved prototype

This version supersedes v1. Read all of it before you plan. The attached HTML file is the visual reference for anything this brief does not describe. Where this brief and the HTML disagree, this brief wins.

## 1. How to work

**Phase 1, plan only.** Read the HTML and this brief. Write an implementation plan artifact with: the screen, sheet, and onboarding inventory; the design token table; the Phosphor icon names you will use, and any name missing from the library with its replacement; the services and endpoints; the data model; the decisions in section 17 with your defaults; and any part that cannot match exactly. Then stop and wait for my approval. Write no app code in this phase.

**Phase 2, foundation.** Create the project, theme, fonts, icon library, and a preview screen with the color swatches, type scale, buttons, and a card. Take a screenshot and stop for my review.

**Phase 3, shared components.** Glass card, greeting bar, side rail, amount text with masking, transaction row, account card, chip, segmented control, toggle, progress segments, bottom sheet, toast, keypad. Screenshot each one.

**Phase 4, onboarding.** Build all seven steps (section 8) in English and Indonesian. Screenshot each step in both languages and stop for review.

**Phase 5, main screens and sheets.** Dashboard, Ledger, Accounts, Account detail, Analytics, Settings, then the sheets. Screenshot each at 390×844 dp and 360×800 dp, including each empty state.

**Phase 6, services.** Weather, exchange rates, city search, location, currency change, clear data, backup rules. Test in airplane mode.

**Phase 7, motion and behavior.** Section 15.

**Phase 8, verification.** Compare visuals with the HTML. Check every number in section 16. Capture network traffic and confirm only the three hosts in section 3 are contacted. Check that no emoji appears in any string or screen. Check that Indonesian text fits.

Keep a README with build and run steps, the decisions you made, and the attribution text from section 11.

If you cannot run an emulator or open the HTML yourself, say so and ask me for screenshots.

## 2. Non-negotiable rules

**Free.** Every library, font, icon set, and service must be free to use, with no paid plan, no card, and no account. Do not add anything that needs an account or an API key.

**Private.** Financial data never leaves the phone. No cloud, no server, no sync. Sync may come later, so do not build it now. No analytics, no advertising, no crash reporting that needs an account, and no third-party SDKs beyond the list in section 3. Turn off Android backup for app data: set allowBackup to false, and exclude app data from device transfer and cloud backup rules.

**Ready for later sync.** Every account and transaction gets a stable UUID and an updatedAt timestamp. Nothing else about sync is built now.

**Icons are Phosphor only.** Every icon comes from Phosphor (section 3.1). No Material icons, no other icon sets.

**No emojis anywhere.** Not in strings, toasts, weather, categories, country lists, empty states, or the launcher icon. The launcher icon is a custom vector mark in cobalt.

**Phone only**, portrait, Android 8.0 (API 26) and newer, latest stable target SDK. Draw edge to edge.

**Do not add** screens, features, or copy that are not in this brief or the HTML.

## 3. Services and libraries (all free)

- Kotlin, Jetpack Compose, AndroidX, Room, DataStore, kotlinx.serialization, OkHttp. All open source.
- **Icons:** Phosphor Icons, MIT license. Use the Compose library com.adamglin:phosphor-icon from Maven Central, Regular weight throughout.
- **Fonts:** Plus Jakarta Sans and Space Grotesk, SIL Open Font License. Bundle them in the app.
- **Weather:** Open-Meteo. No key, no account. Free for open-source and non-commercial use.
  - Current conditions: `https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,weather_code,is_day&timezone=auto`. Add `&temperature_unit=fahrenheit` when the unit setting is Fahrenheit.
  - City search: `https://geocoding-api.open-meteo.com/v1/search?name={query}&count=5&language={appLanguage}&format=json`
- **Exchange rates:** ExchangeRate-API Open Access. No key, no account. It updates once a day and is rate-limited. Do not redistribute it.
  - `https://open.er-api.com/v6/latest/{MAIN_CODE}`. Use the rates map. Show the update time from the response field time_last_update_utc.
  - Refresh automatically at most once every 12 hours. Manual refresh at most once an hour. Cache the last response on the phone.
- **Countries and currencies:** from Android itself (Locale.getISOCountries, java.util.Currency, and the display-name functions). Do not bundle lists.
- **Location:** Android's LocationManager with the coarse location permission only. Do not depend on Google Play Services.
- **Runtime hosts allowed:** api.open-meteo.com, geocoding-api.open-meteo.com, open.er-api.com. No others.
- If you think something else is needed, ask me first.

### 3.1 Icon map (Phosphor, Regular)

| Use | Icon |
|---|---|
| Dashboard tab | SquaresFour |
| Ledger tab | Receipt |
| Accounts tab | Wallet |
| Analytics tab | ChartBar |
| Settings tab | GearSix |
| Add | Plus |
| Balances visible (privacy button) | Lock |
| Balances hidden | EyeSlash |
| Back | CaretLeft |
| Open row | CaretRight |
| Reorder up / down | CaretUp / CaretDown |
| Scroll for more | CaretDown |
| Search | MagnifyingGlass |
| Refresh rates | ArrowsClockwise |
| World currencies | Globe |
| Main currency | CurrencyCircleDollar |
| Close | X |
| Selected | Check |
| Income | ArrowDownLeft |
| Transfer | ArrowsLeftRight |
| Category: Food & Dining | Coffee |
| Category: Transportation | Train |
| Category: Shopping | ShoppingBag |
| Category: Utilities | Lightning |
| Category: Housing | House |
| Category: Investment | ChartLineUp |
| Category: Salary | Money |
| Category: Other | Tag |
| Example seed: dividend | Sparkle |
| Example seed: bike ride | Bicycle |
| Keypad delete | Backspace |
| Weather: clear (day / night) | Sun / Moon |
| Weather: partly cloudy (day / night) | CloudSun / CloudMoon |
| Weather: overcast | Cloud |
| Weather: fog | CloudFog |
| Weather: drizzle, rain, showers | CloudRain |
| Weather: snow | CloudSnow |
| Weather: thunderstorm | CloudLightning |
| Weather unavailable | Thermometer |
| Location option | MapPin |
| Privacy promises (onboarding) | ShieldCheck, DeviceMobile, CloudSlash |
| Empty states | Wallet, Receipt, ChartBar |
| Clear all data | Trash |
| Onboarding finished | CheckCircle |

## 4. What changed from the prototype

- The single onboarding sheet is replaced by the seven-step onboarding in section 8.
- Weather is real. The fixed values by time of day are removed.
- Money uses a main currency chosen by the user's country. Rupiah is the default only for Indonesia.
- Flags and emojis are removed. Currency rows show the currency code in a badge.
- Icons are Phosphor.
- Rates say "Updated daily", not "Real-Time". The free source updates once a day.
- Spending resets on the first day of each month, and days left come from the calendar.
- Analytics bars and the donut use the user's transactions, not fixed sample values.
- The language buttons in Settings work. Language is also chosen in onboarding.
- Number formatting follows the main currency's region.
- The greeting uses the name entered in onboarding.

## 5. Design tokens

**Colors (hex)**
- Cobalt `#1D4ED8` (dark `#1E40AF`, light `#EFF6FF`)
- Emerald `#059669` (dark `#065F46`, light `#ECFDF5`)
- Persimmon `#EA580C` (dark `#C2410C`, light `#FFF7ED`)
- Saffron `#D97706` (dark `#B45309`, light `#FFFBEB`)
- Iris `#6366F1` (dark `#4338CA`, light `#EEF2FF`)
- Berry `#E11D48` (dark `#BE123C`, light `#FFF1F2`)
- Charcoal `#0F172A`, muted text `#475569`, subtle text `#94A3B8`
- Slate: `#F8FAFC`, `#F1F5F9`, `#E2E8F0`, `#CBD5E1`, `#94A3B8`, `#64748B`, `#1E293B`
- Recalibrate status: `#F87171`
- Glass card: white at 78%. Subtle glass card: white at 58%. Side rail: white at 88%. Glass border: 1dp white at 95%.
- Bottom sheets: white at 95%, white top border. Scrim behind sheets: charcoal at 30%.
- Account accent keys: cobalt, emerald, persimmon, saffron, iris, berry.

**Typography**
- Plus Jakarta Sans (300 to 800) for body text.
- Space Grotesk (500 to 700) for labels, numbers, and money. It is not a monospace font.
- Sizes: 10, 11, 12, 14, 16, 20, 24, 30, 32 sp.
- Small labels: 10 sp, uppercase, bold, letter spacing 0.1em, muted color.
- Hero amount: 30 sp extra-bold. Currency symbol before it: 24 sp bold, cobalt.
- Onboarding titles: 32 sp extra-bold.

**Shapes**
- Safe-to-spend card: 28dp. Standard glass cards: 24dp. Subtle cards and transaction rows: 16dp. Carousel account cards: 22dp. Icon tiles and inputs: 12dp. Large buttons: 16dp. Sheet top corners: 36dp. Pills, chips, rail, toast: fully rounded.
- Toggle: 52×30dp, track radius 11dp, knob 24×24dp radius 8dp, 3dp inset.
- Currency badge: 40dp square, radius 12dp, code in Space Grotesk bold.

**Shadows:** soft, low-opacity charcoal. Glass cards use 1dp, 8dp, and 20dp shadows at 4 to 6%. Primary buttons have a cobalt-tinted shadow.

**Spacing:** 16dp card padding, 12dp between sections, 8dp between list items, 20dp page margin.

**Icon sizes:** 16dp in rows and chips, 20dp in the rail and buttons, 24dp in onboarding and the weather block.

**Money format**
- Use the main currency's symbol and the number format of the region. Indonesia: "Rp 12.500.000". United States: "$12,500.00". Indonesia with a dollar value: "US$ 1.176,66".
- Decimal places follow the currency: IDR, JPY, KRW, and VND have none; USD and EUR have two.
- Negative amounts get a true minus sign (U+2212). In lists, income gets "+" and expenses get "−".

## 6. Layout

Measure from the top of the screen.
- **Greeting zone:** the top 15% of the screen height. Nothing scrolls above it.
- **Bottom zone:** the bottom 25%. It stays empty except for the "Scroll for more" pill.
- **Content column:** 20dp from the left edge and 78dp from the right edge while the rail is on the right. Mirrored while the rail is on the left. The column moves with the rail.
- **Greeting bar:** 20dp side margins, 46dp tall, sitting just above the content column.
- **Side rail:** a 42dp floating pill, 12dp from its edge, vertically centered in the content area. From top to bottom: a 42×50dp clock zone, five navigation buttons (42dp circles, 10dp apart), a divider, and a 40dp "+" button.

## 7. Greeting bar and weather

**Weather block** (left of the greeting bar)
- Phosphor weather icon at 24dp, then the temperature, for example "24°C", bold 20 sp.
- Before the first reading, or if the user chose not to use weather: the block shows Thermometer and "--" when there is no data. If the user turned weather off entirely, hide the block.
- A 1dp vertical divider, 28dp tall, light slate, then the greeting.

**Greeting:** "Good morning" before 12:00, "Good afternoon" from 12:00 to 16:59, "Good evening" from 17:00 to 20:59, "Good night" from 21:00. A small uppercase label. The user's name in bold below it. If the name was skipped, show only the greeting, vertically centered.

**Account detail:** the label "Account" and the account name instead.

**Weather icon by condition** (Open-Meteo weather codes, day or night from is_day):
- 0 clear: Sun by day, Moon by night.
- 1 and 2 mainly clear and partly cloudy: CloudSun by day, CloudMoon by night.
- 3 overcast: Cloud.
- 45 and 48 fog: CloudFog.
- 51 to 67 and 80 to 82 drizzle, rain, showers: CloudRain.
- 71 to 77 and 85 and 86 snow: CloudSnow.
- 95 to 99 thunderstorm: CloudLightning.
- Anything else: Cloud.
Use the matching text for accessibility labels: Clear, Mostly clear, Partly cloudy, Overcast, Fog, Drizzle, Rain, Snow, Showers, Thunderstorm, in both languages.

**Refresh rules**
- At launch, show the last saved weather immediately.
- When the app opens, fetch if the saved weather is older than 10 minutes.
- While the app is open, fetch every 10 minutes.
- Do not fetch in the background. Android does not allow background work more often than every 15 minutes, so the weather updates only while the app is open.
- If a fetch fails, keep the last value. Do not show an error on the greeting bar.
- Settings shows "Updated 4 min ago" based on the time of the last successful fetch.
- Open-Meteo's models refresh on their own schedule. The app shows when it fetched, not when the model ran.

**Weather location**
- Approximate location: request the coarse location permission only when the user taps the option. Round the coordinates to two decimal places (about 1 km) before saving or sending them. Store only the rounded coordinates.
- City search: a text field, then up to five results showing name, region, and country. The user picks one. Save its rounded coordinates and its label.
- Temperature unit: Celsius by default, Fahrenheit by default when the region is the United States. The user can change it in Settings.

## 8. Onboarding

Seven steps. Full screen, no sheets. A light background with the slow ambient field from the Dashboard.

**Shared rules**
- Seven progress segments at the top. The current segment is cobalt, the others slate.
- Back button (CaretLeft) on steps 2 to 7.
- Primary button at the bottom, above the navigation bar, 56dp tall, 16dp corners.
- One decision per screen.
- Save each answer as it is given. Set the onboarding-complete flag only at Finish. If the app closes midway, it reopens at step 1 with saved answers filled in.
- Nothing is requested before it is explained.
- Titles 32 sp, body 16 sp. Must work at 200% text size without clipping.
- Every control has a TalkBack label. Touch targets are at least 48dp.
- A light haptic tick on each selection, when Tactile haptics is on (default on).
- Transitions: horizontal slide, 320 ms, spring. With reduce-motion on, crossfade instead.

**Step 1, Welcome**
- Title: "Your money, on your phone."
- Body: "Track accounts, spending, and cash flow. Everything stays on this device."
- Three rows, each with a 40dp tinted tile and a Phosphor icon:
  - ShieldCheck: "No account to create."
  - DeviceMobile: "Your data stays on this phone."
  - CloudSlash: "Nothing is uploaded or synced."
- Button: "Get started".

**Step 2, Language**
- Title: "Choose your language".
- Two large cards: "English" and "Bahasa Indonesia". Each shows its name in its own language. The selected card has a cobalt border and a Check.
- Choosing a language switches the rest of onboarding immediately, with a 250 ms crossfade.
- Default: the device language if it is English or Indonesian, otherwise English.
- Button: "Continue".

**Step 3, Region and currency**
- Title: "Where are you from?"
- Body: "Your main currency will match your country. You can change it later in Settings."
- Search field: "Search country". Matches the country name in the current language.
- List: each row shows the country name, the currency name beneath it, and a currency badge with the code (for example IDR). The selected row has a cobalt border and a Check. No flags.
- Preview under the list: "Main currency: Indonesian Rupiah (IDR)", using Android's names.
- Default: the device's country if it has a currency. If not, nothing is selected and the button stays disabled.
- Button: "Continue".

**Step 4, Name**
- Title: "What should we call you?"
- Body: "We use this only for your greeting."
- Field placeholder: "Your name". Maximum 40 characters.
- Button: "Continue". Text button: "Skip".

**Step 5, Weather**
- Title: "Show local weather?"
- Body: "Weather uses your approximate location, rounded to about 1 km. Only that rounded location is sent to the weather service, Open-Meteo."
- Three option cards:
  - "Use my approximate location" with MapPin. Tapping it asks for permission. If granted, fetch the weather and continue. If denied, show: "Location is off. You can allow it later in Settings, or search for your city." and keep the other options visible.
  - "Search for my city". Opens a search field with results. Continue after a result is picked.
  - "Not now". Weather stays hidden until turned on in Settings. Continue immediately.
- Footer: "Weather data by Open-Meteo.com".

**Step 6, Layout**
- Title: "Where should the menu sit?"
- Body: "You can change this anytime. If you turn on shake, you can also move it by shaking the phone."
- Left / Right segmented control, with a small phone preview beside it. The preview shows the navigation rail on the chosen side and animates when the choice changes.
- Toggle row: "Shake to swap sides", subline "Optional. A firm shake moves the menu to the other side."
- Button: "Continue".

**Step 7, Start**
- Title: "How would you like to start?"
- Two cards:
  - "Start from scratch": "Begin empty. Add your accounts when you're ready."
  - "Load example data": "Sample accounts and transactions so you can explore. Remove them anytime in Settings."
- Default: Load example data. If the main currency is not IDR and no exchange rate is cached yet, this card shows "Needs an internet connection once" and is disabled.
- Button: "Finish".
- After Finish: a short confirmation (CheckCircle scales in over 600 ms), then the Dashboard. The greeting uses the name.

## 9. Main screens

**Dashboard** (opens first)
1. Label "Total Net Worth". Below it, the main currency symbol and the total. Under that, "N accounts · all in {CODE}". Only the number is masked. The symbol stays visible.
2. Safe-to-spend card (28dp, glass). Label "Safe-to-Spend Today". Large amount "{symbol} X" with "/ day". Status pill and subline. On the right, a 64dp ring showing percent spent and the word "spent". When no budget is set, the card shows "Set a monthly budget" and a button to Analytics.
3. "Active Accounts" header with "See All".
4. Horizontal carousel of account cards, 130dp wide, 22dp corners. Name with a colored dot, balance below. Right edge fades out. Tap opens Account detail.
5. "Recent Activity" header with "View transactions".
6. The five most recent transactions.
7. Budget card (16dp, subtle glass). "Monthly Budget Runway", "Budget {amount}" on the right, a progress bar in the status color, "Spent {amount}" on the left, "Days left: N days" on the right.
- **Empty state (no accounts):** the carousel becomes a card with Wallet, the text "Add your first account", and an "Add account" button.

**Ledger**
- Title "Activity Ledger". Subtitle "N transactions logged".
- Filter pills: All, Expense, Income, Transfer, each with a count. Selected pill is cobalt.
- Search field: "Search notes, categories, amounts...". Matches the title, the category, and the raw amount digits. Typing 28000 matches Artisan Coffee. Typing 28.000 does not.
- Day groups with uppercase headers: "Today", "Yesterday", or the weekday and date, for example "Tuesday, Oct 12".
- Empty filter: "No matching transactions." Empty ledger: Receipt, "No transactions yet", and a "Log transaction" button.

**Accounts**
- Title "Accounts". Subtitle "N accounts", plus " · reorder mode" while editing.
- Top right: "Edit" button (becomes a cobalt "Done" in edit mode) and a 32dp square "+" button that opens New account.
- World currency card (subtle glass). Label "Total in World Currencies". Refresh button on the right. The total in the selected currency, large. Beside it, "1 USD = {formatted main-currency value}" for the selected currency. Status line under the total: "Updated daily · Oct 14, 09:30", "Offline · last update Oct 13, 09:30", or "No rates yet".
- Chips: USD, SGD, MYR, JPY, EUR, SAR, skipping the main currency. Default selection: USD, or EUR when the main currency is USD. "+ World" button opens the World currencies sheet.
- Account cards (24dp glass): 4dp left border in the account color. Name in uppercase, account color. Percent pill tinted with the account color. Balance in large bold. Thin bar against the largest balance, minimum 2%. Chevron on the right. In edit mode the chevron becomes up and down buttons; the first up and the last down are disabled at 30% opacity. Tapping does nothing in edit mode.
- Empty state: Wallet, "No accounts yet", and an "Add account" button. The world currency card stays visible.

**Account detail**
- Greeting bar shows "Account" and the account name. The rail's clock zone becomes the back button, and the privacy button hides. Back restores the previous screen and its scroll position.
- Header row: account name in its color, 20 sp bold, and a "N% of total" pill.
- Balance card (24dp glass) with a 4dp left border in the account color. "Balance" label, amount in 30 sp bold.
- Two tiles: "Inflow" (in emerald) and "Outflow" (in charcoal). Inflow is income into the account plus transfers in. Outflow is expenses from the account plus transfers out.
- "Account Activity" label, then the transactions where this account is the source or destination. Empty: "No transactions recorded for this account."

**Analytics**
- Title "Analytics". Subtitle "Cash flow, spending burn, and runway telemetry".
- **Last 6 Months:** for each of the last six calendar months, total income and total expenses. Transfers are excluded. Bar height is the value divided by the largest value across the six months, times the bar area. Month labels are short names in the current language. A month with no data shows no bars, only a baseline dot. Legend: Inflow (emerald), Outflow (persimmon).
- **Monthly Spending Runway:** the budget amount on the right, in cobalt. A square "−" button, a slider, and a square "+" button. Each button moves the budget by one step. Range and step: the IDR values of 2.000.000 to 15.000.000 in steps of 500.000, converted to the main currency and rounded to a clean number. For IDR nothing changes. Without a cached rate, replace the slider with a keypad entry. Below: "Safe to spend: {amount} / day" or "Limit reached. Increase budget to recalibrate.", in the status color. Changing the budget updates the Dashboard.
- **Spending donut:** this month's expenses by category. Show the four largest categories and group the rest as "Other". Percentages of this month's expenses. The center shows "Spent" and the total spent (section 14). Legend beside it with names and percentages. With no expenses: a grey ring and "No spending this month".

**Settings**: section 11.

## 10. Sheets

All sheets slide up and close on the X button or on a tap on the scrim.

**Log transaction** (opened by the rail "+")
- Grab handle. Segmented control: Expense (persimmon when selected), Income (emerald), Transfer (cobalt). Default: Expense.
- Account chips in a horizontal row. Label "Account" for Expense and Income, "Source account" for Transfer. Each chip has a dot in its account color and the account name. Selected chip: filled with its account color. Default: the first account.
- Transfer only: a "To account" row of chips. The source account chip is disabled at 35% opacity. Default: the first other account.
- Note field (hidden for Transfer). Label "Note" with a category pill on the right: the category's Phosphor icon and name. Placeholder: "e.g. Coffee, Lunch, Salary". Category comes from keywords, case-insensitive, checked in this order:
  - Food & Dining: coffee, kopi, food, eat, makan, mie, warung, resto, lunch, dinner
  - Transportation: mrt, gojek, grab, uber, gas, fuel, bensin, tol, train, kereta, transit
  - Shopping: shop, shopee, tokopedia, amazon, cloth, baju
  - Salary: salary, gaji, bonus, freelance, dividend
  - Utilities: electric, power, pln, wifi, internet, water, air, bill, util
  - Other: when nothing matches, including when the sheet opens
- Amount display: the currency symbol in cobalt, then the amount in 36 sp extra-bold, centered. Starts at 0.
- Keypad: three columns. Keys 1 to 9, then the zero key, and a Backspace key in berry. The bottom-left key adds three zeros for whole-number currencies, two zeros for currencies with decimals.
- Decimal currencies fill from the right, like cash entry: entering 1, 2, 5 gives 1.25. Maximum 9 digits.
- Full-width "Save transaction" button (cobalt, uppercase).

**Save rules**
- Amount 0: toast "Enter an amount".
- No source account: toast "Select an account".
- Transfer with no destination, or the same account as the source: toast "Select a different destination account".
- Expense: source balance decreases. It counts toward spending for the current month. Title: the note, or the category name. Icon from the category. Color: the source account's color.
- Income: source balance increases. Does not count toward spending. Title: the note, or "Income" if empty. Icon ArrowDownLeft. Color emerald.
- Transfer: source decreases, destination increases. Does not count toward spending. Title "Source → Destination". Icon ArrowsLeftRight. Color cobalt. Category "Transfer".
- Category for Expense and Income is the detected category ("Other" if nothing matched).
- After saving: the new transaction goes to the top with the date today. The sheet closes, every screen refreshes, a splash plays from the Save button, and the toast "Transaction logged" appears.

**New account**
- Header "New Account" with an X.
- "Account Name" input, 28 characters maximum, placeholder "e.g. Checking Account, Cash Wallet".
- "Starting Balance ({symbol})" numeric input, placeholder "1000000" in IDR-style digits. Empty or invalid becomes 0. Negative becomes 0.
- "Accent Color": six 32dp circles. The selected one has a 2dp cobalt ring, is scaled to 110%, and shows a white Check. Default cobalt.
- "Create Account" button. Empty name: toast "Account name is required". On success the account is added at the end and the toast reads `Account "NAME" created`.

**Main currency** (opened from Settings)
- The same list as onboarding step 3: country, currency name, currency badge. Search field. Selected row marked.
- Choosing a different currency runs "Changing the main currency" (section 14).

**World currencies**
- Header "World Currencies". Status line: "Updated daily · time" with an emerald dot, or "Offline · last update time" with a saffron dot, or "No rates yet".
- Search field: "Search currency (USD, JPY, EUR, SGD, GBP...)". Matches the code or the name.
- Rows: a currency badge, the name from Android, the symbol, "1 CODE = {main-currency value}", and the total converted into that currency. The selected row is marked "Active" with a cobalt border. Others show "Select".
- Tapping a row selects it, shows the toast "Currency changed to CODE", and closes the sheet.
- Opening the sheet refreshes rates only if the cache is older than 12 hours.

## 11. Settings

Cards in this order:

1. **Language.** Segmented control: "English" and "Bahasa Indonesia". Applies immediately to the whole app.
2. **Main currency.** Row showing "Indonesian Rupiah (IDR)". Opens the Main currency sheet.
3. **Security & Access.** PIN Code (on), Biometrics (on), Screen privacy (off). The PIN and biometric switches save their state only. Screen privacy, when on, blocks screenshots and screen recording.
4. **Sensory & Feedback.** Sound effects (on), Tactile haptics (on), Daily reminder (on). The reminder saves its state only.
5. **Sidebar Placement.** Left / Right segmented control, then "Shake to swap sides" (off), subline "Optional, uses motion sensor".
6. **Weather.** Row showing the location choice: "Approximate location", the city label, or "Off". Opens a sheet with the three options and city search. Temperature unit: °C / °F segmented. Line: "Updated 4 min ago".
7. **Data & Portability.** "Export Excel" and "Smart Import": toast "Coming soon". "Clear all data" in red. Confirmation dialog: title "Delete all data?", body "Removes all accounts and transactions on this phone. This can't be undone.", buttons Cancel and "Delete everything". Clear all data keeps settings and language.
8. **About.** "Weather data by Open-Meteo.com" and "Rates by Exchange Rate API" (opens https://www.exchangerate-api.com). App version.

Every switch flips and shows "Enabled" or "Disabled", except shake, which is covered in section 15. Sound effects, when on, play the chime on toggles.

## 12. Side rail

- Navigation buttons with the icons in section 3.1. Active: cobalt fill, white icon, cobalt shadow. Inactive: slate-500 icon.
- "+" button at the bottom, opens Log transaction.
- Clock: five characters (HH:MM) on an arc across the top of the rail, updated every 30 seconds.
- Privacy button under the clock:
  - Balances visible: Lock icon. Tapping hides balances and shows a small popup beside the rail, bold "Local vault" and "Your data stays on this device." for 2.6 seconds.
  - Balances hidden: EyeSlash icon. Tapping reveals balances and shows the toast "Balances revealed".
  - Hidden state is not saved. The app starts with balances visible.
- On nested screens, the clock moves and the back button takes the top spot. The privacy button hides.

## 13. Shared components

- **Transaction row:** 36dp icon tile with a tinted background and a 16dp icon. Title bold 12 sp, one line. Category below in 10 sp. Right: amount in bold 12 sp. Income in emerald with "+". Expenses in charcoal with "−". Transfers in charcoal, no sign.
- **Toast:** charcoal pill, white Space Grotesk bold 12 sp, emerald dot, 1dp white border at 10%. Sits 6% from the top, slides in over 300 ms, hides after 2.2 seconds.
- **Toggle:** off track slate-300, on track cobalt. Knob slides about 22dp with a slight overshoot.
- **Segmented control:** slate-100 container, 4dp padding, 16dp corners. Selected segment filled with its color and white text.
- **Masked amount:** hidden shows seven "•" dots with 0.12em letter spacing. Masking applies to the number in the hero total (the symbol stays) and to every other amount, including the symbol in a currency value.
- **Progress segments** (onboarding): section 8.

## 14. Data, money, and rules

**Entities**
- Account: uuid, name, accent key, balance in minor units (can be negative), sort index, updatedAt.
- Transaction: uuid, title, category, type (expense, income, transfer), signed amount in minor units, date, icon key, accent key, source account uuid, destination account uuid, updatedAt.
- Monthly plan: budget in minor units, starting spent in minor units, the month key it applies to.
- Settings in DataStore: language, main currency code, region, weather location, temperature unit, PIN, biometrics, screen privacy, sound, haptics, reminder, shake, sidebar side, onboarding complete, selected world currency.
- Rates cache: base code, rates, time_last_update_utc, fetched at.
- Weather cache: rounded latitude, rounded longitude, label, temperature, weather code, is_day, fetched at.

**Money:** store whole minor units as Long. Convert to display only when formatting. Never use floating point for stored money.

**Spending and budget**
- Spent this month = starting spent (only if its month key is the current month) + the total of this month's expenses.
- Remaining = max(0, budget − spent).
- Days left = days in the current month minus the day of the month. When it is 0, show "Last day" instead of "0 days left".
- Per day = round(remaining ÷ max(1, days left)).
- Percent = spent ÷ budget.
- Budget 0: show "Set a monthly budget", not a status.
- Remaining 0: status "Recalibrate", color `#F87171`.
- Percent at or above 70%: "Near Limit", saffron.
- Otherwise: "Safe", emerald.
- Subline when remaining is above 0: "{days left} days left · {amount} remaining".
- Ring and percent label: min(100, round(percent × 100)) %.
- Month change: on the first run in a new month, clear the starting spent and update the month key. Budget is kept.

**Changing the main currency**
- If no rate for the current main currency is cached, block the change: "Connect to the internet once to change your currency."
- Otherwise show a confirmation: "Change main currency to {name}? Your accounts, transactions, and budget will be converted at the latest rate (updated {date}). Amounts may change by a few cents." Buttons: Cancel and Convert.
- Convert every account balance, transaction amount, budget, and starting spent. Round to the new currency's decimals. Keep dates. Save the new main currency code.

**Example data**
- Accounts: BCA Main (cobalt, 12.500.000 IDR), Mandiri (emerald, 4.200.000 IDR), GoPay (persimmon, 950.000 IDR), Bibit Vault (iris, 800.000 IDR).
- Transactions, newest first. Dates are relative to the day the examples are loaded:
  1. Artisan Coffee · Food & Dining · expense 28.000 · today · GoPay · Coffee
  2. Tech Corp Salary · Salary · income 4.500.000 · today · BCA Main · ArrowDownLeft
  3. City Subway Pass · Transportation · expense 14.000 · today · GoPay · Train
  4. Lunch & Dining · Food & Dining · expense 35.000 · today · GoPay · ForkKnife (use Coffee for all Food & Dining, so use Coffee)
  5. Portfolio Dividend · Investment · income 185.000 · yesterday · Bibit Vault · Sparkle
  6. Electricity Bill · Utilities · expense 250.000 · yesterday · Mandiri · Lightning
  7. Ride to Office · Transportation · expense 22.000 · yesterday · GoPay · Bicycle
  8. BCA Main → GoPay · Transfer · transfer 500.000 · two days ago · BCA Main to GoPay · ArrowsLeftRight
  9. Apartment Rent · Housing · expense 1.200.000 · two days ago · Mandiri · House
  10. Online Shopping · Shopping · expense 146.000 · two days ago · BCA Main · ShoppingBag
- Budget: 7.500.000 IDR. Starting spent: 3.250.000 IDR, for the month the examples were loaded.
- If the main currency is not IDR, convert all example amounts with the cached rate when the examples are loaded.

**Dates:** show "Today" and "Yesterday" for those days. Older dates show the weekday and date, for example "Tuesday, Oct 12", in the current language.

**Other rules**
- Account percent = balance ÷ total, rounded to a whole number.
- Account bar = balance ÷ largest balance, minimum 2%.
- Greeting, weather, and rates are not stored with the financial data. They are settings or caches.
- Clear all data removes accounts, transactions, and the monthly plan. It keeps settings.

## 15. Motion and behavior

- **Screen switch:** the new screen fades in from 0 to 100% and slides up 6dp over 350 ms, curve (0.16, 1, 0.3, 1). The old screen disappears at once.
- **Sheets:** slide up over 300 ms with the same curve. The scrim fades in.
- **Navigation:** Account detail is pushed; back pops it and restores scroll. Switching tabs clears the stack. The system back gesture does the same as the rail back button. An open sheet closes first. With nothing to go back to, back exits the app.
- **Rail drip:** on tab change, a blue blob leaves the old button, stretches along the rail, and lands on the new button over 420 ms. The old button loses its fill at the start, the new one gains it at the end. An approximation is allowed, but it must read as a drip.
- **Rail move** (Left / Right, from Settings, onboarding, or shake): the rail slides to the other edge on a spring. The leading edge moves faster and tilts up to 18°, so the pill stretches, then settles. Leading edge: stiffness 320, damping 23. Trailing edge: stiffness 120, damping 17. The content column and background move with it. A 10 ms haptic tick plays when the haptics setting is on.
- **Edge fades:** content fades out over the top 40dp as it scrolls under the greeting bar, fully gone 15dp past the edge. Cards entering from the bottom fade in between 38 and 98dp above the bottom zone line and scale from 96% to 100%. Carousel cards fade over the last 48dp at the right edge.
- **Scroll for more:** a floating pill (white at 88%, "Scroll for more" with a cobalt CaretDown) in the bottom zone. Shown only when content is below. Tapping scrolls down 65% of the visible height. The chevron bobs on a 2.4 s loop.
- **Balance hide:** each digit flips right to left, 32 ms apart. Each digit shrinks, fades, and blurs for 90 ms, then becomes a dot. Separators disappear. Width animates over 480 ms. Reveal runs left to right and separators fade back in. With reduce-motion on, switch instantly.
- **Clock arc:** when a nested screen opens, the five characters move from the top arc to the bottom arc, rotating 180°, with an overshoot ease over 900 ms each. Start delays 270, 180, 150, 90, and 0 ms. The back button scales in from 40% at the top. Reverse on leaving.
- **Save splash:** 14 small cobalt droplets burst from the Save button in random directions, pulled down by gravity, fading over about one second.
- **Ambient field:** one soft blurred blob per account, in its accent color, ordered by balance. Size = 28% + 42% × (balance ÷ largest)^0.8 of the screen width. Positions: (30%, 24%), (78%, 62%), (24%, 80%), (76%, 14%), (58%, 94%), (12%, 48%). Opacity 32%, blur about 56dp. Each blob slowly morphs and drifts on a 30 s loop. Shifts with 5% of rail movement. Colors cross-fade over 1 s when balances change. Use blurred radial gradients on a Canvas; full blur needs Android 12 or newer, so use soft radial gradients on older versions.
- **Chime** (when sound effects is on): two quiet sine notes falling from about 1140 Hz to 820 Hz, about 120 ms each. Plays on saving a transaction, changing transaction type, changing the budget with + or −, and toggling a setting switch. Silent when off. Generate the tone in code; do not ship a sound file from elsewhere.
- **Shake to swap sides** (when on): read the accelerometer. A change of 12 m/s² or more between samples is a peak. Peaks closer than 120 ms are ignored. Three peaks within 800 ms count as a shake, which moves the rail. Wait 1.2 s before the next. Toast "Shake sensor activated" or "Shake sensor disabled" on toggle. With no accelerometer: toast "Shake sensor not available on this device" and turn the switch back off.

## 16. Acceptance checks

**Example data, main currency IDR, on October 14, 2026**
- Total net worth: "Rp 18.450.000". Metadata: "4 accounts · all in IDR".
- Spent: "Rp 4.945.000" (3.250.000 starting plus 1.695.000 from this month's expenses).
- Safe-to-spend: "Rp 150.294 / day". Ring 66%. Status "Safe". Subline "17 days left · Rp 2.555.000 remaining".
- Budget card: "Budget Rp 7.500.000", "Spent Rp 4.945.000", "Days left: 17 days".
- Account percentages: BCA Main 68%, Mandiri 23%, GoPay 5%, Bibit Vault 4%.
- Accounts subtitle: "4 accounts".
- Log an expense of Rp 1.000.000 from BCA Main: Spent "Rp 5.945.000", per day "Rp 91.470", ring 79%, status "Near Limit".
- Transfer Rp 500.000 from BCA Main to GoPay: balances move, Spent does not change.

**Other checks**
- Set the main currency to USD with a cached rate: the total equals the IDR total divided by the USD rate, rounded to cents, shown as "$" in the US format, and the Accounts card shows the same value in the Indonesian format when the language is Indonesian and the region is Indonesia.
- Onboarding: choosing Bahasa Indonesia at step 2 translates every step. Choosing Indonesia at step 3 gives IDR and Rp formatting.
- Weather: with airplane mode on, the last saved weather still shows, and the greeting does not break.
- Network: only the three hosts in section 3 are contacted.
- Emoji scan: zero emoji characters in any string, resource file, or screen.
- Backup: the app's backup rules exclude all app data, and allowBackup is false.

## 17. Decisions (use these defaults; list them in your plan)

1. Onboarding step 7 loads example data by default. Starting from scratch is the other option.
2. Rates update once a day. The screen says "Updated daily" with the source's time.
3. Weather uses approximate location when allowed, or a searched city otherwise. Coordinates are rounded to about 1 km. No background updates.
4. Temperature is Celsius, except Fahrenheit for the United States.
5. Spending resets on the first day of each month. Days left comes from the calendar.
6. Number formatting follows the main currency's region. For IDR, decimals use a comma: 1.176,66.
7. Changing the main currency converts all amounts after confirmation. It is blocked when no rate is cached.
8. Negative balances show a minus sign.
9. Analytics uses real transactions. Example data will show different bars than the prototype.
10. The donut's center shows the total spent.
11. PIN, biometrics, and daily reminder save their state only. Screen privacy blocks screenshots.
12. Sound and haptics switches control those effects.
13. The privacy icons behave as in the prototype: Lock means balances are visible, and tapping it hides them.
14. The category is "Other" when no keyword matches.
15. Single transactions cannot be edited or deleted, because the prototype has no way to do either. Clear all data is the only bulk delete.
16. The launcher icon is a custom cobalt mark, not an emoji or a default icon.
17. Write the Indonesian strings in natural Indonesian, not word-for-word. Keep them in the Indonesian resource file and mark any string you are unsure about so I can review it.

## 18. Parts that will not match exactly, and known limits

- Frosted-glass blur is full only on Android 12 and newer. On older phones, use translucent white.
- The drip between tabs, the curved clock digits, and the ambient blobs are Canvas approximations.
- Weather is only as current as the source's models. The app shows when it last fetched.
- Rates update once a day. The free source does not offer real-time rates.
- The Open-Meteo and ExchangeRate-API free tiers are for open-source and non-commercial use and do not allow redistribution. If this project ever earns money, a paid plan or a different source is needed. Stop and tell me before any monetization.

## 19. Code quality and privacy checks

- Kotlin and Jetpack Compose. Use Material 3 only for base components, with your own theme so no default colors leak in.
- Single activity. ViewModels expose StateFlow. Room for accounts and transactions. DataStore for settings and caches.
- Dependencies allowed: Kotlin, AndroidX, Compose, Room, DataStore, kotlinx.serialization, OkHttp, the Phosphor library, and the two fonts. Nothing else without asking.
- Unit tests for: spending and budget rules, days left, per day, percent, currency conversion and rounding, the main currency change, category detection, transfer rules, weather icon mapping, and rate cache timing.
- Strings in resources for English (default) and Indonesian (values-in).
- Use dp and sp for all sizes.
- Before you say the build is done: run the network check, the emoji scan, and the backup check from section 16, and report the results.
