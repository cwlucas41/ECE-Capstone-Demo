package edu.slu.iot.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.slu.iot.IoTClient;
import edu.slu.iot.client.Strand;
import edu.slu.iot.realdaq.Sample;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;

import javax.swing.JScrollPane;
import java.awt.SystemColor;
import javax.swing.JList;
import javax.swing.AbstractListModel;

public class StrandWindow {

	private Strand currentStrand;
	private File configFile = null;
	private IoTClient iotClient;
	private AppendableView listModel = new AppendableView();
	private Set<Sample> sampleSet;
	
	private JFrame frame;
	private JTextField topicField;
	private JButton connectButton;
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
		frame.getContentPane().setLayout(new MigLayout("", "[157.00px,grow][91px][][grow]", "[25.00px][17.00px,grow][grow]"));
		
		JTextPane txtpnChooseAConfiguration = new JTextPane();
		txtpnChooseAConfiguration.setBackground(SystemColor.control);
		txtpnChooseAConfiguration.setText("Choose a configuration file (.conf)");
		frame.getContentPane().add(txtpnChooseAConfiguration, "cell 0 0,alignx left,aligny top");
		
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
				}
			}
		});
		frame.getContentPane().add(btnBrowse, "cell 1 0,alignx center,growy");
		
		topicField = new JTextField();
		frame.getContentPane().add(topicField, "cell 1 1,alignx center,aligny top");
		topicField.setColumns(10);
		
		connectButton = new JButton("Connect to topic");
		
		frame.getContentPane().add(connectButton, "cell 2 0,alignx center,growy");
		
		JButton btnHist = new JButton("Add past data");
		frame.getContentPane().add(btnHist, "cell 3 0 1 2,alignx center,aligny top");
		btnHist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				/* Deprecated as of QOS0 change commits
				Iterator<Sample> sampleIter = sampleSet.iterator(); //naive approach without sequence numbers
				if (sampleIter.hasNext()) {
					Sample firstSample = sampleIter.next();
					long firstTime = firstSample.getTimestamp();
					try {Thread.sleep(100);} catch (InterruptedException e) {}
					Iterator<Sample> secSampleIter = sampleSet.iterator();
					if (secSampleIter.next().getTimestamp() < firstTime) {
						firstTime = secSampleIter.next().getTimestamp();
					}
					
					dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new ProfileCredentialsProvider("DDBCert1/conf.txt", "default")));
					Table table = dynamoDB.getTable("tableName"); //read name from secondary config file
					
			        Item item = table.getItem("Timestamp", // attribute name
			                firstTime, // attribute value
			                "DeviceID, SessionID, Timestamp, Voltage", // projection expression
			                null); // name map - don't know what this is
				}*/
				
				Iterator<Sample> sampleIter = sampleSet.iterator();
				if (sampleIter.hasNext()) { //if we have any data from this session so far, only query data from before it began
					
				} else { // We have nothing so far (session is currently not active), ask for everything
					//AwsClientBuilder builder = new AwsClientBuilder().withCredentials(new ProfileCredentialsProvider("DDBCert1/conf.txt", "default"));
					dynamoDB = AmazonDynamoDBClientBuilder.standard()
	                        .withRegion(Regions.US_WEST_2)
	                        .withCredentials(new ProfileCredentialsProvider("DDBCert1/conf.txt", "default"))
	                        .build();
					Table table = new Table(dynamoDB, "tableName");//((AmazonDynamoDBClient) dynamoDB).describeTable("tableName"); //must know the table name ahead of time and hardcode OR store in a config file
					QuerySpec spec = new QuerySpec()
					    .withKeyConditionExpression("Id = :v_id")
					    .withValueMap(new ValueMap()
					        .withString(":v_id", "Amazon DynamoDB#DynamoDB Thread 1"));
					ItemCollection<QueryOutcome> items = table.query(spec);
					Iterator<Item> iterator = items.iterator();
					Item item = null;
					while (iterator.hasNext()) {
					    item = iterator.next();
					    System.out.println(item.toJSONPretty());
					}
				}
				
			}
		});
		
		JTextPane txtpnEnterTheTopic = new JTextPane();
		txtpnEnterTheTopic.setBackground(SystemColor.control);
		txtpnEnterTheTopic.setText("Enter the topic name");
		frame.getContentPane().add(txtpnEnterTheTopic, "cell 0 1,alignx left,aligny top");
		
		
		connectionStatus = new JTextPane();
		connectionStatus.setBackground(SystemColor.control);
		connectionStatus.setText("Status: Not Connected");
		frame.getContentPane().add(connectionStatus, "cell 2 1,alignx center,aligny top");
		
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, "cell 0 2 4 1,grow");
		
		JList list = new JList(listModel);
		scrollPane.setViewportView(list);
		
	}
	
	public void connectionListener() {
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					try {
						//currentStrand =  new Strand(topicField.getText(), configFile);
						iotClient = new IoTClient(configFile.getPath());
				        iotClient.subscribe(new StrandListener(topicField.getText(), AWSIotQos.QOS0, StrandWindow.this));
				        connectionStatus.setText("Status: Connected"); //TODO: update this value appropriately
					} catch (AWSIotException e) {
						e.printStackTrace();
					}
			}
		});
	}
	
	public void writeLineToList(String lineToWrite) {
		listModel.addToList(lineToWrite);
	}
	
	public void addSample(Sample sample) {
		sampleSet.add(sample);
	}
	
	public class AppendableView extends AbstractListModel<String> {
		private List<String> model;
		
		public AppendableView() {
			model = new ArrayList<String>();
		}
		
		public int getSize() {
			return model.size();
		}
		public String getElementAt(int index) {
			return model.get(index);
		}
		public void addToList(String value) {
			model.add(value);
			fireIntervalAdded(this, this.getSize() - 1, this.getSize() - 1);
		}
	}


	
}
