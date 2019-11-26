package io.jenkins.plugins;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import io.jenkins.plugins.model.AuthenticationInfo;
import io.jenkins.plugins.rest.RequestAPI;
import io.jenkins.plugins.rest.StandardResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static io.jenkins.plugins.model.ITMSConsts.*;


public class CucumberPostBuild extends Notifier {

    private final String itmsAddress;
    private final String reportFolder;
    private final String reportFormat;
    private final String jiraProjectKey;
    private final String jiraTicketKey;
    private final String itmsCycleName;

    @DataBoundConstructor
    public CucumberPostBuild(final String itmsAddress, final String reportFolder,
                             final String reportFormat, final String jiraProjectKey,
                             final String jiraTicketKey, final String itmsCycleName) {
        this.itmsAddress = itmsAddress.trim();
        this.reportFolder = reportFolder.trim();
        this.reportFormat = reportFormat.trim();
        this.jiraProjectKey = jiraProjectKey.trim();
        this.jiraTicketKey = jiraTicketKey.trim();
        this.itmsCycleName = itmsCycleName.trim();
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
                    String content;
                    if (reportFormat.equals(JSON_FORMAT) && file.getName().toLowerCase().endsWith(".json")) {
                        counter++;
                        listener.getLogger().println("Read report file: " + file.getName());
                        content = readFileContent(file);
                        listener.getLogger().println(sendReportContent(file.getName(), content, build));
                    } else if (reportFormat.equals(XML_FORMAT) && file.getName().toLowerCase().endsWith(".xml")) {
                        counter++;
                        listener.getLogger().println("Read report file: " + file.getName());
                        content = readFileContent(file);
                        listener.getLogger().println(sendReportContent(file.getName(), content, build));
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


    private StandardResponse sendXMLContent(String content, AbstractBuild build) {
        AuthenticationInfo authenticationInfo = getDescriptor().getAuthenticationInfo();

//        Cause cause = (Cause) build.getCauses().get(0);
//        String userCause = ((Cause.UserIdCause) cause).getUserId();

        JSONArray jenkinsAttributes = new JSONArray();
        JSONObject jenkinsAttr = new JSONObject();
        jenkinsAttr.put("build_number", build.number);
        jenkinsAttr.put("build_status", Objects.requireNonNull(build.getResult()).toString().toLowerCase());
        jenkinsAttr.put("user", authenticationInfo.getUsername());
        jenkinsAttr.put("report_type", reportFormat);
        jenkinsAttributes.add(jenkinsAttr);

        JSONObject data = new JSONObject();
        data.put("username", authenticationInfo.getUsername());
        data.put("service_name", SERVICE_NAME);
        data.put("token", authenticationInfo.getToken());
        data.put("project_name", jiraProjectKey);
        data.put("jenkins_auto_executions_attributes", jenkinsAttributes);
        data.put("ticket_key", jiraTicketKey);
        data.put("cycle_name", itmsCycleName);
        data.put("is_json", reportFormat.equals(JSON_FORMAT)? Boolean.TRUE : Boolean.FALSE);
        data.put("report_content", content);
        RequestAPI requestAPI = new RequestAPI(itmsAddress);
        return requestAPI.createPOSTRequest(data);
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        Files.lines(Paths.get(file.toString()),
                StandardCharsets.UTF_8).forEach(content::append);
        return content.toString();
    }

    private String sendReportContent(String fileName, String content, AbstractBuild build) {
        if (content.length() > 0) {
            StandardResponse response = sendXMLContent(content, build);
            return "JUnit Cucumber response: " + response.toString();
        }
        return fileName + " is empty!";
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

    public String getJiraTicketKey() {
        return jiraTicketKey;
    }

    public String getItmsCycleName() {
        return itmsCycleName;
    }

    public String getJiraProjectKey() {
        return jiraProjectKey;
    }

}