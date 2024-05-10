package io.v47.jaffree.version

import io.v47.jaffree.process.LinesProcessHandler

private val VERSION_REGEX = Regex("""(\d+)(?:\.(\d+))?(?:\.(\d+))?""")

@Suppress("MagicNumber")
internal class VersionInfoProcessHandler : LinesProcessHandler<VersionInfo>() {
    private var versionMatch: MatchResult? = null
    private val enabledFeatures = mutableSetOf<String>()
    private val disabledFeatures = mutableSetOf<String>()

    override fun onStderrLine(line: String) = Unit

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

    override fun onExit() {
        result = createVersionInfo()
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
