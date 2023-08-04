package com.github.kokorin.jaffree.ffprobe.data;

import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FlatFormatParserTest {

    @Test
    public void parse() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("ffprobe_out.flat")) {
            ProbeData data = new FlatFormatParser().parse(input);
            Assertions.assertNotNull(data);

            List<ProbeData> streams = data.getSubDataList("streams");
            Assertions.assertEquals(2, streams.size());
        }

    }

    @Test
    public void parseMultiline() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("ffprobe_multiline_out.flat")) {
            ProbeData data = new FlatFormatParser().parse(input);
            Assertions.assertNotNull(data);

            List<ProbeData> streams = data.getSubDataList("streams");
            Assertions.assertEquals(2, streams.size());

            String description =
                    data.getSubData("format").getSubData("tags").getString("description");

            int actualLines = description.split("\\n").length;
            Assertions.assertTrue(actualLines > 10);
        }

    }

    @Test
    public void parseHttp() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("ffprobe_http_out.flat")) {
            ProbeData data = new FlatFormatParser().parse(input);
            Assertions.assertNotNull(data);

            List<ProbeData> streams = data.getSubDataList("streams");
            Assertions.assertEquals(2, streams.size());

            String actual = data.getSubData("format").getString("filename");
            String expected =
                    "https://sample-videos.com/video123/mp4/240/big_buck_bunny_240p_30mb.mp4?q1=v1&q2=v2";

            Assertions.assertEquals(expected, actual);
        }

    }

    @Test
    public void parseWithRotate() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("ffprobe_with_rotate.flat")) {
            ProbeData data = new FlatFormatParser().parse(input);
            Assertions.assertNotNull(data);

            List<ProbeData> streams = data.getSubDataList("streams");
            Assertions.assertEquals(2, streams.size());

            ProbeData section = streams.get(0);
            Assertions.assertNotNull(section);

            ProbeData dTag = section.getSubData("tags");
            Assertions.assertNotNull(dTag);

            String rotate = dTag.getString("rotate");
            Assertions.assertEquals("90", rotate);

            FFprobeResult result = new FFprobeResult(data);
            Stream stream = result.getStreams().get(0);
            rotate = stream.getTag("rotate");
            Assertions.assertEquals("90", rotate);
        }
    }
}