package com.theironyard.controllers;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.theironyard.entities.Loop;
import com.theironyard.services.LoopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//import io.minio.MinioClient;


@Controller
public class PlayMeInController {
    @Value("${aws.accessid}")
    private String accessid;

    private Map<String, String> env = System.getenv();

    private String accesskey = env.get("AWS_SECRET_KEY");//TODO refactor to helper controller

    @Value("${aws.bucket}")
    private String bucket;

    @Autowired
    LoopRepository loops;

    public static final ArrayList<String> VOICES = new ArrayList<>(Arrays.asList("bass", "melody", "drum", "alt-drum", "harmony", "alt-harmony"));
    public static final String BASICPATH = "temp/";
    public static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345890";

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home() {
        return "index";
    }

    @RequestMapping(path = "/admin", method = RequestMethod.GET)
    public String admin() {
        return "admin";
    }

    @RequestMapping(path = "/new-song", method = RequestMethod.POST)
    public String newSong(Model model, String genre) throws Exception {
        List<Path> paths = new ArrayList<>();
        Random rng = new Random();
        for (String voice : VOICES) {
            List<Loop> partLoops = loops.findByGenreAndVoice(genre, voice);
            System.out.println(partLoops.get(0).toString());
            int loopNumber = rng.nextInt(partLoops.size()) + 1;
            String pathEnd = String.format("%s%s%d.wav", genre, voice, loopNumber);
            loadMusicAssetsFromS3(pathEnd);
            paths.add(Paths.get(BASICPATH, pathEnd));
        }

        String tempName = generateString(rng);
        String tempFileLocation = mergeSoundFiles(paths, tempName);

        removeTempFiles(paths);
        delayDeleteGeneratedSong(tempFileLocation);

        model.addAttribute("song", tempFileLocation);

        return "preview";
    }

    private void loadMusicAssetsFromS3(String fileName) throws Exception {
        // pull down files from S3 into ____(/tmp ? directory)
//        MinioClient s3Client = new MinioClient("https://s3.amazonaws.com/", accessid, accesskey);
//        InputStream ins = s3Client.getObject(bucket, fileName);

        AmazonS3 s3Client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider().getCredentials());
        S3Object object = s3Client.getObject(new GetObjectRequest(bucket, accesskey));
        InputStream ins = object.getObjectContent();

        FileOutputStream fos = new FileOutputStream(new File("temp/" + fileName));
        fos.write(ins.read());
    }

    public String mergeSoundFiles(List<Path> paths, String tempName) throws IOException {
        List<byte[]> bytesList = new ArrayList<>();
        for (Path p : paths) {
            bytesList.add(Files.readAllBytes(p));
        }
        int byteLength = bytesList.get(0).length;

        // TODO Refactor inner for loop into new method, pass bytesList and byteLength, return value of out

        byte[] out = new byte[byteLength];

        for (int i = 0; i < byteLength; i++) {
            int bytesTotal = 0;
            for (byte[] bytes : bytesList) {
                bytesTotal += (int) bytes[i];
            }
            out[i] = (byte) (bytesTotal >> 2);
        }

        //TODO end section

        ByteArrayInputStream bais = new ByteArrayInputStream(out);
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        AudioInputStream stream = new AudioInputStream(
                bais,
                format,
                out.length / format.getFrameSize()
        );

        String fileName = String.format("%s.wav", tempName);
        File file = new File("temp/", fileName);
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);

        return fileName;
    }

    public static String generateString(Random rng)
    {
        char[] text = new char[12];
        for (int i = 0; i < 12; i++)
        {
            text[i] = CHARS.charAt(rng.nextInt(CHARS.length()));
        }
        return new String(text);
    }

    private void removeTempFiles(List<Path> paths) {
        for (Path path : paths) {
            try {
                Files.delete(path);
            } catch (Exception e) {
            }
        }
    }

    private void delayDeleteGeneratedSong(String tempFileLocation) {
    }
}
