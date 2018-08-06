/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@weareavp.com)
 * Author: Rimsha Khalid (rimsha@weareavp.com)
 * Version: 0.1.6
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@weareavp.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 */
package uk.sipperfly.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.UIDefaults;
import javax.swing.text.DefaultCaret;
import static javax.swing.text.DefaultCaret.ALWAYS_UPDATE;
import javax.xml.parsers.ParserConfigurationException;
import uk.sipperfly.persistent.Configurations;
import uk.sipperfly.repository.ConfigurationsRepo;

import static uk.sipperfly.ui.Exactly.GACOM;
import uk.sipperfly.utils.CommonUtil;
import org.apache.commons.io.FileUtils;
import uk.sipperfly.utils.BagInfoList;
import uk.sipperfly.utils.EmailList;
import uk.sipperfly.utils.EntryList;
import uk.sipperfly.utils.MyPainter;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.tools.FileObject;
import net.sf.vfsjfilechooser.VFSJFileChooser;
import net.sf.vfsjfilechooser.VFSJFileChooser.RETURN_TYPE;
import net.sf.vfsjfilechooser.VFSJFileChooser.SELECTION_MODE;
import net.sf.vfsjfilechooser.accessories.DefaultAccessoriesPanel;
import net.sf.vfsjfilechooser.utils.VFSUtils;

/**
 * This is the main UI class for the tool.
 *
 * @author Nouman Tayyab
 */
public class Exactly extends javax.swing.JFrame {

	private JFileChooser fileChooser;
	private String inputDirPath;
	public String targetPath;
	private BackgroundWorker bgw;
	static FileHandler filehandler;
	static Logger logger;
	static SimpleFormatter simpleformatter;
	static final String GACOM = "com.UKExactly";
	public UIManager uIManager;
	public int totalFiles = 0;
	public int tranferredFiles = 0;
	public int uploadedFiles = 0;
	public int nameCounter = 3;
	public int fieldCounter = 0;
	public EntryList list;
	public BagInfoList bagInfo;
	public EmailList email;
	public int[] idList;
	public int[] emailIdList;
	private int MetadataReminder = 0;
	public int[] bag_size;
	public int metadateUpdated = 0;
	public StringBuilder fileSystem;
	public String sourceChecksum;

