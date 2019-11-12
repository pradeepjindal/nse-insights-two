package org.pra.nse.processor;

import org.pra.nse.ApCo;
import org.pra.nse.ProCo;
import org.pra.nse.bean.out.PraBean;
import org.pra.nse.csv.download.DownloadManager;
import org.pra.nse.csv.merge.CmMerger;
import org.pra.nse.csv.merge.FoMerger;
import org.pra.nse.csv.merge.MtMerger;
import org.pra.nse.csv.read.FoCsvReader;
import org.pra.nse.csv.writer.ManishCsvWriter;
import org.pra.nse.util.NseFileUtils;
import org.pra.nse.util.PraNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


@Component
public class ManishProcessorB {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManishProcessorB.class);

    private final CmMerger cmMerger;
    private final FoMerger foMerger;
    private final MtMerger mtMerger;
    private final NseFileUtils nseFileUtils;
    private final PraNameUtils praNameUtils;
    private final ManishCsvWriter csvWriter;

    public ManishProcessorB(CmMerger cmMerger, FoMerger foMerger, MtMerger mtMerger,
                            ManishCsvWriter csvWriter,
                            NseFileUtils nseFileUtils,
                            PraNameUtils praNameUtils) {
        this.cmMerger = cmMerger;
        this.foMerger = foMerger;
        this.mtMerger = mtMerger;
        this.csvWriter = csvWriter;
        this.nseFileUtils = nseFileUtils;
        this.praNameUtils = praNameUtils;
    }

    public void process(LocalDate processForDate) throws IOException {

        List<PraBean> praBeans = new ArrayList<>();
        // FO
        TreeSet<LocalDate> foMonthlyExpiryDates = foMerger.merge(praBeans, processForDate);
        // MAT
        mtMerger.merge(praBeans, processForDate);
        // CM
        cmMerger.merge(praBeans, processForDate);

        //-------------------------------------------------------
        String outputPathAndFileNameForFixFile = ProCo.outputPathAndFileNameForFixFile(ApCo.MANISH_FILE_NAME, "-b");
        csvWriter.write(praBeans, outputPathAndFileNameForFixFile, foMonthlyExpiryDates);
        //-------------------------------------------------------
        //String foLatestFileName = fileUtils.getLatestFileNameForFo(1);
        //String foLatestFileName = praNameUtils.getLatestFileNameFor(ApCo.FO_FILES_PATH, ApCo.FO_DATA_FILE_PREFIX, ApCo.PRA_DATA_FILE_EXT, 1);
        String foLatestFileName = praNameUtils.getLatestFileNameFor(ApCo.FO_FILES_PATH, ApCo.FO_DATA_FILE_PREFIX, ApCo.PRA_DATA_FILE_EXT, 1, processForDate);
        String outputPathAndFileNameForDynamicFile = ProCo.outputPathAndFileNameForDynamicFile(ApCo.MANISH_FILE_NAME, foLatestFileName, "-b");
        LOGGER.info("Fix File: ", outputPathAndFileNameForFixFile);
        LOGGER.info("Dynamic File: ", outputPathAndFileNameForFixFile);
        csvWriter.write(praBeans, outputPathAndFileNameForDynamicFile, foMonthlyExpiryDates);
        //-------------------------------------------------------

        if(nseFileUtils.isFileExist(outputPathAndFileNameForDynamicFile)) {
            LOGGER.info("---------------------------------------------------------------------------------------------------------------");
            LOGGER.info("SUCCESS! manishData.csv File has been placed at: " + outputPathAndFileNameForDynamicFile);
            LOGGER.info("---------------------------------------------------------------------------------------------------------------");
        } else {
            LOGGER.info("ERROR! something got wrong, could not create data file.");
        }
    }

    //@Override
    public void run(ApplicationArguments args) {
        LOGGER.info("Manish Processor B | ============================== | Kicking");
		try {
            process(LocalDate.now());
		} catch(Exception e) {
            LOGGER.error("{}", e);
		}
        LOGGER.info("Manish Processor B | ============================== | Finished");
    }
}
