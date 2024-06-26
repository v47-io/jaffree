package com.github.kokorin.jaffree.ffmpeg;

import com.github.kokorin.jaffree.Artifacts;
import com.github.kokorin.jaffree.Config;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FFmpegFilterTest {

    /**
     * Test, that creates mosaic video from 4 sources
     * <p>
     * Note, that this example lacks audio filter.
     *
     * @see <a href="https://trac.ffmpeg.org/wiki/Create%20a%20mosaic%20out%20of%20several%20input%20videos">mosaic</a>
     */
    @Test
    public void testMosaic() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("mosaic.mkv");

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4).setDuration(10, TimeUnit.SECONDS))
                .addInput(UrlInput.fromPath(Artifacts.SMALL_FLV).setDuration(10, TimeUnit.SECONDS))
                .addInput(UrlInput.fromPath(Artifacts.SMALL_MP4).setDuration(10, TimeUnit.SECONDS))
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MKV).setDuration(10, TimeUnit.SECONDS))

                .setComplexFilter(FilterGraph.of(
                        FilterChain.of(
                                Filter.withName("nullsrc")
                                        .addArgument("size", "640x480")
                                        .addOutputLink("base")
                        ),
                        FilterChain.of(
                                Filter.fromInputLink("0:v")
                                        .setName("setpts")
                                        .addArgument("PTS-STARTPTS"),
                                Filter.withName("scale")
                                        .addArgument("320x240")
                                        .addOutputLink("upperleft")
                        ),
                        FilterChain.of(
                                Filter.fromInputLink("1:v")
                                        .setName("setpts")
                                        .addArgument("PTS-STARTPTS"),
                                Filter.withName("scale")
                                        .addArgument("320x240")
                                        .addOutputLink("upperright")
                        ),
                        FilterChain.of(
                                Filter.fromInputLink("2:v")
                                        .setName("setpts")
                                        .addArgument("PTS-STARTPTS"),
                                Filter.withName("scale")
                                        .addArgument("320x240")
                                        .addOutputLink("lowerleft")
                        ),
                        FilterChain.of(
                                Filter.fromInputLink("3:v")
                                        .setName("setpts")
                                        .addArgument("PTS-STARTPTS"),
                                Filter.withName("scale")
                                        .addArgument("320x240")
                                        .addOutputLink("lowerright")
                        ),
                        FilterChain.of(
                                Filter.fromInputLink("base")
                                        .addInputLink("upperleft")
                                        .setName("overlay")
                                        .addArgument("shortest", "1")
                                        .addOutputLink("tmp1")
                        ),
                        FilterChain.of(
                                Filter.fromInputLink("tmp1")
                                        .addInputLink("upperright")
                                        .setName("overlay")
                                        //.addOption("shortest", "1")
                                        .addArgument("x", "320")
                                        .addOutputLink("tmp2")
                        ),
                        FilterChain.of(
                                Filter.fromInputLink("tmp2")
                                        .addInputLink("lowerleft")
                                        .setName("overlay")
                                        //.addOption("shortest", "1")
                                        .addArgument("y", "240")
                                        .addOutputLink("tmp3")
                        ),
                        FilterChain.of(
                                Filter.fromInputLink("tmp3")
                                        .addInputLink("lowerright")
                                        .setName("overlay")
                                        //.addOption("shortest", "1")
                                        .addArgument("x", "320")
                                        .addArgument("y", "240")
                        )
                ))

                .addOutput(UrlOutput.toPath(outputPath))
                .execute();

        assertNotNull(result);

        FFprobeResult probe = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(outputPath)
                .setShowStreams(true)
                .execute();

        assertNotNull(probe);

        int width = 0;
        int height = 0;

        for (Stream stream : probe.getStreams()) {
            if (stream.getWidth() != null) {
                width = Math.max(width, stream.getWidth());
            }
            if (stream.getHeight() != null) {
                height = Math.max(height, stream.getHeight());
            }
        }

        Assertions.assertEquals(640, width);
        Assertions.assertEquals(480, height);

    }


    /**
     * Concatenates 2 video with reencoding
     *
     * @see <a href="Concatenate">Concatenate</a>
     */
    @Test
    public void testConcatWithReencode() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("concat.mp4");

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(
                        UrlInput.fromPath(Artifacts.VIDEO_MP4)
                                .setDuration(5, TimeUnit.SECONDS)
                )
                .addInput(
                        UrlInput.fromPath(Artifacts.VIDEO_MKV)
                                .setPositionEof(-5, TimeUnit.SECONDS)
                )
                .setComplexFilter(FilterGraph.of(
                        FilterChain.of(
                                Filter.fromInputLink("0:v")
                                        .addInputLink("0:a")
                                        .addInputLink("1:v")
                                        .addInputLink("1:a")
                                        .setName("concat")
                                        .addArgument("n", "2")
                                        .addArgument("v", "1")
                                        .addOutputLink("v")
                                        .addArgument("a", "1")
                                        .addOutputLink("a")
                        )
                ))

                //On ubuntu ffmpeg uses AAC encoder in experimental mode, this option allows using AAC
                .addArguments("-strict", "-2")

                .addOutput(UrlOutput.toPath(outputPath)
                        .addMap("v")
                        .addMap("a")
                )
                .execute();

        assertNotNull(result);

        FFprobeResult probe = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(outputPath)
                .setShowStreams(true)
                .execute();
        assertNotNull(probe);

        double duration = 0.0;
        for (Stream stream : probe.getStreams()) {
            duration = Math.max(duration, stream.getDuration());
        }

        Assertions.assertEquals(10.0, duration, 0.1);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    @EnabledIf(value = "isDrawtextAvailable", disabledReason = "FFmpeg not compiled with required features")
    public void drawTextWithSpecialCharacters() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("draw_text.mp4");

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(
                        UrlInput.fromPath(Artifacts.VIDEO_MP4)
                                .setDuration(15, TimeUnit.SECONDS)
                )
                .setComplexFilter(
                        FilterGraph.of(
                                FilterChain.of(
                                        Filter.withName("drawtext")
                                                .addInputLink(StreamType.VIDEO)
                                                .addArgument("text",
                                                        "this is a 'string': may contain one, or more," +
                                                                " special characters like: [ or ] or = or even ;")
                                                .addArgument("box", "1")
                                                .addArgument("boxborderw", "5")
                                                .addArgument("boxcolor", "red")
                                                .addArgument("fontsize", "24")
                                ),
                                FilterChain.of(
                                        Filter.withName("afade")
                                                .addInputLink(StreamType.AUDIO)
                                                .addArgument("t", "in")
                                                .addArgument("ss", "0")
                                                .addArgument("d", "10")
                                )
                        )
                )
                .addOutput(UrlOutput.toPath(outputPath))
                .execute();


        assertNotNull(result);

        FFprobeResult probe = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(outputPath)
                .setShowStreams(true)
                .execute();
        assertNotNull(probe);

        double duration = 0.0;
        for (Stream stream : probe.getStreams()) {
            duration = Math.max(duration, stream.getDuration());
        }

        Assertions.assertEquals(15.0, duration, 0.1);
    }

    static boolean isDrawtextAvailable() {
        var features = FFmpeg.atPath(Config.FFMPEG_BIN).version().getEnabledFeatures();

        return features.contains("libfreetype") && features.contains("libharfbuzz");
    }
}