	/**
	 * Creates new form MainFrame
	 */
	public Exactly() {
		this.list = new EntryList(this);
		this.bagInfo = new BagInfoList(this);
		this.email = new EmailList(this);
		initComponents();
		initLogger();
		this.fileSystem = new StringBuilder();
		this.uIManager = new UIManager(this);
		ImageIcon img = new ImageIcon(Exactly.class.getClass().getResource("/uk/sipperfly/ui/resources/Exactly-logo.png"));
		this.setIconImage(img.getImage());
		this.about.setIconImage(img.getImage());
		String osName = System.getProperty("os.name").toLowerCase();
		boolean isMacOs = osName.startsWith("mac os x");
		try {
			List<String> validDirs = new ArrayList<String>();
			validDirs.add("initialing...");
			bgw = new BackgroundWorker(validDirs, this, 0);
			bgw.execute();
		} catch (IOException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, null, ex);
		}
		if (isMacOs) {
			try {
				Class util = Class.forName("com.apple.eawt.Application");
				Method getApplication = util.getMethod("getApplication", new Class[0]);
				Object application = getApplication.invoke(util);
				Class params[] = new Class[1];
				params[0] = Image.class;
				Method setDockIconImage = util.getMethod("setDockIconImage", params);
				URL url = Exactly.class.getClass().getResource("/uk/sipperfly/ui/resources/Exactly-logo.png");
				Image image = Toolkit.getDefaultToolkit().getImage(url);
				setDockIconImage.invoke(application, image);
				Method removeAboutMenuItem = util.getMethod("removeAboutMenuItem");
				removeAboutMenuItem.invoke(application);
			} catch (ClassNotFoundException e) {
				Logger.getLogger(GACOM).log(Level.SEVERE, null, e);
			} catch (NoSuchMethodException e) {
				Logger.getLogger(GACOM).log(Level.SEVERE, null, e);
				// log exception
			} catch (InvocationTargetException e) {
				Logger.getLogger(GACOM).log(Level.SEVERE, null, e);
				// log exception
			} catch (IllegalAccessException e) {
				Logger.getLogger(GACOM).log(Level.SEVERE, null, e);
				// log exception
			}
		}
                jScrollPane3.getVerticalScrollBar().setUnitIncrement(16);
        }

	/**
	 * Initialize the logger.
	 * Creates a log called "logfile.txt" in the current directory.
	 */
	public void initLogger() {
		logger = Logger.getLogger(GACOM);
		logger.setLevel(Level.INFO);
		try {
			filehandler = new FileHandler("logfile.txt");
			simpleformatter = new SimpleFormatter();
			filehandler.setFormatter(simpleformatter);

		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		logger.addHandler(filehandler);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING:
	 * Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jInternalFrame1 = new javax.swing.JInternalFrame();
        warning = new javax.swing.JDialog();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        about = new javax.swing.JDialog();
        aboutPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        aboutArea = new javax.swing.JEditorPane();
        jButton6 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        authorPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        authorArea = new javax.swing.JEditorPane();
        contactPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        contactArea = new javax.swing.JEditorPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        editInputDir1 = new javax.swing.JTextField();
        btnDirChoose1 = new javax.swing.JButton();
        serializeBag = new javax.swing.JCheckBox();
        jProgressBar2 = new javax.swing.JProgressBar();
        jPanel11 = new javax.swing.JPanel();
        jButton9 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        enableBagFields = new javax.swing.JCheckBox();
        jButton3 = new javax.swing.JButton();
        addFieldsButton = new javax.swing.JButton();
        hideTransfer = new javax.swing.JLabel();
        show = new javax.swing.JLabel();
        hide = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        showTransfer = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        bagNameField = new javax.swing.JTextField();
        editInputDir = new javax.swing.JTextField();
        btnDirChoose = new javax.swing.JButton();
        jLabel48 = new javax.swing.JLabel();
        ftpDelivery = new javax.swing.JCheckBox();
        jButton11 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        sftpDelivery = new javax.swing.JCheckBox();
        note = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        inputLocationDir = new javax.swing.JTextField();
        chooseDir = new javax.swing.JButton();
        validBagit = new javax.swing.JButton();
        unBag = new javax.swing.JButton();
        jLabel34 = new javax.swing.JLabel();
        destDirLocation = new javax.swing.JTextField();
        chooseDestDir = new javax.swing.JButton();
        unBaggingProgress = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        editCurrentStatus = new javax.swing.JTextArea();
        clearLog = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        mailServerField = new javax.swing.JTextField();
        userNameField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        jLabel43 = new javax.swing.JLabel();
        saveBtn = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        serverPort = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        sslProtocol = new javax.swing.JCheckBox();
        tlsProtocol = new javax.swing.JCheckBox();
        noneProtocol = new javax.swing.JCheckBox();
        emailNotifications = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        ftpHost = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        activeMode = new javax.swing.JRadioButton();
        passiveMode = new javax.swing.JRadioButton();
        ftpPort = new javax.swing.JTextField();
        ftpUser = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        ftpPass = new javax.swing.JPasswordField();
        jLabel18 = new javax.swing.JLabel();
        ftpDestination = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel7 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        saveEmailBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        currentTemplate = new javax.swing.JTextArea();
        clearTempButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        btnTransferFiles = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        sftp = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        sftp_connect = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        sftp_privateKey = new javax.swing.JTextField();
        jButton16 = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        sftp_passPhrase = new javax.swing.JPasswordField();
        jLabel25 = new javax.swing.JLabel();
        sftp_host = new javax.swing.JTextField();
        sftp_port = new javax.swing.JTextField();
        sftp_dest = new javax.swing.JTextField();
        sftp_type = new javax.swing.JComboBox<>();
        sftp_user = new javax.swing.JTextField();
        sftp_pass = new javax.swing.JPasswordField();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu4 = new javax.swing.JMenu();
        export = new javax.swing.JMenuItem();
        importXml = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        quit = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        emailNotification = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        emailSetting = new javax.swing.JMenuItem();
        ftpSettings = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 4, Short.MAX_VALUE)
        );

        warning.setTitle("Warning");
        warning.setMinimumSize(new java.awt.Dimension(450, 175));
        warning.setResizable(false);

        jLabel8.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel8.setText("No metadata values have been changed from the previous transfer.");

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel9.setText(" Are you sure that you want to proceed?");

        jButton13.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton13.setText("No");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton14.setText("Yes");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout warningLayout = new javax.swing.GroupLayout(warning.getContentPane());
        warning.getContentPane().setLayout(warningLayout);
        warningLayout.setHorizontalGroup(
            warningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(warningLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(warningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(warningLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, warningLayout.createSequentialGroup()
                        .addGap(0, 1, Short.MAX_VALUE)
                        .addComponent(jLabel8)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, warningLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton14)
                .addGap(12, 12, 12))
        );
        warningLayout.setVerticalGroup(
            warningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(warningLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(warningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton13)
                    .addComponent(jButton14))
                .addContainerGap(54, Short.MAX_VALUE))
        );

        about.setTitle("About");
        about.setMinimumSize(new java.awt.Dimension(700, 535));
        about.setName("About"); // NOI18N

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Verdana", 1, 13)); // NOI18N
        jLabel1.setText("Description");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        aboutArea.setMinimumSize(new java.awt.Dimension(106, 150));
        aboutArea.setPreferredSize(new java.awt.Dimension(106, 150));
        aboutArea.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                aboutAreaHyperlinkUpdate(evt);
            }
        });
        jScrollPane6.setViewportView(aboutArea);

        javax.swing.GroupLayout aboutPanelLayout = new javax.swing.GroupLayout(aboutPanel);
        aboutPanel.setLayout(aboutPanelLayout);
        aboutPanelLayout.setHorizontalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        aboutPanelLayout.setVerticalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6)
                .addContainerGap())
        );

        jButton6.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton6.setText("About");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jButton10.setText("Author and License");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton12.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton12.setText("Contact");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        authorPanel.setMinimumSize(new java.awt.Dimension(517, 445));
        authorPanel.setPreferredSize(new java.awt.Dimension(517, 445));

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 13)); // NOI18N
        jLabel4.setText("Author and License");
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        authorArea.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        authorArea.setMinimumSize(new java.awt.Dimension(106, 150));
        authorArea.setPreferredSize(new java.awt.Dimension(106, 150));
        jScrollPane7.setViewportView(authorArea);

        javax.swing.GroupLayout authorPanelLayout = new javax.swing.GroupLayout(authorPanel);
        authorPanel.setLayout(authorPanelLayout);
        authorPanelLayout.setHorizontalGroup(
            authorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(authorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane7)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        authorPanelLayout.setVerticalGroup(
            authorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authorPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addContainerGap())
        );

        contactPanel.setMinimumSize(new java.awt.Dimension(517, 445));
        contactPanel.setPreferredSize(new java.awt.Dimension(517, 445));

        jLabel5.setFont(new java.awt.Font("Verdana", 1, 13)); // NOI18N
        jLabel5.setText("Contact");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        contactArea.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        contactArea.setMinimumSize(new java.awt.Dimension(500, 418));
        contactArea.setPreferredSize(new java.awt.Dimension(500, 22));
        jScrollPane8.setViewportView(contactArea);

        javax.swing.GroupLayout contactPanelLayout = new javax.swing.GroupLayout(contactPanel);
        contactPanel.setLayout(contactPanelLayout);
        contactPanelLayout.setHorizontalGroup(
            contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contactPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE))
                .addContainerGap())
        );
        contactPanelLayout.setVerticalGroup(
            contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contactPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8)
                .addGap(23, 23, 23))
        );

        javax.swing.GroupLayout aboutLayout = new javax.swing.GroupLayout(about.getContentPane());
        about.getContentPane().setLayout(aboutLayout);
        aboutLayout.setHorizontalGroup(
            aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contactPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(authorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aboutPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        aboutLayout.setVerticalGroup(
            aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutLayout.createSequentialGroup()
                .addGroup(aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, aboutLayout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(jButton6)
                        .addGap(18, 18, 18)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton12)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, aboutLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(contactPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                            .addComponent(authorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                            .addComponent(aboutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(52, 52, 52))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Exactly 0.1.6");
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                formPropertyChange(evt);
            }
        });

        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        jPanel5.setPreferredSize(new java.awt.Dimension(600, 543));

        jScrollPane3.setPreferredSize(new java.awt.Dimension(571, 511));

        jPanel1.setAutoscrolls(true);

        jLabel49.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel49.setText("Destination");
        jLabel49.setMaximumSize(new java.awt.Dimension(96, 17));
        jLabel49.setMinimumSize(new java.awt.Dimension(96, 17));
        jLabel49.setPreferredSize(new java.awt.Dimension(96, 17));

        editInputDir1.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        editInputDir1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                editInputDir1PropertyChange(evt);
            }
        });

        btnDirChoose1.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        btnDirChoose1.setText("Browse");
        btnDirChoose1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDirChoose1ActionPerformed(evt);
            }
        });

        serializeBag.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        serializeBag.setText("Zip files?");

        jProgressBar2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jProgressBar2PropertyChange(evt);
            }
        });

        jPanel11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(128, 128, 128), 1, true));
        jPanel11.setAutoscrolls(true);
        jPanel11.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jPanel11PropertyChange(evt);
            }
        });

        jButton9.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton9.setText("Save");
        jButton9.setAutoscrolls(true);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jButton9.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jButton9PropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 43, Short.MAX_VALUE)
        );

        jButton1.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton1.setText("Add Fields");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        enableBagFields.setText("Show all reserved fields");
        enableBagFields.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableBagFieldsActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton3.setText("Save");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        addFieldsButton.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        addFieldsButton.setText("Add Fields");
        addFieldsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        addFieldsButton.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                addFieldsButtonPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(enableBagFields)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3)
                        .addGap(49, 49, 49))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(addFieldsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton9)
                        .addGap(47, 47, 47))))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(43, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(enableBagFields)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton9)
                    .addComponent(addFieldsButton))
                .addGap(24, 24, 24))
        );

        hideTransfer.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        hideTransfer.setForeground(new java.awt.Color(0, 51, 255));
        hideTransfer.setText("hide");
        hideTransfer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                hideTransferMouseClicked(evt);
            }
        });
        hideTransfer.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                hideTransferPropertyChange(evt);
            }
        });

        show.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        show.setText("v");
        show.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                showPropertyChange(evt);
            }
        });

        hide.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        hide.setText(">");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel2.setText("Metadata");

        showTransfer.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        showTransfer.setForeground(new java.awt.Color(0, 51, 255));
        showTransfer.setText("show");
        showTransfer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showTransferMouseClicked(evt);
            }
        });

        jLabel47.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel47.setText("Title");
        jLabel47.setMaximumSize(new java.awt.Dimension(96, 17));
        jLabel47.setMinimumSize(new java.awt.Dimension(96, 17));
        jLabel47.setPreferredSize(new java.awt.Dimension(96, 17));

        bagNameField.setMinimumSize(new java.awt.Dimension(6, 23));
        bagNameField.setPreferredSize(new java.awt.Dimension(6, 23));
        bagNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bagNameFieldActionPerformed(evt);
            }
        });

        editInputDir.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        editInputDir.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                editInputDirPropertyChange(evt);
            }
        });

        btnDirChoose.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        btnDirChoose.setText("Browse");
        btnDirChoose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDirChooseActionPerformed(evt);
            }
        });

        jLabel48.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel48.setText("Source");
        jLabel48.setMaximumSize(new java.awt.Dimension(96, 17));
        jLabel48.setMinimumSize(new java.awt.Dimension(96, 17));
        jLabel48.setPreferredSize(new java.awt.Dimension(96, 17));

        ftpDelivery.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        ftpDelivery.setText("FTP delivery");

        jButton11.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton11.setText("+");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 2, Short.MAX_VALUE)
        );

        sftpDelivery.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        sftpDelivery.setText("SFTP delivery");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(47, 47, 47)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bagNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(editInputDir, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDirChoose)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton11))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(21, 21, 21)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(serializeBag)
                                        .addGap(18, 18, 18)
                                        .addComponent(ftpDelivery)
                                        .addGap(18, 18, 18)
                                        .addComponent(sftpDelivery))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(editInputDir1, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnDirChoose1))
                                    .addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(hide, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(show, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(showTransfer)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hideTransfer)))))
                .addContainerGap(41, Short.MAX_VALUE))
            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bagNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDirChoose)
                    .addComponent(jButton11)
                    .addComponent(editInputDir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serializeBag)
                    .addComponent(ftpDelivery, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sftpDelivery))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editInputDir1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDirChoose1)
                    .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(show)
                    .addComponent(hide, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(hideTransfer)
                    .addComponent(showTransfer))
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(152, 152, 152))
        );

        jScrollPane3.setViewportView(jPanel1);

        note.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        note.setText("To copy and paste, use Windows shortcuts: \"control+c\" and \"control+v\"");
        note.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                notePropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(note, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(210, 210, 210))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(note)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Deliver", jPanel5);

        jLabel33.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel33.setText("Location");
        jLabel33.setMaximumSize(new java.awt.Dimension(96, 17));
        jLabel33.setMinimumSize(new java.awt.Dimension(96, 17));
        jLabel33.setPreferredSize(new java.awt.Dimension(96, 17));

        inputLocationDir.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        inputLocationDir.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray));
        inputLocationDir.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                inputLocationDirPropertyChange(evt);
            }
        });

        chooseDir.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        chooseDir.setText("Browse");
        chooseDir.setMaximumSize(new java.awt.Dimension(95, 25));
        chooseDir.setMinimumSize(new java.awt.Dimension(95, 25));
        chooseDir.setPreferredSize(new java.awt.Dimension(95, 25));
        chooseDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseDirActionPerformed(evt);
            }
        });

        validBagit.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        validBagit.setText("Validate");
        validBagit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validBagitActionPerformed(evt);
            }
        });

        unBag.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        unBag.setText("Unpack");
        unBag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unBagActionPerformed(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel34.setText("Destination");
        jLabel34.setMaximumSize(new java.awt.Dimension(96, 17));
        jLabel34.setMinimumSize(new java.awt.Dimension(96, 17));
        jLabel34.setPreferredSize(new java.awt.Dimension(96, 17));

        destDirLocation.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        destDirLocation.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(192, 192, 192)));
        destDirLocation.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                destDirLocationPropertyChange(evt);
            }
        });

        chooseDestDir.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        chooseDestDir.setText("Browse");
        chooseDestDir.setMaximumSize(new java.awt.Dimension(95, 25));
        chooseDestDir.setMinimumSize(new java.awt.Dimension(95, 25));
        chooseDestDir.setPreferredSize(new java.awt.Dimension(95, 25));
        chooseDestDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseDestDirActionPerformed(evt);
            }
        });

        unBaggingProgress.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                unBaggingProgressPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(100, 100, 100)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(inputLocationDir, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(chooseDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(unBag, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(validBagit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(unBaggingProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destDirLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chooseDestDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(96, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputLocationDir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseDir, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(validBagit)
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(destDirLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chooseDestDir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(unBag)
                .addGap(18, 18, 18)
                .addComponent(unBaggingProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(409, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Receive", jPanel4);

        editCurrentStatus.setColumns(20);
        editCurrentStatus.setRows(5);
        editCurrentStatus.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                editCurrentStatusPropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(editCurrentStatus);

        clearLog.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        clearLog.setText("clear log");
        clearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearLogActionPerformed(evt);
            }
        });

        jPanel9.setMinimumSize(new java.awt.Dimension(64, 48));
        jPanel9.setPreferredSize(new java.awt.Dimension(622, 1045));
        jPanel9.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jPanel9PropertyChange(evt);
            }
        });

        jLabel39.setFont(new java.awt.Font("Verdana", 1, 15)); // NOI18N
        jLabel39.setText("Email Settings");

        jLabel40.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel40.setText("Mail Server");

        jLabel41.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel41.setText("User name");

        jLabel42.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel42.setText("Password");

        mailServerField.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        mailServerField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mailServerFieldActionPerformed(evt);
            }
        });

        userNameField.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        passwordField.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        jLabel43.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 0, 102));
        jLabel43.setText("Provide smtp configurations for sending email.");

        saveBtn.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        saveBtn.setText("Check and Save");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton4.setText("Close");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel6.setText("Port");

        serverPort.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel7.setText("Encryption Method");

        sslProtocol.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        sslProtocol.setText("SSL Protocols");
        sslProtocol.setToolTipText("");
        sslProtocol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sslProtocolActionPerformed(evt);
            }
        });

        tlsProtocol.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        tlsProtocol.setText("TLS Protocols");
        tlsProtocol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tlsProtocolActionPerformed(evt);
            }
        });

        noneProtocol.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        noneProtocol.setText("None");
        noneProtocol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noneProtocolActionPerformed(evt);
            }
        });

        emailNotifications.setText("Enable email notifications");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel42)
                    .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel40)
                        .addGap(56, 56, 56)
                        .addComponent(mailServerField, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel41)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sslProtocol))
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addComponent(tlsProtocol)
                                .addGap(56, 56, 56)
                                .addComponent(noneProtocol))
                            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel9Layout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton4)
                                    .addGap(18, 18, 18)
                                    .addComponent(saveBtn))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel9Layout.createSequentialGroup()
                                    .addGap(12, 12, 12)
                                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel43)
                                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(jPanel9Layout.createSequentialGroup()
                                                .addComponent(serverPort, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(emailNotifications))
                                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                .addContainerGap(128, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel40)
                    .addComponent(mailServerField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel41)
                    .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(emailNotifications))
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sslProtocol)
                    .addComponent(tlsProtocol)
                    .addComponent(noneProtocol))
                .addGap(18, 18, 18)
                .addComponent(jLabel43)
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(saveBtn))
                .addContainerGap(128, Short.MAX_VALUE))
        );

        jPanel3.setMinimumSize(new java.awt.Dimension(64, 48));
        jPanel3.setPreferredSize(new java.awt.Dimension(622, 1045));
        jPanel3.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jPanel3PropertyChange(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Verdana", 1, 15)); // NOI18N
        jLabel11.setText("FTP Settings");

        jLabel12.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel12.setText("Host");

        ftpHost.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel13.setText("Port");

        jLabel14.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel14.setText("Username");

        jLabel16.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel16.setText("Password");

        jLabel17.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel17.setText("Data Connection Mode");

        activeMode.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        activeMode.setText("Active");
        activeMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeModeActionPerformed(evt);
            }
        });

        passiveMode.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        passiveMode.setText("Passive");
        passiveMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passiveModeActionPerformed(evt);
            }
        });

        ftpPort.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        ftpPort.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        ftpUser.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        jButton7.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton7.setText("Close");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton8.setText("Validate and Save");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        ftpPass.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel18.setText("Destination");

        ftpDestination.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel18))
                                .addGap(14, 14, 14)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ftpUser, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ftpHost, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ftpPort, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ftpPass, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ftpDestination, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addGap(53, 53, 53)
                                .addComponent(activeMode)
                                .addGap(31, 31, 31)
                                .addComponent(passiveMode))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(173, 173, 173)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)))
                .addContainerGap(175, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftpHost, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(ftpPort, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftpUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftpPass, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftpDestination, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(activeMode)
                    .addComponent(passiveMode))
                .addGap(39, 39, 39)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane2.setBorder(null);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(622, 1045));
        jScrollPane2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jScrollPane2PropertyChange(evt);
            }
        });

        jPanel7.setAutoscrolls(true);
        jPanel7.setMinimumSize(new java.awt.Dimension(64, 48));
        jPanel7.setPreferredSize(new java.awt.Dimension(603, 450));

        jLabel35.setFont(new java.awt.Font("Verdana", 1, 15)); // NOI18N
        jLabel35.setText("Email Notification");

        jLabel36.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 0, 102));
        jLabel36.setText("Send email notification(s) to:");

        saveEmailBtn.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        saveEmailBtn.setText("Save");
        saveEmailBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveEmailBtnActionPerformed(evt);
            }
        });

        jPanel2.setPreferredSize(new java.awt.Dimension(569, 0));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jButton2.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton2.setText("+");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton5.setText("Close");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(62, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(0, 50, Short.MAX_VALUE)
                                .addComponent(jLabel36)
                                .addGap(224, 224, 224))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(jButton2)
                        .addGap(51, 51, 51))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(saveEmailBtn)
                        .addGap(66, 66, 66))))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addGap(16, 16, 16)
                .addComponent(jLabel36)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveEmailBtn)
                    .addComponent(jButton5))
                .addContainerGap(517, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(jPanel7);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/sipperfly/ui/resources/Exactly1.png"))); // NOI18N

        currentTemplate.setBackground(new java.awt.Color(240, 240, 240));
        currentTemplate.setColumns(20);
        currentTemplate.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        currentTemplate.setRows(3);
        currentTemplate.setText(" Current Template: None");
        currentTemplate.setBorder(null);
        currentTemplate.setCaretColor(new java.awt.Color(204, 204, 204));
        currentTemplate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                currentTemplatePropertyChange(evt);
            }
        });
        jScrollPane4.setViewportView(currentTemplate);

        clearTempButton.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        clearTempButton.setText("Clear Template");
        clearTempButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearTempButtonActionPerformed(evt);
            }
        });
        clearTempButton.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                clearTempButtonPropertyChange(evt);
            }
        });

        btnTransferFiles.setFont(new java.awt.Font("Verdana", 0, 17)); // NOI18N
        btnTransferFiles.setText("Transfer");
        btnTransferFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferFilesActionPerformed(evt);
            }
        });

        btnCancel.setFont(new java.awt.Font("Verdana", 0, 17)); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnCancel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnCancelPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTransferFiles)
                .addGap(31, 31, 31))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnTransferFiles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sftp.setMinimumSize(new java.awt.Dimension(64, 48));
        sftp.setPreferredSize(new java.awt.Dimension(622, 1045));
        sftp.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                sftpPropertyChange(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Verdana", 1, 15)); // NOI18N
        jLabel10.setText("SFTP Settings");

        jLabel15.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel15.setText("Host");

        jLabel19.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel19.setText("Port");

        jLabel20.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel20.setText("Connect Using");

        jLabel21.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel21.setText("Username");

        jLabel22.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel22.setText("Password");

        sftp_connect.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                sftp_connectPropertyChange(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel24.setText("Private Key");

        sftp_privateKey.setEditable(false);
        sftp_privateKey.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        jButton16.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton16.setText("Browse");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel26.setText("Pass Phrase");

        sftp_passPhrase.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        javax.swing.GroupLayout sftp_connectLayout = new javax.swing.GroupLayout(sftp_connect);
        sftp_connect.setLayout(sftp_connectLayout);
        sftp_connectLayout.setHorizontalGroup(
            sftp_connectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sftp_connectLayout.createSequentialGroup()
                .addGroup(sftp_connectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addGap(18, 18, 18)
                .addGroup(sftp_connectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sftp_connectLayout.createSequentialGroup()
                        .addComponent(sftp_privateKey, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton16))
                    .addComponent(sftp_passPhrase, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        sftp_connectLayout.setVerticalGroup(
            sftp_connectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sftp_connectLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sftp_connectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sftp_privateKey, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(sftp_connectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sftp_passPhrase, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26)))
        );

        jLabel25.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jLabel25.setText("Destination");

        sftp_host.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        sftp_port.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        sftp_dest.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        sftp_type.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        sftp_type.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "User/Password", "User/Private Key" }));
        sftp_type.setMinimumSize(new java.awt.Dimension(6, 23));
        sftp_type.setPreferredSize(new java.awt.Dimension(6, 23));
        sftp_type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sftp_typeActionPerformed(evt);
            }
        });

        sftp_user.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        sftp_pass.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        jButton17.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton17.setText("Close");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jButton18.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N
        jButton18.setText("Validate and Save");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sftpLayout = new javax.swing.GroupLayout(sftp);
        sftp.setLayout(sftpLayout);
        sftpLayout.setHorizontalGroup(
            sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sftpLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sftp_connect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(sftpLayout.createSequentialGroup()
                        .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(sftpLayout.createSequentialGroup()
                                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel19)
                                    .addComponent(jLabel25))
                                .addGap(30, 30, 30)
                                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sftp_dest, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sftp_port, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sftp_host, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(sftpLayout.createSequentialGroup()
                                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel22)
                                    .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel21)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sftp_user, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sftp_type, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sftp_pass, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sftpLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton17)
                .addGap(18, 18, 18)
                .addComponent(jButton18)
                .addGap(58, 58, 58))
        );
        sftpLayout.setVerticalGroup(
            sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sftpLayout.createSequentialGroup()
                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(sftpLayout.createSequentialGroup()
                        .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(sftpLayout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel15)
                                    .addGroup(sftpLayout.createSequentialGroup()
                                        .addComponent(sftp_host, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(3, 3, 3)))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel19))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sftpLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(sftp_port, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(50, 50, 50))
                    .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sftp_dest, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel25)))
                .addGap(14, 14, 14)
                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sftp_type, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addGap(17, 17, 17)
                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sftp_user, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addGap(18, 18, 18)
                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(sftp_pass, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sftp_connect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addGroup(sftpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton17)
                    .addComponent(jButton18))
                .addContainerGap(139, Short.MAX_VALUE))
        );

        jMenu4.setText("Exactly");

        export.setText("Export");
        export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportActionPerformed(evt);
            }
        });
        jMenu4.add(export);

        importXml.setText("Import");
        importXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importXmlActionPerformed(evt);
            }
        });
        jMenu4.add(importXml);

        jMenuItem1.setText("About");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem1);

        quit.setText("Quit");
        quit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitActionPerformed(evt);
            }
        });
        jMenu4.add(quit);

        jMenuBar1.add(jMenu4);

        jMenu1.setText("Preferences");

        emailNotification.setText("Email Notifications");
        emailNotification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailNotificationActionPerformed(evt);
            }
        });
        jMenu1.add(emailNotification);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Admin");

        emailSetting.setText("Email Settings");
        emailSetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailSettingActionPerformed(evt);
            }
        });
        jMenu2.add(emailSetting);

        ftpSettings.setText("FTP Settings");
        ftpSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ftpSettingsActionPerformed(evt);
            }
        });
        jMenu2.add(ftpSettings);

        jMenuItem2.setText("SFTP Settings");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(sftp, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 627, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                                    .addComponent(clearLog, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(clearTempButton, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(82, 82, 82))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(13, 13, 13)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(clearLog)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearTempButton))
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 518, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sftp, javax.swing.GroupLayout.PREFERRED_SIZE, 624, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void clearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearLogActionPerformed
		// TODO add your handling code here:
		UpdateTransferResult("");
	}//GEN-LAST:event_clearLogActionPerformed

	private void emailSettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailSettingActionPerformed
		// TODO add your handling code here:
		this.jTabbedPane1.setVisible(false);
