package com.auditoria;

import com.auditoria.dao.AuditoriaDAO;
import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;
import com.auditoria.service.MalhaFinaService;
import com.auditoria.service.RelatorioPdfService;
import com.auditoria.util.Formatador;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

public class DashboardApp extends Application {

    private final MalhaFinaService auditoriaService = new MalhaFinaService();
    private final RelatorioPdfService pdfService = new RelatorioPdfService();
    
    private final AuditoriaDAO dao = new AuditoriaDAO();

    private ObservableList<ResultadoAuditoria> dadosTabela;
    private PieChart grafico;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SISTEMA PENTE FINO - Receita Federal do Brasil");

        List<Dipj> empresas = dao.listarEmpresas();
        List<Dimof> cartoes = dao.listarBancos();
        List<Bem> bens = dao.listarBens();
        
        List<ResultadoAuditoria> resultadosIniciais = auditoriaService.processar(empresas, cartoes, bens);
        
        dadosTabela = FXCollections.observableArrayList(resultadosIniciais);

        TabPane tabPane = new TabPane();
        
        Tab abaDashboard = new Tab("Painel de Controle", criarTabDashboard());
        abaDashboard.setClosable(false);
        
        Tab abaSimulador = new Tab("Simulador Manual", criarTabFormulario(tabPane));
        abaSimulador.setClosable(false);
        
        tabPane.getTabs().addAll(abaDashboard, abaSimulador);

        Scene scene = new Scene(tabPane, 1100, 700);
        
        URL cssUrl = getClass().getResource("/css/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
        
        atualizarGrafico();
    }

