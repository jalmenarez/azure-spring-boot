/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class StorageService {

    public static void uploadFile(final BlobClient blobClient, final File sourceFile) {
        logInfo("Start uploading file %s...", sourceFile);
        try {
            blobClient.uploadFromFile(sourceFile.getAbsolutePath(), true);
            logInfo("File %s is uploaded.", sourceFile.toPath());
        } catch (Exception e) {
            logError("Failed to upload file %s with error %s.", sourceFile.toPath(), e.getMessage());
            throw e;
        }
    }

    public static void deleteBlob(final BlobClient blobClient) {
        logInfo("Start deleting blob %s...", blobClient.getBlobName());
        try {
            blobClient.delete();
            logInfo("Blob %s is deleted.", blobClient.getBlobName());
        } catch (Exception e) {
            logError("Failed to delete blob %s with error %s.", blobClient.getBlobName(), e.getMessage());
            throw e;
        }
    }

    public static void downloadBlob(final BlobClient blobClient, final File downloadToFile) {
        logInfo("Start downloading blob %s to %s...", blobClient.getBlobName(), downloadToFile);
        FileUtils.deleteQuietly(downloadToFile);
        try {
            blobClient.downloadToFile(downloadToFile.getAbsolutePath());
            logInfo("File is downloaded to %s.", downloadToFile);
        } catch (Exception e) {
            logError("Failed to download blob %s with error %s.", blobClient.getBlobName(), e.getMessage());
            throw e;
        }
    }

    public static void createContainerIfNotExists(final BlobContainerClient containerClient) {
        logInfo("Start creating container %s...", containerClient.getBlobContainerName());
        try {
            final Response<Void> response = containerClient.createWithResponse(null, null, null, null);
            logInfo("Storage container %s created with status code: %s.",
                    containerClient.getBlobContainerName(), response.getStatusCode());
        } catch (BlobStorageException e) {
            if (e.getStatusCode() != 409) {
                logError("Failed to create container %s.", containerClient.getBlobContainerName(), e);
                throw e;
            } else {
                logInfo("%s container already exists.", containerClient.getBlobContainerName());
            }
        }
    }

    private static void logInfo(final String log, final Object... params) {
        System.out.println(String.format(log, params));
    }

    private static void logError(final String log, final Object... params) {
        System.err.println(String.format(log, params));
    }
}
