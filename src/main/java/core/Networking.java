package core;

import hashmaps.RaspberryHashMap;
import io.silverspoon.bulldog.core.gpio.DigitalIO;
import io.silverspoon.bulldog.core.pin.Pin;
import io.silverspoon.bulldog.core.platform.Board;
import managers.GpioManager;
import managers.I2CManager;
import managers.SPIManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

class Networking {

    private final static int SOCKET_PORT = 18924;
    private final static int HIGH_VALUE = 1;
    private final static int LOW_VALUE = 0;

    private static boolean connected = false;

    private Board board;
    private PrintWriter out;
    private BufferedReader in;
    private RaspberryHashMap piMap;
    private ServerSocket server;

    Networking() throws IOException {
        server = new ServerSocket(SOCKET_PORT);
        board = Main.board;
        piMap = new RaspberryHashMap();
    }

    void listenSocket() {
        System.out.println("\nServer started.\nWaiting for response from client...\n");
        new Thread(new Runnable() {
            public void run() {
                String input;
                while (true) {
                    try {
                        Socket client = server.accept();
                        client.setSoTimeout(6000000);
                        out = new PrintWriter(client.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        System.out.println("Client connected.");
                        while ((input = in.readLine()) != null) {
                            if (input.equals("Connect")) {
                                connected = true;
                                out.println("connected to server.");
                            } else if (input.equals("Disconnect") && connected) {
                                connected = false;
                                out.println("disconnected from server.");
                            } else if (!(input.equals("Connect") || input.equals("Disconnect")) && connected) {
                                if (isEmbeddedCommand(input)) {
//									sendParsedData(input);
                                    manageCommand(input);
                                } else if (isRequestToSendAll(input)) {
                                    sendAllPinStatus(input);
                                }
                            }
                        }
                        System.out.println("Client disconnected.");
                    } catch (IOException e) {
                        System.out.println("Client disconnected without notification.");
                        connected = false;
                    }
                }
            }
        }).start();
    }

    private void sendAllPinStatus(String input) {
        out.print("START;");
        RequestedPinsParser pinParser = new RequestedPinsParser(input.substring(25));
        ArrayList<String> pinsToSend = pinParser.getPinsToSend();
        System.out.println("Pins to send to client: " + pinsToSend);
        if (pinsToSend != null && !pinsToSend.isEmpty()) {
            for (String pinNumberString : pinsToSend) {
                if (!pinNumberString.isEmpty()) {
                    Integer pinNumberInt = Integer.valueOf(pinNumberString);
                    if (pinNumberString.length() == 1) {
                        pinNumberString = "0" + pinNumberString;
                    }
                    Pin physicalPin = null;
                    for (Pin p : board.getPins()) {
                        if (p.getIndexOnPort() == pinNumberInt) {
                            physicalPin = p;
                        }
                    }
                    if (physicalPin != null) {
                        DigitalIO digitalIO = physicalPin.as(DigitalIO.class);
                        if (digitalIO.isOutputActive()) {
                            if (digitalIO.isHigh()) {
                                System.out.println("Sending output high value");
                                out.print(getDateAndTime() + getPinType(digitalIO) + ":O" + pinNumberString + HIGH_VALUE + ";");
                            } else {
                                System.out.println("Sending output low value");
                                out.print(getDateAndTime() + getPinType(digitalIO) + ":O" + pinNumberString + LOW_VALUE + ";");
                            }
                        } else if (digitalIO.isInputActive()) {
                            if (digitalIO.isHigh()) {
                                System.out.println("Sending input high value");
                                out.print(getDateAndTime() + getPinType(digitalIO) + ":I" + pinNumberString + HIGH_VALUE + ";");
                            } else {
                                System.out.println("Sending input low value");
                                out.print(getDateAndTime() + getPinType(digitalIO) + ":I" + pinNumberString + LOW_VALUE + ";");
                            }
                        } else {
                            System.out.println("Pin was not triggered since system is on. Nothing sent.");
                        }
                    }

                }
            }
            out.println("END");
        }
    }

    private void sendParsedData(String input) {
        String separator = "|";
        RequestParser parser = new RequestParser(input);
        out.println("Day:" + parser.getDay() + separator + "Month:" + parser.getMonth() + separator + "Year:"
                + parser.getYear() + separator + "Hour:" + parser.getHour() + separator + "Minute:"
                + parser.getMinute() + separator + "Second:" + parser.getSecond() + separator + "I/O Type:"
                + parser.getIoType() + separator + "Pin Type:" + parser.getPinType() + separator + "Pin Number:"
                + parser.getPinNumber() + separator + "Value:" + parser.getValue());
    }

    private void manageCommand(String input) {
        RequestParser parser = new RequestParser(input);
        String[] pinTypes = piMap.getValueByKey(Integer.valueOf(parser.getPinNumber()));
        if (pinTypes != null) {
            System.out.println("COMMAND FROM CLIENT: " + input);
            if (parser.getPinType().equals("GPIO")) {
                GpioManager gpio = new GpioManager();
                int setValue = gpio.toggleLed(board, pinTypes[0]);
                System.out.println("Value from GPIO to pin " + parser.getPinNumber() + " set to: " + setValue);
                out.println("Value on GPIO pin " + parser.getPinNumber() + " set to: " + setValue);
            } else if (parser.getPinType().equals("I2C")) {
                System.out.println("Pin type is I2C");
                String hexAddress = parser.getValue().substring(0, 4);
                String message = parser.getValue().substring(4);
                System.out.println("hexa address: " + hexAddress);
                System.out.println("message " + message);
                I2CManager i2c = new I2CManager(board, hexAddress);
                i2c.sendI2CMessage(message);
                String i2cResponse = i2c.receiveI2CMessage();
                System.out.println("I2C value currently on bus: " + i2cResponse);
                out.println("Value on I2C bus set to: " + i2cResponse);
            } else if (parser.getPinType().equals("SPI")) {
                System.out.println("Pin type is SPI. SPI bus is not supported yet.");
                String hexAddress = parser.getValue().substring(0, 4);
                String message = parser.getValue().substring(4);
                SPIManager spi = new SPIManager(board, hexAddress);
                spi.sendSpiMessage(message);
                String spiResponse = spi.receiveSpiMessage();
                System.out.println("SPI value currently on bus: " + spiResponse);
                out.println("Value on I2C bus set to: " + spiResponse);
            } else if (parser.getPinType().equals("UART")) {
                System.out.println("Pin type is UART. UART bus is not supported yet.");
            } else {
                out.println("Command not recognized");
            }
        } else {
            System.out.println("Client trying to make action on invalid pin. Ignoring command");
            out.println("Trying to access invalid pin on currently selected system.");
        }
    }

    private String getPinType(DigitalIO digitalIO) {
        return "GPIO";
    }

    private String getDateAndTime() {
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    private boolean isRequestToSendAll(String input) {
        return input.length() > 20 && input.substring(14).startsWith("REQUEST:990");
    }

    private boolean isEmbeddedCommand(String input) {
        return input.length() > 15
                && (input.contains("GPIO:") || input.contains("SPI:") || input.contains("I2C:") || input
                .contains("UART:"));
    }
}