    private BorderPane criarTabDashboard() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 0, 4, 0, 0);");

        try {
            var imgStream = getClass().getResourceAsStream("/img/logo.png");
            if (imgStream != null) {
                Image logoImg = new Image(imgStream); 
                ImageView logoView = new ImageView(logoImg);
                logoView.setFitHeight(55);
                logoView.setPreserveRatio(true);
                header.getChildren().add(logoView);
            }
        } catch (Exception e) {
            System.err.println("Aviso: Logo não encontrada.");
        }

        VBox titulos = new VBox(2);
        titulos.setAlignment(Pos.CENTER_LEFT);
        Label lblOrgao = new Label("MINISTÉRIO DA FAZENDA");
        lblOrgao.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        Label lblSistema = new Label("SISTEMA PENTE FINO - AUDITORIA DIGITAL");
        lblSistema.getStyleClass().add("label-titulo");

        titulos.getChildren().addAll(lblOrgao, lblSistema);
        header.getChildren().add(titulos);

        grafico = new PieChart();
        grafico.setTitle("Status da Malha Fiscal");
        grafico.setLegendVisible(true);

        TableView<ResultadoAuditoria> tabela = new TableView<>();
        tabela.setItems(dadosTabela);

        TableColumn<ResultadoAuditoria, String> colNome = new TableColumn<>("Contribuinte");
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().razaoSocial()));
        colNome.setPrefWidth(220);

        TableColumn<ResultadoAuditoria, String> colCnpj = new TableColumn<>("CNPJ");
        colCnpj.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().cnpj()));
        colCnpj.setPrefWidth(140);

        TableColumn<ResultadoAuditoria, String> colDiferenca = new TableColumn<>("Divergência");
        colDiferenca.setCellValueFactory(data -> new SimpleStringProperty(Formatador.moeda(data.getValue().diferenca())));
        colDiferenca.setPrefWidth(120);

        TableColumn<ResultadoAuditoria, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status().toString()));
        colStatus.setPrefWidth(120);
        
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    if ("MALHA_FINA".equals(item)) {
                        setStyle("-fx-text-fill: #b71c1c; -fx-background-color: #ffcdd2; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #1b5e20; -fx-background-color: #c8e6c9; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tabela.getColumns().addAll(colNome, colCnpj, colDiferenca, colStatus);

        tabela.setRowFactory(tv -> {
            TableRow<ResultadoAuditoria> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    mostrarDetalhes(row.getItem());
                }
            });
            return row;
        });

        Button btnGerarPdf = new Button("EMITIR NOTIFICAÇÃO (PDF)");
        btnGerarPdf.getStyleClass().add("button-acao");
        btnGerarPdf.setDisable(true);
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            btnGerarPdf.setDisable(novo == null || novo.status() != StatusAuditoria.MALHA_FINA);
        });
        btnGerarPdf.setOnAction(e -> {
            if (tabela.getSelectionModel().getSelectedItem() != null) {
                pdfService.gerarNotificacao(tabela.getSelectionModel().getSelectedItem());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText("Documento Gerado");
                alert.setContentText("O Auto de Infração foi salvo na pasta do projeto.");
                alert.show();
            }
        });

        BorderPane layout = new BorderPane();
        layout.setTop(header);
        VBox right = new VBox(15, new Label("Contribuintes sob Análise:"), tabela, btnGerarPdf);
        right.setPadding(new Insets(10));
        HBox centro = new HBox(20, grafico, right);
        centro.setPadding(new Insets(20));
        layout.setCenter(centro);
        
        return layout;
    }

    private VBox criarTabFormulario(TabPane tabPanePrincipal) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #F0F2F5;");

        Label titulo = new Label("Simulador de Auditoria Fiscal");
        titulo.getStyleClass().add("label-titulo");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        TextField txtCnpj = new TextField(); txtCnpj.setPromptText("00.000.000/0001-00");
        TextField txtNome = new TextField(); txtNome.setPromptText("Razão Social");
        TextField txtReceita = new TextField(); txtReceita.setPromptText("0.00");
        TextField txtDespesa = new TextField(); txtDespesa.setPromptText("0.00");
        TextField txtBanco = new TextField(); txtBanco.setPromptText("0.00");
        TextField txtBens = new TextField(); txtBens.setPromptText("0.00");

        grid.add(new Label("CNPJ:"), 0, 0); grid.add(txtCnpj, 1, 0);
        grid.add(new Label("Razão Social:"), 0, 1); grid.add(txtNome, 1, 1);
        grid.add(new Separator(), 0, 2, 2, 1);
        grid.add(new Label("Receita Declarada (R$):"), 0, 3); grid.add(txtReceita, 1, 3);
        grid.add(new Label("Despesas (R$):"), 0, 4); grid.add(txtDespesa, 1, 4);
        grid.add(new Separator(), 0, 5, 2, 1);
        grid.add(new Label("Informe Bancário (R$):"), 0, 6); grid.add(txtBanco, 1, 6);
        grid.add(new Label("Total em Bens (R$):"), 0, 7); grid.add(txtBens, 1, 7);

        Button btnSimular = new Button("PROCESSAR E SALVAR NO BANCO");
        btnSimular.getStyleClass().add("button-acao");
        btnSimular.setPrefWidth(250);
        
        btnSimular.setOnAction(e -> {
            try {
                BigDecimal receita = new BigDecimal(txtReceita.getText().replace(",", "."));
                BigDecimal despesa = new BigDecimal(txtDespesa.getText().replace(",", "."));
                BigDecimal bancoValor = new BigDecimal(txtBanco.getText().replace(",", "."));
                BigDecimal bensValor = new BigDecimal(txtBens.getText().replace(",", "."));
                
                Dipj novaDipj = new Dipj(txtCnpj.getText(), txtNome.getText(), 2026, receita, despesa);
                Dimof novaDimof = new Dimof(txtCnpj.getText(), "Banco Simulado", bancoValor, BigDecimal.ZERO);
                Bem novoBem = new Bem(txtCnpj.getText(), "Bens Diversos", bensValor);
                
                dao.salvarSimulacao(novaDipj, novaDimof, novoBem);

                List<ResultadoAuditoria> resultado = auditoriaService.processar(
                    List.of(novaDipj), List.of(novaDimof), List.of(novoBem)
                );
                
                dadosTabela.add(resultado.get(0));
                atualizarGrafico();
                
                txtCnpj.clear(); txtNome.clear(); txtReceita.clear(); 
                txtDespesa.clear(); txtBanco.clear(); txtBens.clear();
                
                tabPanePrincipal.getSelectionModel().select(0);
                
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Processamento Concluído");
                alerta.setHeaderText("Dados Gravados");
                alerta.setContentText("O contribuinte foi salvo no Banco de Dados e analisado.");
                alerta.show();

            } catch (Exception ex) {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setContentText("Erro ao salvar: " + ex.getMessage());
                erro.show();
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(titulo, grid, btnSimular);
        return layout;
    }

    private void atualizarGrafico() {
        if (grafico == null || dadosTabela == null) return;
        long qtdMalha = dadosTabela.stream().filter(r -> r.status() == StatusAuditoria.MALHA_FINA).count();
        long qtdRegular = dadosTabela.size() - qtdMalha;
        grafico.getData().clear();
        grafico.getData().add(new PieChart.Data("Regular (" + qtdRegular + ")", qtdRegular));
        grafico.getData().add(new PieChart.Data("Malha Fina (" + qtdMalha + ")", qtdMalha));
    }

    private void mostrarDetalhes(ResultadoAuditoria dados) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dossiê Fiscal");
        alert.setHeaderText("Detalhes da Auditoria: " + dados.razaoSocial());
        
        StringBuilder texto = new StringBuilder();
        texto.append("CNPJ: ").append(dados.cnpj()).append("\n");
        texto.append("Status: ").append(dados.status()).append("\n\n");
        texto.append("Declarado (DIPJ): ").append(Formatador.moeda(dados.declarado())).append("\n");
        texto.append("Movimentado (Cruzamento): ").append(Formatador.moeda(dados.movimentado())).append("\n");
        texto.append("--------------------------------------------------\n");
        texto.append("DIVERGÊNCIA: ").append(Formatador.moeda(dados.diferenca())).append("\n\n");
        if (dados.status() == StatusAuditoria.MALHA_FINA) {
            texto.append("MOTIVO DA AUTUAÇÃO:\n").append(dados.observacao());
        }

        TextArea area = new TextArea(texto.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setMaxWidth(Double.MAX_VALUE);
        area.setMaxHeight(Double.MAX_VALUE);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }
}