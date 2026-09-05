package com.example.localization

enum class Language(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flagEmoji: String
) {
    EN("en", "English", "English", "🇬🇧"),
    TR("tr", "Türkçe", "Turkish", "🇹🇷"),
    ES("es", "Español", "Spanish", "🇪🇸"),
    DE("de", "Deutsch", "German", "🇩🇪"),
    FR("fr", "Français", "French", "🇫🇷"),
    IT("it", "Italiano", "Italian", "🇮🇹"),
    PT("pt", "Português", "Portuguese", "🇵🇹"),
    RU("ru", "Русский", "Russian", "🇷🇺"),
    JA("ja", "日本語", "Japanese", "🇯🇵"),
    KO("ko", "한국어", "Korean", "🇰🇷");

    val displayName: String get() = nativeName
    val flag: String get() = flagEmoji

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
        }
    }
}
