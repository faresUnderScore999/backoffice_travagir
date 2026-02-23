package java_project.controllers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.Spark;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PdfApiIntegrationTest {

    private static int port;

    @BeforeAll
    public static void startServer() throws Exception {
        // Ensure the SessionManager has the expected token for auth checks
        java_project.utils.SessionManager.getInstance().setSession("test-token", "rt", null);

        // find a free port
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        PdfApiServer.startServer(port);
        Spark.awaitInitialization();
    }

    @AfterAll
    public static void stopServer() throws Exception {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    public void exportEndpoint_returnsPdf() throws Exception {
        URL url = new URL("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/api/pdf/export");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer test-token");

        String body = "{\"templateName\":\"sample\",\"data\":{\"title\":\"Integration\",\"body\":\"Integration test PDF\"}}";
        conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

        int code = conn.getResponseCode();
        assertEquals(200, code);

        String contentType = conn.getHeaderField("Content-Type");
        assertTrue(contentType != null && contentType.contains("application/pdf"));

        try (InputStream in = conn.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) baos.write(buf, 0, r);
            byte[] pdf = baos.toByteArray();
            assertTrue(pdf.length > 100);
        }
    }
}
