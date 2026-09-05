package com.example.localization

object EchoStrings {

    fun get(key: String, lang: Language): String = when (key.lowercase()) {
        "play" -> play(lang)
        "levels" -> levels(lang)
        "level" -> level(lang)
        "themes" -> themes(lang)
        "shop" -> shop(lang)
        "settings" -> settings(lang)
        "language" -> language(lang)
        "sound" -> sound(lang)
        "haptics" -> haptics(lang)
        "daily_challenge" -> dailyChallenge(lang)
        "hint" -> hint(lang)
        "breaker" -> breaker(lang)
        "clear" -> clearEchoes(lang)
        else -> key
    }

    fun level(lang: Language): String = when (lang) {
        Language.TR -> "Bölüm"
        Language.ES -> "Nivel"
        Language.DE -> "Level"
        Language.FR -> "Niveau"
        Language.IT -> "Livello"
        Language.PT -> "Nível"
        Language.RU -> "Уровень"
        Language.JA -> "レベル"
        Language.KO -> "레벨"
        Language.EN -> "Level"
    }

    fun play(lang: Language): String = when (lang) {
        Language.TR -> "OYNA"
        Language.ES -> "JUGAR"
        Language.DE -> "SPIELEN"
        Language.FR -> "JOUER"
        Language.IT -> "GIOCA"
        Language.PT -> "JOGAR"
        Language.RU -> "ИГРАТЬ"
        Language.JA -> "プレイ"
        Language.KO -> "플레이"
        Language.EN -> "PLAY"
    }

    fun levels(lang: Language): String = when (lang) {
        Language.TR -> "Bölümler"
        Language.ES -> "Niveles"
        Language.DE -> "Level"
        Language.FR -> "Niveaux"
        Language.IT -> "Livelli"
        Language.PT -> "Níveis"
        Language.RU -> "Уровни"
        Language.JA -> "レベル"
        Language.KO -> "레벨"
        Language.EN -> "Levels"
    }

    fun themes(lang: Language): String = when (lang) {
        Language.TR -> "Temalar"
        Language.ES -> "Temas"
        Language.DE -> "Designs"
        Language.FR -> "Thèmes"
        Language.IT -> "Temi"
        Language.PT -> "Temas"
        Language.RU -> "Темы"
        Language.JA -> "テーマ"
        Language.KO -> "테마"
        Language.EN -> "Themes"
    }

    fun shop(lang: Language): String = when (lang) {
        Language.TR -> "Mağaza"
        Language.ES -> "Tienda"
        Language.DE -> "Shop"
        Language.FR -> "Boutique"
        Language.IT -> "Negozio"
        Language.PT -> "Loja"
        Language.RU -> "Магазин"
        Language.JA -> "ショップ"
        Language.KO -> "상점"
        Language.EN -> "Shop"
    }

    fun settings(lang: Language): String = when (lang) {
        Language.TR -> "Ayarlar"
        Language.ES -> "Ajustes"
        Language.DE -> "Einstellungen"
        Language.FR -> "Paramètres"
        Language.IT -> "Impostazioni"
        Language.PT -> "Configurações"
        Language.RU -> "Настройки"
        Language.JA -> "設定"
        Language.KO -> "설정"
        Language.EN -> "Settings"
    }

    fun language(lang: Language): String = when (lang) {
        Language.TR -> "Dil"
        Language.ES -> "Idioma"
        Language.DE -> "Sprache"
        Language.FR -> "Langue"
        Language.IT -> "Lingua"
        Language.PT -> "Idioma"
        Language.RU -> "Язык"
        Language.JA -> "言語"
        Language.KO -> "언어"
        Language.EN -> "Language"
    }

    fun dailyChallenge(lang: Language): String = when (lang) {
        Language.TR -> "GÜNLÜK GÖREV"
        Language.ES -> "DESAFÍO DIARIO"
        Language.DE -> "TAGESAUFGABE"
        Language.FR -> "DÉFI DU JOUR"
        Language.IT -> "SFIDA DEL GIORNO"
        Language.PT -> "DESAFIO DIÁRIO"
        Language.RU -> "ДЕНЬ ЗАДАНИЕ"
        Language.JA -> "デイリーミッション"
        Language.KO -> "일일 미션"
        Language.EN -> "DAILY CHALLENGE"
    }

    fun dailyCompleted(lang: Language): String = when (lang) {
        Language.TR -> "Tamamlandı"
        Language.ES -> "Completado"
        Language.DE -> "Abgeschlossen"
        Language.FR -> "Terminé"
        Language.IT -> "Completato"
        Language.PT -> "Concluído"
        Language.RU -> "Выполнено"
        Language.JA -> "完了"
        Language.KO -> "완료됨"
        Language.EN -> "Completed"
    }

    fun levelTitle(lang: Language, number: Int): String = when (lang) {
        Language.TR -> "Bölüm $number"
        Language.ES -> "Nivel $number"
        Language.DE -> "Level $number"
        Language.FR -> "Niveau $number"
        Language.IT -> "Livello $number"
        Language.PT -> "Nível $number"
        Language.RU -> "Уровень $number"
        Language.JA -> "レベル $number"
        Language.KO -> "레벨 $number"
        Language.EN -> "Level $number"
    }

