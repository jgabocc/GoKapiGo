package com.gokapigo.home

import androidx.compose.ui.text.font.FontFamily

/** Textos de la portada en los 15 idiomas del estudio (sin recursos, para que el paquete sea igual en todos los juegos). */
data class TextosHome(
    val jugar: String, val abrir: String, val instalar: String,
    /** «Volver a» + nombre; si lleva {x}, el nombre va en ese lugar (coreano, japonés, hindi…). Usar [volverA]. */
    val volver: String,
    val regalo: String,
    /** «Día» + número; si lleva {n}, el número va en ese lugar («{n}日目», «第{n}天»). Usar [diaN]. */
    val dia: String,
    val reclamado: String, val sinAnuncios: String, val otroJuego: String, val otraApp: String, val app: String,
    val idioma: String, val calificar: String, val compartir: String, val ajustes: String, val estrellas: String,
    val tambien: String, val gratis: String,
    /** Pista bajo el carrusel. */
    val desliza: String = "Swipe for more games",
) {
    fun volverA(nombre: String) = if ("{x}" in volver) volver.replace("{x}", nombre) else "$volver $nombre"
    fun diaN(n: Int) = if ("{n}" in dia) dia.replace("{n}", n.toString()) else "$dia $n"
}

object Textos {
    private val ES = TextosHome("JUGAR", "ABRIR", "INSTALAR GRATIS", "Volver a", "Regalo diario", "Día", "Reclamado", "Sin anuncios",
        "Otro juego de GoKapiGo", "App de GoKapiGo", "App", "Idioma", "Calificar", "Compartir", "Ajustes", "estrellas", "Juega también", "GRATIS",
        "Desliza para ver más juegos")
    private val EN = TextosHome("PLAY", "OPEN", "INSTALL FREE", "Back to", "Daily gift", "Day", "Claimed", "No ads",
        "Another GoKapiGo game", "GoKapiGo app", "App", "Language", "Rate", "Share", "Settings", "stars", "Also try", "FREE",
        "Swipe for more games")
    private val PT = TextosHome("JOGAR", "ABRIR", "INSTALAR GRÁTIS", "Voltar a", "Presente diário", "Dia", "Resgatado", "Sem anúncios",
        "Outro jogo da GoKapiGo", "App da GoKapiGo", "App", "Idioma", "Avaliar", "Compartilhar", "Ajustes", "estrelas", "Jogue também", "GRÁTIS",
        "Deslize para ver mais jogos")
    private val FR = TextosHome("JOUER", "OUVRIR", "INSTALLER", "Retour à", "Cadeau du jour", "Jour", "Récupéré", "Sans pub",
        "Un autre jeu GoKapiGo", "App GoKapiGo", "App", "Langue", "Noter", "Partager", "Réglages", "étoiles", "Essaie aussi", "GRATUIT",
        "Glisse pour voir d'autres jeux")
    private val DE = TextosHome("SPIELEN", "ÖFFNEN", "GRATIS LADEN", "Zurück zu", "Tagesgeschenk", "Tag", "Abgeholt", "Ohne Werbung",
        "Weiteres GoKapiGo-Spiel", "GoKapiGo-App", "App", "Sprache", "Bewerten", "Teilen", "Einstellungen", "Sterne", "Probier auch", "GRATIS",
        "Wische für mehr Spiele")
    private val PL = TextosHome("GRAJ", "OTWÓRZ", "ZAINSTALUJ", "Wróć do", "Dzienny prezent", "Dzień", "Odebrano", "Bez reklam",
        "Inna gra GoKapiGo", "Aplikacja GoKapiGo", "App", "Język", "Oceń", "Udostępnij", "Ustawienia", "gwiazdki", "Zagraj też", "ZA DARMO",
        "Przesuń, aby zobaczyć więcej gier")
    private val RU = TextosHome("ИГРАТЬ", "ОТКРЫТЬ", "УСТАНОВИТЬ", "Назад в", "Подарок дня", "День", "Получено", "Без рекламы",
        "Другая игра GoKapiGo", "Приложение GoKapiGo", "App", "Язык", "Оценить", "Поделиться", "Настройки", "звёзды", "Попробуй ещё", "БЕСПЛАТНО",
        "Листай, чтобы увидеть другие игры")
    // 8 idiomas nuevos (dueño 2026-11-12)
    private val TH = TextosHome("เล่น", "เปิด", "ติดตั้งฟรี", "กลับไปที่ {x}", "ของขวัญรายวัน", "วันที่ {n}", "รับแล้ว", "ไม่มีโฆษณา",
        "เกมอื่นจาก GoKapiGo", "แอปจาก GoKapiGo", "แอป", "ภาษา", "ให้คะแนน", "แชร์", "ตั้งค่า", "ดาว", "ลองเล่นด้วย", "ฟรี",
        "ปัดเพื่อดูเกมอื่นๆ")
    private val KO = TextosHome("플레이", "열기", "무료 설치", "{x}로 돌아가기", "일일 선물", "{n}일차", "받음", "광고 없음",
        "GoKapiGo의 다른 게임", "GoKapiGo 앱", "앱", "언어", "평가하기", "공유", "설정", "별", "추천 게임", "무료",
        "밀어서 더 많은 게임 보기")
    private val JA = TextosHome("プレイ", "開く", "無料インストール", "{x}に戻る", "デイリーギフト", "{n}日目", "受け取り済み", "広告なし",
        "GoKapiGoの他のゲーム", "GoKapiGoのアプリ", "アプリ", "言語", "評価", "シェア", "設定", "スター", "こちらもおすすめ", "無料",
        "スワイプして他のゲームを見る")
    private val ZH = TextosHome("开始游戏", "打开", "免费安装", "返回{x}", "每日礼物", "第{n}天", "已领取", "无广告",
        "GoKapiGo 的其他游戏", "GoKapiGo 应用", "应用", "语言", "评分", "分享", "设置", "星星", "也来试试", "免费",
        "滑动查看更多游戏")
    private val HI = TextosHome("खेलें", "खोलें", "मुफ़्त इंस्टॉल", "{x} पर वापस जाएँ", "रोज़ का तोहफ़ा", "दिन {n}", "मिल गया", "विज्ञापन-मुक्त",
        "GoKapiGo का एक और गेम", "GoKapiGo ऐप", "ऐप", "भाषा", "रेट करें", "शेयर करें", "सेटिंग्स", "सितारे", "यह भी खेलें", "मुफ़्त",
        "और गेम देखने के लिए स्वाइप करें")
    private val ID = TextosHome("MAIN", "BUKA", "PASANG GRATIS", "Kembali ke", "Hadiah harian", "Hari", "Sudah diambil", "Tanpa iklan",
        "Game GoKapiGo lainnya", "Aplikasi GoKapiGo", "App", "Bahasa", "Beri nilai", "Bagikan", "Pengaturan", "bintang", "Coba juga", "GRATIS",
        "Geser untuk melihat game lainnya")
    private val TR = TextosHome("OYNA", "AÇ", "ÜCRETSİZ YÜKLE", "Geri dön: {x}", "Günlük hediye", "{n}. gün", "Alındı", "Reklamsız",
        "Başka bir GoKapiGo oyunu", "GoKapiGo uygulaması", "App", "Dil", "Puan ver", "Paylaş", "Ayarlar", "yıldız", "Bunu da dene", "ÜCRETSİZ",
        "Daha fazla oyun için kaydır")
    private val VI = TextosHome("CHƠI", "MỞ", "CÀI MIỄN PHÍ", "Quay lại", "Quà hằng ngày", "Ngày", "Đã nhận", "Không quảng cáo",
        "Trò chơi khác của GoKapiGo", "Ứng dụng GoKapiGo", "App", "Ngôn ngữ", "Đánh giá", "Chia sẻ", "Cài đặt", "sao", "Chơi thử", "MIỄN PHÍ",
        "Vuốt để xem thêm trò chơi")

