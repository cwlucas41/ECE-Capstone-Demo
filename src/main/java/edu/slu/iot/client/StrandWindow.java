package edu.slu.iot.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.NumberFormatter;
import javax.swing.text.PlainDocument;

import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import edu.slu.iot.IoTClient;
import edu.slu.iot.client.Strand;
import edu.slu.iot.data.DaqState;
import edu.slu.iot.data.Sample;
import edu.slu.iot.data.StateSource;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;

import javax.swing.JScrollPane;
import java.awt.SystemColor;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JSeparator;
import javax.swing.JCheckBox;
import com.github.lgooddatepicker.components.*;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings.TimeIncrement;
import javax.swing.JFormattedTextField;

public class StrandWindow {

	private StrandListener sListener;
	private DaqState stateSyncObject;
	private File configFile = null;
	private File writeFile = null;
	private IoTClient iotClient;
	private JList listView;
	private AppendableView listModel = new AppendableView();
	private boolean iotConnected = false;
	private String currentFilePath;
	
	private JFrame frame;
	private JTextField topicField;
	private String topicString;
	private JFormattedTextField gainField;
	private JFormattedTextField frequencyField;
	private JButton connectButton;
	private JButton updateStateButton;
	private JButton allPastDataButton;
	private JButton rangePastDataButton;
	private JCheckBox scrollingCheckBox;
	private JTextPane connectionStatus;
	private JTextPane topicStatus;
	private JTextPane gainStatus;
	private JTextPane frequencyStatus;
	
