package strings;

public class Information {
    public static final String SERVER_STARTED = "\nServer started.\nWaiting for response from client...\n";
    public static final String CLIENT_CONNECTED = "Client connected.";
    public static final String CLIENT_NOT_CONNECTED = "Client sent invalid connection command.";
    public static final String UNKNOWN_COMMAND = "Received unknown command from client.";
    public static final String CLIENT_DISCONNECTED_AND_NOTIFIED = "Client has disconnected from server.";
    public static final String CLIENT_DISCONNECTED_NO_NOTIFICATION = "Client disconnected without notification.";
    public static final String PIN_NOT_TRIGGERED = "Pin was not triggered since system is on.";
    public static final String CLIENT_COMMAND = "Command from client: ";
    public static final String CLIENT_ACTION_INVALID_IO = "Client trying to make action on invalid I/O type. Ignoring command.";
    public static final String CLIENT_ACTION_INVALID_PIN = "Client trying to make action on invalid pin. Ignoring command.";
    public static final String WRONG_PIN_TYPE = "Wrong pin type for GPIO input.";
    public static final String VALUE_FROM_GPIO = "Value from GPIO to pin ";
    public static final String I2C_ON_BUS = "I2C value currently on bus: ";
    public static final String SPI_ON_BUS = "SPI value currently on bus: ";
    public static final String UART_NOT_SUPPORTED = "Pin type is UART. UART bus is not supported yet.";
    public static final String ACCEPTING_BYTE = "Accepting byte: ";
    public static final String BUS_MESSAGE_WROTE_SUCCESSFULLY = " message wrote successfully. Receiving status.";
    public static final String BUS_NOT_SUPPORTED_OR_DISABLED = " bus not supported in system, or is disabled. No message sent.";
}
