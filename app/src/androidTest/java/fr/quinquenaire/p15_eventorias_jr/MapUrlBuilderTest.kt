package fr.quinquenaire.p15_eventorias_jr

import fr.quinquenaire.p15_eventorias_jr.presentation.util.MapUrlBuilder
import org.junit.Assert.assertTrue
import org.junit.Test

class MapUrlBuilderTest {

    @Test
    fun test_build_url_with_default_parameters() {
        //(Arrange)
        val lat = 45.7640
        val lng = 4.8357

        // (Act)
        val url = MapUrlBuilder.build(latitude = lat, longitude = lng)

        // 3. (Assert)
        // différents "morceaux" sont  présents dans la chaîne finale
        assertTrue(url.startsWith("https://maps.googleapis.com/maps/api/staticmap"))

        //  Uri.Builder encode les caractères spéciaux
        //  virgule -> %2C,  deux-points -> %3A et  barre verticale -> %7C
        assertTrue("L'URL doit contenir le centre", url.contains("center=45.764%2C4.8357"))
        assertTrue("L'URL doit contenir le zoom 15", url.contains("zoom=15"))
        assertTrue("L'URL doit contenir la taille 600x300", url.contains("size=600x300"))
        assertTrue("L'URL doit contenir le marqueur rouge", url.contains("markers=color%3Ared%7C45.764%2C4.8357"))
        assertTrue("L'URL doit contenir le paramètre key avec une valeur non vide",
            url.contains(Regex("[?&]key=.+")))
    }

    @Test
    fun test_build_url_with_custom_parameters() {
        // (Arrange)
        val lat = 45.7640
        val lng = 4.8357
        val customZoom = 10
        val customWidth = 800
        val customHeight = 400

        // (Act)
        val url = MapUrlBuilder.build(
            latitude = lat,
            longitude = lng,
            zoom = customZoom,
            widthPx = customWidth,
            heightPx = customHeight
        )

        // (Assert)
        assertTrue("Le zoom personnalisé doit être pris en compte", url.contains("zoom=10"))
        assertTrue("La taille personnalisée doit être prise en compte", url.contains("size=800x400"))
    }

    @Test
    fun test_build_url_with_negative_coordinates() {
        // (Arrange) — Sydney : latitude sud, longitude est...
        // prenons plutôt un cas avec les deux négatifs, ex. Santiago du Chili
        val lat = -33.4489
        val lng = -70.6693
        // (Act)
        val url = MapUrlBuilder.build(latitude = lat, longitude = lng)
        // (Assert) — le signe moins doit être préservé (encodé ou non)
        assertTrue("La latitude négative doit être préservée", url.contains("-33.4489"))
        assertTrue("La longitude négative doit être préservée", url.contains("-70.6693"))
    }
}