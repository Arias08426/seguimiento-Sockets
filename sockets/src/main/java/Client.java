import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket; // Socket para la conexión con el servidor
    private DataInputStream inputStream = null; // se utiliza para recibir datos del servidor
    private DataOutputStream outputStream = null; // se usa para enviar datos al servidor
    private Scanner scanner = new Scanner(System.in); // se usa para leer la entrada del usuario desde la consola
    final String TERMINATION_COMMAND = "exit"; // Comando para terminar la conexión con el servidor

    // Método para establecer la conexión con el servidor
    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port); // Crear un nuevo socket y conectar al servidor
            System.out.println("Connected to: " + socket.getInetAddress().getHostName()); // Mostrar que se ha conectado al servidor
        } catch (IOException e) {
            System.out.println("Exception while connecting: " + e.getMessage());
            System.exit(1); // Salir del programa si hay un error de conexión
        }
    }

    // Método para comunicación con el servidor
    public void openStreams() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Error opening streams"); // Manejar excepciones de apertura de streams
        }
    }

    // Método para enviar un mensaje al servidor
    public void send(String message) {
        try {
            outputStream.writeUTF(message); // Escribir el mensaje salida
            outputStream.flush(); // Vaciar el buffer de salida para asegurarse de que el mensaje se envíe
        } catch (IOException e) {
            System.out.println("IOException while sending message");
        }
    }

    // Método para cerrar la conexión con el servidor
    public void closeConnection() {
        try {
            inputStream.close(); // Cerrar stream de entrada
            outputStream.close(); // Cerrar stream de salida
            socket.close(); // Cerrar el socket
            System.out.println("Connection terminated"); // Mostrar que la conexión ha sido terminada
        } catch (IOException e) {
            System.out.println("IOException while closing connection"); // Manejar excepciones al cerrar la conexión
        } finally {
            System.exit(0); // Salir del programa
        }
    }

    // Método para ejecutar la conexión en un hilo separado
    public void executeConnection(String ip, int port) {
        Thread thread = new Thread(() -> {
            try {
                connect(ip, port); // Establecer conexión con el servidor
                openStreams(); // Abrir los streams de entrada y salida
                receiveData(); // Comenzar a recibir datos del servidor
            } finally {
                closeConnection(); // Cerrar la conexión al finalizar
            }
        });
        thread.start(); // Iniciar el hilo
    }

    // Método para recibir datos del servidor
    public void receiveData() {
        try {
            String message = ""; // Variable para almacenar los mensajes recibidos del servidor
            do {
                message = inputStream.readUTF(); // Leer un mensaje del stream de entrada
                System.out.println("[Server] => " + message); // Mostrar el mensaje recibido del servidor
            } while (!message.equals(TERMINATION_COMMAND)); // Continuar recibiendo mensajes hasta que se reciba el comando de terminación
        } catch (IOException e) {
            System.out.println("IOException while receiving data");
        }
    }

    // Método para que el usuario escriba y envíe datos al servidor
    public void writeData() {
        String input = ""; // Variable para almacenar la entrada del usuario
        while (true) {
            System.out.print("[You] => "); // Mostrar el prompt para que el usuario escriba
            input = scanner.nextLine(); // Leer la entrada del usuario desde la consola
            if (input.length() > 0) // Verificar que la entrada no esté vacía
                send(input); // Enviar la entrada al servidor
        }
    }

    // Método principal del programa
    public static void main(String[] args) {
        Client client = new Client();
        Scanner scanner = new Scanner(System.in); // Crear un nuevo scanner para leer la entrada del usuario
        System.out.print("Enter IP (localhost by default): "); // Solicitar al usuario que ingrese la dirección IP del servidor
        String ip = scanner.nextLine();
        if (ip.isEmpty()) ip = "localhost"; // Utilizar "localhost" como valor predeterminado si no se proporciona ninguna dirección IP

        System.out.print("Enter port (8080 by default): "); // Solicitar al usuario que ingrese el puerto del servidor
        String portStr = scanner.nextLine();
        int port = portStr.isEmpty() ? 8080 : Integer.parseInt(portStr); // Utilizar el puerto 8080 como valor predeterminado si no se proporciona ningún puerto válido

        client.executeConnection(ip, port); // Iniciar la conexión con el servidor
        client.writeData(); // Permitir al usuario escribir y enviar mensajes al servidor
    }
}