    fun hint(lang: Language): String = when (lang) {
        Language.TR -> "İpucu"
        Language.ES -> "Pista"
        Language.DE -> "Hinweis"
        Language.FR -> "Indice"
        Language.IT -> "Indizio"
        Language.PT -> "Dica"
        Language.RU -> "Подсказка"
        Language.JA -> "ヒント"
        Language.KO -> "힌트"
        Language.EN -> "Hint"
    }

    fun breaker(lang: Language): String = when (lang) {
        Language.TR -> "Kırıcı"
        Language.ES -> "Rompedor"
        Language.DE -> "Brecher"
        Language.FR -> "Briseur"
        Language.IT -> "Spezzatore"
        Language.PT -> "Quebrador"
        Language.RU -> "Разрушитель"
        Language.JA -> "ブレーカー"
        Language.KO -> "브레이커"
        Language.EN -> "Breaker"
    }

    fun clearEchoes(lang: Language): String = when (lang) {
        Language.TR -> "Temizle"
        Language.ES -> "Limpiar"
        Language.DE -> "Löschen"
        Language.FR -> "Effacer"
        Language.IT -> "Pulisci"
        Language.PT -> "Limpar"
        Language.RU -> "Очистить"
        Language.JA -> "クリア"
        Language.KO -> "초기화"
        Language.EN -> "Clear"
    }

    fun levelComplete(lang: Language): String = when (lang) {
        Language.TR -> "BÖLÜM TAMAMLANDI!"
        Language.ES -> "¡NIVEL COMPLETADO!"
        Language.DE -> "LEVEL GESCHAFFT!"
        Language.FR -> "NIVEAU TERMINÉ !"
        Language.IT -> "LIVELLO COMPLETATO!"
        Language.PT -> "NÍVEL CONCLUÍDO!"
        Language.RU -> "УРОВЕНЬ ПРОЙДЕН!"
        Language.JA -> "レベルクリア！"
        Language.KO -> "레벨 완료!"
        Language.EN -> "LEVEL COMPLETED!"
    }

    fun nextLevel(lang: Language): String = when (lang) {
        Language.TR -> "Sonraki Bölüm"
        Language.ES -> "Siguiente Nivel"
        Language.DE -> "Nächstes Level"
        Language.FR -> "Niveau Suivant"
        Language.IT -> "Livello Successivo"
        Language.PT -> "Próximo Nível"
        Language.RU -> "Следующий Уровень"
        Language.JA -> "次のレベル"
        Language.KO -> "다음 레벨"
        Language.EN -> "Next Level"
    }

    fun retry(lang: Language): String = when (lang) {
        Language.TR -> "Tekrar Dene"
        Language.ES -> "Reintentar"
        Language.DE -> "Erneut Versuchen"
        Language.FR -> "Réessayer"
        Language.IT -> "Riprova"
        Language.PT -> "Repetir"
        Language.RU -> "Повторить"
        Language.JA -> "リトライ"
        Language.KO -> "다시 시도"
        Language.EN -> "Retry"
    }

    fun watchAd(lang: Language): String = when (lang) {
        Language.TR -> "Reklam İzle"
        Language.ES -> "Ver Anuncio"
        Language.DE -> "Werbung Schauen"
        Language.FR -> "Regarder Pub"
        Language.IT -> "Guarda Pubblicità"
        Language.PT -> "Assistir Anúncio"
        Language.RU -> "Смотреть Рекламу"
        Language.JA -> "広告を見る"
        Language.KO -> "광고 시청"
        Language.EN -> "Watch Video"
    }

    fun adBlockWarningTitle(lang: Language): String = when (lang) {
        Language.TR -> "Reklam Engelleyici Tespit Edildi"
        Language.ES -> "Bloqueador de Anuncios Detectado"
        Language.DE -> "Werbeblocker Erkannt"
        Language.FR -> "Bloqueur de Pub Détecté"
        Language.IT -> "AdBlock Rilevato"
        Language.PT -> "Bloqueador de Anúncios Detectado"
        Language.RU -> "Обнаружен Блокировщик Рекламы"
        Language.JA -> "広告ブロッカーを検出しました"
        Language.KO -> "광고 차단기가 감지되었습니다"
        Language.EN -> "Ad Blocker Detected"
    }

    fun adBlockWarningSubtitle(lang: Language): String = when (lang) {
        Language.TR -> "Uygulama çalıştırılamıyor"
        Language.ES -> "La aplicación no puede iniciarse"
        Language.DE -> "App kann nicht gestartet werden"
        Language.FR -> "L'application ne peut pas démarrer"
        Language.IT -> "L'applicazione non può essere avviata"
        Language.PT -> "O aplicativo não pode ser iniciado"
        Language.RU -> "Приложение не может быть запущено"
        Language.JA -> "アプリを起動できません"
        Language.KO -> "앱을 실행할 수 없습니다"
        Language.EN -> "Application cannot run"
    }

