package com.github.kokorin.jaffree.ffmpeg;

import com.github.kokorin.jaffree.Artifacts;
import com.github.kokorin.jaffree.Config;
import com.github.kokorin.jaffree.JaffreeException;
import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
import com.github.kokorin.jaffree.process.JaffreeAbnormalExitException;
import com.github.kokorin.jaffree.process.ProcessHelper;
import io.v47.jaffree.process.ProcessFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FFmpegTest {
    public static Path ERROR_MP4 = Paths.get("non_existent.mp4");

    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegTest.class);

    @Test
    public void testSimpleCopy() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .copyAllCodecs())
                .execute();

        Assertions.assertNotNull(result);
    }

    // For this test to pass ffmpeg must be added to Operation System PATH environment variable
    @Test
    public void testEnvPath() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpegResult result = FFmpeg.atPath()
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .copyAllCodecs())
                .execute();

        Assertions.assertNotNull(result);
    }

    @Test
    public void testOutputAdditionalOption() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("test.mp3");

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .setCodec(StreamType.AUDIO, "mp3")
                        .disableStream(StreamType.VIDEO)
                        .addArguments("-id3v2_version", "3")
                )
                .execute();

        Assertions.assertNotNull(result);

        FFprobeResult probe = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(outputPath)
                .setShowStreams(true)
                .execute();

        Assertions.assertNotNull(probe);
        assertEquals(1, probe.getStreams().size());
        assertEquals(StreamType.AUDIO, probe.getStreams().get(0).getCodecType());
    }

    @Test
    public void testProgress() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("test.mkv");

        final AtomicLong counter = new AtomicLong();

        ProgressListener listener = (progress, processAccess) -> counter.incrementAndGet();

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_FLV))
                .addOutput(UrlOutput.toPath(outputPath))
                .setProgressListener(listener)
                .execute();

        Assertions.assertNotNull(result);
        assertTrue(counter.get() > 0);

        outputPath = tempDir.resolve("test.flv");
        counter.set(0L);

        result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.SMALL_MP4))
                .addOutput(UrlOutput.toPath(outputPath))
                .setProgressListener(listener)
                .execute();

        Assertions.assertNotNull(result);
        assertTrue(counter.get() > 0);
    }

    @Test
    public void testProgressWithErrorLogLevel() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("test.mkv");

        final AtomicLong counter = new AtomicLong();

        ProgressListener listener = (progress, processAccess) -> counter.incrementAndGet();

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_FLV))
                .addOutput(UrlOutput.toPath(outputPath))
                .setLogLevel(LogLevel.ERROR)
                .setProgressListener(listener)
                .execute();

        Assertions.assertNotNull(result);
        assertTrue(counter.get() > 0);
    }

    @Test
    public void testDuration() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput
                        .fromPath(Artifacts.VIDEO_MP4)
                        .setDuration(10, TimeUnit.SECONDS)
                )
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .copyAllCodecs())
                .execute();

        Assertions.assertNotNull(result);

        double outputDuration = getDuration(outputPath);
        assertEquals(10.0, outputDuration, 0.1);

        result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput
                        .fromPath(Artifacts.VIDEO_MP4)
                        .setDuration(1. / 6., TimeUnit.MINUTES)
                )
                .setOverwriteOutput(true)
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .copyAllCodecs())
                .execute();

        Assertions.assertNotNull(result);

        outputDuration = getDuration(outputPath);
        assertEquals(10.0, outputDuration, 0.1);
    }

    @Test
    public void testForceStopWithThreadInterruption() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        final AtomicReference<FFmpegResult> result = new AtomicReference<>();
        final FFmpeg ffmpeg = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput
                        .fromPath(Artifacts.VIDEO_MP4)
                        .setReadAtFrameRate(true)
                )
                .addOutput(UrlOutput.toPath(outputPath));

        final AtomicReference<Exception> executeException = new AtomicReference<>();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    FFmpegResult r = ffmpeg.execute();
                    result.set(r);
                } catch (Exception e) {
                    executeException.set(e);
                }
            }
        };
        thread.start();

        Thread.sleep(5_000);
        thread.interrupt();

        Thread.sleep(1_000);
        assertNull(result.get());
        assertTrue(Files.exists(outputPath));
        assertEquals(JaffreeException.class, executeException.get().getClass());
        assertEquals("Failed to execute, was interrupted", executeException.get().getMessage());
    }

    @Test
    public void testForceAsyncStop() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpeg ffmpeg = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput
                        .fromPath(Artifacts.VIDEO_MP4)
                        .setReadAtFrameRate(true)
                )
                .addOutput(UrlOutput.toPath(outputPath));

        var futureResult = ffmpeg.executeAsync();

        Thread.sleep(5_000);

        futureResult.getProcessAccess().stopForcefully();

        Thread.sleep(1_000);

        assertTrue(Files.exists(outputPath));
    }

    @Test
    public void testGraceAsyncStop() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        final AtomicBoolean ffmpegStopped = new AtomicBoolean();
        final ProgressListener progressListener =
                (progress, processAccess) -> {
                    System.out.println(progress);
                    if (progress.getTime(TimeUnit.SECONDS) >= 15
                            && ffmpegStopped.compareAndSet(false, true)) {
                        processAccess.stopGracefully();
                    }
                };

        FFmpeg ffmpeg = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .setProgressListener(progressListener)
                .addOutput(UrlOutput.toPath(outputPath));

        var futureResult = ffmpeg.executeAsync();

        FFmpegResult encodingResult = futureResult.get(12, TimeUnit.SECONDS);
        Assertions.assertNotNull(encodingResult);

        FFprobeResult probeResult = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setInput(outputPath)
                .execute();

        assertEquals(2, probeResult.getStreams().size());


        final AtomicReference<Long> durationRef = new AtomicReference<>();
        final ProgressListener progressDurationListener =
                (progress, processAccess) -> {
                    System.out.println(progress);
                    durationRef.set(progress.getTime(TimeUnit.SECONDS));
                };

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(outputPath))
                .setProgressListener(progressDurationListener)
                .addOutput(new NullOutput())
                .execute();

        Assertions.assertNotNull(result);
        assertTrue(durationRef.get() >= 15);
    }

    @Test
    public void testOutputPosition() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .copyAllCodecs()
                        .setOutputPosition(15, TimeUnit.SECONDS)
                )
                .execute();

        Assertions.assertNotNull(result);

        double outputDuration = getDuration(outputPath);
        assertEquals(15.0, outputDuration, 0.1);
    }

    @Test
    public void testSizeLimit() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        final AtomicBoolean muxingErrorDetected = new AtomicBoolean(false);
        OutputListener outputListener = (message, processAccess) -> {
            if (message.endsWith("Error muxing a packet")) {
                LOGGER.warn("Detected a muxing error, which indicates ffmpeg bug #10327");
                muxingErrorDetected.set(true);
            }
        };

        try {
            FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                    .setOutputListener(outputListener)
                    .addOutput(UrlOutput
                            .toPath(outputPath)
                            .copyAllCodecs()
                            .setSizeLimit(1_000_000L)
                    )
                    .execute();
            Assertions.assertNotNull(result);
        } catch (JaffreeAbnormalExitException ex) {
            // Detect ffmpeg bug "Error muxing a packet when limit file size parameter is set"
            // https://trac.ffmpeg.org/ticket/10327
            Assumptions.assumeFalse(
                    muxingErrorDetected.get(),
                    "Hit ffmpeg bug #10327 - we will ignore this test"
            );

            Assertions.fail("Abnormal exit for limit file size");
        }

        long outputSize = Files.size(outputPath);
        assertTrue(outputSize > 900_000);
        assertTrue(outputSize < 1_100_000);
    }

    @Test
    public void testPosition() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput
                        .fromPath(Artifacts.VIDEO_MP4)
                        .setPosition(10, TimeUnit.SECONDS)
                )
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .copyAllCodecs())
                .execute();

        Assertions.assertNotNull(result);

        double inputDuration = getDuration(Artifacts.VIDEO_MP4);
        double outputDuration = getDuration(outputPath);

        assertEquals(inputDuration - 10, outputDuration, 0.5);
    }

    @Test
    public void testPositionNegative() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput
                        .fromPath(Artifacts.VIDEO_MP4)
                        .setPositionEof(-7, TimeUnit.SECONDS)
                )
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .copyAllCodecs())
                .execute();

        Assertions.assertNotNull(result);

        double outputDuration = getDuration(outputPath);

        assertEquals(7.0, outputDuration, 0.5);
    }

    @Test
    public void testNullOutput() {
        final AtomicLong time = new AtomicLong();

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput
                        .fromPath(Artifacts.VIDEO_MP4)
                )
                .addOutput(
                        new NullOutput()
                )
                .setOverwriteOutput(true)
                .setProgressListener(
                        (progress, processAccess) -> time.set(progress.getTimeMillis())
                )
                .execute();

        Assertions.assertNotNull(result);
        assertTrue(time.get() > 165_000);
    }

    @Test
    public void testMap() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .addOutput(UrlOutput
                        .toPath(outputPath)
                        .copyAllCodecs()
                        .addMap(0, StreamType.AUDIO)
                        .addMap(0, StreamType.AUDIO)
                        .addMap(0, StreamType.VIDEO)
                )
                .execute();

        Assertions.assertNotNull(result);

        FFprobeResult probe = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setInput(outputPath)
                .execute();

        List<Stream> streamTypes = probe.getStreams();

        assertEquals(3, streamTypes.size());
        assertEquals(StreamType.AUDIO, streamTypes.get(0).getCodecType());
        assertEquals(StreamType.AUDIO, streamTypes.get(1).getCodecType());
        assertEquals(StreamType.VIDEO, streamTypes.get(2).getCodecType());
    }

    @Test
    public void testExceptionIsThrownIfFfmpegExitsWithError() {
        var ex = assertThrowsExactly(JaffreeAbnormalExitException.class, () -> {
            FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(UrlInput.fromPath(ERROR_MP4))
                    .addOutput(new NullOutput())
                    .execute();
        });

        assertFalse(ex.getProcessErrorLogMessages().isEmpty());
    }

    @Test
    public void testCustomOutputParsing() {
        // StringBuffer - because it's thread safe
        final AtomicReference<String> loudnormReport = new AtomicReference<>();

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .addArguments("-af", "loudnorm=I=-16:TP=-1.5:LRA=11:print_format=json")
                .addOutput(new NullOutput(false))
                .setOutputListener((message, processAccess) -> {
                    if (message.contains("loudnorm")) {
                        loudnormReport.set(message);
                    }
                })
                .execute();

        Assertions.assertNotNull(result);

        var loudnormReportStr = loudnormReport.get();
        Assertions.assertTrue(loudnormReportStr.contains("input_i"));
        Assertions.assertTrue(loudnormReportStr.contains("input_tp"));
        Assertions.assertTrue(loudnormReportStr.contains("input_lra"));
        Assertions.assertTrue(loudnormReportStr.contains("input_thresh"));
        Assertions.assertTrue(loudnormReportStr.contains("output_i"));
        Assertions.assertTrue(loudnormReportStr.contains("output_tp"));
        Assertions.assertTrue(loudnormReportStr.contains("output_lra"));
        Assertions.assertTrue(loudnormReportStr.contains("output_thresh"));
        Assertions.assertTrue(loudnormReportStr.contains("normalization_type"));
        Assertions.assertTrue(loudnormReportStr.contains("target_offset"));
    }

    @Test
    public void testCustomOutputParsing2() {
        // StringBuffer - because it's thread safe
        final StringBuffer idetReport = new StringBuffer();

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .setFilter(StreamType.VIDEO, "idet")
                .addOutput(
                        new NullOutput(false)
                                .setFrameCount(StreamType.VIDEO, 100L)
                )
                .setOutputListener((line, processAccess) -> {
                    if (line.startsWith("[Parsed_idet")) {
                        idetReport.append(line);
                    }
                })
                .execute();

        Assertions.assertNotNull(result);

        var idetReportString = idetReport.toString();
        Assertions.assertTrue(idetReportString.contains("Repeated Fields"));
        Assertions.assertTrue(idetReportString.contains("Single frame detection"));
        Assertions.assertTrue(idetReportString.contains("Multi frame detection"));
    }

    @Test
    public void testPipeInput() throws IOException {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_FLV.getFileName());

        FFmpegResult result;

        try (InputStream inputStream = Files.newInputStream(Artifacts.VIDEO_FLV)) {
            result = FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(PipeInput.pumpFrom(inputStream))
                    .addOutput(UrlOutput.toPath(outputPath))
                    .execute();
        }

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getVideoSize());

        double expectedDuration = getExactDuration(Artifacts.VIDEO_FLV);
        double actualDuration = getExactDuration(outputPath);
        assertEquals(expectedDuration, actualDuration, 1.);
    }

    @Test
    public void testPipeInputPartialRead() throws IOException {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_FLV.getFileName());

        FFmpegResult result;

        try (InputStream inputStream = Files.newInputStream(Artifacts.VIDEO_FLV)) {
            result = FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(
                            PipeInput
                                    .pumpFrom(inputStream)
                                    .setDuration(15, TimeUnit.SECONDS)
                    )
                    .setLogLevel(LogLevel.VERBOSE)
                    .addOutput(UrlOutput.toPath(outputPath))
                    .execute();
        }

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getVideoSize());

        double actualDuration = getExactDuration(outputPath);
        assertEquals(15., actualDuration, 1.);
    }

    @Test
    public void testPipeOutput() throws IOException {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        FFmpegResult result;
        try (OutputStream outputStream = Files.newOutputStream(outputPath, CREATE)) {
            result = FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                    .addOutput(PipeOutput.pumpTo(outputStream).setFormat("flv"))
                    .setOverwriteOutput(true)
                    .execute();
        }

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getVideoSize());

        assertTrue(getExactDuration(outputPath) > 10.);
    }

    @Test
    public void testChannelInput() throws IOException {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("channel.mp4");

        try (SeekableByteChannel channel = Files.newByteChannel(Artifacts.VIDEO_MP4, READ)) {
            FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(
                            new ChannelInput("testChannelInput.mp4", channel)
                    )
                    .addOutput(
                            UrlOutput.toPath(outputPath)
                    )
                    .setLogLevel(LogLevel.DEBUG)
                    .execute();

            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.getVideoSize());
        }

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 1000);
    }

    @Test
    public void testChannelInputPartialRead() throws IOException {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("channel.mp4");

        try (SeekableByteChannel channel = Files.newByteChannel(Artifacts.VIDEO_MP4, READ)) {
            FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(
                            new ChannelInput("testChannelInputPartialRead.mp4", channel)
                                    .setDuration(10, TimeUnit.SECONDS)
                    )
                    .addOutput(
                            UrlOutput.toPath(outputPath)
                    )
                    .setLogLevel(LogLevel.INFO)
                    .execute();

            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.getVideoSize());
        }

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 1000);
    }

    @Test
    public void testChannelInputSeek() throws IOException {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("frame.jpg");

        try (SeekableByteChannel channel = Files.newByteChannel(Artifacts.VIDEO_MP4, READ)) {
            FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(
                            new ChannelInput("testChannelInputSeek.mp4", channel)
                                    .setPosition(1, TimeUnit.MINUTES)
                    )
                    .addOutput(
                            UrlOutput.toPath(outputPath)
                                    .setFrameCount(StreamType.VIDEO, 1L)
                    )
                    .setLogLevel(LogLevel.INFO)
                    .execute();

            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.getVideoSize());
        }

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 1000);
    }

    @Test
    public void testChannelWithNonSeekableInput() throws IOException {
        Path inputTs = Artifacts.VIDEO_TS;
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPng = tempDir.resolve("output1.png");
        try (
                SeekableByteChannel inputChannel = Files.newByteChannel(inputTs,
                        StandardOpenOption.READ)
        ) {
            FFmpeg.atPath()
                    .addInput(ChannelInput.fromChannel(inputChannel).setPosition(2000L))
                    .setOverwriteOutput(true)
                    .addOutput(
                            UrlOutput.toPath(outputPng)
                                    .setFormat("image2")
                                    .setFrameCount(StreamType.VIDEO, 1L)
                                    .addArguments("-q:v", "1")
                                    .disableStream(StreamType.AUDIO)
                                    .disableStream(StreamType.SUBTITLE)
                    )
                    .execute();
        }

        assertTrue(Files.size(outputPng) > 1000);
    }

    @Test
    public void testChannelOutput() throws IOException {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve("channel.mp4");

        LOGGER.debug("Will write to " + outputPath);

        try (SeekableByteChannel channel = Files.newByteChannel(outputPath, CREATE, WRITE, READ,
                TRUNCATE_EXISTING)) {
            FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                    .addInput(
                            UrlInput.fromPath(Artifacts.VIDEO_MP4)
                    )
                    .addOutput(
                            new ChannelOutput("channel.mp4", channel)
                    )
                    .setOverwriteOutput(true)
                    .setLogLevel(LogLevel.INFO)
                    .execute();

            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.getVideoSize());
        }

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 1000);
    }

    @Test
    public void testStreamFilters() throws IOException {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        LOGGER.debug("Will write to " + outputPath);

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .setFilter(StreamType.VIDEO, "crop=64:48:32:32")
                .setFilter(StreamType.AUDIO, "aecho=0.8:0.88:6:0.4")
                .addOutput(UrlOutput
                        .toPath(outputPath)
                )
                .execute();

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getVideoSize());
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 1000);
    }

    private static double getDuration(Path path) {
        FFprobeResult probe = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setInput(path)
                .execute();

        double result = 0.0;
        for (Stream stream : probe.getStreams()) {
            result = Math.max(result, stream.getDuration());
        }

        return result;
    }

    private static double getExactDuration(Path path) {
        final AtomicReference<FFmpegProgress> progressRef = new AtomicReference<>();

        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(path))
                .addOutput(new NullOutput())
                .setProgressListener((progress, processAccess) -> progressRef.set(progress))
                .execute();

        return progressRef.get().getTime(TimeUnit.SECONDS);
    }

    @Test
    @Disabled("This test requires a non-headless environment to work")
    public void testDesktopCapture() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path output = tempDir.resolve("desktop.mp4");
        LOGGER.debug("Will write to " + output);

        //Rectangle area = new Rectangle(80, 60, 160, 120);
        FFmpegResult result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(CaptureInput
                        .captureDesktop()
                        //.setArea(area)
                        .setFrameRate(10)
                )
                .addOutput(UrlOutput
                        .toPath(output)
                        .setDuration(10, TimeUnit.SECONDS)
                )
                .setOverwriteOutput(true)
                .execute();

        Assertions.assertNotNull(result);


        FFprobeResult probe = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setInput(output)
                .execute();

        List<Stream> streamTypes = probe.getStreams();

        assertEquals(1, streamTypes.size());

        final Stream stream0 = streamTypes.get(0);
        assertEquals(StreamType.VIDEO, stream0.getCodecType());
        assertEquals(10.0, stream0.getDuration(), 0.1);
        assertEquals(160L, (long) stream0.getWidth());
        assertEquals(120L, (long) stream0.getHeight());
    }

    @Test
    public void testHelperIsClosedAfterExecution() {
        final AtomicBoolean inputHelperClosed = new AtomicBoolean(false);
        final AtomicBoolean outputHelperClosed = new AtomicBoolean(false);

        class NotifyCloseHelper implements ProcessHelper {
            private final AtomicBoolean helperClosed;

            public NotifyCloseHelper(AtomicBoolean helperClosed) {
                this.helperClosed = helperClosed;
            }

            @Override
            public void close() throws IOException {
                helperClosed.set(true);
            }

            @Override
            public void run() {
            }
        }

        FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(
                        new UrlInput(Artifacts.VIDEO_MP4.toString()) {
                            @Override
                            public ProcessHelper helperThread() {
                                return new NotifyCloseHelper(inputHelperClosed);
                            }
                        }
                )
                .addOutput(
                        new NullOutput() {
                            @Override
                            public ProcessHelper helperThread() {
                                return new NotifyCloseHelper(outputHelperClosed);
                            }
                        }
                )
                .execute();

        assertTrue(inputHelperClosed.get());
        assertTrue(outputHelperClosed.get());
    }

    @Test
    public void testAsyncToCompletableFuture() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        final AtomicReference<FFmpegResult> futureRef = new AtomicReference<>();

        FFmpeg ffmpeg = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_MP4))
                .addOutput(UrlOutput.toPath(outputPath));

        CountDownLatch checkpoint = new CountDownLatch(1);
        ffmpeg.executeAsync().toCompletableFuture().thenAccept(futureRef::set)
                .exceptionally(v -> null)
                .thenRun(checkpoint::countDown);

        Assertions.assertTrue(checkpoint.await(60, TimeUnit.SECONDS));
        FFmpegResult encodingResult = futureRef.get();
        Assertions.assertNotNull(encodingResult);

        FFprobeResult probeResult = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setInput(outputPath)
                .execute();

        assertEquals(2, probeResult.getStreams().size());
    }

    @Test
    public void testCancellationOfProcessFuture() throws Exception {
        Path tempDir = Files.createTempDirectory("jaffree");
        Path outputPath = tempDir.resolve(Artifacts.VIDEO_MP4.getFileName());

        ProcessFuture<FFmpegResult> result = FFmpeg.atPath(Config.FFMPEG_BIN)
                .addInput(UrlInput.fromPath(Artifacts.VIDEO_FLV))
                .addOutput(UrlOutput.toPath(outputPath))
                .executeAsync();

        // waiting for the process to start
        Thread.sleep(500);

        result.cancel(false);
        result.get();

        assertTrue(result.isCancelled());
    }
}
