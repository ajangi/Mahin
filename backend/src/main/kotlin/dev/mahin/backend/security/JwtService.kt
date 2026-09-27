package dev.mahin.backend.security

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.Date
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JwtService(
    @Value("\${mahin.auth.jwt-secret}") private val jwtSecret: String,
    @Value("\${mahin.auth.access-token-ttl-seconds}") private val accessTtlSeconds: Long,
) {
    private val signer = MACSigner(jwtSecret.toByteArray(Charsets.UTF_8))
    private val verifier = MACVerifier(jwtSecret.toByteArray(Charsets.UTF_8))

    fun issueAccessToken(subject: MahinAuthSubject): String {
        val now = Instant.now()
        val claims =
            JWTClaimsSet
                .Builder()
                .subject(subjectTokenId(subject))
                .claim(CLAIM_TYPE, subjectType(subject))
                .claim(CLAIM_DEVICE, subject.deviceId.toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(accessTtlSeconds)))
                .build()
        val signed = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
        signed.sign(signer)
        return signed.serialize()
    }

    @Suppress("ReturnCount")
    fun parseAccessToken(token: String): MahinAuthSubject? {
        return try {
            val jwt = SignedJWT.parse(token)
            if (!jwt.verify(verifier)) {
                return null
            }
            val claims = jwt.jwtClaimsSet
            val expiration = claims.expirationTime?.toInstant() ?: return null
            if (expiration.isBefore(Instant.now())) {
                return null
            }
            val deviceId = UUID.fromString(claims.getStringClaim(CLAIM_DEVICE))
            when (claims.getStringClaim(CLAIM_TYPE)) {
                TYPE_USER ->
                    MahinAuthSubject.RegisteredUser(
                        userId = UUID.fromString(claims.subject),
                        deviceId = deviceId,
                    )
                TYPE_GUEST ->
                    MahinAuthSubject.GuestInstallation(
                        guestInstallationId = UUID.fromString(claims.subject),
                        deviceId = deviceId,
                    )
                else -> null
            }
        } catch (_: JOSEException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun subjectTokenId(subject: MahinAuthSubject): String =
        when (subject) {
            is MahinAuthSubject.RegisteredUser -> subject.userId.toString()
            is MahinAuthSubject.GuestInstallation -> subject.guestInstallationId.toString()
        }

    private fun subjectType(subject: MahinAuthSubject): String =
        when (subject) {
            is MahinAuthSubject.RegisteredUser -> TYPE_USER
            is MahinAuthSubject.GuestInstallation -> TYPE_GUEST
        }

    companion object {
        private const val CLAIM_TYPE = "mahin_typ"
        private const val CLAIM_DEVICE = "mahin_dev"
        private const val TYPE_USER = "user"
        private const val TYPE_GUEST = "guest"
    }
}
