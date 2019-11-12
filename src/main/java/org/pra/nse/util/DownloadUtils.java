package org.pra.nse.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class DownloadUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadUtils.class);

    public void downloadFile(String fromUrl, String toDir, Supplier<String> inputFileSupplier, Consumer<String> outputFileConsumer) {
        String outputDirAndFileName = inputFileSupplier.get();
        LOGGER.info("URL: " + fromUrl);
        LOGGER.info("OUT: " + outputDirAndFileName);
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(fromUrl).openStream());
             FileOutputStream fileOS = new FileOutputStream(outputDirAndFileName)) {
            byte[] data = new byte[1024];
            int byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }
            //unzip(outputDirAndFileName);
            outputFileConsumer.accept(outputDirAndFileName);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
    }
}
