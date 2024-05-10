/**
 * Copyright (C) 2024 jaffree Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
