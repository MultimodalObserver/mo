package mo.exchange.data;

public class IncompatibleOriginDataException extends Exception
{
	public IncompatibleOriginDataException() {
		super();
	}

	public IncompatibleOriginDataException(String message) {
		super(message);
	}

	public IncompatibleOriginDataException(String message, Throwable cause) {
		super(message,cause);
	}

	public IncompatibleOriginDataException(Throwable cause) {
		super(cause);
	}
}