//		this.jScrollPane3.setVisible(false);
		this.jScrollPane2.setVisible(false);
		this.jPanel3.setVisible(false);
		this.jPanel9.setVisible(true);
		this.jPanel6.setVisible(false);
		this.sftp.setVisible(false);
	}//GEN-LAST:event_emailSettingActionPerformed

	private void saveEmailBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveEmailBtnActionPerformed
		String message = "";
		if (this.uIManager.validateEmails()) {
			message = this.uIManager.saveRecipientEmail();
		} else {
			message = "Invalid Email Address.";
		}
		if (this.editCurrentStatus.getText().isEmpty() || this.editCurrentStatus.getText() == null) {
			UpdateResult(message, 0);
		} else {
			UpdateResult(message, 1);
		}
	}//GEN-LAST:event_saveEmailBtnActionPerformed

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
		// TODO add your handling code here:
		this.jTabbedPane1.setVisible(true);
		if (this.jTabbedPane1.getSelectedIndex() == 0) {
			this.jPanel6.setVisible(true);
		} else {
			this.jPanel6.setVisible(false);
		}
		this.jScrollPane2.setVisible(false);
	}//GEN-LAST:event_jButton5ActionPerformed

	private void emailNotificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailNotificationActionPerformed

		this.jTabbedPane1.setVisible(false);
		this.jPanel9.setVisible(false);
		this.jPanel3.setVisible(false);
		this.jScrollPane2.setVisible(true);
		this.jPanel6.setVisible(false);
		this.sftp.setVisible(false);
	}//GEN-LAST:event_emailNotificationActionPerformed

	private void editCurrentStatusPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_editCurrentStatusPropertyChange
		// TODO add your handling code here:
		this.editCurrentStatus.setEditable(false);
		this.editCurrentStatus.setLineWrap(true);
		this.editCurrentStatus.setWrapStyleWord(true);
		DefaultCaret caret = (DefaultCaret) this.editCurrentStatus.getCaret();
		caret.setUpdatePolicy(ALWAYS_UPDATE);
	}//GEN-LAST:event_editCurrentStatusPropertyChange

	private void chooseDestDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseDestDirActionPerformed
		// TODO add your handling code here:
		fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			targetPath = file.getAbsolutePath();
			destDirLocation.setText(targetPath);
		}
	}//GEN-LAST:event_chooseDestDirActionPerformed

	private void unBagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unBagActionPerformed
		UIDefaults defaults = new UIDefaults();

		defaults.put("ProgressBar[Enabled].foregroundPainter", new MyPainter(new Color(0, 102, 0)));
		defaults.put("ProgressBar[Enabled+Finished].foregroundPainter", new MyPainter(new Color(0, 102, 0)));
		this.unBaggingProgress.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
		this.unBaggingProgress.putClientProperty("Nimbus.Overrides", defaults);

		String location = destDirLocation.getText();
		List<String> validDirs = new ArrayList<String>();
		String sourcelocation = this.inputLocationDir.getText();
		if (sourcelocation.isEmpty() || sourcelocation == null) {
			UpdateResult("Must choose a source folder.", 1);
			return;
		}
		if (!location.isEmpty() && location != null) {
			validDirs.add(location);
			File f = new File(destDirLocation.getText());
			if (!f.exists()) {
				UpdateResult("Must choose a valid destination folder.", 1);
				return;
			}
			try {
				bgw = new BackgroundWorker(validDirs, this, 4);
				bgw.execute();
			} catch (IOException ex) {
				Logger.getLogger(GACOM).log(Level.SEVERE, null, ex);
			}
		} else {
			UpdateResult("Must choose destination folder.", 1);
			return;
		}
	}//GEN-LAST:event_unBagActionPerformed

	private void validBagitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validBagitActionPerformed

		String location = this.inputLocationDir.getText();
		List<String> validDirs = new ArrayList<String>();
		if (!location.isEmpty() && location != null) {
			validDirs.add(location);
			try {
				bgw = new BackgroundWorker(validDirs, this, 3);
				bgw.execute();
			} catch (IOException ex) {
				Logger.getLogger(GACOM).log(Level.SEVERE, null, ex);
			}
		} else {
			UpdateResult("Must choose a source folder.", 1);
			return;
		}
	}//GEN-LAST:event_validBagitActionPerformed

	private void chooseDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseDirActionPerformed
		fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fileChooser.showOpenDialog(this);
		List<String> validDirs = new ArrayList<String>();
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			inputDirPath = file.getAbsolutePath();
			String msg = "Working on directory ".concat(inputDirPath);
			Logger.getLogger(GACOM).log(Level.INFO, msg);
			UpdateResult(msg, 1);
			inputLocationDir.setText(inputDirPath);
			if (!inputDirPath.isEmpty() && inputDirPath != null) {
				validDirs.add(inputDirPath);
				try {
					bgw = new BackgroundWorker(validDirs, this, 2);
					bgw.execute();
				} catch (IOException ex) {
					Logger.getLogger(GACOM).log(Level.SEVERE, null, ex);
				}
			}
		}
	}//GEN-LAST:event_chooseDirActionPerformed

	private void jPanel9PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jPanel9PropertyChange
		// TODO add your handling code here:
		this.jPanel9.setVisible(false);
	}//GEN-LAST:event_jPanel9PropertyChange

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
		// TODO add your handling code here:
		this.jTabbedPane1.setVisible(true);
		if (this.jTabbedPane1.getSelectedIndex() == 0) {
			this.jPanel6.setVisible(true);
		} else {
			this.jPanel6.setVisible(false);
		}
		this.jPanel9.setVisible(false);
	}//GEN-LAST:event_jButton4ActionPerformed

	private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
		// TODO add your handling code here:
		if (this.editCurrentStatus.getText().isEmpty() || this.editCurrentStatus.getText() == null) {
			UpdateResult("Saving email settings...", 0);
		} else {
			UpdateResult("Saving email settings...", 1);
		}
		if (this.uIManager.saveEmailSettings()) {
			UpdateResult("Email settings saved successfully.", 0);
		} else {
			UpdateResult("Invalid credentials. Please try again.", 0);
			this.uIManager.saveEmailNotification();
			UpdateResult("Email notification updated successfully.", 0);
			Logger.getLogger(GACOM).log(Level.SEVERE, "Invalid credentials. Please try again.");
			Logger.getLogger(GACOM).log(Level.INFO, "Email notification updated successfully.");
		}
	}//GEN-LAST:event_saveBtnActionPerformed

	private void mailServerFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mailServerFieldActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_mailServerFieldActionPerformed

	private void sslProtocolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sslProtocolActionPerformed
		// TODO add your handling code here:
		this.serverPort.setText("465");
		this.tlsProtocol.setSelected(false);
		this.noneProtocol.setSelected(false);
	}//GEN-LAST:event_sslProtocolActionPerformed

	private void tlsProtocolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tlsProtocolActionPerformed
		// TODO add your handling code here:
		this.serverPort.setText("587");
		this.sslProtocol.setSelected(false);
		this.noneProtocol.setSelected(false);
	}//GEN-LAST:event_tlsProtocolActionPerformed

	private void noneProtocolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noneProtocolActionPerformed
		// TODO add your handling code here:
		this.serverPort.setText("587");
		this.sslProtocol.setSelected(false);
		this.tlsProtocol.setSelected(false);
	}//GEN-LAST:event_noneProtocolActionPerformed

	private void ftpSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ftpSettingsActionPerformed
		// TODO add your handling code here:
		this.jTabbedPane1.setVisible(false);