	static private AmazonDynamoDB dynamoDB;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){
			System.out.println("UIManager Error");
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StrandWindow window = new StrandWindow();
					window.frame.setVisible(true);
					window.connectionListener();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public StrandWindow() {
		initialize(); 
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 425);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[132.00px,grow][48.00px][114.00:104.00][74.00,grow]", "[25.00px,center][][8.00px,grow,center][][grow,center][grow,center][grow,center][][19.00][center][grow,center][][1.00][27.00,grow,center][][grow]"));
		
		JTextPane txtpnChooseAConfiguration = new JTextPane();
		txtpnChooseAConfiguration.setEditable(false);
		txtpnChooseAConfiguration.setBackground(SystemColor.control);
		txtpnChooseAConfiguration.setText("Choose a configuration file (.conf)");
		frame.getContentPane().add(txtpnChooseAConfiguration, "cell 0 0,alignx left,aligny center");
		
		connectButton = new JButton("Connect");
		connectButton.setEnabled(false);
		frame.getContentPane().add(connectButton, "cell 1 2,growx,aligny center");
		
		
		connectionStatus = new JTextPane();
		connectionStatus.setEditable(false);
		connectionStatus.setBackground(SystemColor.control);
		connectionStatus.setText("Status: Not Connected");
		frame.getContentPane().add(connectionStatus, "cell 2 2,alignx center,aligny center");
		
		JSeparator secondSeparator = new JSeparator();
		frame.getContentPane().add(secondSeparator, "cell 0 3 4 1,growx,aligny center");
		
		JTextPane txtpnChangeDeviceState = new JTextPane();
		txtpnChangeDeviceState.setEditable(false);
		txtpnChangeDeviceState.setBackground(SystemColor.menu);
		txtpnChangeDeviceState.setText("Change device state");
		frame.getContentPane().add(txtpnChangeDeviceState, "cell 0 4,growx,aligny center");
		
		JTextPane txtpnTopic = new JTextPane();
		txtpnTopic.setEditable(false);
		txtpnTopic.setBackground(SystemColor.menu);
		txtpnTopic.setText("Topic:");
		frame.getContentPane().add(txtpnTopic, "cell 1 4,growx,aligny center");
	
		topicField = new JTextField();
		topicField.setDocument(new JTextFieldLimiter(100)); //limit length to 100 characters
		frame.getContentPane().add(topicField, "cell 2 4,alignx left,aligny center");
		topicField.setColumns(10);
		
		topicStatus = new JTextPane();
		topicStatus.setEditable(false);
		topicStatus.setText("Current topic: ");
		topicStatus.setBackground(SystemColor.menu);
		frame.getContentPane().add(topicStatus, "cell 3 4,growx,aligny center");
		
		updateStateButton = new JButton("Update");
		updateListener();
	    updateStateButton.setEnabled(false);
		frame.getContentPane().add(updateStateButton, "cell 0 5,growx,aligny center");
		
		JTextPane txtpnGain = new JTextPane();
		txtpnGain.setEditable(false);
		txtpnGain.setBackground(SystemColor.menu);
		txtpnGain.setText("Gain:");
		frame.getContentPane().add(txtpnGain, "cell 1 5,growx,aligny center");

		NumberFormatter gainFormat = new NumberFormatter(new DecimalFormat("#0.0000"));
		gainField = new JFormattedTextField(gainFormat);
		gainField.setColumns(10);
		frame.getContentPane().add(gainField, "cell 2 5,alignx left,aligny center");
		
		gainStatus = new JTextPane();
		gainStatus.setEditable(false);
		gainStatus.setText("Current gain: ");
		gainStatus.setBackground(SystemColor.menu);
		frame.getContentPane().add(gainStatus, "cell 3 5,growx,aligny center");
		
		JTextPane txtpnFrequency = new JTextPane();
		txtpnFrequency.setEditable(false);
		txtpnFrequency.setBackground(SystemColor.menu);
		txtpnFrequency.setText("Frequency:");
		frame.getContentPane().add(txtpnFrequency, "cell 1 6,growx,aligny center");

		NumberFormatter frequencyFormat = new NumberFormatter(new DecimalFormat("#00000.00"));
		frequencyField = new JFormattedTextField(frequencyFormat);
		frequencyField.setColumns(10);
		frame.getContentPane().add(frequencyField, "cell 2 6,alignx left,aligny center");
		
		frequencyStatus = new JTextPane();
		frequencyStatus.setEditable(false);
		frequencyStatus.setText("Current frequency: ");
		frequencyStatus.setBackground(SystemColor.menu);
		frame.getContentPane().add(frequencyStatus, "cell 3 6,growx,aligny center");
		
		
		JSeparator thirdSeparator = new JSeparator();
		frame.getContentPane().add(thirdSeparator, "cell 0 8 4 1,growx,aligny center");
		
		JTextPane pastDataTextBox = new JTextPane();
		pastDataTextBox.setEditable(false);
		pastDataTextBox.setBackground(SystemColor.menu);
		pastDataTextBox.setText("Start time:");
		frame.getContentPane().add(pastDataTextBox, "cell 0 9,alignx left,aligny center");
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser configFileChooser = new JFileChooser();
				File workingDirectory = new File(System.getProperty("user.dir"));
				configFileChooser.setCurrentDirectory(workingDirectory);
				frame.getContentPane().add(configFileChooser);
				int chooseStatus = configFileChooser.showOpenDialog(frame);
				if (chooseStatus == JFileChooser.APPROVE_OPTION) {
                    configFile = configFileChooser.getSelectedFile();
                    connectButton.setEnabled(true);
                    allPastDataButton.setEnabled(true);
                    rangePastDataButton.setEnabled(true);
				}
				connectClient();
			}
		});
		frame.getContentPane().add(btnBrowse, "cell 1 0,growx,aligny center");
		
		JSeparator firstSeparator = new JSeparator();
		frame.getContentPane().add(firstSeparator, "cell 0 1 4 1,growx,aligny center");
		
		JTextPane txtpnEnterTheTopic = new JTextPane();
		txtpnEnterTheTopic.setEditable(false);
		txtpnEnterTheTopic.setBackground(SystemColor.control);
		txtpnEnterTheTopic.setText("Connect to a device");
		frame.getContentPane().add(txtpnEnterTheTopic, "cell 0 2,alignx left,aligny center");
		
		TimePickerSettings timeSettings1 = new TimePickerSettings();
		timeSettings1.setInitialTimeToNow();
		timeSettings1.generatePotentialMenuTimes(TimeIncrement.FiveMinutes, null, null);
		DatePickerSettings dateSettings1 = new DatePickerSettings();
		DateTimePicker startDateTimePicker = new DateTimePicker(dateSettings1, timeSettings1);
		frame.getContentPane().add(startDateTimePicker, "cell 1 9 2 1,alignx left,aligny center");
		
		allPastDataButton = new JButton("Show all past data");
		frame.getContentPane().add(allPastDataButton, "cell 3 9,growx,aligny center");
		allPastDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				loadHistoricalData();
			}
		});
		allPastDataButton.setEnabled(false);
		
		JTextPane txtpnEndTime = new JTextPane();
		txtpnEndTime.setEditable(false);
		txtpnEndTime.setBackground(SystemColor.menu);
		txtpnEndTime.setText("End time:");
		frame.getContentPane().add(txtpnEndTime, "cell 0 10,growx,aligny center");
		
		TimePickerSettings timeSettings2 = new TimePickerSettings();
		timeSettings2.setInitialTimeToNow();
		timeSettings2.generatePotentialMenuTimes(TimeIncrement.FiveMinutes, null, null);
		DatePickerSettings dateSettings2 = new DatePickerSettings();
		DateTimePicker stopDateTimePicker = new DateTimePicker(dateSettings2, timeSettings2);
		frame.getContentPane().add(stopDateTimePicker, "cell 1 10 2 1,alignx left,aligny center");
		
		rangePastDataButton = new JButton("Show range of past data");
		rangePastDataButton.setEnabled(false);
		rangePastDataButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if ((startDateTimePicker.getDatePicker().getDate() != null) && (stopDateTimePicker.getDatePicker().getDate() != null)) {
						ZoneId zoneId = ZoneId.systemDefault();
						long start = startDateTimePicker.getDateTimePermissive().atZone(zoneId).toEpochSecond();
						long end = stopDateTimePicker.getDateTimePermissive().atZone(zoneId).toEpochSecond();
						loadHistoricalData(start << 32, end << 32);
					}
				}
		});
		frame.getContentPane().add(rangePastDataButton, "cell 3 10,growx,aligny center");
		
		JSeparator fourthSeparator = new JSeparator();
		frame.getContentPane().add(fourthSeparator, "cell 0 11 4 1,growx,aligny center");
		
		JTextPane txtpnChooseAFile = new JTextPane();
		txtpnChooseAFile.setEditable(false);
		txtpnChooseAFile.setText("Choose or create a file to write to");
		txtpnChooseAFile.setBackground(SystemColor.menu);
		frame.getContentPane().add(txtpnChooseAFile, "cell 0 13,growx,aligny center");
		JButton writeButton = new JButton("Write to file");
		writeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (writeFile != null) {
					writeToFile(writeFile.getPath());
					pythonGraphing();
				}
			}
		});
		writeButton.setEnabled(false);
		frame.getContentPane().add(writeButton, "cell 2 13,growx,aligny center");
		
		JButton writePathButton = new JButton("Find file...");
		writePathButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser writingFileChooser = new JFileChooser();
				File workingDirectory = new File(System.getProperty("user.dir"));
				writingFileChooser.setCurrentDirectory(workingDirectory);
				frame.getContentPane().add(writingFileChooser);
				int chooseStatus = writingFileChooser.showOpenDialog(frame);
				if (chooseStatus == JFileChooser.APPROVE_OPTION) {
                    writeFile = writingFileChooser.getSelectedFile();
                    writeButton.setEnabled(true);
				}
			}
		});
		frame.getContentPane().add(writePathButton, "cell 1 13,growx,aligny center");
		
		scrollingCheckBox = new JCheckBox("Scroll to Bottom"); //TODO: weird bug with this button?
		frame.getContentPane().add(scrollingCheckBox, "cell 3 13,growx,aligny center");
		scrollingCheckBox.setSelected(true);
		
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, "cell 0 15 4 1,grow");
		
		listView = new JList(listModel);
		scrollPane.setViewportView(listView);	
	}
	
	public void pythonGraphing() {
		writeToFile(currentFilePath);
		try {
			Runtime.getRuntime().exec("python graphingScript.py \"" + currentFilePath + "\"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToFile(String filePath) {
		currentFilePath = filePath;
		File target = new File(filePath);
		if (!target.exists())
			try {
				target.createNewFile();
			} catch (IOException e) {
				System.out.println("Could not create new file " + writeFile.getPath());
				e.printStackTrace();
			}
		FileOutputStream fstream = null;
		try {fstream = new FileOutputStream(target);}
		catch (FileNotFoundException fnfe) {
			System.out.println("File not found exception in file write");
		}
		BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(fstream));
		try {
			Sample sample = null;
			bwriter.write("time,voltage");
			bwriter.newLine();
			for (int i = 0; i < listModel.getSize(); i++) {
				sample = listModel.getElementAt(i);
				long timeStamp = sample.getTimestamp();
				long seconds = timeStamp >> 32;
				long nanoseconds = timeStamp & 0x0000FFFF;
				System.out.println(nanoseconds);
				int nanoDigits = ((int) Math.log10(nanoseconds)) + 1; // need to add leading zeroes to nanosecond value
				System.out.println(nanoDigits);
				int leadingZeroes = 9 - nanoDigits;
				String zeroes = "";
				for (int j = 0; j < leadingZeroes; j++) {
					zeroes = zeroes + "0";
				}
				bwriter.write(Long.toString(seconds) + "." + zeroes + nanoseconds + "," + Float.toString(sample.getValue())); //let's not even bother converting to a data type -- use a lossless representation
				bwriter.newLine();
			}
			bwriter.close();
		}
		catch (IOException ioe) {
			System.out.println("IOException on file write");
		}
	}
	
	public void loadHistoricalData() {
		loadHistoricalData(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	public void loadHistoricalData(long startTime, long endTime) {
		listModel.clearList();
		QuerySpec spec;
		if (topicString == null) {
			System.out.println("Please enter your session ID.");
		}
		else {
			if (listModel.getSize() != 0) { //if we have any data from this session so far, only query data from before it began
				endTime = listModel.getElementAt(0).getTimestamp();
			}
			spec = new QuerySpec()
				.withRangeKeyCondition(new RangeKeyCondition("timestamp").between(startTime, endTime))
				.withHashKey("sessionID", topicString);
			dynamoDB = AmazonDynamoDBClientBuilder.standard()
					.withRegion(Regions.US_WEST_2)
					.withCredentials(new ProfileCredentialsProvider("Certificate1/ddbconf.txt", "default"))
					.build();
			Table table = new Table(dynamoDB, getTableName());
			ItemCollection<QueryOutcome> items = table.query(spec);
			Iterator<Item> iterator = items.iterator();
			Item item = null;
			List<Sample> writeToView = new ArrayList<Sample>();
			while (iterator.hasNext()) { //make sure this doesn't interrupt rendering too much
			    item = iterator.next();
			    writeToView.add(new Sample(item));
			}
			listModel.addBulkToList(writeToView);
		}
	}
	
	public void connectionListener() {
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					connectClient();
			}
		});
	}
	
	public void connectClient() {
		if (iotConnected) {
			try {
				iotClient.disconnect();
			} catch (AWSIotException e) {
				e.printStackTrace();
			}
			iotConnected = false;
	        connectButton.setText("Connect");
	        connectionStatus.setText("Status: Not Connected");
		    updateStateButton.setEnabled(false);
		}
		else {
			try {
				iotClient = new IoTClient(configFile.getPath());
		        listModel.clearList();
				stateSyncObject = new StateSource<DaqState>(iotClient, iotClient.getTargetThingName(), DaqState.class).getState();
		        iotConnected = true;
				connectionStatus.setText("Status: Connected");
			    connectButton.setText("Stop");
			    updateStateButton.setEnabled(true);
			} catch (AWSIotException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateListener() {
		updateStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String topic = null;
				Double gain = null;
				Double freq = null;
				if (!topicField.getText().equals("")) { //update topic
					topic = topicField.getText();
					sListener = new StrandListener(topic, AWSIotQos.QOS0, StrandWindow.this);
					try {
						iotClient.subscribe(sListener);
						listModel.clearList();
						topicStatus.setText("Current topic: " + topicField.getText());
						topicString = topicField.getText();
						topicField.setText("");
					} catch (AWSIotException e) {
						e.printStackTrace(); //TODO: fix all of these catch blocks to do something sensible
					}
				}

				if (!gainField.getText().equals("")) { //update gain
					gain = Math.abs(Double.parseDouble(gainField.getText()));
					gainStatus.setText("Current gain: " + gain.toString());
					gainField.setText("");
				}
				if (!frequencyField.getText().equals("")) { //update frequency
					freq = Math.abs(Double.parseDouble(frequencyField.getText()));
					frequencyStatus.setText("Current frequency: " + freq.toString() + " Hz");
					frequencyField.setText("");
				}
				
				stateSyncObject.update(topic, freq, gain);
			}
		});
	}
	
	public void writeLineToList(Sample sampleToWrite) {
		List<Sample> storedList = listModel.getList(); //so we can have a more direct reference
		if (( sampleToWrite.compareTo(storedList.get(storedList.size()-1)) ) < 0) {
			for (int i = 2; i < storedList.size(); i++) {
				if (sampleToWrite.compareTo(storedList.get(storedList.size()-i)) >= 0) {
					listModel.insertIntoList((storedList.size() - i + 1), sampleToWrite);
					break;
				}
			}
		}
		else {
			listModel.appendToList(sampleToWrite);
		}
		if (scrollingCheckBox.isSelected()) {
			int lastIndex = listModel.getSize() - 1;
			if (lastIndex > 0) {
			   listView.ensureIndexIsVisible(lastIndex);
			}
		}
	}
	
	private String getTableName() {
		File config = new File(configFile.getPath());
    	Scanner sc = null;
		String tableName;
		try {
			sc = new Scanner(config);
	    	String line = sc.nextLine();
	    	String[] fields = line.split("\\s+");
	    	if (fields.length != 2) {
	    		throw new IllegalArgumentException("invalid format for config file");
	    	}
	    	tableName = fields[1];    	
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("bad filename for config file");
		} finally {
			sc.close();
		}
		return tableName;
	}
	
	public class AppendableView extends AbstractListModel<Sample> {
		private List<Sample> model;
		
		public AppendableView() {
			model = new ArrayList<Sample>();
		}
		
		public int getSize() {
			return model.size();
		}
		public Sample getElementAt(int index) {
			return model.get(index);
		}
		public void appendToList(Sample value) {
			model.add(value);
			fireIntervalAdded(this, this.getSize() - 1, this.getSize() - 1);
		}
		public void insertIntoList(int index, Sample value) {
			model.add(index, value);
			fireIntervalAdded(this, index, this.getSize() - 1);
		}
		public void addBulkToList(List<Sample> listOfValues) {
			model.addAll(listOfValues);
			Collections.sort(model);
			if (this.getSize() > 0)
				fireIntervalAdded(this, 0, this.getSize() - 1);
			if (scrollingCheckBox.isSelected()) {
				int lastIndex = this.getSize() - 1;
				if (lastIndex > 0) {
				   listView.ensureIndexIsVisible(lastIndex);
				}
			}
		}
		public List<Sample> getList() {
			return model;
		}
		public void clearList() {
			int size = this.getSize();
			if (size > 0) {
				model.clear();
				fireIntervalRemoved(this, 0, size - 1);
			}
		}
	}


	
}