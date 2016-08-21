package managers;

import java.io.IOException;

import io.silverspoon.bulldog.core.io.bus.i2c.I2cBus;
import io.silverspoon.bulldog.core.platform.Board;
import io.silverspoon.bulldog.core.io.bus.i2c.I2cConnection;

public class I2CManager {

    private final static int RECEIVE_LENGTH = 40;

    private final I2cBus i2c;
    private I2cConnection connection;

    public I2CManager(Board board, String address) {
        i2c = board.getI2cBuses().get(0);
        connection = i2c.createI2cConnection(Byte.decode(address));
    }

    public void sendI2CMessage(String message)  {
        byte[] requestBuffer = new byte[message.length() / 2];
        for (int i = 0; i < requestBuffer.length; i++) {
            String value = "0x" + message.substring(2 * i, 2 * (i + 1));
            requestBuffer[i] = Integer.decode(value).byteValue();
            System.out.println("Accepting byte: " + value);
        }
        // TODO: 21.8.2016 Tu to padne ked neni nic pripojene - zistit exception
        try {
            connection.writeBytes(requestBuffer);
            System.out.println("I2C message sent succesfully. Receiving status.");
        } catch (IOException e) {
            System.out.println(e + ": I2C bus probably not connected to system. No message sent.");
        }
    }

    public String receiveI2CMessage() {
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
            System.out.println(e + ": I2C bus probably not connected to system. No message received.");
        }
        return "No message received from I2C bus.";
    }
}