    fun adBlockWarningBody(lang: Language): String = when (lang) {
        Language.TR -> "ECHO tamamen ücretsiz bir oyundur ve sunucu/geliştirme masrafları reklam gelirleri sayesinde karşılanmaktadır.\n\nOyuna devam edebilmek için lütfen cihazınızdaki reklam engelleyiciyi (Özel DNS, AdGuard, Blokada vb.) kapatın."
        Language.ES -> "ECHO es un juego completamente gratuito y sus costos se financian mediante publicidad.\n\nPara continuar jugando, desactive el bloqueador de anuncios (DNS Privado, AdGuard, Blokada, etc.)."
        Language.DE -> "ECHO ist ein kostenloses Spiel, das durch Werbung finanziert wird.\n\nBitte deaktivieren Sie Ihren Werbeblocker (Privates DNS, AdGuard, Blokada usw.), um fortzufahren."
        Language.FR -> "ECHO est un jeu totalement gratuit financé par la publicité.\n\nPour continuer à jouer, veuillez désactiver votre bloqueur de pub (DNS Privé, AdGuard, Blokada, etc.)."
        Language.IT -> "ECHO è un gioco gratuito supportato dalla pubblicità.\n\nPer continuare a giocare, disattiva il blocco annunci (DNS Privato, AdGuard, Blokada, ecc.)."
        Language.PT -> "ECHO é um jogo totalmente gratuito financiado por anúncios.\n\nPara continuar jogando, desative o bloqueador de anúncios (DNS Privado, AdGuard, Blokada, etc.)."
        Language.RU -> "ECHO — полностью бесплатная игра, финансируемая за счет рекламы.\n\nДля продолжения отключите блокировщик рекламы (Private DNS, AdGuard, Blokada и др.)."
        Language.JA -> "ECHOは広告によって運営されている完全無料のゲームです。\n\nプレイを続けるには、広告ブロッカー（プライベートDNS、AdGuard、Blokada等）を無効にしてください。"
        Language.KO -> "ECHO는 광고 수익으로 운영되는 완전 무료 게임입니다.\n\n게임을 계속하려면 광고 차단기(개인 DNS, AdGuard, Blokada 등)를 해제해 주세요."
        Language.EN -> "ECHO is a completely free indie game supported by advertisements.\n\nTo continue playing, please disable your ad blocker (Private DNS, AdGuard, Blokada, etc.)."
    }

    fun recheck(lang: Language): String = when (lang) {
        Language.TR -> "Tekrar Kontrol Et"
        Language.ES -> "Verificar de Nuevo"
        Language.DE -> "Erneut Prüfen"
        Language.FR -> "Vérifier à Nouveau"
        Language.IT -> "Ricontrolla"
        Language.PT -> "Verificar Novamente"
        Language.RU -> "Проверить Снова"
        Language.JA -> "再確認する"
        Language.KO -> "다시 확인"
        Language.EN -> "Re-Check"
    }

    fun exitGame(lang: Language): String = when (lang) {
        Language.TR -> "Oyundan Çık"
        Language.ES -> "Salir del Juego"
        Language.DE -> "Spiel Beenden"
        Language.FR -> "Quitter le Jeu"
        Language.IT -> "Esci dal Gioco"
        Language.PT -> "Sair do Jogo"
        Language.RU -> "Выйти из Игры"
        Language.JA -> "ゲームを終了"
        Language.KO -> "게임 종료"
        Language.EN -> "Exit Game"
    }

    fun sound(lang: Language): String = when (lang) {
        Language.TR -> "Ses Efektleri & Müzik"
        Language.ES -> "Sonido y Música"
        Language.DE -> "Sound & Musik"
        Language.FR -> "Son & Musique"
        Language.IT -> "Suoni & Musica"
        Language.PT -> "Sons & Música"
        Language.RU -> "Звуки и Музыка"
        Language.JA -> "サウンドとBGM"
        Language.KO -> "사운드 및 음악"
        Language.EN -> "Sound & Music"
    }

    fun haptics(lang: Language): String = when (lang) {
        Language.TR -> "Titreşim (Haptik)"
        Language.ES -> "Vibración Háptica"
        Language.DE -> "Vibrationsfeedback"
        Language.FR -> "Retour Haptique"
        Language.IT -> "Feedback Aptico"
        Language.PT -> "Vibração Háptica"
        Language.RU -> "Тактильный Отклик"
        Language.JA -> "触覚フィードバック"
        Language.KO -> "진동 피드백"
        Language.EN -> "Haptic Feedback"
    }

    fun resetProgress(lang: Language): String = when (lang) {
        Language.TR -> "Tüm İlerlemeyi Sıfırla"
        Language.ES -> "Restablecer Progreso"
        Language.DE -> "Fortschritt Zurücksetzen"
        Language.FR -> "Réinitialiser les Progrès"
        Language.IT -> "Azzera Progressi"
        Language.PT -> "Redefinir Progresso"
        Language.RU -> "Сбросить Весь Прогресс"
        Language.JA -> "進行状況をリセット"
        Language.KO -> "진행 상황 초기화"
        Language.EN -> "Reset All Progress"
    }
}
