package managers;

import io.silverspoon.bulldog.core.io.bus.spi.SpiBus;
import io.silverspoon.bulldog.core.io.bus.spi.SpiConnection;
import io.silverspoon.bulldog.core.platform.Board;

import java.io.IOException;

public class SPIManager {

    private final static int RECEIVE_LENGTH = 40;

    private final SpiBus spi;
    private SpiConnection connection;

    public SPIManager(Board board, String address) {
        spi = board.getSpiBuses().get(0);
        connection = spi.createSpiConnection(Byte.decode(address));
    }

    public void sendSpiMessage(String message) {
        try {
            byte[] requestBuffer = new byte[message.length() / 2];
            for (int i = 0; i < requestBuffer.length; i++) {
                String value = "0x" + message.substring(2 * i, 2 * (i + 1));
                requestBuffer[i] = Integer.decode(value).byteValue();
                System.out.println("Accepting byte: " + value);
            }
            connection.writeBytes(requestBuffer);
            System.out.println("I2C message sent succesfully. Receiving status.");
        } catch (IOException e) {
            System.out.println(e + ": SPI bus probably not connected to system. No message sent.");
        }
    }

    public String receiveSpiMessage() {
        try {
            byte[] responseBuffer = new byte[RECEIVE_LENGTH];
            int count = connection.readBytes(responseBuffer);
            StringBuilder response = new StringBuilder();
            for (int i = 0; i < count && i < RECEIVE_LENGTH; i++) {
                response.append(Integer.toHexString(responseBuffer[i]));
            }
            if (response.toString().length() > 0) {
                return response.toString();
            }
        } catch (IOException e) {
            System.out.println(e + ": SPI bus probably not connected to system. No message received.");
        }
        return "N/A";
    }
}
