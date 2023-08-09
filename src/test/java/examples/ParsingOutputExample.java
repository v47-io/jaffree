package examples;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.NullOutput;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;

public class ParsingOutputExample {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Exactly 1 argument expected: path to media file");
            System.exit(1);
        }

        String pathToVideo = args[0];

        // StringBuffer - because it's thread safe
        final StringBuffer loudnormReport = new StringBuffer();

        FFmpeg.atPath()
                .addInput(UrlInput.fromUrl(pathToVideo))
                .addArguments("-af", "loudnorm=I=-16:TP=-1.5:LRA=11:print_format=json")
                .addOutput(new NullOutput(false))
                .setOutputListener((line, processAccess) -> loudnormReport.append(line))
                .execute();

        System.out.println("Loudnorm report:\n" + loudnormReport);
    }
}
