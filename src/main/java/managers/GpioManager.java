package managers;

import io.silverspoon.bulldog.core.gpio.DigitalOutput;
import io.silverspoon.bulldog.core.platform.Board;

public class GpioManager {

	public int turnLedOn(Board board, String pin) throws NullPointerException{
		if (pin.startsWith("0")) {
			pin = pin.substring(1);
		}
		DigitalOutput output = board.getPin(pin).as(DigitalOutput.class);
		if (output.isLow()) {
			output.high();
		}
		return 1;
	}

	public int turnLedOff(Board board, String pin) throws NullPointerException{
		if (pin.startsWith("0")) {
			pin = pin.substring(1);
		}
		DigitalOutput output = board.getPin(pin).as(DigitalOutput.class);
		if (output.isHigh()) {
			output.low();
		}
		return 0;
	}

	public int toggleLed(Board board, String pin) throws NullPointerException{
		if (pin.startsWith("0")) {
			pin = pin.substring(1);
		}
		DigitalOutput output = board.getPin(pin).as(DigitalOutput.class);
		if (output.isLow()) {
			output.high();
			return 1;
		} else {
			output.low();
			return 0;
		}
	}
}
