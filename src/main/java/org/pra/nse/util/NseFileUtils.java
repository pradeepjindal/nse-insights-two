package org.pra.nse.util;

import org.pra.nse.ApCo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class NseFileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NseFileUtils.class);

    public void createFolder(String outputPathAndFileName) {
        String dataDir = ApCo.BASE_DATA_DIR + File.separator + ApCo.PRA_DATA_DIR_NAME;
        File folder = new File(dataDir);
        File[] listOfFiles = folder.listFiles();
        if(null == folder.listFiles()) {
            createDataDir(dataDir);
        }
    }

    public void createDataDir(String dataDir) {
        File newFolder = new File(dataDir);
        boolean created =  newFolder.mkdir();
        LOGGER.info("creating folder: " + dataDir);
        if(created)
            LOGGER.info("Folder was created!");
        else
            LOGGER.info("Unable to create folder");
    }

    public boolean isFileExist(String filePathAndName) {
        return new File(filePathAndName).exists();
    }

    public void unzip(String outputDirAndFileName) throws IOException {
        int lastIndex = outputDirAndFileName.lastIndexOf("\\");
        File destDir = new File(outputDirAndFileName.substring(0, lastIndex));
        byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        zis = new ZipInputStream(new FileInputStream(outputDirAndFileName));
        ZipEntry zipEntry;
        zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile;
            newFile = newFile(destDir, zipEntry);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }
    public void unzipNew(String outputDirAndFileName, String filePrefix) throws IOException {
        int lastIndex = outputDirAndFileName.lastIndexOf(File.separator);
        File destDir = new File(outputDirAndFileName.substring(0, lastIndex));
        byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        zis = new ZipInputStream(new FileInputStream(outputDirAndFileName));
        ZipEntry zipEntry;
        zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String csvFilePathAndName = destDir + File.separator + filePrefix
                    + extractDate(zipEntry.getName()) + ApCo.PRA_DATA_FILE_EXT;
            File csvFile = new File(csvFilePathAndName);
            FileOutputStream fos = new FileOutputStream(csvFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName().replace("bhav",""));
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    public List<String> constructFileNames(LocalDate fromDate, String fileDateFormat,
                                            String filePrefix, String fileSuffix) {
        List<String> fileNameList = new ArrayList<>();
        LocalDate localDate = fromDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fileDateFormat);
        LocalDate todayDate = LocalDate.now();
        while(localDate.compareTo(todayDate) < 1) {
            //LOGGER.info(localDate);
            //LOGGER.info(localDate.getDayOfWeek());
            if("SATURDAY".equals(localDate.getDayOfWeek().name()) || "SUNDAY".equals(localDate.getDayOfWeek().name())) {
                // Deepawali, sunday trading
                if(localDate.equals(LocalDate.of(2019,10,27))) {
                    LOGGER.info("Deepawali Found");
                    //LOGGER.info(localDate.getDayOfWeek());
                    String newFileName = filePrefix + formatter.format(localDate).toUpperCase() + fileSuffix;
                    //LOGGER.info(newFileName);
                    fileNameList.add(newFileName);
                }
                //LOGGER.info(localDate.getDayOfWeek());
            } else {
                //LOGGER.info(localDate.getDayOfWeek());
                String newFileName = filePrefix + formatter.format(localDate).toUpperCase() + fileSuffix;
                //LOGGER.info(newFileName);
                fileNameList.add(newFileName);
            }
            localDate = localDate.plusDays(1);
        }
        //fileNamesToBeDownloaded.forEach(fileName -> LOGGER.info(fileName));
        LOGGER.info("Total File Count: " + fileNameList.size());
//        fileNameList.forEach(name-> {
//            String s = extractDate(name);
//            System.out.println(s);
//        });
        return fileNameList;
    }

    public List<String> fetchFileNames(String dirPathAndName, String filePrefix, String fileSuffix) {
        File folder = new File(dirPathAndName);
        File[] listOfFiles = folder.listFiles();
        if(null == folder.listFiles()) {
            createDataDir(dirPathAndName);
            listOfFiles = folder.listFiles();
        }
        List<String> existingFiles = new ArrayList<>();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                //LOGGER.info("File " + listOfFiles[i].getName());
                existingFiles.add(listOfFile.getName());
            } else if (listOfFile.isDirectory()) {
                LOGGER.info("Directory " + listOfFile.getName());
            }
        }
