package org.http4k.connect.openai.auth.oauth.impl

import org.http4k.connect.openai.auth.oauth.AccessTokenStore
import org.http4k.connect.openai.auth.oauth.AuthCodeStore
import org.http4k.connect.openai.auth.oauth.OAuthMachinery
import org.http4k.connect.openai.auth.oauth.SecureStrings
import org.http4k.security.oauth.server.AccessTokens
import org.http4k.security.oauth.server.AuthRequestTracking
import org.http4k.security.oauth.server.AuthorizationCodes
import org.http4k.security.oauth.server.refreshtoken.RefreshTokens
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Simple implementation of OAuthMachinery, backed by storage providers.
 */
fun <Principal : Any> StorageOAuthMachinery(
    storageProvider: StorageProvider,
    strings: SecureStrings,
    validity: Duration,
    cookieDomain: String,
    clock: Clock
): OAuthMachinery<Principal> {
    val tokenStorage = storageProvider<Instant>()
    val accessTokens = StorageAccessTokens(tokenStorage, strings, validity, clock)
    return object : OAuthMachinery<Principal>,
        AccessTokens by accessTokens,
        AuthorizationCodes by StorageAuthorizationCodes(storageProvider(), clock, strings, validity),
        AuthRequestTracking by StorageAuthRequestTracking(storageProvider(), cookieDomain, clock, strings, validity),
        RefreshTokens by StorageRefreshTokens(storageProvider(), accessTokens),
        AccessTokenStore<Principal> by StorageAccessTokenStore(storageProvider()),
        AuthCodeStore<Principal> by StorageAuthCodeStore(storageProvider()) {
    }
}
