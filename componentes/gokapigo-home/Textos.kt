package com.gokapigo.home

/** Textos de la portada en los 7 idiomas del estudio (sin recursos, para que el paquete sea igual en todos los juegos). */
data class TextosHome(
    val jugar: String, val abrir: String, val instalar: String, val volver: String, val regalo: String, val dia: String,
    val reclamado: String, val sinAnuncios: String, val otroJuego: String, val otraApp: String, val app: String,
    val idioma: String, val calificar: String, val compartir: String, val ajustes: String, val estrellas: String,
)

object Textos {
    private val ES = TextosHome("JUGAR", "ABRIR", "INSTALAR GRATIS", "Volver a", "Regalo diario", "Día", "Reclamado", "Sin anuncios",
        "Otro juego de GoKapiGo", "App de GoKapiGo", "App", "Idioma", "Calificar", "Compartir", "Ajustes", "estrellas")
    private val EN = TextosHome("PLAY", "OPEN", "INSTALL FREE", "Back to", "Daily gift", "Day", "Claimed", "No ads",
        "Another GoKapiGo game", "GoKapiGo app", "App", "Language", "Rate", "Share", "Settings", "stars")
    private val PT = TextosHome("JOGAR", "ABRIR", "INSTALAR GRÁTIS", "Voltar a", "Presente diário", "Dia", "Resgatado", "Sem anúncios",
        "Outro jogo da GoKapiGo", "App da GoKapiGo", "App", "Idioma", "Avaliar", "Compartilhar", "Ajustes", "estrelas")
    private val FR = TextosHome("JOUER", "OUVRIR", "INSTALLER", "Retour à", "Cadeau du jour", "Jour", "Récupéré", "Sans pub",
        "Un autre jeu GoKapiGo", "App GoKapiGo", "App", "Langue", "Noter", "Partager", "Réglages", "étoiles")
    private val DE = TextosHome("SPIELEN", "ÖFFNEN", "GRATIS LADEN", "Zurück zu", "Tagesgeschenk", "Tag", "Abgeholt", "Ohne Werbung",
        "Weiteres GoKapiGo-Spiel", "GoKapiGo-App", "App", "Sprache", "Bewerten", "Teilen", "Einstellungen", "Sterne")
    private val PL = TextosHome("GRAJ", "OTWÓRZ", "ZAINSTALUJ", "Wróć do", "Dzienny prezent", "Dzień", "Odebrano", "Bez reklam",
        "Inna gra GoKapiGo", "Aplikacja GoKapiGo", "App", "Język", "Oceń", "Udostępnij", "Ustawienia", "gwiazdki")
    private val RU = TextosHome("ИГРАТЬ", "ОТКРЫТЬ", "УСТАНОВИТЬ", "Назад в", "Подарок дня", "День", "Получено", "Без рекламы",
        "Другая игра GoKapiGo", "Приложение GoKapiGo", "App", "Язык", "Оценить", "Поделиться", "Настройки", "звёзды")

    /** Por idioma de la app ("es", "pt-BR"…); si no está, inglés. */
    fun de(idioma: String): TextosHome = when (idioma.take(2).lowercase()) {
        "es" -> ES; "pt" -> PT; "fr" -> FR; "de" -> DE; "pl" -> PL; "ru" -> RU
        else -> EN
    }
}
