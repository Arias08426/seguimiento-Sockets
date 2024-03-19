import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket; // Socket del servidor
    private Socket socket; // Socket para la conexión con el cliente
    private DataInputStream inputStream = null; // recibir datos del cliente
    private DataOutputStream outputStream = null; //  enviar datos al cliente
    private Scanner scanner = new Scanner(System.in); // leer la entrada del servidor desde la consola
    final String TERMINATION_COMMAND = "exit"; // Comando para terminar la conexión con el cliente

    // Método para establecer la conexión con un cliente
    public void establishConnection(int port) {
        try {
            serverSocket = new ServerSocket(port); // Crear el socket del servidor en el puerto especificado
            System.out.println("Waiting for incoming connection on port " + port + "..."); // Esperar conexión entrante
            socket = serverSocket.accept(); // Aceptar la conexión entrante del cliente
            System.out.println("Connection established with: " + socket.getInetAddress().getHostName() + "\n"); // Mostrar que la conexión se ha establecido con éxito
        } catch (IOException e) {
            System.out.println("Error in establishConnection(): " + e.getMessage()); // Manejar errores de conexión
            System.exit(1); // Salir del programa si hay un error
        }
    }

    // Método para comunicarse con el cliente
    public void openStreams() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.flush(); // Vaciar el buffer de salida
        } catch (IOException e) {
            System.out.println("Error opening streams"); // Manejar errores al abrir los streams
        }
    }

    // Método para recibir datos del cliente
    public void receiveData() {
        try {
            String message = "";
            do {
                message = inputStream.readUTF(); // Leer un mensaje del cliente
                System.out.println("[Client] => " + message); // Mostrar el mensaje recibido del cliente
            } while (!message.equals(TERMINATION_COMMAND)); // Seguir recibiendo mensajes hasta que se reciba el comando de terminación
        } catch (IOException e) {
            System.out.println("IOException while receiving data"); // Manejar errores al recibir datos
            closeConnection(); // Cerrar la conexión si hay un error
        }
    }

    // Método para enviar un mensaje al cliente
    public void send(String message) {
        try {
            outputStream.writeUTF(message); // Escribir el mensaje en el stream de salida
            outputStream.flush(); // Vaciar el buffer de salida para asegurarse de que el mensaje se envíe
        } catch (IOException e) {
            System.out.println("Error in send(): " + e.getMessage());
        }
    }

    // Método para que el servidor escriba y envíe datos al cliente
    public void writeData() {
        while (true) {
            System.out.print("[You] => "); // Mostrar el prompt para que el servidor escriba
            send(scanner.nextLine()); // Leer la entrada del servidor y enviarla al cliente
        }
    }

    // Método para cerrar la conexión con el cliente
    public void closeConnection() {
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
            System.out.println("Connection closed"); // Mostrar que la conexión ha sido cerrada
        } catch (IOException e) {
            System.out.println("Exception in closeConnection(): " + e.getMessage()); // Manejar errores al cerrar la conexión
        } finally {
            System.exit(0); // Salir del programa
        }
    }

    // Método para ejecutar la conexión en un hilo separado
    public void executeConnection(int port) {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    establishConnection(port); // Establecer conexión con el cliente
                    openStreams(); // Abrir los streams de entrada y salida
                    receiveData(); // Comenzar a recibir datos del cliente
                } finally {
                    closeConnection(); // Cerrar la conexión al finalizar
                }
            }
        });
        thread.start(); // Iniciar el hilo
    }

    // Método principal del programa
    public static void main(String[] args) {
        Server server = new Server();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter port (8080 by default): "); // Solicitar al usuario que ingrese el puerto del servidor
        String portStr = scanner.nextLine(); // Leer el puerto del usuario
        int port = portStr.isEmpty() ? 8080 : Integer.parseInt(portStr); // Utilizar el puerto 8080 como valor predeterminado si no se proporciona ningún puerto válido

        server.executeConnection(port); // Iniciar la conexión con el cliente
        server.writeData(); // Permitir al servidor escribir y enviar mensajes al cliente
    }
}
