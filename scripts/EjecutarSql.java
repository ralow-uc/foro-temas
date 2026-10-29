import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Ejecuta un archivo .sql contra Oracle por JDBC.
 *
 * Existe porque en el equipo de desarrollo no hay instalados sqlplus ni sqlcl.
 * Cumple la misma funcion: leer el script, partirlo en sentencias y ejecutarlas
 * en orden, mostrando el resultado de los SELECT.
 *
 * Uso:
 *   java --class-path <ojdbc11.jar>:<oraclepki.jar> EjecutarSql.java \
 *        <jdbcUrl> <usuario> <contrasena> <archivo.sql> [--continuar-con-errores]
 */
public class EjecutarSql {

    public static void main(String[] args) throws IOException, SQLException {
        if (args.length < 4) {
            System.err.println("Uso: EjecutarSql <jdbcUrl> <usuario> <contrasena> <archivo.sql> [--continuar-con-errores]");
            System.exit(2);
        }
        String url = args[0];
        String usuario = args[1];
        String contrasena = args[2];
        Path archivo = Path.of(args[3]);
        boolean continuarConErrores = args.length > 4 && "--continuar-con-errores".equals(args[4]);

        List<String> sentencias = separarSentencias(Files.readString(archivo));
        System.out.println("Archivo   : " + archivo.getFileName());
        System.out.println("Usuario   : " + usuario);
        System.out.println("Sentencias: " + sentencias.size());
        System.out.println("-".repeat(70));

        int fallidas = 0;
        try (Connection conexion = DriverManager.getConnection(url, usuario, contrasena)) {
            for (String sentencia : sentencias) {
                String resumen = resumir(sentencia);
                try (Statement st = conexion.createStatement()) {
                    boolean hayResultado = st.execute(sentencia);
                    if (hayResultado) {
                        try (ResultSet rs = st.getResultSet()) {
                            System.out.println("OK   " + resumen);
                            imprimir(rs);
                        }
                    } else {
                        int filas = st.getUpdateCount();
                        System.out.println("OK   " + resumen + (filas > 0 ? "   (" + filas + " filas)" : ""));
                    }
                } catch (SQLException e) {
                    fallidas++;
                    System.out.println("FALLA " + resumen);
                    System.out.println("      " + e.getMessage().strip());
                    if (!continuarConErrores) {
                        throw e;
                    }
                }
            }
        }
        System.out.println("-".repeat(70));
        System.out.println(fallidas == 0
                ? "Todas las sentencias se ejecutaron sin errores."
                : "Sentencias con error: " + fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    /**
     * Parte el script en sentencias usando el punto y coma como separador,
     * ignorando los comentarios de linea que empiezan con "--".
     */
    private static List<String> separarSentencias(String script) {
        List<String> sentencias = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String linea : script.split("\n")) {
            String limpia = linea.strip();
            if (limpia.isEmpty() || limpia.startsWith("--")) {
                continue;
            }
            actual.append(linea).append("\n");
            if (limpia.endsWith(";")) {
                String sentencia = actual.toString().strip();
                sentencias.add(sentencia.substring(0, sentencia.length() - 1).strip());
                actual.setLength(0);
            }
        }
        if (!actual.toString().isBlank()) {
            sentencias.add(actual.toString().strip());
        }
        return sentencias;
    }

    private static String resumir(String sentencia) {
        String plano = sentencia.replaceAll("\\s+", " ").strip();
        return plano.length() > 60 ? plano.substring(0, 60) + "..." : plano;
    }

    private static void imprimir(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnas = meta.getColumnCount();
        StringBuilder cabecera = new StringBuilder("      ");
        for (int i = 1; i <= columnas; i++) {
            cabecera.append(meta.getColumnLabel(i)).append(i < columnas ? " | " : "");
        }
        System.out.println(cabecera);
        int filas = 0;
        while (rs.next()) {
            StringBuilder fila = new StringBuilder("      ");
            for (int i = 1; i <= columnas; i++) {
                fila.append(rs.getString(i)).append(i < columnas ? " | " : "");
            }
            System.out.println(fila);
            filas++;
        }
        System.out.println("      (" + filas + " filas)");
    }
}
