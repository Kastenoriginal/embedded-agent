package core;

import hashmaps.RaspberryHashMap;
import io.silverspoon.bulldog.core.gpio.DigitalIO;
import io.silverspoon.bulldog.core.pin.Pin;
import io.silverspoon.bulldog.core.platform.Board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import managers.GpioManager;
import managers.I2CManager;
import managers.SPIManager;

public class Networking {

	private static boolean connected = false;
	private static final int HIGH_VALUE = 1;
	private static final int LOW_VALUE = 0;
	private Board board;
	private PrintWriter out;
	private BufferedReader in;
	private RaspberryHashMap piMap;
	private ServerSocket server;

	public Networking() throws IOException {
		server = new ServerSocket(Main.SOCKET_PORT);
		board = Main.board;
		piMap = new RaspberryHashMap();
	}

	public void listenSocket() {
		new Thread(new Runnable() {
			public void run() {
				String input = "";
				while (true) {
					try {
						Socket client = server.accept();
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
									sendParsedData(input);
									manageCommand(input);
								} else if (isRequestToSendAll(input)) {
									sendAllPinStatus(input);
								} else {
								}
							}
						}
						System.out.println("Client Disconnected.");
					} catch (IOException e) {
						System.out.println("Client is unavalable. " + e);
						connected = false;
					}
				}
			}
		}).start();
	}

	private void sendAllPinStatus(String input) {
		out.print("START;");
		RequestedPinsParser pinParser = new RequestedPinsParser(input.substring(25));
		System.out.println("Pins to Send to client: " + pinParser.getPinsToSend());
		for (String pinNumberString : pinParser.getPinsToSend()) {
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
						System.out.println("SENDING 1");
						out.print(getDateAndTime() + getPinType(digitalIO) + ":O" + pinNumberString + HIGH_VALUE + ";");
					} else {
						System.out.println("SENDING 2");
						out.print(getDateAndTime() + getPinType(digitalIO) + ":O" + pinNumberString + LOW_VALUE + ";");
					}
				} else if (digitalIO.isInputActive()) {
					if (digitalIO.isHigh()) {
						System.out.println("SENDING 3");
						out.print(getDateAndTime() + getPinType(digitalIO) + ":I" + pinNumberString + HIGH_VALUE + ";");
					} else {
						System.out.println("SENDING 4");
						out.print(getDateAndTime() + getPinType(digitalIO) + ":I" + pinNumberString + LOW_VALUE + ";");
					}
				} else {
					System.out.println("Neposlalo sa nic");
				}
			}
		}
		out.println("END");
	}

	private void sendParsedData(String input) {
		String separator = "|";
		RequestParser parser = new RequestParser(input);
//		parser.parseRequest(input);
		out.println("Day:" + parser.getDay() + separator + "Month:" + parser.getMonth() + separator + "Year:"
				+ parser.getYear() + separator + "Hour:" + parser.getHour() + separator + "Minute:"
				+ parser.getMinute() + separator + "Second:" + parser.getSecond() + separator + "Pin Type:"
				+ parser.getPinType() + separator + "Pin Number:" + parser.getPinNumber() + separator + "Value:"
				+ parser.getValue());
	}

	private void manageCommand(String input) {
		RequestParser parser = new RequestParser(input);
//		parser.parseRequest(input);

		GpioManager gpio = new GpioManager();

		String[] pinTypes = piMap.getValueByKey(Integer.valueOf(parser.getPinNumber()));
		System.out.println("COMMAND FROM CLIENT: " + input);
		System.out.println("PIN TYPE FROM COMMAND IS: " + parser.getPinType());
		if (parser.getPinType().equals("GPIO")) {
			System.out.println("Value from GUI to pin " + parser.getPinNumber() + ": " + parser.getValue());
			out.println("Value on GPIO pin " + parser.getPinNumber() + " set to: " + gpio.toggleLed(board, pinTypes[0]));
            // TODO: 20.8.2016 out.println that gpio has successfully set to value
        } else if (parser.getPinType().equals("I2C")) {
			System.out.println("Pin type is I2C");
			String hexAddress = parser.getValue().substring(0, 4);
			String message = parser.getValue().substring(4);
			I2CManager i2c = new I2CManager(board, hexAddress);
			try {
				i2c.sendI2CMessage(message);
                String i2cResponse = i2c.receiveI2CMessage();
				System.out.println("I2C value currently on bus: " + i2cResponse);
				out.println("Value on I2C bus set to: " + i2cResponse);
            } catch (IOException e) {
				e.printStackTrace();
			}
		} else if (parser.getPinType().equals("SPI")) {
            System.out.println("Pin type is SPI. SPI bus is not supported yet.");
            String hexAddress = parser.getValue().substring(0, 4);
            String message = parser.getValue().substring(4);
            SPIManager spi = new SPIManager(board, hexAddress);

            try {
                spi.sendSpiMessage(message);
                String spiResponse = spi.receiveSpiMessage();
                System.out.println("SPI value currently on bus: " + spiResponse);
				out.println("Value on I2C bus set to: " + spiResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (parser.getPinType().equals("UART")) {
			System.out.println("Pin type is UART. UART bus is not supported yet.");
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
		if (input.length() > 20) {
			if (input.substring(14).startsWith("REQUEST:990")) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean isEmbeddedCommand(String input) {
		if (input.length() > 15
				&& (input.contains("GPIO:") || input.contains("SPI:") || input.contains("I2C:") || input
						.contains("UART:"))) {
			return true;
		} else {
			return false;
		}
	}
}