//        existingFiles.forEach(name-> {
//            String s = extractDate(name);
//            System.out.println(s);
//        });
        return existingFiles;
    }

    public List<String> constructFileDownloadUrlWithYearAndMonth(String baseUrl, List<String> filesToBeDownloaded) {
        List<String> filesUrl;
        filesUrl = filesToBeDownloaded.stream().map(fileName -> {
            //LOGGER.info(fileName);
            return baseUrl + "/" + fileName.substring(4, 7) + "/" + fileName;
        }).collect(Collectors.toList());
        //String newUrl = baseUrl + "/" + localDate.getMonth().name().substring(0,3) + "/fo" + formatter.format(localDate).toUpperCase() + "bhav.csv.zip";
        filesUrl.forEach(LOGGER::info);
        return filesUrl;
    }
    public List<String> constructFileDownloadUrl(String baseUrl, List<String> filesToBeDownloaded) {
        List<String> filesUrl;
        filesUrl = filesToBeDownloaded.stream().map(fileName -> {
            //LOGGER.info(fileName);
            return baseUrl + "/" + fileName;
        }).collect(Collectors.toList());
        //String newUrl = baseUrl + "/" + localDate.getMonth().name().substring(0,3) + "/fo" + formatter.format(localDate).toUpperCase() + "bhav.csv.zip";
        //filesUrl.forEach(url -> LOGGER.info(url));
        return filesUrl;
    }

    public String extractDate(String inputString) {
        //String input = "John Doe at:2016-01-16 Notes:This is a test";
        //String regex = " at:(\\d{4}-\\d{2}-\\d{2}) Notes:";
        String regex = "\\d{2}[A-Z]{3}\\d{4}";
        Matcher m = Pattern.compile(regex).matcher(inputString);
        if (m.find()) {
            DateTimeFormatter dtf = new DateTimeFormatterBuilder()
                    // case insensitive to parse JAN and FEB
                    .parseCaseInsensitive()
                    // add pattern
                    .appendPattern("ddMMMyyyy")
                    // create formatter (use English Locale to parse month names)
                    .toFormatter(Locale.ENGLISH);
            return LocalDate.parse(m.group(0), dtf).toString();
        } else {
            // Bad input
            LOGGER.warn("date not found in given file name");
            return "";
        }
    }

    public List<LocalDate> getFilesToBeComputed(LocalDate downloadFromDate) {
        String dataDir = ApCo.BASE_DATA_DIR + File.separator + ApCo.PRA_DATA_DIR_NAME;
        List<String> filesToBeDownloaded = constructFileNames(
                downloadFromDate,
                ApCo.PRA_FILE_NAME_DATE_FORMAT,
                ApCo.PRADEEP_FILE_NAME + "-",
                ApCo.PRA_DATA_FILE_EXT);
        filesToBeDownloaded.removeAll(fetchFileNames(dataDir, null, null));
        List<LocalDate> localDates = new ArrayList<>();
        filesToBeDownloaded.forEach( file -> {
            String date = file.replace(ApCo.PRADEEP_FILE_NAME + "-", "");
            date = date.replace(ApCo.PRA_DATA_FILE_EXT, "");
            localDates.add(LocalDate.of(Integer.valueOf(date.substring(0,4)),
                                        Integer.valueOf(date.substring(5,7)),
                                        Integer.valueOf(date.substring(8,10))));
        });
        return localDates;
    }
}
