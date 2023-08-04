package com.github.kokorin.jaffree.ffprobe;

import com.github.kokorin.jaffree.Artifacts;
import com.github.kokorin.jaffree.Config;
import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.Rational;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffprobe.data.FlatFormatParser;
import com.github.kokorin.jaffree.ffprobe.data.FormatParser;
import com.github.kokorin.jaffree.ffprobe.data.JsonFormatParser;
import com.github.kokorin.jaffree.process.JaffreeAbnormalExitException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class FFprobeTest {
    private static FlatFormatParser flatFormatParser = new FlatFormatParser();
    private static JsonFormatParser jsonFormatParser = new JsonFormatParser();

    static java.util.stream.Stream<Arguments> parserImplementations() {
        return java.util.stream.Stream.of(
                Arguments.of(flatFormatParser),
                Arguments.of(jsonFormatParser)
        );
    }

    @ParameterizedTest
    @MethodSource("parserImplementations")
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAllParsers {
    }

    //private boolean showData;


    @TestAllParsers
    public void testShowDataWithShowStreams(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowData(true)
                .setShowStreams(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertFalse(result.getStreams().isEmpty());

        Stream stream = result.getStreams().get(0);
        assertNotNull(stream.getExtradata());
        assertEquals(Rational.valueOf(30L), stream.getAvgFrameRate());
    }

    // For this test to pass ffmpeg must be added to Operation System PATH environment variable

    @TestAllParsers
    public void testEnvPath(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath()
                .setInput(Artifacts.VIDEO_MP4)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
    }


    @TestAllParsers
    public void testShowDataWithShowPackets(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowData(true)
                .setShowPackets(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getPackets());
        assertFalse(result.getPackets().isEmpty());
        assertNotNull(result.getPackets().get(0).getData());
        for (Packet packet : result.getPackets()) {
            assertNotNull(packet.getCodecType());
        }
    }


    //private String showDataHash;


    @TestAllParsers
    public void testShowDataHashWithShowStreams(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowDataHash("MD5")
                .setShowStreams(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertFalse(result.getStreams().isEmpty());
        assertNotNull(result.getStreams().get(0).getExtradataHash());
    }


    @TestAllParsers
    public void testShowDataHashWithShowPackets(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowDataHash("MD5")
                .setShowPackets(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getPackets());
        assertFalse(result.getPackets().isEmpty());
        for (Packet packet : result.getPackets()) {
            assertNotNull(packet.getCodecType());
            assertNotNull(packet.getDataHash());
        }
    }

    //private boolean showFormat;


    @TestAllParsers
    public void testShowFormat(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowFormat(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        Format format = result.getFormat();

        assertNotNull(format);
        assertNotNull(format.getFilename());
        assertNotNull(format.getNbStreams());
        assertNotNull(format.getNbPrograms());
        assertNotNull(format.getFormatName());
        assertNotNull(format.getFormatLongName());
        assertNotNull(format.getStartTime());
        assertNotNull(format.getDuration());
        assertNotNull(format.getSize());
        assertNotNull(format.getBitRate());
        assertNotNull(format.getProbeScore());
        assertEquals("isom", format.getTag("major_brand"));
        assertNotNull(format.getTagInteger("minor_version"));
        assertNotNull(format.getTagLong("minor_version"));
        assertNotNull(format.getTagDouble("minor_version"));
        assertNotNull(format.getTagFloat("minor_version"));
    }

    //private String showFormatEntry;
    //private String showEntries;


    @TestAllParsers
    public void testShowEntries(FormatParser formatParser) throws Exception {

        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowEntries(
                        "packet=pts_time,duration_time,stream_index : stream=index,codec_type")
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);

        assertNotNull(result.getPackets());
        assertTrue(result.getPackets().size() > 0);
        assertNotNull(result.getPackets().get(0).getPtsTime());
        assertNotNull(result.getPackets().get(0).getDurationTime());
        assertNotNull(result.getPackets().get(0).getStreamIndex());

        assertNotNull(result.getStreams());
        assertTrue(result.getStreams().size() > 0);
        assertNotNull(result.getStreams().get(0).getIndex());
        assertNotNull(result.getStreams().get(0).getCodecType());

    }

    //private boolean showFrames;


    @Disabled("fails when run against ffmpeg/ffprobe 5.0")
    @TestAllParsers
    public void testShowFrames(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_WITH_SUBTITLES)
                .setShowFrames(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getFrames());
        assertFalse(result.getFrames().isEmpty());

        Set<StreamType> streamTypes = EnumSet.noneOf(StreamType.class);

        for (FrameSubtitle frameSubtitle : result.getFrames()) {

            if (frameSubtitle instanceof Subtitle) {
                Subtitle subtitle = (Subtitle) frameSubtitle;
                streamTypes.add(subtitle.getMediaType());
                assertNotNull(subtitle.getPts());
                assertNotNull(subtitle.getPtsTime());
                assertNotNull(subtitle.getFormat());
                assertNotNull(subtitle.getStartDisplayTime());
                assertNotNull(subtitle.getEndDisplayTime());
                assertNotNull(subtitle.getNumRects());
                continue;
            }

            assertTrue(frameSubtitle instanceof Frame);
            Frame frame = (Frame) frameSubtitle;
            streamTypes.add(frame.getMediaType());
            if (frame.getMediaType() == StreamType.VIDEO) {
                assertNotNull(frame.getWidth());
                assertNotNull(frame.getHeight());
                assertNotNull(frame.getSampleAspectRatio());
                assertNotNull(frame.getPixFmt());
            }
            if (frame.getMediaType() == StreamType.AUDIO) {
                assertNotNull(frame.getChannels());
                assertNotNull(frame.getChannelLayout());
                assertNotNull(frame.getNbSamples());
            }
        }

        assertTrue(streamTypes.containsAll(
                List.of(StreamType.VIDEO, StreamType.AUDIO, StreamType.SUBTITLE)));
    }

    //private LogLevel showLog;


    @TestAllParsers
    public void testShowLog(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowFrames(true)
                .setShowLog(LogLevel.TRACE)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getFrames());
        assertFalse(result.getFrames().isEmpty());

        int framesWithLogs = 0;
        for (FrameSubtitle frameSubtitle : result.getFrames()) {
            Assertions.assertTrue(frameSubtitle instanceof Frame);
            Frame frame = (Frame) frameSubtitle;

            if (frame.getLogs() != null && !frame.getLogs().isEmpty()) {
                framesWithLogs++;

                for (Log log : frame.getLogs()) {
                    assertNotNull(log.getLevel());
                    assertNotNull(log.getCategory());
                    assertNotNull(log.getContext());
                    assertNotNull(log.getMessage());
                }
            }
        }
        assertTrue(framesWithLogs > 1000);
    }

    //private boolean showStreams;


    @TestAllParsers
    public void testShowStreams(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowStreams(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertEquals(2, result.getStreams().size());

        Stream videoStream = result.getStreams().get(0);

        assertEquals(StreamType.VIDEO, videoStream.getCodecType());
        assertEquals("h264", videoStream.getCodecName());
        assertEquals("0x31637661", videoStream.getCodecTag());
        assertEquals("avc1", videoStream.getCodecTagString());
        assertNotNull(videoStream.getIndex());
        assertEquals((Integer) 640, videoStream.getWidth());
        assertEquals((Integer) 480, videoStream.getHeight());
        assertNotNull(videoStream.getSampleAspectRatio());
        assertNotNull(videoStream.getDisplayAspectRatio());
        assertNotNull(videoStream.getStartPts());
        assertNotNull(videoStream.getTimeBase());
        assertEquals((Float) 180.f, videoStream.getDuration(), 0.01f);
        assertNotNull(videoStream.getBitRate());
        assertNotNull(videoStream.getNbFrames());
        assertEquals((Integer) 0, videoStream.hasBFrames());
        assertNotNull(videoStream.getBitsPerRawSample());
        assertNotNull(videoStream.getPixFmt());
        assertNotNull(videoStream.getRFrameRate());
        assertNotNull(videoStream.getAvgFrameRate());
        assertNotNull(videoStream.getDisposition());
        assertEquals("VideoHandler", videoStream.getTag("handler_name"));

        Stream audioStream = result.getStreams().get(1);

        assertEquals(StreamType.AUDIO, audioStream.getCodecType());
        assertEquals((Integer) 1, audioStream.getIndex());
        assertEquals("aac", audioStream.getCodecName());
        assertEquals("0x6134706d", audioStream.getCodecTag());
        assertEquals("mp4a", audioStream.getCodecTagString());
        assertNotNull(audioStream.getChannels());
        assertNotNull(audioStream.getChannelLayout());
        assertNotNull(audioStream.getSampleRate());
        assertNotNull(audioStream.getSampleFmt());
        assertNotNull(audioStream.getBitsPerSample());

        StreamDisposition disposition = audioStream.getDisposition();
        assertNotNull(disposition);
        assertEquals(Boolean.TRUE, disposition.getDefault());
        assertEquals(Boolean.FALSE, disposition.getDub());
        assertEquals(Boolean.FALSE, disposition.getOriginal());
        assertEquals(Boolean.FALSE, disposition.getComment());
        assertEquals(Boolean.FALSE, disposition.getLyrics());
        assertEquals(Boolean.FALSE, disposition.getKaraoke());
        assertEquals(Boolean.FALSE, disposition.getForced());
        assertEquals(Boolean.FALSE, disposition.getHearingImpaired());
        assertEquals(Boolean.FALSE, disposition.getVisualImpaired());
        assertEquals(Boolean.FALSE, disposition.getCleanEffects());
        assertEquals(Boolean.FALSE, disposition.getAttachedPic());
        assertEquals(Boolean.FALSE, disposition.getTimedThumbnails());
    }


    @TestAllParsers
    public void testSelectStreamWithShowStreams(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowStreams(true)
                .setSelectStreams(StreamType.VIDEO)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertEquals(1, result.getStreams().size());

        Stream stream = result.getStreams().get(0);
        assertEquals(StreamType.VIDEO, stream.getCodecType());
    }


    @TestAllParsers
    public void testSelectStreamWithShowPackets(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowPackets(true)
                .setSelectStreams("1")
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getPackets());
        assertTrue(result.getPackets().size() > 7000);
        assertNotNull(result.getPackets().get(0).getCodecType());
    }

    //private boolean showPrograms;


    @Disabled("fails when run against ffmpeg/ffprobe 5.0")
    @TestAllParsers
    public void testShowPrograms(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_WITH_PROGRAMS)
                .setShowPrograms(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getPrograms());
        assertEquals(3, result.getPrograms().size());

        Program program1 = result.getPrograms().get(0);
        assertEquals("first_program", program1.getTag("service_name"));
        assertEquals((Integer) 1, program1.getProgramId());
        assertEquals((Integer) 1, program1.getProgramNum());
        assertEquals((Integer) 2, program1.getNbStreams());
        assertNotNull(program1.getPmtPid());
        assertNotNull(program1.getPcrPid());
        assertNotNull(program1.getStartPts());
        assertNotNull(program1.getStartTime());
        assertNotNull(program1.getEndPts());
        assertNotNull(program1.getEndTime());
        assertNotNull(program1.getStreams());
        assertEquals(2, program1.getStreams().size());

        Program program2 = result.getPrograms().get(1);
        assertEquals("second program", program2.getTag("service_name"));
        assertEquals((Integer) 2, program2.getProgramNum());
        assertEquals((Integer) 2, program2.getNbStreams());
        assertNotNull(program2.getStreams());
        assertEquals(2, program2.getStreams().size());

        Program program3 = result.getPrograms().get(2);
        assertEquals("3rdProgram", program3.getTag("service_name"));
        assertEquals((Integer) 3, program3.getProgramNum());
        assertEquals((Integer) 2, program3.getNbStreams());
        assertNotNull(program3.getStreams());
        assertEquals(2, program3.getStreams().size());
    }

    //private boolean showChapters;


    @TestAllParsers
    public void testShowChapters(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_WITH_CHAPTERS)
                .setShowChapters(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getChapters());
        assertEquals(3, result.getChapters().size());

        Chapter chapter1 = result.getChapters().get(0);
        assertEquals(1, chapter1.getId());
        assertEquals("FirstChapter", chapter1.getTag("title"));
        assertEquals(new Rational(1L, 1_000_000_000L), chapter1.getTimeBase());
        assertEquals((Long) 0L, chapter1.getStart());
        assertEquals((Double) 0., chapter1.getStartTime(), 0.01);
        assertEquals((Long) 60_000_000_000L, chapter1.getEnd());
        assertEquals((Double) 60., chapter1.getEndTime(), 0.01);

        Chapter chapter2 = result.getChapters().get(1);
        assertEquals(2, chapter2.getId());
        assertEquals("Second Chapter", chapter2.getTag("title"));
        assertEquals((Long) 60_000_000_000L, chapter2.getStart());
        assertEquals((Double) 60., chapter2.getStartTime(), 0.01);

        Chapter chapter3 = result.getChapters().get(2);
        assertEquals(3, chapter3.getId());
        assertEquals("Final", chapter3.getTag("title"));
    }

    //private boolean countFrames;
    //private boolean countPackets;


    @TestAllParsers
    public void testCountFramesAndPackets(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowStreams(true)
                .setCountFrames(true)
                .setCountPackets(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        for (Stream stream : result.getStreams()) {
            assertTrue(stream.getNbFrames() > 0);
            assertTrue(stream.getNbReadFrames() > 0);
            assertTrue(stream.getNbReadPackets() > 0);
        }
    }

    //private String readIntervals;


    @TestAllParsers
    public void testReadIntervals(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MP4)
                .setShowPackets(true)
                .setReadIntervals("30%+#42")
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getPackets());
        assertEquals(42, result.getPackets().size());
        for (Packet packet : result.getPackets()) {
            assertNotNull(packet.getCodecType());
        }
    }


    @Disabled("fails when run against ffmpeg/ffprobe 5.0")
    @TestAllParsers
    public void testShowPacketsAndFrames(FormatParser formatParser) {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_WITH_SUBTITLES)
                .setShowPackets(true)
                .setShowFrames(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getPacketsAndFrames());
        assertTrue(result.getPacketsAndFrames().size() > 20_000);

        Set<Class<? extends PacketFrameSubtitle>> resultClasses = new HashSet<>();
        for (PacketFrameSubtitle pfs : result.getPacketsAndFrames()) {
            resultClasses.add(pfs.getClass());

            if (pfs instanceof Packet) {
                Packet packet = (Packet) pfs;

                assertNotNull(packet.getPts());
                assertNotNull(packet.getPtsTime());
                assertNotNull(packet.getCodecType());
                assertNotNull(packet.getDts());
                assertNotNull(packet.getDtsTime());
                assertNotNull(packet.getDuration());
                assertNotNull(packet.getDurationTime());
                assertNotNull(packet.getSize());
                assertNotNull(packet.getPos());
                assertNotNull(packet.getFlags());
                continue;
            }
            if (pfs instanceof Frame) {
                Frame frame = (Frame) pfs;

                assertNotNull(frame.getMediaType());
                assertNotNull(frame.getStreamIndex());
                assertNotNull(frame.getKeyFrame());
                assertNotNull(frame.getPktPts());
                assertNotNull(frame.getPktPtsTime());
                assertNotNull(frame.getPktDts());
                assertNotNull(frame.getPktDtsTime());
                assertNotNull(frame.getBestEffortTimestamp());
                assertNotNull(frame.getBestEffortTimestampTime());
                assertNotNull(frame.getPktDuration());
                assertNotNull(frame.getPktDurationTime());
                assertNotNull(frame.getPktPos());
                assertNotNull(frame.getPktSize());

                switch (frame.getMediaType()) {
                    case VIDEO:
                        assertNotNull(frame.getWidth());
                        assertNotNull(frame.getHeight());
                        assertNotNull(frame.getPixFmt());
                        assertNotNull(frame.getSampleAspectRatio());
                        assertNotNull(frame.getPictType());
                        assertNotNull(frame.getCodedPictureNumber());
                        assertNotNull(frame.getDisplayPictureNumber());
                        assertNotNull(frame.getInterlacedFrame());
                        assertNotNull(frame.getTopFieldFirst());
                        assertNotNull(frame.getRepeatPict());
                        break;
                    case AUDIO:
                        assertNotNull(frame.getSampleFmt());
                        assertNotNull(frame.getNbSamples());
                        assertNotNull(frame.getChannels());
                        assertNotNull(frame.getChannelLayout());
                        break;
                    default:
                        fail("Unexpected media type: " + frame.getMediaType());
                }
                continue;
            }
            if (pfs instanceof Subtitle) {
                Subtitle subtitle = (Subtitle) pfs;

                assertEquals(StreamType.SUBTITLE, subtitle.getMediaType());
                assertNotNull(subtitle.getPts());
                assertNotNull(subtitle.getPtsTime());
                assertNotNull(subtitle.getFormat());
                assertNotNull(subtitle.getStartDisplayTime());
                assertNotNull(subtitle.getEndDisplayTime());
                assertNotNull(subtitle.getNumRects());
                continue;
            }

            fail("Unexpected type: " + pfs);
        }

        assertTrue(resultClasses.containsAll(List.of(Packet.class, Frame.class, Subtitle.class)));
    }


    @TestAllParsers
    public void testStreamSideDataListAttributes(FormatParser formatParser) throws Exception {
        FFprobeResult result;
        try (InputStream rotatedInput = FFprobeTest.class.getResourceAsStream("rotated.mp4")) {
            assertNotNull(rotatedInput);
            result = FFprobe.atPath(Config.FFMPEG_BIN)
                    .setInput(rotatedInput)
                    .setShowStreams(true)
                    .setFormatParser(formatParser)
                    .setLogLevel(LogLevel.DEBUG)
                    .execute();
        }

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertEquals(1, result.getStreams().size());

        Stream stream = result.getStreams().get(0);
        assertNotNull(stream);

        assertNotNull(stream.getSideDataList());
        assertEquals(1, stream.getSideDataList().size());

        SideData sideData = stream.getSideDataList().get(0);
        assertNotNull(sideData);
        assertNotNull(sideData.getDisplayMatrix());
        assertEquals(3, sideData.getDisplayMatrix().trim().split("\n").length);
        assertNotNull(sideData.getRotation());
    }


    @Disabled("ffprobe 4.4 doesn't output frame side data")
    @TestAllParsers
    public void testFrameSideDataListAttributes(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.VIDEO_MJPEG)
                .setShowFrames(true)
                .setLogLevel(LogLevel.DEBUG)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result, "Null result");
        assertNotNull(result.getFrames(), "Null frames");
        assertFalse(result.getFrames().isEmpty(), "No frames");

        int sideDataCount = 0;
        for (FrameSubtitle frameSubtitle : result.getFrames()) {
            if (frameSubtitle instanceof Frame) {
                Frame frame = (Frame) frameSubtitle;
                if (frame.getSideDataList() == null) {
                    continue;
                }

                for (SideData sideData : frame.getSideDataList()) {
                    assertNotNull("Side data type", sideData.getSideDataType());
                    sideDataCount++;
                }
            }
        }

        assertTrue(sideDataCount > 0, "No Side Data");
    }


    @TestAllParsers
    public void testPacketSideDataListAttributes(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setInput(Artifacts.AUDIO_OPUS)
                .setShowPackets(true)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getPackets());
        assertFalse(result.getPackets().isEmpty());

        int sideDataCount = 0;
        for (Packet packet : result.getPackets()) {
            if (packet.getSideDataList() == null) {
                continue;
            }
            sideDataCount++;
            for (SideData sideData : packet.getSideDataList()) {
                assertNotNull(sideData.getSideDataType());
                assertNotNull(sideData.getLong("skip_samples"));
                assertNotNull(sideData.getLong("discard_padding"));
                assertNotNull(sideData.getLong("skip_reason"));
                assertNotNull(sideData.getLong("discard_reason"));
            }
        }

        assertTrue(sideDataCount >= 1);
    }


    @TestAllParsers
    public void testExceptionIsThrownIfFfprobeExitsWithError(FormatParser formatParser) {
        try {
            FFprobe.atPath(Config.FFMPEG_BIN)
                    .setInput(Paths.get("nonexistent.mp4"))
                    .setFormatParser(formatParser)
                    .execute();
        } catch (JaffreeAbnormalExitException e) {
            assertEquals(
                    "Process execution has ended with non-zero status: 1. Check logs for detailed error message.",
                    e.getMessage());
            assertEquals(1, e.getProcessErrorLogMessages().size());
            assertEquals("[error] nonexistent.mp4: No such file or directory",
                    e.getProcessErrorLogMessages().get(0).message);
            return;
        }

        fail("JaffreeAbnormalExitException should have been thrown!");
    }


    @TestAllParsers
    public void testProbeSize(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setProbeSize(10_000_000L)
                .setInput(Artifacts.VIDEO_MP4)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertFalse(result.getStreams().isEmpty());
    }


    @TestAllParsers
    public void testAnalyzeDuration(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setAnalyzeDuration(10_000_000L)
                .setInput(Artifacts.VIDEO_MP4)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertFalse(result.getStreams().isEmpty());
    }


    @TestAllParsers
    public void testAnalyzeDuration2(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setAnalyzeDuration(10, TimeUnit.SECONDS)
                .setInput(Artifacts.VIDEO_MP4)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertFalse(result.getStreams().isEmpty());
    }


    @TestAllParsers
    public void testFpsProbeSize(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setFpsProbeSize(100L)
                .setInput(Artifacts.VIDEO_MP4)
                .setFormatParser(formatParser)
                .execute();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertFalse(result.getStreams().isEmpty());
    }


    @TestAllParsers
    public void testAdditionalArguments(FormatParser formatParser) {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                // The same as .setShowStreams(true), just for testing
                .addArgument("-show_streams")
                .addArguments("-select_streams", "v")
                .setInput(Artifacts.VIDEO_MP4)
                .setFormatParser(formatParser)
                .execute();


        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertEquals(1, result.getStreams().size());

        Stream stream = result.getStreams().get(0);
        assertEquals(StreamType.VIDEO, stream.getCodecType());
    }


    @TestAllParsers
    public void testInputStream(FormatParser formatParser) throws Exception {
        FFprobeResult result;

        try (InputStream inputStream = Files
                .newInputStream(Artifacts.VIDEO_FLV, StandardOpenOption.READ)) {
            result = FFprobe.atPath(Config.FFMPEG_BIN)
                    .setShowStreams(true)
                    .setInput(inputStream)
                    .setFormatParser(formatParser)
                    .execute();
        }

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertFalse(result.getStreams().isEmpty());
    }


    @TestAllParsers
    public void testInputChannel(FormatParser formatParser) throws Exception {
        FFprobeResult result;

        try (SeekableByteChannel channel = Files
                .newByteChannel(Artifacts.VIDEO_MP4, StandardOpenOption.READ)) {
            result = FFprobe.atPath(Config.FFMPEG_BIN)
                    .setShowStreams(true)
                    .setInput(channel)
                    .setFormatParser(formatParser)
                    .execute();
        }

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertFalse(result.getStreams().isEmpty());
    }


    @TestAllParsers
    public void testAsyncExecution(FormatParser formatParser) throws Exception {
        FFprobeResult result = FFprobe.atPath(Config.FFMPEG_BIN)
                .setShowStreams(true)
                .setInput(Artifacts.VIDEO_MP4)
                .setFormatParser(formatParser)
                .executeAsync()
                .get();

        assertNotNull(result);
        assertNotNull(result.getStreams());
        assertEquals(2, result.getStreams().size());

        Stream stream = result.getStreams().get(0);
        assertEquals(StreamType.VIDEO, stream.getCodecType());

        stream = result.getStreams().get(1);
        assertEquals(StreamType.AUDIO, stream.getCodecType());
    }


    @TestAllParsers
    public void testAsyncExecutionWithException(FormatParser formatParser) throws Exception {
        var exception = Assertions.assertThrows(ExecutionException.class,
                () -> FFprobe.atPath(Config.FFMPEG_BIN)
                        .setShowStreams(true)
                        .setInput("non_existent.mp4")
                        .setFormatParser(formatParser)
                        .executeAsync()
                        .get());

        Assertions.assertTrue(exception.getMessage()
                .contains("Process execution has ended with non-zero status: 1"));
    }
}
