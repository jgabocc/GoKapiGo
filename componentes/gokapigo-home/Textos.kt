package com.gokapigo.home

/** Textos de la portada en los 7 idiomas del estudio (sin recursos, para que el paquete sea igual en todos los juegos). */
data class TextosHome(
    val jugar: String, val abrir: String, val instalar: String, val volver: String, val regalo: String, val dia: String,
    val reclamado: String, val sinAnuncios: String, val otroJuego: String, val otraApp: String, val app: String,
    val idioma: String, val calificar: String, val compartir: String, val ajustes: String, val estrellas: String,
    val tambien: String, val gratis: String,
    /** Pista bajo el carrusel. */
    val desliza: String = "Swipe for more games",
)

object Textos {
    private val ES = TextosHome("JUGAR", "ABRIR", "INSTALAR GRATIS", "Volver a", "Regalo diario", "Día", "Reclamado", "Sin anuncios",
        "Otro juego de GoKapiGo", "App de GoKapiGo", "App", "Idioma", "Calificar", "Compartir", "Ajustes", "estrellas", "Juega también", "GRATIS")
    private val EN = TextosHome("PLAY", "OPEN", "INSTALL FREE", "Back to", "Daily gift", "Day", "Claimed", "No ads",
        "Another GoKapiGo game", "GoKapiGo app", "App", "Language", "Rate", "Share", "Settings", "stars", "Also try", "FREE")
    private val PT = TextosHome("JOGAR", "ABRIR", "INSTALAR GRÁTIS", "Voltar a", "Presente diário", "Dia", "Resgatado", "Sem anúncios",
        "Outro jogo da GoKapiGo", "App da GoKapiGo", "App", "Idioma", "Avaliar", "Compartilhar", "Ajustes", "estrelas", "Jogue também", "GRÁTIS")
    private val FR = TextosHome("JOUER", "OUVRIR", "INSTALLER", "Retour à", "Cadeau du jour", "Jour", "Récupéré", "Sans pub",
        "Un autre jeu GoKapiGo", "App GoKapiGo", "App", "Langue", "Noter", "Partager", "Réglages", "étoiles", "Essaie aussi", "GRATUIT")
    private val DE = TextosHome("SPIELEN", "ÖFFNEN", "GRATIS LADEN", "Zurück zu", "Tagesgeschenk", "Tag", "Abgeholt", "Ohne Werbung",
        "Weiteres GoKapiGo-Spiel", "GoKapiGo-App", "App", "Sprache", "Bewerten", "Teilen", "Einstellungen", "Sterne", "Probier auch", "GRATIS")
    private val PL = TextosHome("GRAJ", "OTWÓRZ", "ZAINSTALUJ", "Wróć do", "Dzienny prezent", "Dzień", "Odebrano", "Bez reklam",
        "Inna gra GoKapiGo", "Aplikacja GoKapiGo", "App", "Język", "Oceń", "Udostępnij", "Ustawienia", "gwiazdki", "Zagraj też", "ZA DARMO")
    private val RU = TextosHome("ИГРАТЬ", "ОТКРЫТЬ", "УСТАНОВИТЬ", "Назад в", "Подарок дня", "День", "Получено", "Без рекламы",
        "Другая игра GoKapiGo", "Приложение GoKapiGo", "App", "Язык", "Оценить", "Поделиться", "Настройки", "звёзды", "Попробуй ещё", "БЕСПЛАТНО")

    /** Por idioma de la app ("es", "pt-BR"…); si no está, inglés. */
    fun de(idioma: String): TextosHome = when (idioma.take(2).lowercase()) {
        "es" -> ES; "pt" -> PT; "fr" -> FR; "de" -> DE; "pl" -> PL; "ru" -> RU
        else -> EN
    }.let { it.copy(desliza = DESLIZA[idioma.take(2).lowercase()] ?: it.desliza) }

    private val DESLIZA = mapOf(
        "es" to "Desliza para ver más juegos", "pt" to "Deslize para ver mais jogos", "fr" to "Glisse pour voir d'autres jeux",
        "de" to "Wische für mehr Spiele", "pl" to "Przesuń, aby zobaczyć więcej gier", "ru" to "Листай, чтобы увидеть другие игры",
    )
}