//		this.jScrollPane3.setVisible(false);
		this.jPanel9.setVisible(false);
		this.jPanel3.setVisible(true);
		this.jScrollPane2.setVisible(false);
		this.jPanel6.setVisible(false);
		this.sftp.setVisible(false);
	}//GEN-LAST:event_ftpSettingsActionPerformed

	private void jPanel3PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jPanel3PropertyChange
		// TODO add your handling code here:
		this.jPanel3.setVisible(false);
	}//GEN-LAST:event_jPanel3PropertyChange

	private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
		// TODO add your handling code here:
		this.jTabbedPane1.setVisible(true);
		if (this.jTabbedPane1.getSelectedIndex() == 0) {
			this.jPanel6.setVisible(true);
		} else {
			this.jPanel6.setVisible(false);
		}
		this.jPanel9.setVisible(false);
		this.jPanel3.setVisible(false);
		this.jScrollPane2.setVisible(false);
	}//GEN-LAST:event_jButton7ActionPerformed

	private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
		if (this.editCurrentStatus.getText().isEmpty() || this.editCurrentStatus.getText() == null) {
			UpdateResult("Validating and saving FTP settings...", 0);
		} else {
			UpdateResult("Validating and saving FTP settings... ", 1);
		}
		try {
			if (this.uIManager.saveFTPSettings()) {
				UpdateResult("FTP settings validated and saved successfully.", 0);
			} else {
				UpdateResult("Invalid credentials. Please try again.", 0);
				Logger.getLogger(GACOM).log(Level.SEVERE, "Invalid credentials. Please try again.");
			}
		} catch (IOException ex) {
			Logger.getLogger(Exactly.class.getName()).log(Level.SEVERE, null, ex);
		}
	}//GEN-LAST:event_jButton8ActionPerformed

	private void formPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_formPropertyChange
		this.setResizable(false);

	}//GEN-LAST:event_formPropertyChange

	private void quitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitActionPerformed
		System.exit(0);
	}//GEN-LAST:event_quitActionPerformed

	private void activeModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeModeActionPerformed
		this.passiveMode.setSelected(false);
	}//GEN-LAST:event_activeModeActionPerformed

	private void passiveModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passiveModeActionPerformed
		this.activeMode.setSelected(false);
	}//GEN-LAST:event_passiveModeActionPerformed

	private void exportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportActionPerformed

		fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setApproveButtonText("Export");
		removeFileTypeComponents(fileChooser);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String exportPath = file.getAbsolutePath().toString();
			try {
				UpdateResult("Exporting Info", 1);
				this.uIManager.exportInfo(exportPath);
				UpdateResult("Done exporting...", 0);
			} catch (IOException ex) {
				Logger.getLogger(Exactly.class.getName()).log(Level.SEVERE, null, ex);
			} catch (ParserConfigurationException ex) {
				Logger.getLogger(Exactly.class.getName()).log(Level.SEVERE, null, ex);
			} catch (Exception ex) {
				Logger.getLogger(Exactly.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}//GEN-LAST:event_exportActionPerformed

	private void removeFileTypeComponents(Container con) {
		Component[] components = con.getComponents();
		for (Component component : components) {
			if (component instanceof JComboBox) {
				Object sel = ((JComboBox) component).getSelectedItem();
				if (sel.toString().contains("AcceptAllFileFilter")) {
//               component.setVisible(false);
					// OR
					con.remove(component);
				}
			}
			if (component instanceof JLabel) {
				String text = ((JLabel) component).getText();
				if (text.equals("Files of Type:")) {
//               component.setVisible(false);
					// OR
					con.remove(component);
				}
			}
			if (component instanceof Container) {
				removeFileTypeComponents((Container) component);
			}
		}
	}

	private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
		this.list.addEntry("");
	}//GEN-LAST:event_jButton11ActionPerformed

	private void btnDirChooseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDirChooseActionPerformed
		// TODO add your handling code here:
		fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setMultiSelectionEnabled(true);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			if (files.length >= 1) {
				inputDirPath = files[0].getAbsolutePath();
				editInputDir.setText(inputDirPath);
				for (int i = 1; i < files.length; i++) {
					this.list.addEntry(files[i].getAbsolutePath().toString());
				}
			}

		}
	}//GEN-LAST:event_btnDirChooseActionPerformed

	private void editInputDirPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_editInputDirPropertyChange
		// TODO add your handling code here:
		this.editInputDir.setEditable(false);
	}//GEN-LAST:event_editInputDirPropertyChange

	private void bagNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bagNameFieldActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_bagNameFieldActionPerformed

	private void btnTransferFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferFilesActionPerformed
