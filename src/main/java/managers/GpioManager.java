package managers;

import io.silverspoon.bulldog.core.gpio.DigitalOutput;
import io.silverspoon.bulldog.core.platform.Board;

public class GpioManager {

	public void turnLedOn(Board board, String pin) throws NullPointerException{
		if (pin.startsWith("0")) {
			pin.substring(1);
		}
		DigitalOutput output = board.getPin(pin).as(DigitalOutput.class);
		if (output.isLow()) {
			output.high();
		}
	}

	public void turnLedOff(Board board, String pin) throws NullPointerException{
		if (pin.startsWith("0")) {
			pin.substring(1);
		}
		DigitalOutput output = board.getPin(pin).as(DigitalOutput.class);
		if (output.isHigh()) {
			output.low();
		}
	}

	public int toggleLed(Board board, String pin) throws NullPointerException{
		if (pin.startsWith("0")) {
			pin.substring(1);
		}
		DigitalOutput output = board.getPin(pin).as(DigitalOutput.class);
		if (output.isLow()) {
			output.high();
			System.out.println("SENT SHOULD BE 1");
			return 1;
		} else {
			output.low();
			System.out.println("SENT SHOULD BE 0");
			return 0;
		}
	}
}
