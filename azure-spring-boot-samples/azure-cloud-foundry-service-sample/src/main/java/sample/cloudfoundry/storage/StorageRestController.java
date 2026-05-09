/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.cloudfoundry.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@RestController
public class StorageRestController {

    public static final String IMAGE_PATH =
            "https://raw.githubusercontent.com/mjeffries-pivotal/pcf-samples/master/images/azure-pcf.jpg";
    private static final Logger LOG = LoggerFactory.getLogger(StorageRestController.class);

    @Autowired
    private BlobContainerClient containerClient;

    @RequestMapping(value = "/blob", method = RequestMethod.GET)
    @ResponseBody
    public void showBlob(final HttpServletResponse response) {
        InputStream is = null;
        try {
            LOG.info("showBlob start");
            if (containerClient == null) {
                LOG.error("BlobContainerClient is null!");
                return;
            }

            final URL u = new URL(IMAGE_PATH);
            is = u.openStream();
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            IOUtils.copy(is, response.getOutputStream());

            if (!containerClient.exists()) {
                containerClient.create();
            }

            LOG.debug("Uploading image...");
            final BlobClient blobClient = containerClient.getBlobClient("image1.jpg");
            final File imageFile = File.createTempFile("azure-image", ".jpg");
            try (InputStream imageStream = new URL(IMAGE_PATH).openStream()) {
                FileUtils.copyInputStreamToFile(imageStream, imageFile);
            }
            blobClient.uploadFromFile(imageFile.getAbsolutePath(), true);
            LOG.debug("Uploading image complete");

        } catch (IOException e) {
            LOG.error("Error retrieving image", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.warn("Failed to close the InputStream.", e);
                }
            }
        }
    }
}