//		int bagSize = 0;
//		if (this.bagSize.getText() != null && !this.bagSize.getText().equals("")) {
//			try {
//				bagSize = Integer.valueOf(this.bagSize.getText());
//			} catch (NumberFormatException ex) {
//				UpdateResult("Bag size must be a positive integer", 1);
//				return;
//			}
//			if (bagSize > 0) {
//				this.uIManager.saveBagSize();
//			}
//		}
//		if (bagSize <= 0) {
//			UpdateResult("Bag size must be greater than 0", 1);
//			return;
//		}
                System.out.println( "Initial" );
		if (this.MetadataReminder == 1) {
			UpdateResult("Save Metadata before starting Transfer.", 1);
			return;
		}

		if (!this.uIManager.validateTitle(this.bagNameField.getText())) {
			UpdateResult("Folder name " + this.bagNameField.getText() + " can't contain special characters < > \\ / ? * | \" :", 1);
			return;
		}
		if (!this.setDropLocation()) {
			this.btnTransferFiles.setEnabled(true);
			this.btnCancel.setVisible(false);
			return;
		}
		this.fileSystem = new StringBuilder();
		CommonUtil commonUtil = new CommonUtil();
		Boolean isSelected = false;
		List<String> directories = new ArrayList<String>();
		directories = this.uIManager.getInputDirectories();
		directories.add(editInputDir.getText());
		StringBuilder invalidNames = new StringBuilder();
		long size = 0;
		ConfigurationsRepo configRepo = new ConfigurationsRepo();
		Configurations config = configRepo.getOneOrCreateOne();
		sourceChecksum = "";
                System.out.println( "before dir scan" );
		for (String directory : directories) {
			if (!directory.isEmpty()) {
                                System.out.println( "Dir scanning" + directory.toString() );
				isSelected = true;
				File f = new File(directory);
				if (!f.exists()) {
					UpdateResult("Must choose a valid input folder(s).", 1);
					return;
				} else if (f.isFile()) {
                                        
						this.totalFiles = this.totalFiles + 1;
						sourceChecksum += f.getName() + "_|_" + commonUtil.checkSum(f.getAbsolutePath()) + "\n";
                                                System.out.println( "File: " + f.getName() );
				} else {
					this.totalFiles = this.totalFiles + commonUtil.countFilesInDirectory(f, config.getFilters());
                                        System.out.println( "Dir Files count: " + this.totalFiles );
					sourceChecksum += commonUtil.getDirectoryChecksum(f);
				}
				this.uIManager.validateFolderName(f);

				if (this.uIManager.output != null && !this.uIManager.output.equals("")) {
					invalidNames.append(this.uIManager.output);
					this.uIManager.output = "";
				}
				String s;
				Process p;
				try {
                                        System.out.println("Startedprocesing: 2404");
					String osName = System.getProperty("os.name").toLowerCase();
					boolean isMacOs = osName.startsWith("mac os x");
					if (isMacOs) {
						p = Runtime.getRuntime().exec(new String[]{"find", f.getAbsolutePath(), "-type", "f", "-exec", "stat", "{}", "+"});
						this.fileSystem.append(f.getAbsolutePath());
						this.fileSystem.append(System.getProperty("line.separator"));
					} else {
						p = Runtime.getRuntime().exec("cmd /c dir \"" + f.getAbsolutePath() + "\" /S /A");
					}
                                        System.out.println("processing complete: 2414");
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					while ((s = br.readLine()) != null) {
						this.fileSystem.append(s);
						this.fileSystem.append(System.getProperty("line.separator"));
					}
					p.waitFor();
					p.destroy();
				} catch (Exception e) {
					System.out.println("error: " + e.toString());
				}
			}
		}

		if (invalidNames != null && invalidNames.length() > 0) {
			UpdateResult("Following Folder name(s) contain special characters < > \\ / ? * | \" :", 1);
			UpdateResult("Please rename before transferring", 0);
			UpdateResult(invalidNames.toString(), 0);
			return;
		}
		this.uploadedFiles = this.totalFiles;

		if (!isSelected) {
			UpdateResult("Must choose an input folder.", 1);
			return;
		}
		if (this.metadateUpdated == 0) {
                        this.warning.setAlwaysOnTop(true);
                        this.warning.setModal(true);
                        this.warning.setLocationRelativeTo(this);
			this.warning.setVisible(true);
		} else {
			this.jButton14ActionPerformed(evt);
		}
	}//GEN-LAST:event_btnTransferFilesActionPerformed

	private void btnCancelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnCancelPropertyChange
		this.btnCancel.setVisible(false);
	}//GEN-LAST:event_btnCancelPropertyChange

	private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed

		if (!this.bgw.isDone()) {
			UpdateResult("Canceling transfer...", 1);
			this.list.resetEntryList();
			try {
				this.bgw.cancel(true);
			} catch (CancellationException ca) {
				this.bgw.resetTransferFiles();
				Logger.getLogger(GACOM).log(Level.SEVERE, null, ca);
			}
			this.bgw.resetTransferFiles();
		}
		if (this.bgw.isDone()) {
			this.list.resetEntryList();
			this.bgw.resetTransferFiles();
		}
	}//GEN-LAST:event_btnCancelActionPerformed

	private void showTransferMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showTransferMouseClicked
		// TODO add your handling code here:
		this.hideTransfer.setVisible(true);
		this.show.setVisible(true);
		this.hide.setVisible(false);
		this.showTransfer.setVisible(false);
		this.jPanel11.setVisible(true);
		this.note.setVisible(true);
	}//GEN-LAST:event_showTransferMouseClicked

	private void showPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_showPropertyChange
		// TODO add your handling code here:
		this.show.setVisible(false);
	}//GEN-LAST:event_showPropertyChange

	private void hideTransferPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_hideTransferPropertyChange
		// TODO add your handling code here:
		this.hideTransfer.setVisible(false);
	}//GEN-LAST:event_hideTransferPropertyChange

	private void hideTransferMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hideTransferMouseClicked
		// TODO add your handling code here:
		this.hideTransfer.setVisible(false);
		this.show.setVisible(false);
		this.hide.setVisible(true);
		this.showTransfer.setVisible(true);
		this.jPanel11.setVisible(false);
		this.note.setVisible(false);
	}//GEN-LAST:event_hideTransferMouseClicked

	private void jPanel11PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jPanel11PropertyChange
		// TODO add your handling code here:
		this.jPanel11.setVisible(false);
	}//GEN-LAST:event_jPanel11PropertyChange

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		this.bagInfo.addEntry("", "");
		this.MetadataReminder = 1;
	}//GEN-LAST:event_jButton1ActionPerformed

	private void jButton9PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jButton9PropertyChange
		//        this.jButton9.setVisible(false);
	}//GEN-LAST:event_jButton9PropertyChange

	private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
		this.MetadataReminder = 0;
		UpdateResult("Validating Bag Info", 1);
		if (this.uIManager.validateBageInfo()) {
			if (this.uIManager.saveBagInfo()) {
				UpdateResult("Metadata saved successfully.", 0);
				this.metadateUpdated = 1;
				return;
			} else {
				UpdateResult("Something went wrong. Please try agian.", 0);
			}
		} else {
			UpdateResult("Label is required", 0);
			return;
		}
	}//GEN-LAST:event_jButton9ActionPerformed

	private void btnDirChoose1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDirChoose1ActionPerformed
		// TODO add your handling code here:
		fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			inputDirPath = file.getAbsolutePath();
			editInputDir1.setText(inputDirPath);
		}
	}//GEN-LAST:event_btnDirChoose1ActionPerformed

	private void editInputDir1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_editInputDir1PropertyChange
		this.editInputDir1.setEditable(false);
	}//GEN-LAST:event_editInputDir1PropertyChange

	private void unBaggingProgressPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_unBaggingProgressPropertyChange
		UIDefaults defaults = new UIDefaults();

		defaults.put("ProgressBar[Enabled].foregroundPainter", new MyPainter(new Color(0, 102, 0)));
		defaults.put("ProgressBar[Enabled+Finished].foregroundPainter", new MyPainter(new Color(0, 102, 0)));
	}//GEN-LAST:event_unBaggingProgressPropertyChange

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		this.email.addEntry();
	}//GEN-LAST:event_jButton2ActionPerformed

	private void jScrollPane2PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jScrollPane2PropertyChange
		this.jScrollPane2.setVisible(false);
