/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package sample.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.microsoft.azure.spring.autoconfigure.storage.StorageProperties;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@SpringBootApplication
public class StorageSampleApplication implements CommandLineRunner {
    private static final String SOURCE_FILE = "storageTestFile.txt";

    @Autowired
    private BlobContainerClient containerClient;

    @Autowired
    private StorageProperties properties;

    public static void main(final String[] args) {
        SpringApplication.run(StorageSampleApplication.class);
    }

    public void run(final String... var1) throws IOException {
        final File sourceFile = new File(this.getClass().getClassLoader().getResource(SOURCE_FILE).getFile());
        final File downloadFile = Files.createTempFile("azure-storage-test", null).toFile();

        StorageService.createContainerIfNotExists(containerClient);
        final BlobClient blobClient = containerClient.getBlobClient(SOURCE_FILE);

        System.out.println("Enter a command:");
        System.out.println("(P)utBlob | (G)etBlob | (D)eleteBlobs | (E)xitSample");
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        boolean isExit = false;
        while (!isExit) {
            System.out.println("Enter a command:");
            final String input = reader.readLine();
            if (input == null) {
                continue;
            }

            switch (input) {
                case "P":
                    StorageService.uploadFile(blobClient, sourceFile);
                    break;
                case "G":
                    StorageService.downloadBlob(blobClient, downloadFile);
                    break;
                case "D":
                    StorageService.deleteBlob(blobClient);
                    break;
                case "E":
                    System.out.println("Cleaning up container and tmp file...");
                    containerClient.delete();
                    FileUtils.deleteQuietly(downloadFile);
                    isExit = true;
                    break;
                default:
                    break;
            }
        }
    }
}
