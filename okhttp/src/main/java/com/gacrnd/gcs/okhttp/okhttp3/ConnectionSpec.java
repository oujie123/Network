package com.gacrnd.gcs.okhttp.okhttp3;

import com.gacrnd.gcs.okhttp.okhttp3.internal.Util;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocket;

import static com.gacrnd.gcs.okhttp.okhttp3.internal.Util.nonEmptyIntersection;
import static okhttp3.internal.Util.concat;
import static okhttp3.internal.Util.indexOf;
import static okhttp3.internal.Util.intersect;

public final class ConnectionSpec {


    private static final CipherSuite[] APPROVED_CIPHER_SUITES = new CipherSuite[]{
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,

            // Note that the following cipher suites are all on HTTP/2's bad cipher suites list.
            // We'll
            // continue to include them until better suites are commonly available. For example,
            // none
            // of the better cipher suites listed above shipped with Android 4.4 or Java 7.
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
    };

    /**
     * A modern TLS connection with extensions like SNI and ALPN available.
     */
    public static final ConnectionSpec MODERN_TLS = new Builder(true)
            .cipherSuites(APPROVED_CIPHER_SUITES)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1,
                    TlsVersion.TLS_1_0)
            .supportsTlsExtensions(true)
            .build();

    /**
     * A backwards-compatible fallback connection for interop with obsolete servers.
     */
    public static final ConnectionSpec COMPATIBLE_TLS = new Builder(MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_0)
            .supportsTlsExtensions(true)
            .build();

    /**
     * Unencrypted, unauthenticated connections for {@code http:} URLs.
     */
    public static final ConnectionSpec CLEARTEXT = new Builder(false).build();

    final boolean tls;
    final boolean supportsTlsExtensions;
    final @Nullable
    String[] cipherSuites;
    final @Nullable
    String[] tlsVersions;

    ConnectionSpec(Builder builder) {
        this.tls = builder.tls;
        this.cipherSuites = builder.cipherSuites;
        this.tlsVersions = builder.tlsVersions;
        this.supportsTlsExtensions = builder.supportsTlsExtensions;
    }

    public boolean isTls() {
        return tls;
    }

    /**
     * Returns the cipher suites to use for a connection. Returns null if all of the SSL socket's
     * enabled cipher suites should be used.
     */
    public @Nullable
    List<CipherSuite> cipherSuites() {
        return cipherSuites != null ? CipherSuite.forJavaNames(cipherSuites) : null;
    }

    /**
     * Returns the TLS versions to use when negotiating a connection. Returns null if all of the SSL
     * socket's enabled TLS versions should be used.
     */
    public @Nullable
    List<TlsVersion> tlsVersions() {
        return tlsVersions != null ? TlsVersion.forJavaNames(tlsVersions) : null;
    }

    public boolean supportsTlsExtensions() {
        return supportsTlsExtensions;
    }

    /**
     * Applies this spec to {@code sslSocket}.
     */
    void apply(SSLSocket sslSocket, boolean isFallback) {
        ConnectionSpec specToApply = supportedSpec(sslSocket, isFallback);

        if (specToApply.tlsVersions != null) {
            sslSocket.setEnabledProtocols(specToApply.tlsVersions);
        }
        if (specToApply.cipherSuites != null) {
            sslSocket.setEnabledCipherSuites(specToApply.cipherSuites);
        }
    }

    /**
     * Returns a copy of this that omits cipher suites and TLS versions not enabled by {@code
     * sslSocket}.
     */
    private ConnectionSpec supportedSpec(SSLSocket sslSocket, boolean isFallback) {
        String[] cipherSuitesIntersection = cipherSuites != null
                ? intersect(CipherSuite.ORDER_BY_NAME, sslSocket.getEnabledCipherSuites(),
                cipherSuites)
                : sslSocket.getEnabledCipherSuites();
        String[] tlsVersionsIntersection = tlsVersions != null
                ? intersect(Util.NATURAL_ORDER, sslSocket.getEnabledProtocols(), tlsVersions)
                : sslSocket.getEnabledProtocols();

        // In accordance with https://tools.ietf.org/html/draft-ietf-tls-downgrade-scsv-00
        // the SCSV cipher is added to signal that a protocol fallback has taken place.
        String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
        int indexOfFallbackScsv = indexOf(
                CipherSuite.ORDER_BY_NAME, supportedCipherSuites, "TLS_FALLBACK_SCSV");
        if (isFallback && indexOfFallbackScsv != -1) {
            cipherSuitesIntersection = concat(
                    cipherSuitesIntersection, supportedCipherSuites[indexOfFallbackScsv]);
        }

        return new Builder(this)
                .cipherSuites(cipherSuitesIntersection)
                .tlsVersions(tlsVersionsIntersection)
                .build();
    }

    /**
     * Returns {@code true} if the socket, as currently configured, supports this connection spec
     * . In
     * order for a socket to be compatible the enabled cipher suites and protocols must intersect.
     *
     * <p>For cipher suites, at least one of the {@link #cipherSuites() required cipher suites} must
     * match the socket's enabled cipher suites. If there are no required cipher suites the socket
     * must have at least one cipher suite enabled.
     *
     * <p>For protocols, at least one of the {@link #tlsVersions() required protocols} must match
     * the
     * socket's enabled protocols.
     */
    public boolean isCompatible(SSLSocket socket) {
        if (!tls) {
            return false;
        }

        if (tlsVersions != null && !nonEmptyIntersection(
                Util.NATURAL_ORDER, tlsVersions, socket.getEnabledProtocols())) {
            return false;
        }

        if (cipherSuites != null && !nonEmptyIntersection(
                CipherSuite.ORDER_BY_NAME, cipherSuites, socket.getEnabledCipherSuites())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (!(other instanceof ConnectionSpec)) return false;
        if (other == this) return true;

        ConnectionSpec that = (ConnectionSpec) other;
        if (this.tls != that.tls) return false;

        if (tls) {
            if (!Arrays.equals(this.cipherSuites, that.cipherSuites)) return false;
            if (!Arrays.equals(this.tlsVersions, that.tlsVersions)) return false;
            if (this.supportsTlsExtensions != that.supportsTlsExtensions) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (tls) {
            result = 31 * result + Arrays.hashCode(cipherSuites);
            result = 31 * result + Arrays.hashCode(tlsVersions);
            result = 31 * result + (supportsTlsExtensions ? 0 : 1);
        }
        return result;
    }

    @Override
    public String toString() {
        if (!tls) {
            return "ConnectionSpec()";
        }

        String cipherSuitesString = cipherSuites != null ? cipherSuites().toString() : "[all " +
                "enabled]";
        String tlsVersionsString = tlsVersions != null ? tlsVersions().toString() : "[all enabled]";
        return "ConnectionSpec("
                + "cipherSuites=" + cipherSuitesString
                + ", tlsVersions=" + tlsVersionsString
                + ", supportsTlsExtensions=" + supportsTlsExtensions
                + ")";
    }

    public static final class Builder {
        boolean tls;
        @Nullable
        String[] cipherSuites;
        @Nullable
        String[] tlsVersions;
        boolean supportsTlsExtensions;

        Builder(boolean tls) {
            this.tls = tls;
        }

        public Builder(ConnectionSpec connectionSpec) {
            this.tls = connectionSpec.tls;
            this.cipherSuites = connectionSpec.cipherSuites;
            this.tlsVersions = connectionSpec.tlsVersions;
            this.supportsTlsExtensions = connectionSpec.supportsTlsExtensions;
        }

        public Builder allEnabledCipherSuites() {
            if (!tls) throw new IllegalStateException("no cipher suites for cleartext connections");
            this.cipherSuites = null;
            return this;
        }

        public Builder cipherSuites(CipherSuite... cipherSuites) {
            if (!tls) throw new IllegalStateException("no cipher suites for cleartext connections");

            String[] strings = new String[cipherSuites.length];
            for (int i = 0; i < cipherSuites.length; i++) {
                strings[i] = cipherSuites[i].javaName;
            }
            return cipherSuites(strings);
        }

        public Builder cipherSuites(String... cipherSuites) {
            if (!tls) throw new IllegalStateException("no cipher suites for cleartext connections");

            if (cipherSuites.length == 0) {
                throw new IllegalArgumentException("At least one cipher suite is required");
            }

            this.cipherSuites = cipherSuites.clone(); // Defensive copy.
            return this;
        }

        public Builder allEnabledTlsVersions() {
            if (!tls) throw new IllegalStateException("no TLS versions for cleartext connections");
            this.tlsVersions = null;
            return this;
        }

        public Builder tlsVersions(TlsVersion... tlsVersions) {
            if (!tls) throw new IllegalStateException("no TLS versions for cleartext connections");

            String[] strings = new String[tlsVersions.length];
            for (int i = 0; i < tlsVersions.length; i++) {
                strings[i] = tlsVersions[i].javaName;
            }

            return tlsVersions(strings);
        }

        public Builder tlsVersions(String... tlsVersions) {
            if (!tls) throw new IllegalStateException("no TLS versions for cleartext connections");

            if (tlsVersions.length == 0) {
                throw new IllegalArgumentException("At least one TLS version is required");
            }

            this.tlsVersions = tlsVersions.clone(); // Defensive copy.
            return this;
        }

        public Builder supportsTlsExtensions(boolean supportsTlsExtensions) {
            if (!tls)
                throw new IllegalStateException("no TLS extensions for cleartext connections");
            this.supportsTlsExtensions = supportsTlsExtensions;
            return this;
        }

        public ConnectionSpec build() {
            return new ConnectionSpec(this);
        }
    }
}