//		this.jScrollPane2.set
	}//GEN-LAST:event_jScrollPane2PropertyChange

	private void enableBagFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableBagFieldsActionPerformed
		this.uIManager.enableORdisableFields();
	}//GEN-LAST:event_enableBagFieldsActionPerformed

	private void importXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importXmlActionPerformed
		fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);//FILES_AND_DIRECTORIES
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (file.getName().indexOf("xml") > 0) {
				UpdateResult("Importing xml.", 0);
				String message = this.uIManager.importXml(file.getAbsolutePath().toString());
				if (!message.equalsIgnoreCase("Invalid xml format")) {
					this.currentTemplate.setText("Current Template: ".concat(file.getName()));
					this.clearTempButton.setVisible(true);
				}
				UpdateResult(message, 0);
			} else {
				UpdateResult("Select xml file.", 0);
			}
		}
	}//GEN-LAST:event_importXmlActionPerformed

	private void inputLocationDirPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_inputLocationDirPropertyChange
		this.inputLocationDir.setEditable(false);
	}//GEN-LAST:event_inputLocationDirPropertyChange

	private void destDirLocationPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_destDirLocationPropertyChange
		this.destDirLocation.setEditable(false);
	}//GEN-LAST:event_destDirLocationPropertyChange

	private void jProgressBar2PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jProgressBar2PropertyChange
		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new MyPainter(new Color(0, 102, 0)));
		defaults.put("ProgressBar[Enabled+Finished].foregroundPainter", new MyPainter(new Color(0, 102, 0)));
	}//GEN-LAST:event_jProgressBar2PropertyChange

	private void notePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_notePropertyChange
		this.note.setVisible(false);
	}//GEN-LAST:event_notePropertyChange

	private void addFieldsButtonPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_addFieldsButtonPropertyChange
		this.addFieldsButton.setVisible(false);
	}//GEN-LAST:event_addFieldsButtonPropertyChange

	private void currentTemplatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_currentTemplatePropertyChange
		this.currentTemplate.setEditable(false);
		this.currentTemplate.setLineWrap(true);
		this.currentTemplate.setWrapStyleWord(true);
		DefaultCaret caret = (DefaultCaret) this.currentTemplate.getCaret();
		caret.setUpdatePolicy(ALWAYS_UPDATE);
	}//GEN-LAST:event_currentTemplatePropertyChange

	private void clearTempButtonPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_clearTempButtonPropertyChange
		this.clearTempButton.setVisible(false);
	}//GEN-LAST:event_clearTempButtonPropertyChange

	private void clearTempButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearTempButtonActionPerformed
		this.uIManager.resetDefaultTemplate();
		this.currentTemplate.setText("Current Template: None");
		this.clearTempButton.setVisible(false);
		this.UpdateResult("Reset to default template.", 1);
	}//GEN-LAST:event_clearTempButtonActionPerformed

	private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
		if (this.jTabbedPane1.getSelectedIndex() == 0) {
			this.jPanel6.setVisible(true);
		} else {
			this.jPanel6.setVisible(false);
		}
	}//GEN-LAST:event_jTabbedPane1MouseClicked

	private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
