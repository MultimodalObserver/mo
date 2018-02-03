package mo.exchange.data.plugin;

import java.io.File;

public interface ExchangeablePlugin
{
	String getCodeName();
	String getName();
	String getDescription();
	void setOrigin(File origin);
	void setDestiny(File destiny);
	void execute();
	void cancel();
}