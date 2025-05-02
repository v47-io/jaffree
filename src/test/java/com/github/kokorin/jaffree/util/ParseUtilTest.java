package com.github.kokorin.jaffree.util;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParseUtilTest {

    @Test
    public void parseLogLevel() {
        Assertions.assertEquals(LogLevel.INFO, ParseUtil.parseLogLevel(
                "[info]"
        ));
        Assertions.assertEquals(LogLevel.INFO, ParseUtil.parseLogLevel(
                "[info] frame= 1759 fps=421 q=-1.0 Lsize=    3714kB time=00:00:58.59 bitrate= 519.2kbits/s speed=  14x"
        ));
        Assertions.assertEquals(LogLevel.INFO, ParseUtil.parseLogLevel(
                "[libx264 @ 0x5640c7caa580] [info] frame I:13    Avg QP:23.45  size: 11116"
        ));
        Assertions.assertEquals(LogLevel.INFO, ParseUtil.parseLogLevel(
                "[libx264 @ 0x5640c7caa580] [info]"
        ));

        Assertions.assertEquals(LogLevel.VERBOSE, ParseUtil.parseLogLevel(
                "[AVIOContext @ 0x5640c7c802c0] [verbose] Statistics: 6 seeks, 26 writeouts"
        ));

        Assertions.assertEquals(LogLevel.DEBUG, ParseUtil.parseLogLevel(
                "[debug] 4268 frames successfully decoded, 0 decoding errors"
        ));

        Assertions.assertEquals(LogLevel.DEBUG, ParseUtil.parseLogLevel(
                "[matroska @ 0x5640c7cb2000] [debug] stream 1 end duration = 58239"
        ));

        Assertions.assertEquals(LogLevel.ERROR, ParseUtil.parseLogLevel(
                "[error] .artifacts/MPEG-4/videosadfasdf.mp4: No such file or directory"
        ));

        Assertions.assertEquals(LogLevel.TRACE, ParseUtil.parseLogLevel(
                "[mov,mp4,m4a,3gp,3g2,mj2 @ 0x56288d084700] [] stream 3, sample 45, dts 5201270"
        ));

        Assertions.assertEquals(LogLevel.TRACE, ParseUtil.parseLogLevel(
                "[mov,mp4,m4a,3gp,3g2,mj2 @ 0x56288d084700] [trace] stream 3, sample 45, dts 5201270"
        ));

        Assertions.assertEquals(LogLevel.ERROR, ParseUtil.parseLogLevel(
                "[loudnorm @ 0x55c3e47a6e40] [Eval @ 0x7ffc5e716b40] [error] Undefined constant or missing '(' in 'pfnb'"
        ));

        for (LogLevel logLevel : LogLevel.values()) {
            Assertions.assertEquals(logLevel,
                    ParseUtil.parseLogLevel("[" + logLevel.name().toLowerCase() + "]"));
        }

        for (LogLevel logLevel : LogLevel.values()) {
            Assertions.assertNull(ParseUtil.parseLogLevel("[" + logLevel.name().toLowerCase()));
        }

        Assertions.assertNull(
                ParseUtil.parseLogLevel("[mov,mp4,m4a,3gp,3g2,mj2 @ 0x56288d084700]"));
        Assertions.assertNull(
                ParseUtil.parseLogLevel("[mov,mp4,m4a,3gp,3g2,mj2 @ 0x56288d084700] [inf"));
    }

    @Test
    public void parseKibiByteFormats() {
        final Long oldFormat = ParseUtil.parseSizeInKibiBytes("2904kB");
        Assertions.assertEquals(2904L, oldFormat.longValue());

        final Long newFormat = ParseUtil.parseSizeInKibiBytes("2904KiB");
        Assertions.assertEquals(2904L, newFormat.longValue());

        final Long unknownFormat = ParseUtil.parseSizeInKibiBytes("2904KB");
        Assertions.assertNull(unknownFormat);
    }

    @Test
    public void parseResult() throws Exception {
        String value =
                "video:1417kB audio:113kB subtitle:0kB other streams:0kB global headers:0kB muxing overhead: unknown";
        FFmpegResult result = ParseUtil.parseResult(value);

        Assertions.assertNotNull(result);
        Assertions.assertEquals((Long) 1_451_008L, result.getVideoSize());
        Assertions.assertEquals((Long) 115_712L, result.getAudioSize());
        Assertions.assertEquals((Long) 0L, result.getSubtitleSize());
        Assertions.assertEquals((Long) 0L, result.getOtherStreamsSize());
        Assertions.assertEquals((Long) 0L, result.getGlobalHeadersSize());
        Assertions.assertNull(result.getMuxingOverheadRatio());
    }

    @Test
    public void parseZeroResult() throws Exception {
        String value =
                "video:0kB audio:0kB subtitle:0kB other streams:0kB global headers:0kB muxing overhead: 0.000000%";
        FFmpegResult result = ParseUtil.parseResult(value);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getVideoSize().longValue());
        Assertions.assertEquals(0, result.getAudioSize().longValue());
        Assertions.assertEquals(0, result.getSubtitleSize().longValue());
        Assertions.assertEquals(0, result.getOtherStreamsSize().longValue());
        Assertions.assertEquals(0, result.getGlobalHeadersSize().longValue());
        Assertions.assertEquals(0, result.getMuxingOverheadRatio(), 0.00000001);
    }


    @Test
    public void parseResultWhichDoesntContainResult() throws Exception {
        String value = "This= 5Random String : doesn't contain progre==55 info";
        FFmpegResult result = ParseUtil.parseResult(value);

        Assertions.assertNull(result);
    }

}