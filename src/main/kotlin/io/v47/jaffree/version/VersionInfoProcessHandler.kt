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

import io.v47.jaffree.process.LinesProcessHandler

private val VERSION_REGEX = Regex("""(\d+)(?:\.(\d+))?(?:\.(\d+))?""")

@Suppress("MagicNumber")
internal class VersionInfoProcessHandler : LinesProcessHandler<VersionInfo>() {
    private var versionMatch: MatchResult? = null
    private val enabledFeatures = mutableSetOf<String>()
    private val disabledFeatures = mutableSetOf<String>()

    override fun onStderrLine(line: String) = onStdoutLine(line)

    override fun onStdoutLine(line: String) {
        if (versionMatch == null && "version" in line)
            versionMatch = VERSION_REGEX.find(line)
        else if (line.startsWith("configuration:")) {
            line
                .split(" ")
                .forEach { raw ->
                    if (raw.startsWith("--enable-"))
                        enabledFeatures += raw.substring(9)
                    else if (raw.startsWith("--disable-"))
                        disabledFeatures += raw.substring(10)
                }
        }
    }

    override fun getResult(exitCode: Int) =
        runCatching {
            createVersionInfo()
        }

    private fun createVersionInfo(): VersionInfo {
        val vm = versionMatch
        require(vm != null) { "No version number found." }

        return VersionInfo(
            vm.value,
            vm.groupValues[1].toInt(),
            vm.groupValues.getOrNull(2)?.toInt() ?: 0,
            vm.groupValues.getOrNull(3)?.toInt() ?: 0,
            enabledFeatures,
            disabledFeatures
        )
    }
}
