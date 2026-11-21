// Algoritmo: decide si una placa es de carro o de moto
public class Clasificador {

    // Deja la placa en mayúsculas y sin espacios ni guiones
    public static String limpiar(String placa) {
        return placa.toUpperCase().replace(" ", "").replace("-", "");
    }

    // Devuelve "Moto", "Carro" o null si la placa no es válida
    public static String identificar(String placa) {
        placa = limpiar(placa);

        // Moto: 3 letras + 2 números + 1 letra (ej: ABC12D)
        if (placa.matches("[A-Z]{3}[0-9]{2}[A-Z]")) {
            return "Moto";
        }
        // Carro: 3 letras + 3 números (ej: ABC123)
        if (placa.matches("[A-Z]{3}[0-9]{3}")) {
            return "Carro";
        }
        return null;
    }
}
