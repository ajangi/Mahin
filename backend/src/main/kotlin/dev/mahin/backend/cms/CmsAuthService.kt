package dev.mahin.backend.cms

import dev.mahin.backend.cms.persistence.CmsStaffEntity
import dev.mahin.backend.cms.persistence.CmsStaffRepository
import dev.mahin.backend.cms.persistence.CmsStaffRoleEntity
import dev.mahin.backend.content.AdminAuthTokenResponse
import dev.mahin.backend.content.AdminLoginRequest
import dev.mahin.backend.security.JwtService
import dev.mahin.backend.security.MahinAuthSubject
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CmsAuthService(
    private val staffRepository: CmsStaffRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    @Value("\${mahin.auth.access-token-ttl-seconds}") private val accessTtlSeconds: Long,
) {
    fun login(request: AdminLoginRequest): AdminAuthTokenResponse {
        val staff =
            staffRepository.findByEmailIgnoreCase(request.email.trim()).orElseThrow {
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials")
            }
        if (!staff.active || !passwordEncoder.matches(request.password, staff.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials")
        }
        val roles = staff.roles.map { it.role }.toSet()
        val token =
            jwtService.issueAccessToken(
                MahinAuthSubject.CmsStaff(
                    staffId = staff.id,
                    email = staff.email,
                    roles = roles,
                ),
            )
        return AdminAuthTokenResponse(
            accessToken = token,
            expiresInSeconds = accessTtlSeconds,
            roles = roles.sorted(),
        )
    }

    @Transactional
    fun createStaff(
        email: String,
        password: String,
        displayName: String,
        roles: Set<CmsRole>,
    ): CmsStaffEntity {
        if (staffRepository.findByEmailIgnoreCase(email).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "email_exists")
        }
        val now = Instant.now()
        val staff =
            CmsStaffEntity(
                id = UUID.randomUUID(),
                email = email.trim().lowercase(),
                passwordHash = passwordEncoder.encode(password),
                displayName = displayName,
                createdAt = now,
                updatedAt = now,
            )
        roles.forEach { role ->
            staff.roles.add(
                CmsStaffRoleEntity(
                    staffId = staff.id,
                    role = role.name,
                    staff = staff,
                ),
            )
        }
        return staffRepository.save(staff)
    }
}
