package com.example.data

import com.example.model.LevelData
import com.example.model.LevelNode

object LevelsRepository {

    val levels: List<LevelData> = listOf(
        // Level 1: Kare (Square Introduction)
        LevelData(
            levelId = 1,
            title = "Kare Başlangıcı",
            gridSize = 3,
            nodes = listOf(
                LevelNode(id = 0, x = 80f, y = 140f),
                LevelNode(id = 1, x = 280f, y = 140f),
                LevelNode(id = 2, x = 280f, y = 340f),
                LevelNode(id = 3, x = 80f, y = 340f)
            ),
            parEchoes = 0,
            hintOrder = listOf(0, 1, 2, 3),
            description = "Tüm düğümleri tek bir kesintisiz çizgiyle birleştirin."
        ),

        // Level 2: Ev Çatısı (Pentagon / House)
        LevelData(
            levelId = 2,
            title = "Ev Çatısı",
            gridSize = 3,
            nodes = listOf(
                LevelNode(id = 0, x = 180f, y = 100f), // roof peak
                LevelNode(id = 1, x = 290f, y = 190f), // right eave
                LevelNode(id = 2, x = 290f, y = 370f), // right bottom
                LevelNode(id = 3, x = 70f, y = 370f),  // left bottom
                LevelNode(id = 4, x = 70f, y = 190f)   // left eave
            ),
            parEchoes = 0,
            hintOrder = listOf(0, 1, 2, 3, 4),
            description = "Düğümleri sırayla bağlayarak yankı bırakmadan çatıyı tamamlayın."
        ),

        // Level 3: Kum Saati (Hourglass / Bowtie)
        LevelData(
            levelId = 3,
            title = "Kum Saati",
            gridSize = 3,
            nodes = listOf(
                LevelNode(id = 0, x = 80f, y = 120f),
                LevelNode(id = 1, x = 280f, y = 120f),
                LevelNode(id = 2, x = 180f, y = 240f), // center waist
                LevelNode(id = 3, x = 80f, y = 360f),
                LevelNode(id = 4, x = 280f, y = 360f)
            ),
            parEchoes = 1,
            hintOrder = listOf(0, 1, 2, 3, 4),
            description = "Merkez düğümden dikkatle geçin. Yankılar yolu kapatabilir!"
        ),

        // Level 4: Beşgen Yıldız (Star Nodes)
        LevelData(
            levelId = 4,
            title = "Yıldız Döngüsü",
            gridSize = 4,
            nodes = listOf(
                LevelNode(id = 0, x = 180f, y = 90f),
                LevelNode(id = 1, x = 300f, y = 180f),
                LevelNode(id = 2, x = 250f, y = 350f),
                LevelNode(id = 3, x = 110f, y = 350f),
                LevelNode(id = 4, x = 60f, y = 180f)
            ),
            parEchoes = 1,
            hintOrder = listOf(0, 2, 4, 1, 3),
            description = "Çapraz yıldız rotasını çizerek düğümleri birleştirin."
        ),

        // Level 5: Altıgen Ağ (Hexagon)
        LevelData(
            levelId = 5,
            title = "Altıgen Ağ",
            gridSize = 4,
            nodes = listOf(
                LevelNode(id = 0, x = 180f, y = 90f),
                LevelNode(id = 1, x = 290f, y = 160f),
                LevelNode(id = 2, x = 290f, y = 310f),
                LevelNode(id = 3, x = 180f, y = 380f),
                LevelNode(id = 4, x = 70f, y = 310f),
                LevelNode(id = 5, x = 70f, y = 160f)
            ),
            parEchoes = 1,
            hintOrder = listOf(0, 1, 2, 3, 4, 5),
            description = "Altıgen sınırları boyunca tek nefeste çizin."
        ),

        // Level 6: Zikzak Köprü (Zigzag Bridge)
        LevelData(
            levelId = 6,
            title = "Zikzak Köprü",
            gridSize = 4,
            nodes = listOf(
                LevelNode(id = 0, x = 70f, y = 120f),
                LevelNode(id = 1, x = 290f, y = 120f),
                LevelNode(id = 2, x = 110f, y = 230f),
                LevelNode(id = 3, x = 250f, y = 230f),
                LevelNode(id = 4, x = 70f, y = 350f),
                LevelNode(id = 5, x = 290f, y = 350f)
            ),
            parEchoes = 1,
            hintOrder = listOf(0, 1, 3, 2, 4, 5),
            description = "Yankı çizgileriyle kendi yolunuzu kilitlememeye özen gösterin."
        ),

        // Level 7: 3x3 Izgara (The 3x3 Matrix)
        LevelData(
            levelId = 7,
            title = "3x3 Izgara",
            gridSize = 3,
            nodes = listOf(
                LevelNode(id = 0, x = 70f, y = 120f),
                LevelNode(id = 1, x = 180f, y = 120f),
                LevelNode(id = 2, x = 290f, y = 120f),
                LevelNode(id = 3, x = 290f, y = 240f),
                LevelNode(id = 4, x = 180f, y = 240f),
                LevelNode(id = 5, x = 70f, y = 240f),
                LevelNode(id = 6, x = 70f, y = 360f),
                LevelNode(id = 7, x = 180f, y = 360f),
                LevelNode(id = 8, x = 290f, y = 360f)
            ),
            parEchoes = 2,
            hintOrder = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8),
            description = "Tüm 9 noktayı ziyaret eden kesintisiz bir S rotası çizin."
        ),

