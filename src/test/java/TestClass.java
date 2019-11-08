import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestClass {
    @Test
    public void readXML() throws ParserConfigurationException, IOException, SAXException {
        File file = new File("D:\\Software\\Jenkins\\workspace\\First project\\target\\surefire-reports\\TEST-InjectedTest.xml");
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
//                .newInstance();
//        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//        Document document = documentBuilder.parse(file);
//        document.getDocumentElement().normalize();
//        document.getDocumentElement().getNodeName();
        StringBuilder content = new StringBuilder();
        Files.lines(Paths.get("D:\\Software\\Jenkins\\workspace\\First project\\target\\surefire-reports\\TEST-InjectedTest.xml"),
                StandardCharsets.UTF_8).forEach(content::append);
        System.out.print(content.toString());
//        Assert.assertEquals("asdfsdf", content.toString());
    }
}
