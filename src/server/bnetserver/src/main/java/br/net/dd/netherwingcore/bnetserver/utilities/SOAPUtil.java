package br.net.dd.netherwingcore.bnetserver.utilities;

import br.net.dd.netherwingcore.common.utilities.Util;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class SOAPUtil {

    public static void sendJson(HttpExchange exchange, String json, int status) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json");
        byte[] resp = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, resp.length);
        OutputStream os = exchange.getResponseBody();
        os.write(resp);
        os.close();
    }

    public static String getTicketFromHeader(HttpExchange exchange) {
        List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearer = authHeaders.getFirst();
            if (bearer.startsWith("Bearer ")) return bearer.substring(7);
        }
        return null;
    }

    public static String calculateShaPassHash(String name, String password) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] nameDigest = sha256.digest(name.getBytes(StandardCharsets.UTF_8));
            String nameHex = Util.bytesToHex(nameDigest);
            String concat = nameHex + ":" + password;
            byte[] hash = sha256.digest(concat.getBytes(StandardCharsets.UTF_8));
            return Util.bytesToHex(hash).toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }

}