    /**
     * Código corto del idioma de la app: "es", "pt-BR" → "pt", "in"/"id" → "id" (indonesio), "zh-CN"/"zh-Hans" → "zh".
     * El chino tradicional (zh-TW, zh-HK, zh-MO, zh-Hant) no tiene recursos (values-zh-rCN es solo simplificado),
     * así que va en inglés igual que el resto del juego.
     */
    fun codigo(idioma: String): String {
        val tag = idioma.replace('_', '-').lowercase()
        val base = tag.substringBefore('-')
        if (base == "zh" && listOf("-hant", "-tw", "-hk", "-mo").any { it in tag }) return "en"
        return if (base == "in") "id" else base
    }

    /** Por idioma de la app ("es", "pt-BR", "zh-Hans-CN"…); si no está, inglés. */
    fun de(idioma: String): TextosHome = when (codigo(idioma)) {
        "es" -> ES; "pt" -> PT; "fr" -> FR; "de" -> DE; "pl" -> PL; "ru" -> RU
        "th" -> TH; "ko" -> KO; "ja" -> JA; "zh" -> ZH; "hi" -> HI; "id" -> ID; "tr" -> TR; "vi" -> VI
        else -> EN
    }

    /**
     * Fuente que se puede usar en ese idioma. Las fuentes de los juegos (Rubik, Nunito, Russo…) no traen las letras
     * del vietnamita (ạ, ơ, ư, ế…): Android las sacaría de otra fuente letra por letra y las palabras se verían mezcladas.
     * En vietnamita se usa la del sistema (Roboto, completa). Tailandés, hindi, chino, japonés y coreano no necesitan
     * nada: esas letras no existen en la fuente del juego y salen enteras de la del sistema (Noto).
     * [sinLetras]: idiomas cuyas letras le faltan a esa fuente (`idiomas.py fuentes` lo dice). Por defecto el vietnamita;
     * p. ej. Baloo 2 tampoco tiene ğ ş ı İ → `Textos.fuente(Baloo, idioma, setOf("vi", "tr"))`. Si la fuente sí las tiene
     * todas (Nunito completa), `emptySet()`.
     */
    fun fuente(f: FontFamily, idioma: String, sinLetras: Set<String> = setOf("vi")): FontFamily =
        if (codigo(idioma) in sinLetras) FontFamily.Default else f
}