        // Level 8: Çift Halka (Concentric Squares)
        LevelData(
            levelId = 8,
            title = "Çift Halka",
            gridSize = 4,
            nodes = listOf(
                LevelNode(id = 0, x = 60f, y = 100f),
                LevelNode(id = 1, x = 300f, y = 100f),
                LevelNode(id = 2, x = 300f, y = 380f),
                LevelNode(id = 3, x = 60f, y = 380f),
                LevelNode(id = 4, x = 130f, y = 170f),
                LevelNode(id = 5, x = 230f, y = 170f),
                LevelNode(id = 6, x = 230f, y = 310f),
                LevelNode(id = 7, x = 130f, y = 310f)
            ),
            parEchoes = 2,
            hintOrder = listOf(0, 1, 2, 6, 5, 4, 7, 3),
            description = "Dış çemberden iç çembere geçişte yankı bariyerlerine çarpmayın."
        ),

        // Level 9: Kristal Elmas (Diamond Mesh)
        LevelData(
            levelId = 9,
            title = "Kristal Elmas",
            gridSize = 5,
            nodes = listOf(
                LevelNode(id = 0, x = 180f, y = 80f),  // top
                LevelNode(id = 1, x = 290f, y = 180f), // mid right
                LevelNode(id = 2, x = 180f, y = 280f), // center
                LevelNode(id = 3, x = 70f, y = 180f),  // mid left
                LevelNode(id = 4, x = 180f, y = 400f), // bottom
                LevelNode(id = 5, x = 290f, y = 330f), // bottom right
                LevelNode(id = 6, x = 70f, y = 330f)   // bottom left
            ),
            parEchoes = 2,
            hintOrder = listOf(0, 1, 2, 3, 6, 4, 5),
            description = "Kristal ekseninde zarif bir akış planlayın."
        ),

        // Level 10: Spiral Labirent (Inward Spiral)
        LevelData(
            levelId = 10,
            title = "Spiral Labirent",
            gridSize = 4,
            nodes = listOf(
                LevelNode(id = 0, x = 60f, y = 90f),
                LevelNode(id = 1, x = 300f, y = 90f),
                LevelNode(id = 2, x = 300f, y = 390f),
                LevelNode(id = 3, x = 120f, y = 390f),
                LevelNode(id = 4, x = 120f, y = 170f),
                LevelNode(id = 5, x = 240f, y = 170f),
                LevelNode(id = 6, x = 240f, y = 310f),
                LevelNode(id = 7, x = 180f, y = 250f)
            ),
            parEchoes = 2,
            hintOrder = listOf(0, 1, 2, 3, 4, 5, 6, 7),
            description = "İçeriye doğru kıvrılan spiralde en ufak bir hata tüm yolu kapatır."
        ),

        // Level 11: Kozmik Yıldız (10-Node Complex Web)
        LevelData(
            levelId = 11,
            title = "Kozmik Yıldız",
            gridSize = 5,
            nodes = listOf(
                LevelNode(id = 0, x = 180f, y = 80f),
                LevelNode(id = 1, x = 270f, y = 140f),
                LevelNode(id = 2, x = 300f, y = 240f),
                LevelNode(id = 3, x = 260f, y = 340f),
                LevelNode(id = 4, x = 180f, y = 390f),
                LevelNode(id = 5, x = 100f, y = 340f),
                LevelNode(id = 6, x = 60f, y = 240f),
                LevelNode(id = 7, x = 90f, y = 140f),
                LevelNode(id = 8, x = 180f, y = 200f),
                LevelNode(id = 9, x = 180f, y = 280f)
            ),
            parEchoes = 3,
            hintOrder = listOf(0, 1, 2, 3, 4, 9, 8, 7, 6, 5),
            description = "Çember ve iç çekirdek arasındaki hassas denge."
        ),

        // Level 12: Yankı Matrisi (Final Grandmaster Challenge)
        LevelData(
            levelId = 12,
            title = "Yankı Matrisi",
            gridSize = 4,
            nodes = listOf(
                LevelNode(id = 0, x = 70f, y = 90f),
                LevelNode(id = 1, x = 180f, y = 90f),
                LevelNode(id = 2, x = 290f, y = 90f),
                LevelNode(id = 3, x = 70f, y = 210f),
                LevelNode(id = 4, x = 180f, y = 210f),
                LevelNode(id = 5, x = 290f, y = 210f),
                LevelNode(id = 6, x = 70f, y = 330f),
                LevelNode(id = 7, x = 180f, y = 330f),
                LevelNode(id = 8, x = 290f, y = 330f),
                LevelNode(id = 9, x = 180f, y = 410f)
            ),
            parEchoes = 3,
            hintOrder = listOf(0, 1, 2, 5, 4, 3, 6, 7, 8, 9),
            description = "Usta seviye: Yankı engelleri arasında tek bir mükemmel çizgi var."
        )
    )

    fun getLevel(index: Int): LevelData {
        val safeIndex = index.coerceIn(0, levels.size - 1)
        return levels[safeIndex]
    }
}
