package core;

import io.silverspoon.bulldog.core.gpio.DigitalOutput;
import io.silverspoon.bulldog.core.platform.Board;
import io.silverspoon.bulldog.core.platform.Platform;
import io.silverspoon.bulldog.core.util.BulldogUtil;
import io.silverspoon.bulldog.raspberrypi.RaspiNames;

import java.io.IOException;

public class Main {

	static Board board = Platform.createBoard();

	public static void main(String[] args) {
		blinkLed(5, RaspiNames.P1_11, 100, 100);

		try {
			Networking networking = new Networking();
			networking.listenSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void blinkLed(int count, String pin, int millisLedOn, int millisLedOff) {
		if (count < 1) {
			count = 1;
		} else if ((count * (millisLedOff + millisLedOff)) > 20000) {
			millisLedOn = 1000;
			millisLedOff = 1000;
			count = 20;
		}
		DigitalOutput output = board.getPin(pin).as(DigitalOutput.class);
		for (int i = 1; i < count + 1; i++) {
			System.out.println("Blinked " + i + ". time.");
			output.high();
			BulldogUtil.sleepMs(millisLedOn);
			output.low();
			BulldogUtil.sleepMs(millisLedOff);
		}
		System.out.println("Blinks completed.");
	}
}