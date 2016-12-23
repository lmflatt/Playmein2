package com.theironyard.controllers;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.theironyard.entities.Loop;
import com.theironyard.services.LoopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

//import io.minio.MinioClient;

/**
 * Created by lee on 11/3/16.
 */
@Controller
public class PlayMeInRestController {
    @Value("${aws.accessid}")
    private String accessid;

    @Value("${aws.bucket}")
    private String bucket;

    private Map<String, String> env = System.getenv();

    private String accesskey = env.get("AWS_ACCESSKEY");

    @Autowired
    LoopRepository loops;

    // RECEIVING String genre, String voice, and Multipart-File music
    @RequestMapping("/upload")
    public String upload(
            String genre,
            String voice,
            MultipartFile sample
    ) throws Exception {
        int partNumber = loops.findByGenreAndVoice(genre, voice).size() + 1;
        String filePrefix = genre + voice + partNumber;

        File wavFile = new File("/tmp/", filePrefix + ".wav");
        FileOutputStream fos = new FileOutputStream(wavFile);
        fos.write(sample.getBytes());

//        FileInputStream fis = new FileInputStream(wavFile);
//
        storeMusicAssetsToS3(filePrefix + ".wav", wavFile);

        Loop loop = new Loop(genre, voice, partNumber);
        loops.save(loop);

        return "index";
    }

    private void storeMusicAssetsToS3(String fileName, File file) throws Exception {
//        System.out.println("\nBUCKET = " + bucket + "\nSECRET_ACCESS_ID = " + accessid + "\n" + accesskey);
//        MinioClient s3Client = new MinioClient("https://s3.amazonaws.com", accessid, accesskey);
//        s3Client.putObject(bucket, fileName, stream, stream.available(), "audio/wav");

        try {
//            EnvironmentVariableCredentialsProvider evcp = new EnvironmentVariableCredentialsProvider();
            AmazonS3 s3Client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider().getCredentials());
            s3Client.putObject(new PutObjectRequest(bucket, accesskey, file));
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
