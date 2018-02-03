package mo.exchange.data;

import mo.exchange.data.plugin.ExchangeablePlugin;
import javax.swing.SwingWorker;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.List;
import java.util.ArrayList;



import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;



public class ExchangeableWorker
{
	public final static Logger logger = Logger.getLogger(ExchangeableWorker.class.getName());

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);



	private SwingWorker exchanger;
	private SwingWorker cancelExchanger;

	private ExchangeablePlugin plugin;

	private boolean canceledExchange = false;

    
    public enum ExchangeStatusValue {DONE}
    
    public enum CancelExchangeStatusValue {DONE}


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    private ExchangeStatusValue exchangeValue;

    private ExchangeStatusValue getExchangeValue() {
        return exchangeValue;
    }

    private void setExchangeStatusValue(ExchangeStatusValue newExchangeValue) {
        ExchangeStatusValue oldExchangeValue = this.exchangeValue;
        this.exchangeValue = newExchangeValue;
        this.pcs.firePropertyChange("exchangeValue", oldExchangeValue, newExchangeValue);
    }


    private CancelExchangeStatusValue cancelExchangeValue;

    private CancelExchangeStatusValue getCancelExchangeValue() {
        return cancelExchangeValue;
    }

    private void setCancelExchangeStatusValue(CancelExchangeStatusValue newExchangeValue) {
        CancelExchangeStatusValue oldCancelExchangeValue = this.cancelExchangeValue;
        this.cancelExchangeValue = newExchangeValue;
        this.pcs.firePropertyChange("cancelExchangeValue", oldCancelExchangeValue, newExchangeValue);
    }


	public ExchangeableWorker(ExchangeablePlugin plugin) {
		this.plugin = plugin;
	}

	public void execute() {
		exchanger = new SwingWorker<Void,Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    plugin.execute();
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };

        exchanger.addPropertyChangeListener(
            new PropertyChangeListener() {
                public  void propertyChange(PropertyChangeEvent evt) {
                    if (SwingWorker.StateValue.DONE.equals(evt.getNewValue()) && !canceledExchange) {
                        setExchangeStatusValue(ExchangeStatusValue.DONE);
                    }
                }
            }
        );

        exchanger.execute();
	}

	public void cancel() {
		canceledExchange = true;

        // if (exchanger != null) {
        // }
        
        exchanger.cancel(true);

		cancelExchanger = new SwingWorker<Void,Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    plugin.cancel();
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };

        cancelExchanger.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                        setCancelExchangeStatusValue(CancelExchangeStatusValue.DONE);
                    }
                }
            }
        );

        cancelExchanger.execute();
	}
}