package meowdb.boot;

import com.mysql.cj.jdbc.MysqlDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import meowdb.common.Keywords;
import meowdb.config.Configs;
import meowdb.dao.JdbcRepository;
import meowdb.entity.config.Server;
import meowdb.entity.config.ServerConfig;
import meowdb.entity.jdbc.ColumnInfo;
import meowdb.entity.jdbc.DataTable;
import meowdb.entity.jdbc.PrimaryKey;
import meowdb.entity.node.DbNode;
import meowdb.entity.node.NodeBase;
import meowdb.entity.node.ServerNode;
import meowdb.entity.node.TabData;
import meowdb.entity.node.TableNode;
import meowdb.util.CollectionUtils;
import meowdb.util.Images;
import meowdb.util.ResizeUtils;
import meowdb.util.StringUtils;
import meowdb.util.TableUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 启动类
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/19 9:48
 */
public class Bootstrap {

    private final Stage mainStage;
    private final Set<String> keywordCache = new CopyOnWriteArraySet<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Map<String, MysqlDataSource> dataSourceCache = new HashMap<>();
//    private final Map<String, ServerCache> serverCache = new HashMap<>();

    private Map<String, Server> serverConfig;
    private Stage serverStage;
    private Stage dbStage;
    private BorderPane pane;
    private SplitPane splitPane;
    private TreeView<Object> tree;
    private TabPane mainTabPane;
    private Alert errorAlert;
    private Alert infoAlert;
    private Alert confirmAlert;
    private ProgressBar progressBar;

    private Image iconImg;
    private Image serverImg;
    private Image dbImg;
    private Image tableImg;

    private KeyCodeCombination ctrl_r;
    private KeyCodeCombination ctrl_shift_r;

    public Bootstrap(Stage stage) {
        this.mainStage = stage;
    }

    public void start() throws IOException {
        loadConfig();
        loadKeyword();
        loadImg();
        loadKeyCombination();

        pane = new BorderPane();
        mainTabPane = new TabPane();
        pane.setStyle("-fx-font-family: 'Source Code Pro';");
        pane.setStyle("-fx-font-size: 12;");

        splitPane = new SplitPane();
        splitPane.setDividerPositions(0.2, 0.8);
        drawTree();
        splitPane.getItems().add(mainTabPane);
        pane.setCenter(splitPane);
        drawMenu();
        drawAlert();

        Scene scene = new Scene(pane, 1100, 600);
        scene.getStylesheets().add(Bootstrap.class.getClassLoader().getResource("grammar.css").toExternalForm());
        mainStage.setTitle("MeowDb");
        mainStage.setScene(scene);
        mainStage.getIcons().add(iconImg);

        mainStage.show();
    }

