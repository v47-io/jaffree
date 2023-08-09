package com.github.kokorin.jaffree.ffmpeg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FFmpegProgressReaderTest {

    @Test
    public void readProgress() throws Exception {
        final List<FFmpegProgress> progressList = new ArrayList<>();

        ProgressListener listener = (progress, processAccess) -> progressList.add(progress);

        FFmpegProgressReader reader = new FFmpegProgressReader(listener);
        try (InputStream inputStream = getClass().getResourceAsStream("progress.log")) {
            reader.readProgress(inputStream);
        }

        Assertions.assertEquals(3, progressList.size());

        FFmpegProgress progress = progressList.get(0);

        Assertions.assertEquals((Long) 162L, progress.getFrame());
        Assertions.assertEquals((Double) 0., progress.getFps());
        Assertions.assertEquals((Double) 29., progress.getQ());
        Assertions.assertEquals((Double) 828.7, progress.getBitrate());
        Assertions.assertEquals((Long) 524_336L, progress.getSize());
        Assertions.assertEquals((Long) 5L, progress.getTime(TimeUnit.SECONDS));
        Assertions.assertEquals((Long) 5_061L, progress.getTimeMillis());
        Assertions.assertEquals((Long) 5_061L, progress.getTime(TimeUnit.MILLISECONDS));
        Assertions.assertEquals((Long) 5_061_950L, progress.getTimeMicros());
        Assertions.assertEquals((Long) 5_061_950L, progress.getTime(TimeUnit.MICROSECONDS));
        Assertions.assertEquals((Long) 4L, progress.getDup());
        Assertions.assertEquals((Long) 2L, progress.getDrop());
        Assertions.assertEquals((Double) 10.1, progress.getSpeed());

        progress = progressList.get(1);

        Assertions.assertEquals((Long) 2240L, progress.getFrame());
        Assertions.assertEquals((Double) 279.07, progress.getFps());
        Assertions.assertEquals((Double) 28., progress.getQ());
        Assertions.assertEquals((Double) 1125.4, progress.getBitrate());
        Assertions.assertEquals((Long) 10_485_808L, progress.getSize());
        Assertions.assertEquals((Long) 74_536_054L, progress.getTimeMicros());
        Assertions.assertEquals((Long) 0L, progress.getDup());
        Assertions.assertEquals((Long) 0L, progress.getDrop());
        Assertions.assertEquals((Double) 9.29, progress.getSpeed());
    }

    /**
     * Tests progress that has N/A values (first pass in 2 pass encoding for example)
     */
    @Test
    public void readProgressNA() throws Exception {
        final List<FFmpegProgress> progressList = new ArrayList<>();

        ProgressListener listener = (progress, processAccess) -> progressList.add(progress);

        FFmpegProgressReader reader = new FFmpegProgressReader(listener);
        try (InputStream inputStream = getClass().getResourceAsStream("progress-na.log")) {
            reader.readProgress(inputStream);
        }

        Assertions.assertEquals(3, progressList.size());

        FFmpegProgress progress = progressList.get(0);

        Assertions.assertEquals((Long) 1L, progress.getFrame());
        Assertions.assertEquals((Double) 0., progress.getFps());
        Assertions.assertEquals((Double) 0., progress.getQ());
        Assertions.assertNull(progress.getBitrate());
        Assertions.assertNull(progress.getSize());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.SECONDS));
        Assertions.assertEquals((Long) 0L, progress.getTimeMillis());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.MILLISECONDS));
        Assertions.assertEquals((Long) 0L, progress.getTimeMicros());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.MICROSECONDS));
        Assertions.assertEquals((Long) 0L, progress.getDup());
        Assertions.assertEquals((Long) 0L, progress.getDrop());
        Assertions.assertEquals((Double) 0., progress.getSpeed());

        progress = progressList.get(1);

        Assertions.assertEquals((Long) 7L, progress.getFrame());
        Assertions.assertEquals((Double) 0., progress.getFps());
        Assertions.assertEquals((Double) 0., progress.getQ());
        Assertions.assertNull(progress.getBitrate());
        Assertions.assertNull(progress.getSize());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.SECONDS));
        Assertions.assertEquals((Long) 0L, progress.getTimeMillis());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.MILLISECONDS));
        Assertions.assertEquals((Long) 0L, progress.getTimeMicros());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.MICROSECONDS));
        Assertions.assertEquals((Long) 0L, progress.getDup());
        Assertions.assertEquals((Long) 0L, progress.getDrop());
        Assertions.assertEquals((Double) 0., progress.getSpeed());

        progress = progressList.get(2);

        Assertions.assertEquals((Long) 17L, progress.getFrame());
        Assertions.assertEquals((Double) 14.77, progress.getFps());
        Assertions.assertEquals((Double) 0., progress.getQ());
        Assertions.assertNull(progress.getBitrate());
        Assertions.assertNull(progress.getSize());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.SECONDS));
        Assertions.assertEquals((Long) 0L, progress.getTimeMillis());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.MILLISECONDS));
        Assertions.assertEquals((Long) 0L, progress.getTimeMicros());
        Assertions.assertEquals((Long) 0L, progress.getTime(TimeUnit.MICROSECONDS));
        Assertions.assertEquals((Long) 0L, progress.getDup());
        Assertions.assertEquals((Long) 0L, progress.getDrop());
        Assertions.assertEquals((Double) 0., progress.getSpeed());
    }
}