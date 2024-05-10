package io.v47.jaffree.version

/**
 * Contains information about the running FFmpeg version including its enabled features.
 */
data class VersionInfo(
    val versionString: String,
    val versionMajor: Int,
    val versionMinor: Int,
    val versionPatch: Int,
    val enabledFeatures: Set<String>,
    val disabledFeatures: Set<String>,
)