//		if (this.uIManager.isDefaultTemplate()) {
//			this.uIManager.resetMetadataValues(false);
//		} else {
//			this.uIManager.resetMetadata(false);
//		}
	}//GEN-LAST:event_formWindowClosing

	private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
		this.about.setVisible(true);
                this.authorPanel.setVisible(false);
		this.aboutPanel.setVisible(true);
		this.contactPanel.setVisible(false);
                this.aboutAreaContentPanel();
	}//GEN-LAST:event_jMenuItem1ActionPerformed
        private void aboutAreaContentPanel() {                                         
		this.aboutArea.setEditable(false);
		this.aboutArea.setContentType("text/html");

		this.aboutArea.setText("<html>Exactly 0.1.6<br>"
				+ "<p>Exactly was developed by the Louie B. Nunn Center at the University of Kentucky Libraries and AVP and can be found at <a href='https://www.weareavp.com/products/exactly'>www.weareavp.com/products/exactly</a>.</p><br>"
				+ "The GitHub repository for Exactly can be found at <a href='https://github.com/WeAreAVP/uk-exactly'>https://github.com/WeAreAVP/uk-exactly</a>.<br><br>"
				+ "<p>Exactly is a simple and easy to use application for remotely and safely transferring any born-digital material to the archive. Exactly is a user-friendly application that utilizes the BagIt File Packaging Format, supports FTP transfer, as well as standard network transfers, and integrates into desktop-based file sharing workflows such as Dropbox or Google Drive. Additionally, Exactly allows the archive to create preset configurations, as well as customized metadata templates for the donor to fill out before submission. With structured metadata coming into the archive with the digital object, the accessioning process can be much more efficiently. Exactly can send email notifications when files have been delivered to the archive. Exactly is addressing one of the Nunn Center's greatest workflow challenges, but also one of the greatest challenges facing any archive working with born-digital material.</p>"
				+ "</html>");
	}                
	private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
		this.authorPanel.setVisible(true);
		this.aboutPanel.setVisible(false);
		this.contactPanel.setVisible(false);
                this.authorAreaPanelContent();
	}//GEN-LAST:event_jButton10ActionPerformed

	private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
		this.authorPanel.setVisible(false);
		this.aboutPanel.setVisible(true);
		this.contactPanel.setVisible(false);
	}//GEN-LAST:event_jButton6ActionPerformed

	private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
		this.authorPanel.setVisible(false);
		this.aboutPanel.setVisible(false);
		this.contactPanel.setVisible(true);
                this.contactAreaPanel();
	}//GEN-LAST:event_jButton12ActionPerformed
