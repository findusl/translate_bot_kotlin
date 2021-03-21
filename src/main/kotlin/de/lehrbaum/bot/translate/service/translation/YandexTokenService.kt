package de.lehrbaum.bot.translate.service.translation

import de.lehrbaum.bot.translate.config.Secrets
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.*
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

class YandexTokenService(private val httpClient: HttpClient, private val secrets: Secrets) {

	private val key = loadPrivateKey()

	private fun loadPrivateKey(): PrivateKey {
		val privateKeyBytes = Base64.getDecoder().decode(secrets.yandex.privateKey)
		val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
		return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
	}

	suspend fun requestIAMToken(): String {
		val jwt = generateIAMTokenRequestJWT()
		val response: BearerTokenResponse = httpClient.post {
			url("https://iam.api.cloud.yandex.net/iam/v1/tokens")
			contentType(Application.Json)
			body = BearerTokenRequest(jwt)
		}
		println("Got iamToken ${response.iamToken}")
		return response.iamToken
	}

	private fun generateIAMTokenRequestJWT(): String {
		val header = Jwts.jwsHeader()
			.setKeyId(secrets.yandex.keyId) // id of the key

		val now = Date(System.currentTimeMillis())
		// yandex only allows tokens that are up to one hour valid
		val oneHourFromNow = Calendar.getInstance().run {
			time = now
			add(Calendar.HOUR, 1)
			time
		}

		return Jwts.builder()
			.setHeader(header)
			.setIssuer(secrets.yandex.serviceAccountId) // id of service account
			.setAudience("https://iam.api.cloud.yandex.net/iam/v1/tokens")
			.setIssuedAt(now)
			.setExpiration(oneHourFromNow)
			.signWith(key, SignatureAlgorithm.PS256)
			.compact()
	}
}

private data class BearerTokenRequest(val jwt: String)

private data class BearerTokenResponse(val iamToken: String, val expiresAt: String)
// TODO see if expiresAt can be autoparsed to date
