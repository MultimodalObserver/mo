package mo.exchange.data.visualization;

import java.util.List;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.Dialog.ModalityType;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;

public class WaitDialog
{
	private JDialog dialog;
	private JLabel label;
	private JButton cancelButton, okButton;
	private List<ButtonPressedEventListener> listeners = new ArrayList();

	public WaitDialog() {
		cancelButton = new JButton("Cancelar");
        cancelButton.setEnabled(true);
        cancelButton.addActionListener((ActionEvent e) -> {
        	for (ButtonPressedEventListener listener : listeners) {
        		listener.cancelButtonPressed();
        	}
        });

        okButton = new JButton("Aceptar");
        okButton.setEnabled(false);
        okButton.addActionListener((ActionEvent e) -> {
        	fireOkButtonPressed();
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        label = new JLabel();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        dialog = new JDialog();
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setLocationRelativeTo(null);
        dialog.setTitle("Please Wait...");
        dialog.add(panel);
	}

	private void fireOkButtonPressed() {
    	for (ButtonPressedEventListener listener : listeners) {
    		listener.okButtonPressed();
    	}
	}

	private void fireCancelButtonPressed() {
    	for (ButtonPressedEventListener listener : listeners) {
    		listener.cancelButtonPressed();
    	}
	}

	public void setTitle(String title) {
		dialog.setTitle(title);
	}

	public void setText(String text) {
		label.setText(text);
	}

	public void enableOkButton() {
		okButton.setEnabled(true);
	}

	public void disableOkButton() {
		okButton.setEnabled(false);
	}

	public void enableCancelButton() {
		cancelButton.setEnabled(true);
	}

	public void disableCancelButton() {
		cancelButton.setEnabled(false);
	}

	public void addButtonPressedEventListener(ButtonPressedEventListener toAdd) {
		listeners.add(toAdd);
	}

	public interface ButtonPressedEventListener {
		void okButtonPressed();
		void cancelButtonPressed();
	}

	public void show() {
		dialog.pack();
    	dialog.setVisible(true);
	}

	public void hide() {
		dialog.setVisible(false);
	}
}