private void contactAreaPanel() {                                           
		this.contactArea.setEditable(false);
		this.contactArea.setContentType("text/html");
		
		this.contactArea.setText("<html>Please post issues and feature requests at <a href='https://github.com/WeAreAVP/uk-exactly/issues'> https://github.com/WeAreAVP/uk-exactly/issues</a>.<br><br> Please send questions, comments or feedback to info@weareavp.com.</html>");
	}                                          

	private void authorAreaPanelContent() {                                          
		this.authorArea.setEditable(false);
		this.authorArea.setContentType("text/html");
		
		this.authorArea.setText("<html><b>Exactly Copyright and License</b><br><br>"
				+ "Copyright (C) 2015-2016 University of Kentucky Libraries.<br><br>"
				+ "Exactly is licensed under an Apache License."
				+ "<p>Exactly is a simple and easy to use application for remotely and safely transferring any born-digital material to the archive. Exactly is a user-friendly application that utilizes the BagIt File Packaging Format (an Internet Engineering Task-Force standard, developed by the Library of Congress and the California Digital Library, with current support from George Washington University and the University of Maryland), supports FTP transfer, as well as standard network transfers, and integrates into desktop-based file sharing workflows such as Dropbox or Google Drive. Additionally, Exactly allows the archive to create customized metadata templates for the donor to fill out before submission. With structured metadata coming into the archive with the digital object, the accessioning process will be a quick importing activity. Exactly can send email notifications when files have been delivered to the archive. Exactly is addressing one of the Nunn Center's greatest workflow challenges, but also one of the greatest challenges facing any archive working with born-digital material.</p>"
				+ "<br><b>Exactly License</b><br>"
				+ "<br>Apache License, Version 2<br>"
				+ "<br>Copyright (c) 2015-2016 University of Kentucky<br>"
				+ "<p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License.</p>"
				+ "You may obtain a copy of the License at<br><br>"
				+ "<a href='http://www.apache.org/licenses/LICENSE-2.0'>http://www.apache.org/licenses/LICENSE-2.0</a><br>"
				+ "<p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. </p>"
				+ "See the License for the specific language governing permissions and limitations under the License."
				+ "</html>");
                
                this.authorArea.setCaretPosition(0);
	}       
	private void aboutAreaHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_aboutAreaHyperlinkUpdate
		if (HyperlinkEvent.EventType.ACTIVATED.equals(evt.getEventType())) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(evt.getURL().toURI());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} 
	}//GEN-LAST:event_aboutAreaHyperlinkUpdate

	private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
		this.warning.setVisible(false);
		this.btnTransferFiles.setEnabled(false);
		this.btnCancel.setVisible(true);
		List<String> validDirs = new ArrayList<String>();
		List<String> directories = new ArrayList<String>();
		directories = this.uIManager.getInputDirectories();
		directories.add(editInputDir.getText());
		for (String directory : directories) {
			if (!directory.isEmpty()) {
				validDirs.add(directory);
			}
		}

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new MyPainter(new Color(0, 102, 0)));
		defaults.put("ProgressBar[Enabled+Finished].foregroundPainter", new MyPainter(new Color(0, 102, 0)));
		this.jProgressBar2.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
		this.jProgressBar2.putClientProperty("Nimbus.Overrides", defaults);

		try {
			bgw = new BackgroundWorker(validDirs, this, 1);
			bgw.execute();
		} catch (IOException ex) {
			this.btnCancel.setVisible(false);
			this.btnTransferFiles.setEnabled(true);
			Logger.getLogger(GACOM).log(Level.SEVERE, null, ex);
		}
	}//GEN-LAST:event_jButton14ActionPerformed

	private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
		this.warning.setVisible(false);
	}//GEN-LAST:event_jButton13ActionPerformed

	private void sftp_typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sftp_typeActionPerformed
		if (this.sftp_type.getSelectedIndex() == 1) {
			this.sftp_connect.setVisible(true);
			this.jLabel22.setVisible(false);
			this.sftp_pass.setVisible(false);
		} else {
			this.sftp_connect.setVisible(false);
			this.jLabel22.setVisible(true);
			this.sftp_pass.setVisible(true);
		}
	}//GEN-LAST:event_sftp_typeActionPerformed

	private void sftp_connectPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_sftp_connectPropertyChange
		this.sftp_connect.setVisible(false);
	}//GEN-LAST:event_sftp_connectPropertyChange

	private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
		this.jTabbedPane1.setVisible(false);
		this.jScrollPane2.setVisible(false);
		this.jPanel3.setVisible(false);
		this.jPanel9.setVisible(false);
		this.jPanel6.setVisible(false);
		this.sftp.setVisible(true);
	}//GEN-LAST:event_jMenuItem2ActionPerformed

	private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
		this.jTabbedPane1.setVisible(true);
		if (this.jTabbedPane1.getSelectedIndex() == 0) {
			this.jPanel6.setVisible(true);
		} else {
			this.jPanel6.setVisible(false);
		}
		this.sftp.setVisible(false);
	}//GEN-LAST:event_jButton17ActionPerformed

	private void sftpPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_sftpPropertyChange
		this.sftp.setVisible(false);
	}//GEN-LAST:event_sftpPropertyChange

	private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
		if (this.editCurrentStatus.getText().isEmpty() || this.editCurrentStatus.getText() == null) {
			UpdateResult("Validating and saving SFTP settings...", 0);
		} else {
			UpdateResult("Validating and saving SFTP settings... ", 1);
		}
		if (this.sftp_type.getSelectedIndex() == 1) {
			if (this.sftp_privateKey.getText().isEmpty()) {
				UpdateResult("Private Key is required", 0);
				return;
			}
			File file = new File(this.sftp_privateKey.getText());
			if (!file.exists()) {
				UpdateResult("Private Key file doesn't existed.", 0);
				return;
			}
		}
		try {
			if (this.uIManager.saveSFTPSettings()) {
				UpdateResult("SFTP settings validated and saved successfully.", 0);
			} else {
				UpdateResult("Invalid credentials. Please try again.", 0);
				Logger.getLogger(GACOM).log(Level.SEVERE, "Invalid credentials. Please try again.");
			}
		} catch (IOException ex) {
			Logger.getLogger(Exactly.class.getName()).log(Level.SEVERE, null, ex);
		}
	}//GEN-LAST:event_jButton18ActionPerformed

	private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
		fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			this.sftp_privateKey.setText(file.getAbsolutePath());
		}
	}//GEN-LAST:event_jButton16ActionPerformed

	/**
	 * Validates the folder string and verifies that an actual folder exists in the file system.
	 *
	 * @param folder The path to the folder
	 *
	 * @return True if the folder exists, false otherwise
	 */
	public boolean ValidateInputFolder(String folder) {
		if (folder == null || folder.length() < 1) {
			return false;
		}
		File f = new File(folder);
		return f.exists();
	}

	/**
	 * Updates the progress bar to the current count.
	 *
	 * @param count The current count
	 */
	public void UpdateProgressBar(int count) {
		this.jProgressBar2.setValue(count);
	}

	/**
	 * Updates the current status edit field with the given text.
	 *
	 * @param text The current status
	 */
	public void UpdateTransferResult(String text) {
		this.editCurrentStatus.setText(text);

	}

	public void UpdateResult(String text, int newline) {
		String result = this.editCurrentStatus.getText();
		String new_result = "";
		if (!result.isEmpty() && result != null) {
			if (newline == 1) {
				new_result = result + "\r\n\r\n" + text;
			} else {
				new_result = result + "\r\n" + text;
			}
			this.UpdateTransferResult(new_result);
		} else {
			this.UpdateTransferResult(text);
		}
	}

	public boolean setDropLocation() {
		String inputDirText = editInputDir1.getText();
		if (inputDirText == null || inputDirText.isEmpty()) {
			this.UpdateResult("Please select transfer destination.", 0);
			Logger.getLogger(GACOM).log(Level.SEVERE, "Please select transfer destination.");
			return false;
		}
		if (!inputDirText.equals(this.uIManager.getDestinationPath())) {
			if (!ValidateInputFolder(inputDirText)) {
				UpdateResult("Must choose a valid Transfer location path.", 0);
				this.uIManager.restoreTransferPath();
				return false;
			}
			UpdateResult("Saving Destination...", 0);
			String filters = "";//this.filterField.getText();
			this.uIManager.saveLocationAndFilter(inputDirText, filters);
			UpdateResult("Destination location updated successfully.", 0);
		}
		return true;
	}

	/**
	 * Returns the background worker thread.
	 *
	 * @return The thread
	 */
	public BackgroundWorker GetBackgroundWorker() {
		return this.bgw;
	}

	/**
	 * The main entry point of the application.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {


		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
                    String osName = System.getProperty("os.name").toLowerCase();
                    boolean isMacOs = osName.startsWith("mac os x");
                    if( isMacOs )
                    {
                        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
                    }else{
                        javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                    }
                    
                    
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(GACOM).log(java.util.logging.Level.SEVERE, null, ex);
		}
            //</editor-fold>
            
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Exactly().setVisible(true);
			}
		});

	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog about;
    private javax.swing.JEditorPane aboutArea;
    private javax.swing.JPanel aboutPanel;
    public javax.swing.JRadioButton activeMode;
    public javax.swing.JButton addFieldsButton;
    private javax.swing.JEditorPane authorArea;
    private javax.swing.JPanel authorPanel;
    public javax.swing.JTextField bagNameField;
    public javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDirChoose;
    private javax.swing.JButton btnDirChoose1;
    public javax.swing.JButton btnTransferFiles;
    private javax.swing.JButton chooseDestDir;
    private javax.swing.JButton chooseDir;
    private javax.swing.JButton clearLog;
    public javax.swing.JButton clearTempButton;
    private javax.swing.JEditorPane contactArea;
    private javax.swing.JPanel contactPanel;
    public javax.swing.JTextArea currentTemplate;
    public javax.swing.JTextField destDirLocation;
    public javax.swing.JTextArea editCurrentStatus;
    public javax.swing.JTextField editInputDir;
    public javax.swing.JTextField editInputDir1;
    private javax.swing.JMenuItem emailNotification;
    public javax.swing.JCheckBox emailNotifications;
    private javax.swing.JMenuItem emailSetting;
    public javax.swing.JCheckBox enableBagFields;
    private javax.swing.JMenuItem export;
    public javax.swing.JCheckBox ftpDelivery;
    public javax.swing.JTextField ftpDestination;
    public javax.swing.JTextField ftpHost;
    public javax.swing.JPasswordField ftpPass;
    public javax.swing.JTextField ftpPort;
    private javax.swing.JMenuItem ftpSettings;
    public javax.swing.JTextField ftpUser;
    public javax.swing.JLabel hide;
    public javax.swing.JLabel hideTransfer;
    private javax.swing.JMenuItem importXml;
    public javax.swing.JTextField inputLocationDir;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    public javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel10;
    public javax.swing.JPanel jPanel11;
    public javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    public javax.swing.JPanel jPanel7;
    public javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    public javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JTextField mailServerField;
    public javax.swing.JCheckBox noneProtocol;
    public javax.swing.JLabel note;
    public javax.swing.JRadioButton passiveMode;
    public javax.swing.JPasswordField passwordField;
    private javax.swing.JMenuItem quit;
    private javax.swing.JButton saveBtn;
    private javax.swing.JButton saveEmailBtn;
    public javax.swing.JCheckBox serializeBag;
    public javax.swing.JTextField serverPort;
    private javax.swing.JPanel sftp;
    public javax.swing.JCheckBox sftpDelivery;
    public javax.swing.JPanel sftp_connect;
    public javax.swing.JTextField sftp_dest;
    public javax.swing.JTextField sftp_host;
    public javax.swing.JPasswordField sftp_pass;
    public javax.swing.JPasswordField sftp_passPhrase;
    public javax.swing.JTextField sftp_port;
    public javax.swing.JTextField sftp_privateKey;
    public javax.swing.JComboBox<String> sftp_type;
    public javax.swing.JTextField sftp_user;
    public javax.swing.JLabel show;
    public javax.swing.JLabel showTransfer;
    public javax.swing.JCheckBox sslProtocol;
    public javax.swing.JCheckBox tlsProtocol;
    private javax.swing.JButton unBag;
    public javax.swing.JProgressBar unBaggingProgress;
    public javax.swing.JTextField userNameField;
    private javax.swing.JButton validBagit;
    private javax.swing.JDialog warning;
    // End of variables declaration//GEN-END:variables
}