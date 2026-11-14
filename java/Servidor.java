import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Servidor {

    // Aquí se guardan los vehículos registrados
    static List<String[]> vehiculos = new ArrayList<String[]>();
    static String mensaje = "";

    public static void main(String[] args) throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/", Servidor::mostrarPagina);
        servidor.createContext("/registrar", Servidor::registrar);
        servidor.createContext("/css/estilos.css", Servidor::enviarCss);

        servidor.start();
        System.out.println("Servidor listo en http://localhost:8080");
    }

    // Lee index.html y reemplaza MENSAJE y FILAS con los datos
    static void mostrarPagina(HttpExchange ex) throws IOException {
        String html = new String(Files.readAllBytes(Paths.get("html/index.html")), StandardCharsets.UTF_8);

        StringBuilder filas = new StringBuilder();
        for (String[] v : vehiculos) {
            filas.append("<tr><td>").append(v[0]).append("</td><td>")
                    .append(v[1]).append("</td><td>").append(v[2]).append("</td></tr>");
        }

        html = html.replace("MENSAJE", mensaje).replace("FILAS", filas.toString());
        responder(ex, html, "text/html; charset=UTF-8");
    }

    // Recibe la placa y clasifica
    static void registrar(HttpExchange ex) throws IOException {
        String cuerpo;
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8))) {
            cuerpo = lector.readLine();
        }
        String placa = URLDecoder.decode(cuerpo.split("=", 2)[1], "UTF-8");

        String tipo = Clasificador.identificar(placa);
        if (tipo == null) {
            mensaje = "Placa no válida. Use 3 letras + 3 números (carro) o 3 letras + 2 números + 1 letra (moto).";
        } else {
            String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
            vehiculos.add(new String[] { Clasificador.limpiar(placa), tipo, hora });
            mensaje = "Registrado: " + Clasificador.limpiar(placa) + " es " + tipo;
        }

        // volver pagina inicial
        ex.getResponseHeaders().add("Location", "/");
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    static void enviarCss(HttpExchange ex) throws IOException {
        String css = new String(Files.readAllBytes(Paths.get("css/estilos.css")), StandardCharsets.UTF_8);
        responder(ex, css, "text/css; charset=UTF-8");
    }

    static void responder(HttpExchange ex, String texto, String tipo) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", tipo);
        ex.sendResponseHeaders(200, bytes.length);
        OutputStream salida = ex.getResponseBody();
        salida.write(bytes);
        salida.close();
    }
}
