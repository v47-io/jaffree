package examples;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.NullOutput;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class StopExample {
    public static void stopWithException(final FFmpeg ffmpeg) throws Exception {
        final AtomicBoolean stopped = new AtomicBoolean();
        ffmpeg.setProgressListener(
                (progress, processAccess) -> {
                    if (stopped.get()) {
                        throw new RuntimeException("Stooped with exception!");
                    }
                }
        );

        final AtomicReference<FFmpegResult> result = new AtomicReference<>();

        new Thread() {
            @Override
            public void run() {
                FFmpegResult r = ffmpeg.execute();
                result.set(r);
            }
        }.start();

        Thread.sleep(5_000);
        stopped.set(true);

        Thread.sleep(1_000);
        System.out.println(result.get());
    }

    public static void stopWithInterruption(final FFmpeg ffmpeg) throws Exception {
        final AtomicReference<FFmpegResult> result = new AtomicReference<>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                FFmpegResult r = ffmpeg.execute();
                result.set(r);
            }
        };
        thread.start();

        Thread.sleep(5_000);
        thread.interrupt();

        Thread.sleep(1_000);
        System.out.println(result.get());
    }

    public static void stopForcefully(final FFmpeg ffmpeg) throws Exception {
        var future = ffmpeg.executeAsync();

        Thread.sleep(5_000);
        future.getProcessAccess().stopForcefully();

        Thread.sleep(1_000);
        System.out.println(future.get());

        // Uncaught exception in executeAsync thread:
        // Process execution has ended with non-zero status: 1
    }

    public static void stopGracefully(final FFmpeg ffmpeg) throws Exception {
        var future = ffmpeg.executeAsync();

        Thread.sleep(5_000);
        future.getProcessAccess().stopGracefully();

        Thread.sleep(1_000);
        System.out.println(future.get());
    }

    public static void main(String[] args) throws Exception {
        FFmpeg ffmpeg;
        ffmpeg = createTestFFmpeg();
        stopWithException(ffmpeg);

        ffmpeg = createTestFFmpeg();
        stopWithInterruption(ffmpeg);

        ffmpeg = createTestFFmpeg();
        stopForcefully(ffmpeg);

        ffmpeg = createTestFFmpeg();
        stopGracefully(ffmpeg);
    }

    public static FFmpeg createTestFFmpeg() {
        return FFmpeg.atPath()
                .addInput(
                        UrlInput
                                .fromUrl("testsrc=duration=3600:size=1280x720:rate=30")
                                .setFormat("lavfi")
                )
                .setProgressListener((progress, processAccess) -> {
                    //System.out.println(progress);
                })
                .addOutput(
                        new NullOutput()
                );
    }
}