    private void loadKeyCombination() {
        ctrl_r = new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN);
        ctrl_shift_r = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    }

    private void drawAlert() {
        errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Info");
        confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm");
    }

    private void drawMenu() {
        HBox hBox = new HBox();
        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("File");
        Menu edit = new Menu("Edit");
        Menu manage = new Menu("Manage");
        Menu help = new Menu("Help");
        MenuItem newServerMenu = new MenuItem("New Server");
        drawServerWindow();
        newServerMenu.setOnAction(event -> {
            serverStage.setTitle("Add Server");
            Scene scene = serverStage.getScene();
            TextField id = (TextField) scene.lookup("#serverId");
            TextField name = (TextField) scene.lookup("#serverName");
            TextField host = (TextField) scene.lookup("#serverHost");
            TextField port = (TextField) scene.lookup("#serverPort");
            TextField user = (TextField) scene.lookup("#serverUser");
            PasswordField passwd = (PasswordField) scene.lookup("#serverPassword");
            recoverServerField(id, name, host, port, user, passwd);
            serverStage.show();
        });

        file.getItems().addAll(newServerMenu);

        MenuItem ddlMenu = new MenuItem("Execute DDL");
        ddlMenu.setOnAction(event -> {
            Stage st = new Stage();
            st.setTitle("Execute DDL");
            st.getIcons().add(iconImg);
            TreeView<Object> treeView = new TreeView<>();
            treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            treeView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<Object>>() {
                @Override
                public void onChanged(Change<? extends TreeItem<Object>> c) {
                    c.getList().forEach(l -> ((CheckBoxTreeItem<Object>) l).setSelected(true));
                }
            });
            TreeItem<Object> root = new TreeItem<>();
            treeView.setShowRoot(false);

            List<TreeItem<Object>> ss = new ArrayList<>();
            for (Server server : serverConfig.values()) {
                MysqlDataSource ds = dataSourceCache.get(server.getId());
                List<String> dbs;
                try {
                    dbs = JdbcRepository.findDataBases(ds);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                TreeItem<Object> item = new TreeItem<>();
                ServerNode serverNode = new ServerNode(server);
                item.setValue(serverNode);
                for (String db : dbs) {
                    DbNode dbNode = new DbNode(db);
                    CheckBoxTreeItem<Object> it = new CheckBoxTreeItem<>(dbNode);
                    item.getChildren().add(it);
                }

                ss.add(item);
            }
            root.getChildren().addAll(ss);
            treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
            treeView.setRoot(root);
            SplitPane pane = new SplitPane();
            pane.setStyle("-fx-font-family: 'Calibri Light'");
            pane.setDividerPositions(0.3, 0.7);
            VBox box = new VBox();
            CodeArea code = new CodeArea();
            code.setPrefHeight(400);
            ResizeUtils.enableVerticalResize(code);
            box.getChildren().add(code);
            Button executeBtn = new Button("Execute");
            executeBtn.setOnAction(e -> {
                String sql = code.getText();
                List<TreeItem<Object>> its = root.getChildren();
                List<TreeItem<Object>> selectedDbs = new ArrayList<>();
                for (TreeItem<Object> it : its) {
                    for (TreeItem<Object> i : it.getChildren()) {
                        if (((CheckBoxTreeItem<Object>)i).isSelected()) {
                            selectedDbs.add(i);
                        }
                    }
                }
                for (TreeItem<Object> selectedDb : selectedDbs) {
                    DbNode dNode = (DbNode) selectedDb.getValue();
                    ServerNode sNode = (ServerNode) selectedDb.getParent().getValue();
                    MysqlDataSource ds = dataSourceCache.get(sNode.getId());
                    ds.setDatabaseName(dNode.getName());
                    try {
                        JdbcRepository.query(ds, sql);
                    } catch (SQLException ex) {
                        alertErrorMsg(ex.getMessage());
                        break;
                    }
                }
                alertInfoMsg("Successfully executed ddl.");
            });
            box.getChildren().add(executeBtn);
            pane.getItems().addAll(treeView, box);
            Scene scene = new Scene(pane, 700, 500);
            st.setScene(scene);
            st.initOwner(mainStage);
            st.show();
        });

        manage.getItems().addAll(ddlMenu);
        menuBar.getMenus().addAll(file, edit, manage, help);

        HBox hB = new HBox();
        hB.setAlignment(Pos.BASELINE_RIGHT);
        hB.setPrefWidth(900);
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(150);
        progressBar.setProgress(0.0D);
        progressBar.setVisible(false);
        hB.getChildren().add(progressBar);

        hBox.getChildren().addAll(menuBar, hB);
        pane.setTop(hBox);
    }

    private Label drawLabel(String text) {
        return drawLabel(text, 60D);
    }

    private Label drawLabel(String text, double width) {
        Label label = new Label(text);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setPrefWidth(width);
        return label;
    }

    private TextField drawField(String id, String defVal, double width) {
        TextField field = new TextField(defVal);
        field.setPrefWidth(width);
        field.setId(id);
        return field;
    }

    private HBox drawRow(Node... nodes) {
        HBox box = new HBox(nodes);
        box.setPadding(new Insets(0D, 10D, 5D, 10D));
        return box;
    }

    private void drawServerWindow() {
        Pane serverPane = new Pane();

        Label serverNameLabel = drawLabel("Name");
        TextField serverNameField = drawField("serverName", "", 80D);
        TextField idField = new TextField();
        idField.setId("serverId");
        idField.setVisible(false);
        HBox nameBox = drawRow(serverNameLabel, serverNameField, idField);

        Label hostLabel = drawLabel("Host");
        TextField hostField = drawField("serverHost", "127.0.0.1", 120D);
        HBox hostBox = drawRow(hostLabel, hostField);

        Label portLabel = drawLabel("Port");
        TextField portField = drawField("serverPort", "3306", 60D);
        HBox portBox = drawRow(portLabel, portField);

        Label userLabel = drawLabel("User");
        TextField userField = drawField("serverUser", "root", 80D);
        HBox userBox = drawRow(userLabel, userField);

        Label passwdLabel = drawLabel("Password");
        PasswordField passwdField = new PasswordField();
        passwdField.setId("serverPassword");
        passwdField.setPrefWidth(120D);
        HBox passwdBox = drawRow(passwdLabel, passwdField);

        Button testBtn = new Button("Test");
        testBtn.setOnAction(event -> {
            Server server = buildServerFrom(idField, serverNameField, hostField, portField, userField, passwdField);
            try {
                MysqlDataSource ds = initDataSource(server);
                JdbcRepository.connectionTest(ds);
                alertInfoMsg("Connection succeeded.");
            } catch (SQLException e) {
                alertErrorMsg("Connection failed, please check your setting.");
            }
        });

        Button saveServerBtn = new Button("Save");
        saveServerBtn.setId("saveServerBtn");
        saveServerBtn.setOnAction(event -> {
            Server server = buildServerFrom(idField, serverNameField, hostField, portField, userField, passwdField);
            if (server.getId() == null) {
                server.setId(Configs.nextServerId());
            } else {
                removeServerNode(server);
            }
            addNewServerNode(tree.getRoot(), server);
            refreshServerConfigCache(server);
            try {
                Configs.saveServerConfig(new ServerConfig(serverConfig));
            } catch (IOException e) {
                alertErrorMsg(e.getMessage());
                return;
            }
            serverStage.hide();
            recoverServerField(idField, serverNameField, hostField, portField, userField, passwdField);
        });

        HBox btnBox = new HBox(testBtn, saveServerBtn);
        btnBox.setPadding(new Insets(5D, 0D, 0D, 10D));
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(25, 25, 25, 25));
        vBox.getChildren().addAll(nameBox, hostBox, portBox, userBox, passwdBox, btnBox);
        serverPane.getChildren().add(vBox);
        Scene scene = new Scene(serverPane, 400, 220);
        serverStage = new Stage();
        serverStage.getIcons().add(iconImg);
        serverStage.setScene(scene);
        serverStage.initOwner(mainStage);
        serverStage.setTitle("New Server");
    }

    private void drawDatabaseWindow() {
        Pane databasePane = new Pane();

        Label dbNameLabel = drawLabel("Name", 100D);
        TextField dbNameField = drawField("databaseName", "", 80D);
        HBox nameBox = drawRow(dbNameLabel, dbNameField);

        Label charSetLabel = drawLabel("Character Set", 100D);
        TextField charSetField = drawField("characterSet", "utf8", 120D);
        charSetField.setDisable(true);
        HBox charSetBox = drawRow(charSetLabel, charSetField);

        Button saveDbBtn = new Button("Save");
        saveDbBtn.setId("saveDbBtn");
        saveDbBtn.setOnAction(event -> {
            TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
            Object val = item.getValue();
            if (val instanceof ServerNode) {
                ServerNode serverNode = (ServerNode) val;
                DbNode dbNode = new DbNode(dbNameField.getText());
                dbNode.setServer(serverNode);

                ImageView dbIco = new ImageView(dbImg);
                dbIco.setFitWidth(16);
                dbIco.setFitHeight(16);
                TreeItem<Object> dbItem = new TreeItem<>(dbNode, dbIco);
                dbItem.setValue(dbNode);
                item.getChildren().add(dbItem);
                String serverId = serverNode.getId();
                MysqlDataSource ds = dataSourceCache.get(serverId);
                try {
                    JdbcRepository.createDatabase(ds, dbNode.getName());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                dbStage.hide();
                dbNameField.clear();
            }
        });

        HBox btnBox = new HBox(saveDbBtn);
        btnBox.setPadding(new Insets(5D, 0D, 0D, 10D));
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(25, 25, 25, 25));
        vBox.getChildren().addAll(nameBox, charSetBox, btnBox);
        databasePane.getChildren().add(vBox);
        Scene scene = new Scene(databasePane, 400, 120);
        dbStage = new Stage();
        dbStage.getIcons().add(iconImg);
        dbStage.setScene(scene);
        dbStage.initOwner(mainStage);
        dbStage.setTitle("New Database");
    }

    private void removeServerNode(Server server) {
        ObservableList<TreeItem<Object>> serverNodes = tree.getRoot().getChildren();
        Iterator<TreeItem<Object>> it = serverNodes.iterator();
        while (it.hasNext()) {
            TreeItem<Object> item = it.next();
            Object val = item.getValue();
            if (val instanceof ServerNode) {
                if (((ServerNode) val).getId().equals(server.getId())) {
                    it.remove();
                }
            }
        }
    }

    private void recoverServerField(TextField idField, TextField serverNameField, TextField hostField, TextField portField, TextField userField, PasswordField passwdField) {
        idField.clear();
        serverNameField.clear();
        serverNameField.requestFocus();
        hostField.setText("127.0.0.1");
        portField.setText("3306");
        userField.setText("root");
        passwdField.clear();
    }

    private void alertErrorMsg(String msg) {
        errorAlert.setHeaderText(msg);
        errorAlert.showAndWait();
    }

    private void alertInfoMsg(String msg) {
        infoAlert.setHeaderText(msg);
        infoAlert.showAndWait();
    }

    private Optional<ButtonType> alertConfirmMsg(String msg) {
        confirmAlert.setHeaderText(msg);
        return confirmAlert.showAndWait();
    }

    private Server buildServerFrom(TextField idField, TextField serverNameField, TextField hostField, TextField portField, TextField userField, PasswordField passwdField) {
        String serverName = serverNameField.getText().trim();
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        String user = userField.getText().trim();
        String passwd = passwdField.getText().trim();
        Server server = new Server(serverName);
        server.setHost(host);
        server.setPort(port);
        server.setUser(user);
        server.setPassword(passwd);
        String id = idField.getText();
        if (!StringUtils.isEmpty(id)) {
            server.setId(id);
        }
        return server;
    }

    private void loadImg() {
        serverImg = Images.of("server_16.png");
        dbImg = Images.of("db_16.png");
        tableImg = Images.of("table_16.png");
        iconImg = Images.of("icon.png");
    }

    private void loadKeyword() {
        Arrays.stream(Keywords.values()).forEach(v -> addKeyWords(v.getWord()));
    }

    private void loadConfig() throws IOException {
        ServerConfig conf = Configs.loadServerConfig();
        Map<String, Server> servers = conf.getServers();
        serverConfig = servers;
        servers.forEach((k, v) -> {
            try {
                dataSourceCache.put(k, initDataSource(v));
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private MysqlDataSource initDataSource(Server server) throws SQLException {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUser(server.getUser());
        ds.setPassword(server.getPassword());
        ds.setPort(server.getPort());
        ds.setServerName(server.getHost());
        ds.setAllowMultiQueries(true);
        return ds;
    }

    private void addKeyWords(String... keywords) {
        for (String keyword : keywords) {
            keywordCache.add(keyword.toLowerCase());
            keywordCache.add(keyword.toUpperCase());
        }
    }

    private VBox drawNewQueryTab(String serverId, String serverName, String dbName) {
        Tab tab = new Tab(serverName + "/" + dbName);
        TabData data = new TabData();
        String sessionId = String.valueOf(System.currentTimeMillis());
        data.setSessionId(sessionId);
        data.setServerId(serverId);
        data.setDbName(dbName);
        data.setServerName(serverName);
        tab.setUserData(data);
        mainTabPane.getTabs().add(tab);

        VBox queryBox = new VBox();
        HBox btnBox = new HBox();
        btnBox.setPadding(new Insets(5D, 0D, 0D, 10D));
        btnBox.setAlignment(Pos.CENTER_LEFT);
        Button queryBtn = new Button();
        queryBtn.setGraphic(new ImageView(Images.of("query_24.png")));
        btnBox.getChildren().add(queryBtn);

        Button querySelectBtn = new Button();
        querySelectBtn.setGraphic(new ImageView(Images.of("query_select_24.png")));
        btnBox.getChildren().add(querySelectBtn);
        Text serverLabel = new Text("    Server: ");
        serverLabel.setFont(Font.font(12));
        Text serverNameTxt = new Text(serverName);
        serverNameTxt.setFont(Font.font(12));
        serverNameTxt.setFill(Color.GREEN);
        Text dbLabel = new Text("    Database: ");
        dbLabel.setFont(Font.font(12));
        Text dbNameTxt = new Text(dbName);
        dbNameTxt.setFont(Font.font(12));
        dbNameTxt.setFill(Color.BLUE);
        btnBox.getChildren().addAll(serverLabel, serverNameTxt);
        btnBox.getChildren().addAll(dbLabel, dbNameTxt);

        HBox codeBox = new HBox();
        codeBox.setPadding(new Insets(5D, 0D, 0D, 0D));
        CodeArea codeArea = drawCodeArea();
        drawPopupOn(codeArea);
        codeBox.getChildren().add(codeArea);

        queryBox.getChildren().add(btnBox);
        queryBox.getChildren().add(codeBox);

        VBox resultBox = new VBox();
        resultBox.setId("resultBox_" + sessionId);
        // 查询按钮事件绑定
        queryBtn.setOnAction(event -> {
            final String sql = codeArea.getText();
            drawQueryResult(resultBox, serverId, dbName, sql);
        });
        mainStage.getScene().getAccelerators().put(ctrl_r, queryBtn::fire);

        // 查询选定语句事件绑定
        querySelectBtn.setOnAction(event -> {
            final String sql = codeArea.getSelectedText();
            drawQueryResult(resultBox, serverId, dbName, sql);
        });
        queryBox.getChildren().add(resultBox);
        mainStage.getScene().getAccelerators().put(ctrl_shift_r, querySelectBtn::fire);
        tab.setContent(queryBox);
        mainTabPane.getSelectionModel().select(tab);
        return resultBox;
    }

    private void drawTree() {
        drawDatabaseWindow();
        tree = new TreeView<>();
        TreeItem<Object> root = drawTreeItem();
        tree.setRoot(root);
        tree.setShowRoot(false);
        tree.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                TreeItem<Object> selectedItem = tree.getSelectionModel().getSelectedItem();
                Object value = selectedItem.getValue();
                if (value == null)
                    return;

                if (value instanceof ServerNode) {
                    ServerNode serverNode = (ServerNode) value;
                    if (serverNode.isLoaded())
                        return;

                    String serverId = serverNode.getId();
                    MysqlDataSource ds = dataSourceCache.get(serverId);
                    List<String> dataBases;
                    try {
                        dataBases = JdbcRepository.findDataBases(ds);
                    } catch (SQLException ex) {
                        alertErrorMsg(ex.getMessage());
                        return;
                    }

//                    ServerCache server = new ServerCache(serverId);
                    for (String dbName : dataBases) {
                        DbNode dbNode = new DbNode(dbName);
                        dbNode.setServer(serverNode);
                        ImageView dbIco = new ImageView(dbImg);
                        dbIco.setFitWidth(16);
                        dbIco.setFitHeight(16);
                        TreeItem<Object> dbItem = new TreeItem<>(dbNode, dbIco);
                        selectedItem.getChildren().add(dbItem);
//                        server.addDatabase(new DbCache(dbName));
                    }
                    serverNode.setLoaded(true);
                    selectedItem.setExpanded(true);
//                    serverCache.put(server.getKey(), server);
                } else if (value instanceof DbNode) {
                    DbNode dbNode = (DbNode) value;
                    if (dbNode.isLoaded())
                        return;

                    String dbName = dbNode.getName();
                    ServerNode serverNode = ((ServerNode) selectedItem.getParent().getValue());
                    String serverName = serverNode.getName();
                    String serverId = serverNode.getId();
                    MysqlDataSource ds = dataSourceCache.get(serverId);
                    ds.setDatabaseName(dbName);
                    List<String> tables;
                    try {
                        tables = JdbcRepository.findTables(ds);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        tables = Collections.emptyList();
                    }

//                    DbCache db = serverCache.get(serverId).getDatabases().get(dbName);
                    drawNewQueryTab(serverId, serverName, dbName);
//                    pane.setCenter(mainTabPane);
                    for (String tableName : tables) {
                        TableNode tableNode = new TableNode(tableName);
                        tableNode.setDatabase(dbNode);
                        ImageView tableIco = new ImageView(tableImg);
                        tableIco.setFitWidth(16);
                        tableIco.setFitHeight(16);
                        TreeItem<Object> tableItem = new TreeItem<>(tableNode, tableIco);
                        selectedItem.getChildren().add(tableItem);
//                        db.addTable(new TableCache(tableName));
                        loadColumnCache(serverId, dbName, tableName, tableItem);
                    }
                    dbNode.setLoaded(true);
                    selectedItem.setExpanded(true);
                } else if (value instanceof TableNode) {
                    TableNode tableNode = (TableNode) value;
                    TreeItem<Object> dbItem = selectedItem.getParent();
                    DbNode dbNode = (DbNode) selectedItem.getParent().getValue();
                    TreeItem<Object> serverItem = dbItem.getParent();
                    ServerNode serverNode = (ServerNode) serverItem.getValue();
                    String serverId = serverNode.getId();
                    String serverName = serverNode.getName();
                    String dbName = dbNode.getName();
                    Tab currTab = mainTabPane.getSelectionModel().getSelectedItem();
                    VBox resultBox;
                    if (currTab == null) {
                        resultBox = drawNewQueryTab(serverId, serverName, dbName);
                    } else {
                        TabData data = (TabData) currTab.getUserData();
                        if (!serverId.equals(data.getServerId()) || !dbName.equals(data.getDbName())) {
                            resultBox = drawNewQueryTab(serverId, serverName, dbName);
                        } else {
                            resultBox = (VBox) pane.getScene().lookup("#resultBox_" + data.getSessionId());
                        }
                    }
                    String tableName = tableNode.getName();
                    String sql = "select * from " + tableName;
                    drawQueryResult(resultBox, serverId, dbName, sql, tableName, true, tableNode.getPks());
                }
            }
        });

        ContextMenu serverMenu = new ContextMenu();
        MenuItem delServerMenu = new MenuItem("Delete Server");
        delServerMenu.setOnAction(event -> {
            TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
            Object val = item.getValue();
            if (val instanceof ServerNode) {
                ServerNode node = (ServerNode) val;
                String serverName = node.getName();
                String serverId = node.getId();
                alertConfirmMsg("Delete the server " + serverName + "?").ifPresent(answer -> {
                    if (answer.getButtonData().isDefaultButton()) {
                        removeServer(serverId);
                        root.getChildren().remove(item);
                    }
                });
            }
        });

        MenuItem editServerMenu = new MenuItem("Edit Server");
        editServerMenu.setOnAction(event -> {
            TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
            Object node = item.getValue();
            if (node instanceof ServerNode) {
                String serverId = ((ServerNode) node).getId();
                showEditServerStage(serverId);
            }
        });

        MenuItem createDatabaseMenu = new MenuItem("Create Database");
        createDatabaseMenu.setOnAction(event -> {
            dbStage.show();
        });

        serverMenu.getItems().addAll(editServerMenu, delServerMenu, createDatabaseMenu);
        ContextMenu dbMenu = new ContextMenu();
        MenuItem delDbMenu = new MenuItem("Drop Database");
        delDbMenu.setOnAction(event -> {
            TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
            Object node = item.getValue();
            if (node instanceof DbNode) {
                DbNode db = (DbNode) node;
                String dbName = db.getName();
                String serverId = db.getServer().getId();
                alertConfirmMsg("Delete the database " + db.getName() + "?").ifPresent(answer -> {
                    if (answer.getButtonData().isDefaultButton()) {
                        dropDatabase(serverId, dbName);
                        item.getParent().getChildren().remove(item);
                    }
                });

            }
        });
        MenuItem editDbMenu = new MenuItem("Edit Database");
        dbMenu.getItems().addAll(editDbMenu, delDbMenu);

        ContextMenu tableMenu = new ContextMenu();
        MenuItem truncateTableMenu = new MenuItem("Truncate Table");

        truncateTableMenu.setOnAction(event -> {
            TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
            Object val = item.getValue();
            if (val instanceof TableNode) {
                TableNode node = (TableNode) val;
                String tableName = node.getName();
                alertConfirmMsg("Truncate table " + tableName + "?").ifPresent(answer -> {
                    if (answer.getButtonData().isDefaultButton()) {
                        DbNode dbNode = node.getDatabase();
                        ServerNode serverNode = dbNode.getServer();
                        MysqlDataSource ds = dataSourceCache.get(serverNode.getId());
                        ds.setDatabaseName(dbNode.getName());
                        try {
                            JdbcRepository.truncateTable(ds, tableName);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
        MenuItem dropTableMenu = new MenuItem("Drop Table");

        dropTableMenu.setOnAction(event -> {
            TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
            Object val = item.getValue();
            if (val instanceof TableNode) {
                TableNode node = (TableNode) val;
                String tableName = node.getName();
                alertConfirmMsg("Drop table " + tableName + "?").ifPresent(answer -> {
                    if (answer.getButtonData().isDefaultButton()) {
                        DbNode dbNode = node.getDatabase();
                        ServerNode serverNode = dbNode.getServer();
                        MysqlDataSource ds = dataSourceCache.get(serverNode.getId());
                        ds.setDatabaseName(dbNode.getName());
                        try {
                            JdbcRepository.dropTable(ds, tableName);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        item.getParent().getChildren().remove(item);
                    }
                });
            }
        });
        MenuItem designTableMenu = new MenuItem("Design Table");

        tableMenu.getItems().addAll(designTableMenu, truncateTableMenu, dropTableMenu);
        Callback<TreeView<Object>, TreeCell<Object>> callback = TextFieldTreeCell.forTreeView(new StringConverter<Object>() {
            @Override
            public String toString(Object object) {
                return object.toString();
            }

            @Override
            public Object fromString(String string) {
                return new NodeBase(string);
            }
        });
        tree.setCellFactory(tr -> {
            TreeCell<Object> cell = callback.call(tr);
            cell.updateTreeView(tr);
            cell.treeItemProperty().addListener((o, oldIt, newIt) -> {
                if (newIt != null) {
                    TreeItem<Object> item = newIt;
                    Object value = item.getValue();
                    if (value instanceof ServerNode) {
                        cell.setContextMenu(serverMenu);
                    } else if (value instanceof DbNode) {
                        cell.setContextMenu(dbMenu);
                    } else if (value instanceof TableNode) {
                        cell.setContextMenu(tableMenu);
                    }
                }
            });
            return cell;
        });

//        tree.setMaxWidth(250);
        ResizeUtils.enableHorizontalResize(tree);
//        pane.setLeft(tree);
        splitPane.getItems().add(tree);
    }

    private void dropDatabase(String serverId, String dbName) {
        MysqlDataSource ds = dataSourceCache.get(serverId);
        try {
            JdbcRepository.dropDatabase(ds, dbName);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showEditServerStage(String serverId) {
        Server server = serverConfig.get(serverId);
        if (server != null) {
            serverStage.setTitle("Edit Server");
            Scene scene = serverStage.getScene();

            TextField serverIdField = (TextField) scene.lookup("#serverId");
            serverIdField.setText(server.getId());
            TextField serverNameField = (TextField) scene.lookup("#serverName");
            serverNameField.setText(server.getName());
            TextField host = (TextField) scene.lookup("#serverHost");
            host.setText(server.getHost());

            TextField port = (TextField) scene.lookup("#serverPort");
            port.setText(server.getPort().toString());

            TextField user = (TextField) scene.lookup("#serverUser");
            user.setText(server.getUser());

            TextField passwd = (TextField) scene.lookup("#serverPassword");
            passwd.setText(server.getPassword());

            serverStage.show();
        }
    }

    private void removeServer(String serverId) {
        // 删除配置项
        serverConfig.remove(serverId);
        ServerConfig config = new ServerConfig(serverConfig);
        try {
            Configs.saveServerConfig(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 删除关联的查询tab
        ObservableList<Tab> tabs = mainTabPane.getTabs();
        tabs.removeIf(tab -> {
            TabData data = (TabData) tab.getUserData();
            return serverId.equals(data.getServerId());
        });
    }

    private TreeItem<Object> drawTreeItem() {
        NodeBase rootNode = new NodeBase("Server");
        TreeItem<Object> rootItem = new TreeItem<>(rootNode);
        rootItem.setExpanded(true);
        serverConfig.forEach((k, server) -> {
            addNewServerNode(rootItem, server);
        });

        return rootItem;
    }

    private void addNewServerNode(TreeItem<Object> rootItem, Server server) {
        ServerNode serverNode = new ServerNode(server);
        ImageView serverIco = new ImageView(serverImg);
        serverIco.setFitWidth(16);
        serverIco.setFitHeight(16);
        TreeItem<Object> serverItem = new TreeItem<>(serverNode, serverIco);

        rootItem.getChildren().add(serverItem);
        refreshDataSourceCache(server);
    }

    private void refreshServerConfigCache(Server server) {
        serverConfig.put(server.getId(), server);
    }

    private void refreshDataSourceCache(Server server) {
        try {
            dataSourceCache.put(server.getId(), initDataSource(server));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadColumnCache(String serverId, String dbName, String tableName, TreeItem<Object> tableItem) {
        // 异步加载字段关键字缓存
        executor.submit(() -> {
//            TableCache table = serverCache.get(serverId).getDatabase(dbName).getTable(tableName);
            MysqlDataSource ds = dataSourceCache.get(serverId);
            ds.setDatabaseName(dbName);
            try {
                List<ColumnInfo> infos = JdbcRepository.findColumns(ds, tableName);
                List<String> pks = new ArrayList<>();
                List<String> columns = new ArrayList<>();
                for (ColumnInfo info : infos) {
                    if ("PRI".equals(info.getKey())) {
                        pks.add(info.getField().toLowerCase());
                    }
                    columns.add(info.getField().toLowerCase());
                }
                columns.forEach(column -> {
                    addKeyWords(dbName, tableName, column);
                    TableNode table = (TableNode) tableItem.getValue();
                    table.setPks(pks);
                    tableItem.setValue(table);
//                    table.addColumn(new ColumnCache(column));
                });
//                System.out.println("load keyword cache of table=" + table.getKey());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void drawQueryResult(VBox resultBox, String serverId, String dbName, String sql) {
        drawQueryResult(resultBox, serverId, dbName, sql, null, false, null);
    }

    private void drawQueryResult(VBox resultBox, String serverId, String dbName,
                                 String sql, String tableName, boolean editable, List<String> pks) {
        progressBar.setVisible(true);
        TabPane tabPane = new TabPane();
        HBox statusBox = new HBox();
        Text statusTxt = new Text();
        statusBox.getChildren().add(statusTxt);
        statusBox.setPrefHeight(30);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(0, 0, 0, 15));
        resultBox.getChildren().clear();

        Task<List<Tab>> task = new Task<List<Tab>>() {
            @Override
            protected List<Tab> call() throws SQLException {
                updateProgress(0, 100);
                MysqlDataSource ds = dataSourceCache.get(serverId);
                ds.setDatabaseName(dbName);
                long startTime = System.currentTimeMillis();
                Map<String, DataTable> results;
                List<Tab> tabs = new ArrayList<>();
                updateProgress(10, 100);
                results = JdbcRepository.query(ds, sql);
                updateProgress(50, 100);
                long endTime = System.currentTimeMillis();
                long executeTime = endTime - startTime;

                Callback<TableColumn<Map, String>, TableCell<Map, String>> factory = TextFieldTableCell.forTableColumn();
                results.forEach((name, dt) -> {
                    TableView<Map> table = new TableView<>(FXCollections.observableArrayList(dt.getRows()));
                    table.getSelectionModel().setCellSelectionEnabled(true);
                    table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                    table.setEditable(true);
                    List<List<PrimaryKey>> keysByRow = new ArrayList<>();
                    for (Map<String, String> row : dt.getRows()) {
                        List<PrimaryKey> ks = new ArrayList<>();
                        row.forEach((k, v) -> {
                            if (pks != null && pks.contains(k)) {
                                ks.add(new PrimaryKey(k, v));
                            }
                        });
                        keysByRow.add(ks);
                    }
                    dt.getColumns().forEach(col -> {
                        TableColumn<Map, String> column = new TableColumn<>(col);
                        if (editable && !StringUtils.isEmpty(tableName)) { //可编辑的表格
                            column.setEditable(editable);
                            column.setCellFactory(it -> {
                                TableCell<Map, String> cell = factory.call(it);
                                return cell;
                            });
                            column.setOnEditCommit(event -> {
                                int row = event.getTablePosition().getRow();
                                try {
                                    JdbcRepository.updateSingleValue(ds, tableName, col, event.getNewValue(), keysByRow.get(row));
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }

                        column.setUserData(null);
                        column.setCellValueFactory(new MapValueFactory<>(col));
                        table.getColumns().add(column);
                    });
                    TableUtils.installCopyPasteHandler(table);
                    Tab tab = new Tab(name);
                    tab.setContent(table);
                    tabs.add(tab);
                    updateProgress(70, 100);
                });
                updateProgress(100, 100);
                updateMessage("Time Cost: " + executeTime + "ms");
                updateValue(tabs);
                return tabs;
            }
        };

        task.setOnSucceeded(event -> {
            List<Tab> val = (List<Tab>) event.getSource().getValue();
            tabPane.getTabs().addAll(val);
            progressBar.setVisible(false);
        });

        task.setOnFailed(event -> {
            Throwable ex = event.getSource().getException();
            Text errorTxt = new Text(ex.getMessage());
            errorTxt.setFill(Color.RED);
            resultBox.getChildren().add(errorTxt);
            progressBar.setVisible(false);
        });

        statusTxt.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        executor.submit(task);

        resultBox.getChildren().add(tabPane);
        resultBox.getChildren().add(statusBox);
    }

    private void drawPopupOn(CodeArea codeArea) {
        Popup popup = new Popup();

        ListView<String> listView = new ListView<>();

        listView.setStyle("-fx-text-fill: black; " +
                "-fx-font-family: 'Courier New'; -fx-font-style: italic; -fx-font-size: 12;");
        listView.setMaxHeight(250);
        popup.getContent().add(listView);
        popup.setAutoFix(true);
        // 根据输入提示关键字
        codeArea.textProperty().addListener((o, oldValue, newValue) -> {
            if (!StringUtils.isEmpty(newValue)) {
                int caretPosition = codeArea.getCaretPosition();
                newValue = newValue.substring(0, caretPosition);
                int index = -1;
                char[] chars = newValue.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    boolean matches = String.valueOf(chars[i]).matches("^[A-Za-z0-9_]+$");
                    if (!matches) {
                        index = i;
                    }
                }
                String substring = newValue.substring(index + 1);
                if (StringUtils.isEmpty(substring)) {
                    popup.hide();
                    return;
                }
                List<String> filteredList = new ArrayList<>();
                for (String word : keywordCache) {
                    if (word.startsWith(substring)) {
                        filteredList.add(word);
                    }
                }

                ObservableList<String> list = FXCollections.observableArrayList(filteredList);
                if (!CollectionUtils.isEmpty(list)) {
                    listView.setItems(list);
                    listView.getSelectionModel().selectFirst();
                    try {
                        codeArea.getCaretBounds()
                                .ifPresent(bound -> popup.show(codeArea.getScene().getWindow(), bound.getMaxX(), bound.getMaxY()));
                    } catch (Exception e) {
                        popup.show(codeArea.getScene().getWindow());
                    }
                } else {
                    popup.hide();
                }

            } else {
                popup.hide();
            }
        });

        codeArea.getVisibleParagraphs()
                .addModificationObserver(new SqlStyler<>(codeArea, SqlStyler::computeHighlighting));

        listView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            String selectVal = listView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                int pos = codeArea.getCaretPosition();
                char[] text = codeArea.getText().toCharArray();
                int index = 0;
                for (int i = pos - 1; i >= 0; i--) {
                    if (!String.valueOf(text[i]).matches("[A-Za-z0-9_]")) {
                        index = i + 1;
                        break;
                    }
                }
                codeArea.replaceText(index, pos, selectVal);
                listView.getSelectionModel().clearSelection();
                popup.hide();
            }
        });
        // 监听按键操作提示框
        listView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            String selectVal = listView.getSelectionModel().getSelectedItem();

            // 选择关键字后替换输入
            switch (event.getCode()) {
                case ENTER:
                    int pos = codeArea.getCaretPosition();
                    char[] text = codeArea.getText().toCharArray();
                    int index = 0;
                    for (int i = pos - 1; i >= 0; i--) {
                        if (!String.valueOf(text[i]).matches("[A-Za-z0-9_]")) {
                            index = i + 1;
                            break;
                        }
                    }
                    codeArea.replaceText(index, pos, selectVal);
                    listView.getSelectionModel().clearSelection();
                    popup.hide();
                    break;
                case TAB:
                    listView.getSelectionModel().selectNext();
                    break;
                case UP:
                case DOWN:
                    break;
                default:
                    listView.getSelectionModel().clearSelection();
                    popup.hide();
            }
        });
    }

    private CodeArea drawCodeArea() {
        CodeArea codeArea = new CodeArea();
        codeArea.setPrefSize(1000, 200);
        codeArea.setMinHeight(100);
        codeArea.setMaxHeight(600);
        codeArea.setEditable(true);
        codeArea.setWrapText(true);
        codeArea.setStyle("-fx-font-family: 'Courier New';" +
                "-fx-font-size: 12;" +
                "-fx-border-color: grey;");
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        ResizeUtils.enableVerticalResize(codeArea);
        return codeArea;
    }
}
