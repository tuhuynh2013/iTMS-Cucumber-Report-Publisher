package io.jenkins.plugins;

import hidden.jth.org.apache.http.HttpResponse;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import io.jenkins.rest.RequestAPI;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static io.jenkins.plugins.ITMSConsts.SERVICE_NAME;

public class CucumberPostBuild extends Notifier {

    private final String itmsAddress;
    private final String reportFolder;
    private final String reportFormat;
    private final String ticketTitle;
    private final String cycleName;


    private final String JSON_FORMAT = "Cucumber Json";
    private final String XML_FORMAT = "Cucumber JUnit";

    @DataBoundConstructor
    public CucumberPostBuild(final String itmsAddress, final String reportFolder,
                             final String reportFormat, final String ticketTitle, final String cycleName) {
        this.itmsAddress = itmsAddress;
        this.reportFolder = reportFolder;
        this.reportFormat = reportFormat;
        this.ticketTitle = ticketTitle;
        this.cycleName = cycleName;
    }

    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener) {
        int counter = 0;
        try {
            listener.getLogger().println("Starting Post Build Action");

            File folder = new File(build.getWorkspace() + reportFolder);
            listener.getLogger().println("Report folder: " + folder.getPath());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    String content = "";
                    if (reportFormat.equals(JSON_FORMAT) && file.getName().toLowerCase().endsWith(".json")) {
                        counter++;
                        content = readFileContent(file);
                        listener.getLogger().println(sendReportContent(content, build));
                    } else if (reportFormat.equals(XML_FORMAT) && file.getName().toLowerCase().endsWith(".xml")) {
                        counter++;
                        content = readFileContent(file);
                        listener.getLogger().println(sendReportContent(content, build));
                    }
                }

                if (counter < 1) {
                    listener.getLogger().println("Report file not found! Check your report folder and format type");
                }

            } else {
                listener.getLogger().println("Folder is empty!");
            }

        } catch (Exception e) {
            listener.getLogger().printf("Error Occurred : %s ", e);
        }
        listener.getLogger().println("Finished Post Build Action");
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public CucumberGlobalConfiguration getDescriptor() {
        return (CucumberGlobalConfiguration) super.getDescriptor();
    }

    private HttpResponse sendXMLContent(String content, AbstractBuild build) throws IOException {
        AuthenticationInfo authenticationInfo = getDescriptor().getAuthenticationInfo();

        Cause cause = (Cause) build.getCauses().get(0);
        String userCause = ((Cause.UserIdCause) cause).getUserId();

        JSONArray jenkinsAttributes = new JSONArray();
        JSONObject jenkinsAttr = new JSONObject();
        jenkinsAttr.put("build_number", build.number);
        jenkinsAttr.put("build_status", Objects.requireNonNull(build.getResult()).toString().toLowerCase());
        jenkinsAttr.put("user", userCause);
        jenkinsAttr.put("report_type", reportFormat);
        jenkinsAttributes.add(jenkinsAttr);

        JSONObject data = new JSONObject();
        data.put("username", authenticationInfo.getUsername());
        data.put("service_name", SERVICE_NAME);
        data.put("token", authenticationInfo.getToken());
        data.put("project_name", build.getProject().getName());
        data.put("jenkins_auto_executions_attributes", jenkinsAttributes);
        data.put("ticket_title", ticketTitle);
        data.put("cycle_name", cycleName);
        data.put("json_result", content);
        RequestAPI requestAPI = new RequestAPI(itmsAddress);
        return requestAPI.createPOSTRequest(data);
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        Files.lines(Paths.get(file.toString()),
                StandardCharsets.UTF_8).forEach(content::append);
        return content.toString();
    }

    private String sendReportContent (String content, AbstractBuild build) throws IOException {
        String responseStr = "";
        if (content.length() > 0) {
            HttpResponse response = sendXMLContent(content, build);
            responseStr = "Cucumber plugin response: " + response.getStatusLine().getReasonPhrase();
        } else {
            responseStr = "Report file(s) is empty!";
        }
        return responseStr;
    }

    public String getItmsAddress() {
        return itmsAddress;
    }

    public String getReportFolder() {
        return reportFolder;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    public String getTicketTitle() {
        return ticketTitle;
    }

    public String getCycleName() {
        return cycleName;
    }

}