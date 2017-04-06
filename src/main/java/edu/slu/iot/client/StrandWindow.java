package edu.slu.iot.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import edu.slu.iot.IoTClient;
import edu.slu.iot.client.Strand;
import edu.slu.iot.data.Sample;

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

public class StrandWindow {

	private Strand currentStrand;
	private StrandListener sListener;
	private File configFile = null;
	private File writeFile = null;
	private IoTClient iotClient;
	private JList listView;
	private AppendableView listModel = new AppendableView();
	private boolean streamPaused = false;
	private boolean iotConnected = false;
	
	private JFrame frame;
	private JTextField topicField;
	private JButton connectButton;
	private JButton playPauseButton;
	private JTextPane connectionStatus;
	
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
		frame.setBounds(100, 100, 525, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[157.00px,grow][91px][grow][grow]", "[25.00px][][56.00px,grow][][30.00][56.00,grow][grow]"));
		
		JTextPane txtpnChooseAConfiguration = new JTextPane();
		txtpnChooseAConfiguration.setBackground(SystemColor.control);
		txtpnChooseAConfiguration.setText("Choose a configuration file (.conf)");
		frame.getContentPane().add(txtpnChooseAConfiguration, "cell 0 0,alignx left,aligny center");
		
		JButton btnHist = new JButton("Add past data");
		frame.getContentPane().add(btnHist, "cell 3 2,alignx center,growy");
		btnHist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				long startTime = Long.MIN_VALUE;
				QuerySpec spec;
				String sessionID = topicField.getText();
				if (sessionID == null) {
					System.out.println("Please enter your session ID.");
				}
				else {
					if (listModel.getSize() != 0) { //if we have any data from this session so far, only query data from before it began
						startTime = listModel.getElementAt(0).getTimestamp();
						spec = new QuerySpec()
							.withRangeKeyCondition(new RangeKeyCondition("timestamp").lt(startTime))
							.withHashKey("sessionID", sessionID);
					} else {
						spec = new QuerySpec()
							.withRangeKeyCondition(new RangeKeyCondition("timestamp").gt(startTime))
							.withHashKey("sessionID", sessionID);
					}
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
		});
		btnHist.setEnabled(false);
		
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
                    btnHist.setEnabled(true);
				}
			}
		});
		frame.getContentPane().add(btnBrowse, "cell 1 0,alignx center,growy");
		
		
		connectionStatus = new JTextPane();
		connectionStatus.setBackground(SystemColor.control);
		connectionStatus.setText("Status: Not Connected");
		frame.getContentPane().add(connectionStatus, "cell 2 0,alignx center,aligny center");
		
		JSeparator firstSeparator = new JSeparator();
		frame.getContentPane().add(firstSeparator, "cell 0 1 4 1,grow");
		
		topicField = new JTextField();
		frame.getContentPane().add(topicField, "cell 1 2,alignx center,aligny center");
		topicField.setColumns(10);
		
		JTextPane txtpnEnterTheTopic = new JTextPane();
		txtpnEnterTheTopic.setBackground(SystemColor.control);
		txtpnEnterTheTopic.setText("Enter the session name");
		frame.getContentPane().add(txtpnEnterTheTopic, "cell 0 2,alignx left,aligny center");
		
		connectButton = new JButton("Connect to session");
		connectButton.setEnabled(false);
		frame.getContentPane().add(connectButton, "cell 2 2,alignx center,growy");
		
		playPauseButton = new JButton("Stop Scrolling");
		playPauseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				streamPaused = !streamPaused;
				if (streamPaused == true) {
					playPauseButton.setText("Scroll on update");
				}
				else {
					playPauseButton.setText("Stop scrolling");
				}
			}
		});
		frame.getContentPane().add(playPauseButton, "cell 2 3 2 1,grow");
		playPauseButton.setEnabled(false);
		
		JSeparator secondSeparator = new JSeparator();
		frame.getContentPane().add(secondSeparator, "cell 0 4 4 1,grow");
		
		JTextPane txtpnChooseAFile = new JTextPane();
		txtpnChooseAFile.setText("Choose or create a file to write to");
		txtpnChooseAFile.setBackground(SystemColor.menu);
		frame.getContentPane().add(txtpnChooseAFile, "cell 0 5,growx,aligny center");
		JButton writeButton = new JButton("Write to file");
		writeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (writeFile != null) {
					File target = new File(writeFile.getPath());
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
						for (int i = 0; i < listModel.getSize(); i++) {
							sample = listModel.getElementAt(i);
							bwriter.write(Long.toString(sample.getTimestamp()) + ", " + Float.toString(sample.getValue()));
							bwriter.newLine();
						}
						bwriter.close();
					}
					catch (IOException ioe) {
						System.out.println("IOException on file write");
					}
				}
			}
		});
		writeButton.setEnabled(false);
		frame.getContentPane().add(writeButton, "cell 2 5,alignx center,growy");
		
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
		frame.getContentPane().add(writePathButton, "cell 1 5,alignx center,growy");
		
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, "cell 0 6 4 1,grow");
		
		listView = new JList(listModel);
		scrollPane.setViewportView(listView);
		
	}
	
	public void connectionListener() {
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					if (iotConnected) {
						try {
							iotClient.unsubscribe(sListener, 1000);
						} catch (AWSIotException | AWSIotTimeoutException te) {
							te.printStackTrace();
						}
						iotConnected = false;
				        connectButton.setText("Connect to session");
				        connectionStatus.setText("Status: Not Connected");
						playPauseButton.setEnabled(false);
					}
					else {
						try {
							//currentStrand =  new Strand(topicField.getText(), configFile);
							iotClient = new IoTClient(configFile.getPath());
							sListener = new StrandListener(topicField.getText(), AWSIotQos.QOS0, StrandWindow.this);
					        iotClient.subscribe(sListener);
					        listModel.clearList();
					        
						} catch (AWSIotException e) {
							e.printStackTrace();
						}
						iotConnected = true;
						connectionStatus.setText("Status: Connected");
					    connectButton.setText("Stop");
					    playPauseButton.setEnabled(true);
					}
			}
		});
	}
	
	public void writeLineToList(Sample sampleToWrite) {
		listModel.appendToList(sampleToWrite);
		if (!streamPaused) {
			int lastIndex = listModel.getSize() - 1;
			if (lastIndex >= 0) {
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
		public void addBulkToList(List<Sample> listOfValues) {
			model.addAll(listOfValues);
			Collections.sort(model);
			fireIntervalAdded(this, 0, this.getSize() - 1);
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