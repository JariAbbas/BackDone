package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PreDestroy;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class AllLoanDataService { // New Service Class

    private IReportEngine birtEngine;
    private static final Logger logger = Logger.getLogger(AllLoanDataService.class.getName());


    public byte[] generateReport(
            Map<String, Object> parameters) // Keep the parameters argument, even if it's empty
            throws BirtException {

        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setEngineHome("");
        Platform.startup(engineConfig);
        IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        IReportEngine engine = factory.createReportEngine(engineConfig);

        IReportRunnable report = engine.openReportDesign("/home/bahl/Desktop/LBS_PROJECT/project/May0605Final-master-updated/May0605Final-master/loan-booking-be/BAHL_TWO_APIS/bahl-backend/src/main/resources/loanReports.rptdesign"); //changed rptdesign

        IRunAndRenderTask task = engine.createRunAndRenderTask(report);
        PDFRenderOption options = new PDFRenderOption();
        options.setOutputFormat("pdf");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        options.setOutputStream(outputStream);
        task.setParameterValues(parameters); // Pass the (empty) parameters

        task.setRenderOption(options);
        task.run();
        task.close();

        byte[] reportContent = outputStream.toByteArray();
        engine.destroy();
        Platform.shutdown();
        return reportContent;
    }

    @PreDestroy
    public void shutdown() {
        if (birtEngine != null) {
            birtEngine.destroy();
            Platform.shutdown();
        }
    }
}